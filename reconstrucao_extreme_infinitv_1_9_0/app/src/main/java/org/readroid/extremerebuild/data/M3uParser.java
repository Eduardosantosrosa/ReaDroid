package org.readroid.extremerebuild.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small defensive parser for playlist entries using EXTINF metadata and the following URI line. */
public final class M3uParser {
    private static final Pattern ATTR=Pattern.compile("([\\w-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s,]+))");
    private M3uParser() {}
    private static String attr(String text,String key) {
        Matcher m=ATTR.matcher(text);
        while(m.find()) if(m.group(1).equalsIgnoreCase(key)) return decodeAttribute(first(m.group(2),m.group(3),m.group(4)));
        return "";
    }
    private static String decodeAttribute(String value){return value.replace("&quot;","\"").replace("&amp;","&");}
    private static String first(String... x) { for(String s:x) if(s!=null) return s; return ""; }
    private static int nameSeparator(String line) {
        boolean quoted=false, escaped=false; char quote=0;
        for(int i=8;i<line.length();i++){char c=line.charAt(i);if(escaped){escaped=false;continue;}if(c=='\\'&&quoted){escaped=true;continue;}if((c=='"'||c=='\'')&&(!quoted||c==quote)){quoted=!quoted;quote=quoted?c:0;continue;}if(c==','&&!quoted)return i;}return -1;
    }
    public static List<StreamItem> parse(String text) {
        List<StreamItem> out=new ArrayList<>(); String name="", group="", logo="", lineGroup=""; int n=0;Map<String,String> headers=new LinkedHashMap<>();
        for(String raw:text.replace("\r","").split("\n")) {
            String line=raw.trim();
            if(line.regionMatches(true,0,"#EXTINF:",0,8)) {
                int comma=nameSeparator(line); String meta=comma>=0?line.substring(0,comma):line;
                name=comma>=0?line.substring(comma+1).trim():"Channel";
                String title=attr(meta,"tvg-name"); if(!title.isBlank()) name=title;
                group=attr(meta,"group-title"); logo=attr(meta,"tvg-logo"); lineGroup="";headers=new LinkedHashMap<>();
            } else if(line.regionMatches(true,0,"#EXTGRP:",0,8)) lineGroup=line.substring(8).trim();
            else if(line.regionMatches(true,0,"#EXTVLCOPT:",0,11)) {
                int equals=line.indexOf('=',11);if(equals>11){String option=line.substring(11,equals).trim().toLowerCase(),value=line.substring(equals+1).replace("\r","").replace("\n","").trim();String key=null;
                    if(option.equals("http-user-agent"))key="User-Agent";else if(option.equals("http-referrer")||option.equals("http-referer"))key="Referer";else if(option.equals("http-cookie"))key="Cookie";else if(option.equals("http-authorization"))key="Authorization";
                    if(key!=null&&!value.isEmpty())headers.put(key,value);
                }
            }
            else if(!line.isEmpty() && !line.startsWith("#")) {
                String kind="live"; String lower=line.toLowerCase();
                if(lower.contains(".mpd")) kind="dash"; else if(lower.contains(".m3u8")) kind="hls"; else if(lower.matches(".*\\.(mp4|mkv|mov)(\\?.*)?$")) kind="vod";
                if(!line.startsWith("http://")&&!line.startsWith("https://")) { name=""; continue; }
                out.add(new StreamItem("m3u-"+(++n),name.trim().isEmpty()?"Stream "+n:name,line,lineGroup.isBlank()?group:lineGroup,kind,logo,"",headers));
                name=""; group=""; logo=""; lineGroup="";headers=new LinkedHashMap<>();
            }
        }
        return out;
    }
}
