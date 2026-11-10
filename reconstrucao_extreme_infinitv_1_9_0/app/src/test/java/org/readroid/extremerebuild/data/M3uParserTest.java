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
}
