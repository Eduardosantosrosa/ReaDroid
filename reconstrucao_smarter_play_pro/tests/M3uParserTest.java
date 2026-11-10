import org.independent.streambox.*;
import java.io.*;
import java.util.*;

public final class M3uParserTest {
 private static int checks=0;
 private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  String sample="#EXTM3U\n#EXTINF:-1 tvg-logo=\"https://img/logo.png\" group-title=\"News\",Example TV\nhttps://media.example/live.m3u8\n#EXTINF:-1 group-title=\"News\",Broken\nnot-a-url\n#EXTINF:-1,Film\nhttps://media.example/movie.mp4\n#EXTINF:-1 group-title=\"Series\",Episode\nhttps://media.example/series/ep1.ts\n";
  List<MediaEntry> x=M3uParser.parse(new StringReader(sample));
  check(x.size()==3,"invalid URL ignored");check(x.get(0).name.equals("Example TV"),"title parsed");check(x.get(0).group.equals("News"),"group parsed");check(x.get(0).logo.equals("https://img/logo.png"),"logo metadata parsed");check(x.get(0).type.equals("live"),"live stream classified");check(x.get(1).type.equals("movie"),"movie extension classified");check(x.get(2).type.equals("series"),"series category classified");
  check(M3uParser.parse(new StringReader("#EXTM3U\n")).isEmpty(),"empty playlist");check(M3uParser.parse(new StringReader("#EXTINF:-1,No URL\n")).isEmpty(),"missing URL ignored");
  System.out.println("M3uParserTest: "+checks+" assertions passed");
 }
}
