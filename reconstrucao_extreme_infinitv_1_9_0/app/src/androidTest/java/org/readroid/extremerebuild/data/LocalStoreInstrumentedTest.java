package org.readroid.extremerebuild.data;

import static org.junit.Assert.*;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LocalStoreInstrumentedTest {
    private Context context;
    private SharedPreferences prefs;

    @Before public void clearStorage() {
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefs=context.getSharedPreferences("local-library",Context.MODE_PRIVATE);
        assertTrue(prefs.edit().clear().commit());
    }

    @Test public void keepsMultipleSourcesAndEncryptsCredentialsAndPlaylistUrl() {
        LocalStore store=new LocalStore(context);
        String first=store.saveSource(new PlaylistSource("","News","xtream","https://provider.example","acct-9341","secret-pass",""));
        String second=store.saveSource(new PlaylistSource("","Local M3U","m3u","","","","https://lists.example/key-5"));

        assertEquals(2,store.sources().size());
        assertEquals(second,store.activeSource().id);
        String raw=prefs.getString("sources","");
        assertFalse(raw.contains("secret-pass"));
        assertFalse(raw.contains("acct-9341"));
        assertFalse(raw.contains("key-5"));

        store.activateSource(first);
        LocalStore reopened=new LocalStore(context);
        assertEquals("acct-9341",reopened.get("user"));
        assertEquals("secret-pass",reopened.get("pass"));
        assertEquals("News",reopened.activeSource().name);
        reopened.deleteSource(first);
        assertEquals(1,reopened.sources().size());
    }

    @Test public void encryptsFavoritesWatchlistAndHistoryAndRestoresThem() {
        LocalStore store=new LocalStore(context);
        StreamItem item=new StreamItem("item-7","A film","https://provider.example/movie/user/pass/token.m3u8","Films","vod","", "source-a");
        store.favorites(List.of(item));
        store.watchlist(List.of(item));
        store.recordHistory(item);

        String raw=prefs.getString("favorites","")+prefs.getString("watchlist","")+prefs.getString("history","");
        assertFalse(raw.contains("provider.example"));
        assertFalse(raw.contains("token.m3u8"));
        LocalStore reopened=new LocalStore(context);
        assertEquals(item.url,reopened.favorites().get(0).url);
        assertEquals(item.sourceId,reopened.watchlist().get(0).sourceId);
        assertEquals("item-7",reopened.history().get(0).id);
        reopened.recordHistory(item);
        assertEquals("History deduplicates the same source item",1,reopened.history().size());
    }

    @Test public void downloadQueueStoresProviderUrlEncrypted() {
        LocalStore store=new LocalStore(context);String url="https://provider.example/movie/user/password-token/42.mp4";
        store.saveDownload(new DownloadEntry("job-1","Film",url,"film-42.mp4","queued",0,-1,"source-1",java.util.Collections.singletonMap("Authorization","Bearer private-token")));
        String raw=prefs.getString("downloads","");assertFalse(raw.contains("password-token"));assertFalse(raw.contains("private-token"));
        DownloadEntry loaded=new LocalStore(context).downloads().get(0);assertEquals(url,loaded.url);assertEquals("Bearer private-token",loaded.headers.get("Authorization"));
    }

    @Test public void migratesLegacySingleSourceAndFavoritesThenRemovesPlaintextCredentials() throws Exception {
        StreamItem old=new StreamItem("legacy-item","Old channel","https://host.example/live/u/p/channel.ts","Live","live","");
        prefs.edit().putString("type","xtream").putString("host","https://host.example")
                .putString("user","old-user").putString("pass","old-secret").putString("url","")
                .putString("favorites","["+old.json().toString()+"]").commit();

        LocalStore migrated=new LocalStore(context);
        assertEquals("old-secret",migrated.get("pass"));
        assertEquals("legacy-item",migrated.favorites().get(0).id);
        assertFalse(prefs.contains("pass"));
        assertFalse(prefs.getString("sources","").contains("old-secret"));
        assertFalse(prefs.getString("favorites","").contains("host.example"));
    }

    @Test public void encryptedBackupRestoresAcrossEmptyStoreAndRejectsWrongPassphrase() throws Exception {
        LocalStore original=new LocalStore(context);
        original.saveSource(new PlaylistSource("source-1","Home","xtream","https://provider.example","user-sensitive","password-sensitive",""));
        original.favorites(List.of(new StreamItem("movie-3","Film","https://provider.example/movie/user/password-sensitive/3.mp4","VOD","vod","","source-1")));
        original.setPreference("pip",false);
        ByteArrayOutputStream output=new ByteArrayOutputStream();BackupManager.write(output,original,"correct horse battery".toCharArray());byte[] backup=output.toByteArray();
        String bytes=new String(backup,StandardCharsets.ISO_8859_1);assertFalse(bytes.contains("password-sensitive"));assertTrue(bytes.startsWith("READROID-STREAM-BACKUP-1\n"));
        assertTrue(prefs.edit().clear().commit());
        LocalStore restored=new LocalStore(context);
        org.junit.Assert.assertThrows(Exception.class,()->BackupManager.restore(new ByteArrayInputStream(backup),restored,"wrong passphrase".toCharArray()));
        assertTrue(restored.sources().isEmpty());
        BackupManager.restore(new ByteArrayInputStream(backup),restored,"correct horse battery".toCharArray());
        assertEquals("password-sensitive",restored.get("pass"));assertEquals("movie-3",restored.favorites().get(0).id);assertFalse(restored.preference("pip",true));
    }
}
