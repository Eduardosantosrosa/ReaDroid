package org.readroid.extremerebuild.data;

import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;

/** Persistent private-file download job. Its source URL and headers are stored inside encrypted LocalStore data. */
public final class DownloadEntry {
    public final String id,title,url,fileName,state,sourceId;
    public final long bytes,total;
    public final Map<String,String> headers;
    public DownloadEntry(String id,String title,String url,String fileName,String state,long bytes,long total,String sourceId,Map<String,String> headers){this.id=id;this.title=title;this.url=url;this.fileName=fileName;this.state=state;this.bytes=bytes;this.total=total;this.sourceId=sourceId;this.headers=new LinkedHashMap<>(headers);}
    public JSONObject json(){JSONObject o=new JSONObject();try{o.put("id",id);o.put("title",title);o.put("url",url);o.put("fileName",fileName);o.put("state",state);o.put("bytes",bytes);o.put("total",total);o.put("sourceId",sourceId);JSONObject h=new JSONObject();for(Map.Entry<String,String>e:headers.entrySet())h.put(e.getKey(),e.getValue());o.put("headers",h);}catch(Exception ignored){}return o;}
    public static DownloadEntry from(JSONObject o){Map<String,String> h=new LinkedHashMap<>();JSONObject raw=o.optJSONObject("headers");if(raw!=null){Iterator<String> keys=raw.keys();while(keys.hasNext()){String k=keys.next();h.put(k,raw.optString(k));}}return new DownloadEntry(o.optString("id"),o.optString("title"),o.optString("url"),o.optString("fileName"),o.optString("state"),o.optLong("bytes"),o.optLong("total",-1),o.optString("sourceId"),h);}
    public DownloadEntry state(String value,long received,long length){return new DownloadEntry(id,title,url,fileName,value,received,length,sourceId,headers);}
}
