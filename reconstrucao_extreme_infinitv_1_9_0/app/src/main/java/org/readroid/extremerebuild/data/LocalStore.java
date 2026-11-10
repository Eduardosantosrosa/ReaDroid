package org.readroid.extremerebuild.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Private app storage. User credentials and media URLs are encrypted with an Android Keystore key. */
public final class LocalStore {
    private static final String PREFS="local-library";
    private static final String KEY_ALIAS="readroid_stream_local_data_v1";
    private static final int GCM_TAG_BITS=128;
    private final SharedPreferences p;

    public LocalStore(Context c) {
        p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        migrateLegacySource();
        migrateLegacyList("favorites");
    }

    private SecretKey key() throws Exception {
        KeyStore ks=KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
        if(ks.containsAlias(KEY_ALIAS))return (SecretKey)ks.getKey(KEY_ALIAS,null);
        KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build());
        return generator.generateKey();
    }

    private String encrypt(String clear) {
        if(clear==null||clear.isEmpty())return "";
        try {
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE,key());
            byte[] encrypted=cipher.doFinal(clear.getBytes(StandardCharsets.UTF_8));
            ByteBuffer blob=ByteBuffer.allocate(cipher.getIV().length+encrypted.length).put(cipher.getIV()).put(encrypted);
            return "v1:"+Base64.encodeToString(blob.array(),Base64.NO_WRAP);
        } catch(Exception e){throw new IllegalStateException("Não foi possível proteger dados locais.",e);}
    }

    private String decrypt(String value) {
        if(value==null||value.isEmpty())return "";
        if(!value.startsWith("v1:"))return value;
        try {
            byte[] blob=Base64.decode(value.substring(3),Base64.NO_WRAP); ByteBuffer data=ByteBuffer.wrap(blob);
            byte[] iv=new byte[12]; data.get(iv); byte[] encrypted=new byte[data.remaining()]; data.get(encrypted);
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(GCM_TAG_BITS,iv));
            return new String(cipher.doFinal(encrypted),StandardCharsets.UTF_8);
        } catch(Exception e){throw new IllegalStateException("Os dados locais não puderam ser descriptografados.",e);}
    }

    private JSONObject encodeSource(PlaylistSource s) throws Exception {
        JSONObject o=new JSONObject(); o.put("id",s.id); o.put("name",s.name); o.put("type",s.type); o.put("host",s.host);
        o.put("username",encrypt(s.username)); o.put("password",encrypt(s.password)); o.put("url",encrypt(s.url)); return o;
    }
    private PlaylistSource decodeSource(JSONObject o) {
        return new PlaylistSource(o.optString("id"),o.optString("name"),o.optString("type"),o.optString("host"),
                decrypt(o.optString("username")),decrypt(o.optString("password")),decrypt(o.optString("url")));
    }
    public synchronized List<PlaylistSource> sources() {
        List<PlaylistSource> out=new ArrayList<>();
        try { JSONArray a=new JSONArray(p.getString("sources","[]")); for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null)out.add(decodeSource(o));} }
        catch(Exception e){throw new IllegalStateException("Não foi possível ler as fontes salvas.",e);}
        return out;
    }
    public synchronized String saveSource(PlaylistSource input) {
        String id=input.id==null||input.id.isEmpty()?UUID.randomUUID().toString():input.id;
        PlaylistSource source=new PlaylistSource(id,input.name,input.type,input.host,input.username,input.password,input.url);
        List<PlaylistSource> all=sources(); boolean replaced=false; JSONArray a=new JSONArray();
        try {
            for(PlaylistSource old:all){PlaylistSource current=old.id.equals(id)?source:old;if(old.id.equals(id))replaced=true;a.put(encodeSource(current));}
            if(!replaced)a.put(encodeSource(source));
            p.edit().putString("sources",a.toString()).putString("activeSource",id).apply();
        } catch(Exception e){throw new IllegalStateException("Não foi possível salvar a fonte.",e);}
        return id;
    }
    public synchronized PlaylistSource activeSource() {
        String id=p.getString("activeSource",""); List<PlaylistSource> all=sources();
        for(PlaylistSource s:all)if(s.id.equals(id))return s;
        return all.isEmpty()?null:all.get(0);
    }
    public synchronized void activateSource(String id) {
        for(PlaylistSource s:sources())if(s.id.equals(id)){p.edit().putString("activeSource",id).apply();return;}
        throw new IllegalArgumentException("Fonte inexistente.");
    }
    public synchronized void deleteSource(String id) {
        JSONArray a=new JSONArray(); try{for(PlaylistSource s:sources())if(!s.id.equals(id))a.put(encodeSource(s));}
        catch(Exception e){throw new IllegalStateException("Não foi possível atualizar as fontes.",e);}
        SharedPreferences.Editor edit=p.edit().putString("sources",a.toString());
        if(id.equals(p.getString("activeSource","")))edit.putString("activeSource",a.length()>0?a.optJSONObject(0).optString("id"):"");
        edit.apply();
    }

    public String get(String field) {
        PlaylistSource s=activeSource(); if(s==null)return "";
        switch(field){case "type":return s.type;case "host":return s.host;case "user":return s.username;case "pass":return s.password;case "url":return s.url;default:return "";}
    }
    public void source(String type,String host,String user,String pass,String url) {
        PlaylistSource old=activeSource(); saveSource(new PlaylistSource(old==null?"":old.id,old==null?"Playlist":old.name,type,host,user,pass,url));
    }

    public void favorites(List<StreamItem> rows){writeItems("favorites",rows);}
    public List<StreamItem> favorites(){return readItems("favorites");}
    public void watchlist(List<StreamItem> rows){writeItems("watchlist",rows);}
    public List<StreamItem> watchlist(){return readItems("watchlist");}
    public synchronized void recordHistory(StreamItem item) {
        List<StreamItem> all=history(); all.removeIf(x->x.sourceId.equals(item.sourceId)&&x.id.equals(item.id)&&x.kind.equals(item.kind));
        all.add(0,item); while(all.size()>100)all.remove(all.size()-1); writeItems("history",all);
    }
    public List<StreamItem> history(){return readItems("history");}
    public synchronized List<DownloadEntry> downloads(){List<DownloadEntry> out=new ArrayList<>();try{JSONArray rows=new JSONArray(decrypt(p.getString("downloads","[]")));for(int i=0;i<rows.length();i++){JSONObject o=rows.optJSONObject(i);if(o!=null)out.add(DownloadEntry.from(o));}return out;}catch(Exception e){throw new IllegalStateException("Não foi possível ler os downloads.",e);}}
    public synchronized void saveDownload(DownloadEntry entry){List<DownloadEntry> rows=downloads();boolean found=false;JSONArray out=new JSONArray();for(DownloadEntry item:rows){DownloadEntry value=item.id.equals(entry.id)?entry:item;if(item.id.equals(entry.id))found=true;out.put(value.json());}if(!found)out.put(entry.json());p.edit().putString("downloads",encrypt(out.toString())).apply();}
    public synchronized void removeDownload(String id){JSONArray out=new JSONArray();for(DownloadEntry item:downloads())if(!item.id.equals(id))out.put(item.json());p.edit().putString("downloads",encrypt(out.toString())).apply();}
    public boolean preference(String key,boolean fallback){return p.getBoolean("setting_"+key,fallback);}
    public void setPreference(String key,boolean value){p.edit().putBoolean("setting_"+key,value).apply();}
    public void clearHistory(){p.edit().putString("history",encrypt("[]")).apply();}
    public void clearFavorites(){p.edit().putString("favorites",encrypt("[]")).apply();}
    public void clearWatchlist(){p.edit().putString("watchlist",encrypt("[]")).apply();}
    public synchronized JSONObject exportSnapshot(){JSONObject root=new JSONObject();JSONArray sourcesJson=new JSONArray(),favoritesJson=new JSONArray(),watchlistJson=new JSONArray(),historyJson=new JSONArray(),downloadsJson=new JSONArray();try{for(PlaylistSource s:sources()){JSONObject o=new JSONObject();o.put("id",s.id);o.put("name",s.name);o.put("type",s.type);o.put("host",s.host);o.put("username",s.username);o.put("password",s.password);o.put("url",s.url);sourcesJson.put(o);}for(StreamItem x:favorites())favoritesJson.put(x.json());for(StreamItem x:watchlist())watchlistJson.put(x.json());for(StreamItem x:history())historyJson.put(x.json());for(DownloadEntry x:downloads())downloadsJson.put(x.json());JSONObject settings=new JSONObject();settings.put("autoplay",preference("autoplay",true));settings.put("pip",preference("pip",true));root.put("version",1);root.put("activeSource",p.getString("activeSource",""));root.put("sources",sourcesJson);root.put("favorites",favoritesJson);root.put("watchlist",watchlistJson);root.put("history",historyJson);root.put("downloads",downloadsJson);root.put("settings",settings);return root;}catch(Exception e){throw new IllegalStateException("Não foi possível criar o backup.",e);}}
    public synchronized void restoreSnapshot(JSONObject root){try{JSONArray sourcesJson=root.optJSONArray("sources");for(PlaylistSource old:new ArrayList<>(sources()))deleteSource(old.id);if(sourcesJson!=null)for(int i=0;i<sourcesJson.length();i++){JSONObject o=sourcesJson.optJSONObject(i);if(o!=null)saveSource(new PlaylistSource(o.optString("id"),o.optString("name"),o.optString("type"),o.optString("host"),o.optString("username"),o.optString("password"),o.optString("url")));}String active=root.optString("activeSource","");if(!active.isEmpty())try{activateSource(active);}catch(IllegalArgumentException ignored){}favorites(readSnapshotItems(root.optJSONArray("favorites")));watchlist(readSnapshotItems(root.optJSONArray("watchlist")));writeItems("history",readSnapshotItems(root.optJSONArray("history")));p.edit().putString("downloads",encrypt(root.optJSONArray("downloads")==null?"[]":root.optJSONArray("downloads").toString())).apply();JSONObject settings=root.optJSONObject("settings");if(settings!=null){setPreference("autoplay",settings.optBoolean("autoplay",true));setPreference("pip",settings.optBoolean("pip",true));}}catch(Exception e){throw new IllegalStateException("Não foi possível restaurar o backup.",e);}}
    private List<StreamItem> readSnapshotItems(JSONArray array){List<StreamItem> result=new ArrayList<>();if(array!=null)for(int i=0;i<array.length();i++){JSONObject item=array.optJSONObject(i);if(item!=null)result.add(StreamItem.from(item));}return result;}
    private void writeItems(String key,List<StreamItem> rows){JSONArray a=new JSONArray();for(StreamItem x:rows)a.put(x.json());p.edit().putString(key,encrypt(a.toString())).apply();}
    private List<StreamItem> readItems(String key){List<StreamItem> out=new ArrayList<>();try{JSONArray a=new JSONArray(decrypt(p.getString(key,"[]")));for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null)out.add(StreamItem.from(o));}}catch(Exception e){throw new IllegalStateException("Não foi possível ler a biblioteca local.",e);}return out;}

    private void migrateLegacySource() {
        if(!p.getString("sources","").isEmpty())return;
        String type=p.getString("type",""); if(type.isEmpty())return;
        saveSource(new PlaylistSource("legacy-default","Playlist",type,p.getString("host",""),p.getString("user",""),p.getString("pass",""),p.getString("url","")));
        p.edit().remove("type").remove("host").remove("user").remove("pass").remove("url").apply();
    }
    private void migrateLegacyList(String key) {
        String old=p.getString(key,""); if(old.isEmpty()||old.startsWith("v1:"))return;
        try{JSONArray rows=new JSONArray(old);List<StreamItem> items=new ArrayList<>();for(int i=0;i<rows.length();i++)items.add(StreamItem.from(rows.getJSONObject(i)));writeItems(key,items);}
        catch(Exception e){throw new IllegalStateException("Não foi possível migrar os favoritos.",e);}
    }
}
