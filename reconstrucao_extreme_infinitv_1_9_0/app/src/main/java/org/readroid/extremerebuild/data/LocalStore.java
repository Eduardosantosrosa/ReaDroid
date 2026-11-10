package org.readroid.extremerebuild.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class LocalStore {
    private final SharedPreferences p;
    public LocalStore(Context c){p=c.getSharedPreferences("local-library",Context.MODE_PRIVATE);}
    public void source(String type,String host,String user,String pass,String url){p.edit().putString("type",type).putString("host",host).putString("user",user).putString("pass",pass).putString("url",url).apply();}
    public String get(String key){return p.getString(key,"");}
    public void favorites(List<StreamItem> rows){JSONArray a=new JSONArray();for(StreamItem x:rows)a.put(x.json());p.edit().putString("favorites",a.toString()).apply();}
    public List<StreamItem> favorites(){List<StreamItem> out=new ArrayList<>();try{JSONArray a=new JSONArray(p.getString("favorites","[]"));for(int i=0;i<a.length();i++)out.add(StreamItem.from(a.getJSONObject(i)));}catch(Exception ignored){}return out;}
}
