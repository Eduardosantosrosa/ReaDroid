package org.readroid.extremerebuild.data;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

public class M3uParserTest {
    @Test public void readsQuotedMetadataAndGroup() {
        List<StreamItem> rows=M3uParser.parse("#EXTM3U\n#EXTINF:-1 tvg-id=\"one\" tvg-logo=\"https://img/logo.png\" group-title=\"News, World\",Channel One\nhttps://example.test/live.m3u8\n");
        assertEquals(1,rows.size()); assertEquals("Channel One",rows.get(0).name); assertEquals("News, World",rows.get(0).category); assertEquals("https://img/logo.png",rows.get(0).logo); assertEquals("hls",rows.get(0).kind);
    }
    @Test public void supportsExtGrpAndCRLF() {
        List<StreamItem> rows=M3uParser.parse("#EXTM3U\r\n#EXTINF:-1,Audio\r\n#EXTGRP:Radio\r\nhttps://example.test/radio.mp3\r\n");
        assertEquals(1,rows.size()); assertEquals("Radio",rows.get(0).category); assertEquals("Audio",rows.get(0).name);
    }
    @Test public void ignoresCommentsAndInvalidUris() {
        List<StreamItem> rows=M3uParser.parse("#EXTM3U\n#EXTINF:-1,Bad\nfile:///private.m3u8\n# comment\n#EXTINF:-1,Good\nhttps://example.test/a.ts\n");
        assertEquals(1,rows.size()); assertEquals("Good",rows.get(0).name);
    }
    @Test public void emptyInputReturnsNoEntries() { assertTrue(M3uParser.parse("").isEmpty()); }

    @Test public void serializerRoundTripsNameGroupLogoAndUrl() {
        StreamItem original=new StreamItem("x","News, World","https://example.test/live.m3u8","Local, \"News\" & More","hls","https://img.example/logo.png");
        String output=M3uSerializer.serialize(java.util.Collections.singletonList(original));
        List<StreamItem> parsed=M3uParser.parse(output);
        assertEquals(1,parsed.size());assertEquals(original.name,parsed.get(0).name);
        assertEquals(original.category,parsed.get(0).category);assertEquals(original.logo,parsed.get(0).logo);assertEquals(original.url,parsed.get(0).url);
    }
    @Test public void serializerSkipsNonHttpAndRemovesEmbeddedNewlines() {
        String output=M3uSerializer.serialize(java.util.Collections.singletonList(new StreamItem("x","A\nChannel","file:///private","","live","")));
        assertEquals("#EXTM3U\n",output);
    }
    @Test public void readsAndExportsSupportedHttpHeaders() {
        String source="#EXTM3U\n#EXTINF:-1,Protected channel\n#EXTVLCOPT:http-user-agent=PlayerAgent\n#EXTVLCOPT:http-referrer=https://portal.example/\n#EXTVLCOPT:http-cookie=session%3Dabc\nhttps://stream.example/live.m3u8\n";
        StreamItem item=M3uParser.parse(source).get(0);
        assertEquals("PlayerAgent",item.headers.get("User-Agent"));assertEquals("https://portal.example/",item.headers.get("Referer"));assertEquals("session%3Dabc",item.headers.get("Cookie"));
        StreamItem reparsed=M3uParser.parse(M3uSerializer.serialize(java.util.Collections.singletonList(item))).get(0);
        assertEquals(item.headers,reparsed.headers);
    }
}
