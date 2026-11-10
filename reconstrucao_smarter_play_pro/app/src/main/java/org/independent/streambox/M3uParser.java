package org.independent.streambox;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public final class M3uParser {
 private static final Pattern ATTR=Pattern.compile("([\\w-]+)=\"([^\"]*)\"");
 private M3uParser() {}
 public static List<MediaEntry> parse(Reader source) throws IOException {
  BufferedReader in=new BufferedReader(source); ArrayList<MediaEntry> out=new ArrayList<>();
  String line, name=null, group="Uncategorized", logo="";
  while((line=in.readLine())!=null) {
   line=line.trim(); if(line.isEmpty()) continue;
   if(line.startsWith("#EXTINF:")) {
    int comma=line.indexOf(','); name=comma>=0?line.substring(comma+1).trim():"Untitled";
    Matcher m=ATTR.matcher(line.substring(0,comma>=0?comma:line.length())); group="Uncategorized"; logo="";
    while(m.find()) { if(m.group(1).equalsIgnoreCase("group-title"))group=m.group(2); if(m.group(1).equalsIgnoreCase("tvg-logo"))logo=m.group(2); }
   } else if(!line.startsWith("#") && name!=null) {
    if(line.matches("(?i)https?://.+")) {
     String lower=(line+" "+group).toLowerCase(Locale.ROOT); String type="live";
     if(lower.matches(".*(/series/|\\bseries\\b).*")) type="series";
     else if(lower.matches(".*(/movie/|/movies/|\\b(vod|movies?|films?|cinema)\\b|\\.(mp4|mkv|avi|mov|m4v|webm)([?#].*)?$).*")) type="movie";
     out.add(new MediaEntry(name,line,group,type,logo));
    }
    name=null;
   }
  }
  return out;
 }
}
