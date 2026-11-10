package org.readroid.extremerebuild.data;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

/** Direct client: requests are sent only to the user-entered provider host. */
public final class XtreamClient {
    public final String host,user,password;
    public XtreamClient(String host,String user,String password) {
        String h=host.trim(); while(h.endsWith("/"))h=h.substring(0,h.length()-1); this.host=h; this.user=user; this.password=password;
    }
    private String enc(String s)throws Exception{return URLEncoder.encode(s, StandardCharsets.UTF_8.name());}
    private String request(String query)throws Exception {
        URL u=new URL(host+"/player_api.php?username="+enc(user)+"&password="+enc(password)+(query.isEmpty()?"":"&"+query));
        HttpURLConnection c=(HttpURLConnection)u.openConnection(); c.setConnectTimeout(12000); c.setReadTimeout(20000); c.setRequestProperty("Accept","application/json");
        try(InputStream in=c.getInputStream();ByteArrayOutputStream b=new ByteArrayOutputStream()) { byte[] chunk=new byte[8192]; int n; while((n=in.read(chunk))!=-1)b.write(chunk,0,n); return new String(b.toByteArray(),StandardCharsets.UTF_8); }
        finally { c.disconnect(); }
    }
    private JSONArray array(String action)throws Exception { return new JSONArray(request("action="+action)); }
    public void validate()throws Exception { JSONObject root=new JSONObject(request("")); JSONObject info=root.optJSONObject("user_info"); if(info==null||!info.optString("auth","0").equals("1")) throw new Exception("Credenciais rejeitadas pelo provedor."); }
    public List<String> categories(String kind)throws Exception {
        String action=kind.equals("live")?"get_live_categories":kind.equals("movie")?"get_vod_categories":"get_series_categories";
        JSONArray a=array(action); List<String> out=new ArrayList<>(); out.add("Todas"); for(int i=0;i<a.length();i++) out.add(a.optJSONObject(i).optString("category_name","Sem categoria")); return out;
    }
    public List<StreamItem> items(String kind,String category)throws Exception {
        String action=kind.equals("live")?"get_live_streams":kind.equals("movie")?"get_vod_streams":"get_series";
        String catAction=kind.equals("live")?"get_live_categories":kind.equals("movie")?"get_vod_categories":"get_series_categories";
        String catId=""; if(!category.equals("Todas")) { JSONArray cats=array(catAction); for(int i=0;i<cats.length();i++){JSONObject c=cats.optJSONObject(i);if(c.optString("category_name").equals(category)){catId=c.optString("category_id");break;}} }
        JSONArray a=array(action+(catId.isEmpty()?"":"&category_id="+enc(catId))); List<StreamItem> out=new ArrayList<>();
        for(int i=0;i<a.length();i++) {
            JSONObject o=a.optJSONObject(i); if(o==null)continue; String id=o.optString(kind.equals("series")?"series_id":"stream_id"); if(id.isEmpty())continue;
            String ext=o.optString("container_extension",kind.equals("live")?"ts":"mp4");
            String path=kind.equals("live")?"/live/":"/movie/";
            // A series_id is not a playable stream_id; episode resolution requires get_series_info.
            String url=kind.equals("series")?"":host+path+enc(user)+"/"+enc(password)+"/"+id+"."+ext;
            out.add(new StreamItem(id,o.optString("name","Stream"),url,category.equals("Todas")?o.optString("category_name",category):category,kind,o.optString("stream_icon",o.optString("cover",""))));
        }
        return out;
    }

    /** Resolves a series ID into playable episode IDs using the Xtream series-info action. */
    public List<StreamItem> episodes(String seriesId,String seriesName)throws Exception {
        JSONObject root=new JSONObject(request("action=get_series_info&series_id="+enc(seriesId)));
        JSONObject seasons=root.optJSONObject("episodes");
        List<StreamItem> out=new ArrayList<>();
        if(seasons==null)return out;
        List<String> seasonIds=new ArrayList<>();
        Iterator<String> keys=seasons.keys();
        while(keys.hasNext())seasonIds.add(keys.next());
        Collections.sort(seasonIds,Comparator.comparingInt((String key)->{
            try{return Integer.parseInt(key);}catch(NumberFormatException ignored){return Integer.MAX_VALUE;}
        }));
        for(String season:seasonIds) {
            JSONArray rows=seasons.optJSONArray(season);
            if(rows==null)continue;
            for(int i=0;i<rows.length();i++) {
                JSONObject episode=rows.optJSONObject(i);
                if(episode==null)continue;
                String id=episode.optString("id");
                if(id.isEmpty())continue;
                String title=episode.optString("title",seriesName+" — S"+season+"E"+(i+1));
                String ext=episode.optString("container_extension","mp4");
                String image=episode.optJSONObject("info")!=null
                        ?episode.optJSONObject("info").optString("movie_image",""):"";
                String stream=host+"/series/"+enc(user)+"/"+enc(password)+"/"+enc(id)+"."+enc(ext);
                out.add(new StreamItem(id,title,stream,"Temporada "+season,"episode",image));
            }
        }
        return out;
    }
}
