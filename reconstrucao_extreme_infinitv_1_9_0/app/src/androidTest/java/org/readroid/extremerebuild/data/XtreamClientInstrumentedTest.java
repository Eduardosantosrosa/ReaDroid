package org.readroid.extremerebuild.data;

import static org.junit.Assert.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class XtreamClientInstrumentedTest {
    @Test public void seriesIdsResolveToEpisodeStreamsInSeasonOrder() throws Exception {
        String body="{\"episodes\":{\"2\":[{\"id\":20,\"title\":\"S2E1\",\"container_extension\":\"mkv\"}],\"1\":[{\"id\":10,\"title\":\"S1E1\",\"container_extension\":\"mp4\"}]}}";
        try(MockHttp server=new MockHttp(body)){
            List<StreamItem> episodes=new XtreamClient(server.url(),"u name","p/secret").episodes("show/5","Example Show");
            assertEquals(2,episodes.size());assertEquals("S1E1",episodes.get(0).name);assertEquals("Temporada 1",episodes.get(0).category);
            assertTrue(episodes.get(0).url.endsWith("/10.mp4"));assertTrue(server.requestLine().contains("series_id=show%2F5"));
            assertTrue(episodes.get(0).url.contains("/u+name/p%2Fsecret/"));
        }
    }

    @Test public void shortEpgDecodesProviderTextAndFormatsTimestamps() throws Exception {
        String body="{\"epg_listings\":[{\"title\":\"U29tZSB0aXRsZQ==\",\"description\":\"QSBkZXNjcmlwdGlvbg==\",\"start_timestamp\":\"1760000000\",\"stop_timestamp\":\"1760003600\"}]}";
        try(MockHttp server=new MockHttp(body)){
            ProgramEntry program=new XtreamClient(server.url(),"u","p").shortEpg("12",2).get(0);
            assertEquals("Some title",program.title);assertEquals("A description",program.description);
            assertTrue(program.start.matches("\\d{2}:\\d{2}"));assertTrue(server.requestLine().contains("action=get_short_epg"));
        }
    }

    private static final class MockHttp implements AutoCloseable {
        private final ServerSocket socket;private final Thread worker;private final String body;private volatile String line;
        MockHttp(String body)throws Exception{this.body=body;socket=new ServerSocket(0);worker=new Thread(this::serve,"readroid-mock-xtream");worker.start();}
        String url(){return "http://127.0.0.1:"+socket.getLocalPort();}
        String requestLine()throws InterruptedException{worker.join(5000);return line==null?"":line;}
        private void serve(){try(Socket client=socket.accept()){
            BufferedReader reader=new BufferedReader(new InputStreamReader(client.getInputStream(),StandardCharsets.UTF_8));line=reader.readLine();String header;while((header=reader.readLine())!=null&&!header.isEmpty()){}
            byte[] bytes=body.getBytes(StandardCharsets.UTF_8);OutputStream out=client.getOutputStream();out.write(("HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=utf-8\r\nContent-Length: "+bytes.length+"\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));out.write(bytes);out.flush();
        }catch(Exception ignored){}}
        @Override public void close()throws Exception{socket.close();worker.join(1000);}
    }
}
