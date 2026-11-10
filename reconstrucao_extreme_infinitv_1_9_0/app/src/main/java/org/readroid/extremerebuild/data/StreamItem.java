package org.readroid.extremerebuild.data;

import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;

public final class StreamItem {
    public final String id, name, url, category, kind, sourceId;
    public final String logo;
    public final Map<String,String> headers;
    public StreamItem(String id, String name, String url, String category, String kind, String logo) {
        this(id,name,url,category,kind,logo,"",new LinkedHashMap<>());
    }
    public StreamItem(String id, String name, String url, String category, String kind, String logo, String sourceId) {
        this(id,name,url,category,kind,logo,sourceId,new LinkedHashMap<>());
    }
    public StreamItem(String id,String name,String url,String category,String kind,String logo,String sourceId,Map<String,String> headers) {
        this.id=id; this.name=name; this.url=url; this.category=category; this.kind=kind; this.logo=logo; this.sourceId=sourceId; this.headers=new LinkedHashMap<>(headers);
    }
    public StreamItem withSource(String value) { return new StreamItem(id,name,url,category,kind,logo,value,headers); }
    public JSONObject json() {
        JSONObject o=new JSONObject();
        try { o.put("id",id); o.put("name",name); o.put("url",url); o.put("category",category); o.put("kind",kind); o.put("logo",logo); o.put("sourceId",sourceId);JSONObject h=new JSONObject();for(Map.Entry<String,String>e:headers.entrySet())h.put(e.getKey(),e.getValue());o.put("headers",h); } catch(Exception ignored) {}
        return o;
    }
    public static StreamItem from(JSONObject o) {
        Map<String,String> headers=new LinkedHashMap<>();JSONObject h=o.optJSONObject("headers");if(h!=null){Iterator<String> keys=h.keys();while(keys.hasNext()){String key=keys.next();headers.put(key,h.optString(key));}}
        return new StreamItem(o.optString("id"),o.optString("name"),o.optString("url"),o.optString("category"),o.optString("kind"),o.optString("logo"),o.optString("sourceId"),headers);
    }
}
