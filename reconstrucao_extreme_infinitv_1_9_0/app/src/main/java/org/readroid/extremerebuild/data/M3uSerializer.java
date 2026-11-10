package org.readroid.extremerebuild.data;

import java.util.List;

/** Exports the entries currently selected by the user as a portable extended M3U. */
public final class M3uSerializer {
    private M3uSerializer() {}
    public static String serialize(List<StreamItem> items) {
        StringBuilder out=new StringBuilder("#EXTM3U\n");
        for(StreamItem item:items) {
            if(item.url==null||(!item.url.startsWith("http://")&&!item.url.startsWith("https://")))continue;
            out.append("#EXTINF:-1");
            if(item.logo!=null&&!item.logo.isEmpty())out.append(" tvg-logo=\"").append(attribute(item.logo)).append('"');
            if(item.category!=null&&!item.category.isEmpty())out.append(" group-title=\"").append(attribute(item.category)).append('"');
            out.append(',').append(line(item.name)).append('\n');
            for(java.util.Map.Entry<String,String> header:item.headers.entrySet()) {
                String key=header.getKey().toLowerCase(java.util.Locale.ROOT),option=null;
                if(key.equals("user-agent"))option="http-user-agent";else if(key.equals("referer")||key.equals("referrer"))option="http-referrer";else if(key.equals("cookie"))option="http-cookie";else if(key.equals("authorization"))option="http-authorization";
                if(option!=null)out.append("#EXTVLCOPT:").append(option).append('=').append(line(header.getValue())).append('\n');
            }
            out.append(line(item.url)).append('\n');
        }
        return out.toString();
    }
    private static String attribute(String value){return line(value).replace("&","&amp;").replace("\"","&quot;");}
    private static String line(String value){return value==null?"":value.replace("\r","").replace("\n","");}
}
