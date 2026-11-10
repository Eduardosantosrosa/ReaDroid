package org.readroid.extremerebuild.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public final class VideoActivity extends Activity {
    public static void launch(Activity from,String title,String url){Intent i=new Intent(from,VideoActivity.class);i.putExtra("title",title);i.putExtra("url",url);from.startActivity(i);}
    private ExoPlayer player;
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);PlayerView view=new PlayerView(this);view.setKeepScreenOn(true);setContentView(view);String url=getIntent().getStringExtra("url");if(url==null||url.isBlank()){finish();return;}player=new ExoPlayer.Builder(this).build();view.setPlayer(player);player.setMediaItem(MediaItem.fromUri(url));player.addListener(new Player.Listener(){@Override public void onPlayerError(PlaybackException error){android.widget.Toast.makeText(VideoActivity.this,"Erro de reprodução: "+error.getErrorCodeName(),android.widget.Toast.LENGTH_LONG).show();}});player.prepare();player.play();}
    @Override protected void onStop(){super.onStop();if(player!=null)player.pause();}
    @Override protected void onDestroy(){super.onDestroy();if(player!=null){player.release();player=null;}}
}
