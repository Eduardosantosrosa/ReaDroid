package org.readroid.extremerebuild.ui;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.util.Rational;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.TrackSelectionDialogBuilder;
import org.readroid.extremerebuild.data.LocalStore;

public final class VideoActivity extends Activity {
    public static void launch(Activity from,String title,String url){launch(from,title,url,java.util.Collections.emptyMap());}
    public static void launch(Activity from,String title,String url,Map<String,String> headers){Intent i=new Intent(from,VideoActivity.class);i.putExtra("title",title);i.putExtra("url",url);ArrayList<String> names=new ArrayList<>(headers.keySet()),values=new ArrayList<>();for(String key:names)values.add(headers.get(key));i.putStringArrayListExtra("headerNames",names);i.putStringArrayListExtra("headerValues",values);from.startActivity(i);}
    private ExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private LinearLayout trackButtons;
    private boolean resumeOnStart;
    private LocalStore store;
    @Override public void onCreate(Bundle state){super.onCreate(state);store=new LocalStore(this);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);String url=getIntent().getStringExtra("url");if(url==null||url.isBlank()){finish();return;}
        HashMap<String,String> headers=new HashMap<>();ArrayList<String> names=getIntent().getStringArrayListExtra("headerNames"),values=getIntent().getStringArrayListExtra("headerValues");if(names!=null&&values!=null)for(int i=0;i<Math.min(names.size(),values.size());i++)headers.put(names.get(i),values.get(i));
        DefaultHttpDataSource.Factory http=new DefaultHttpDataSource.Factory().setConnectTimeoutMs(15000).setReadTimeoutMs(30000).setDefaultRequestProperties(headers);
        DefaultDataSource.Factory dataSource=new DefaultDataSource.Factory(this,http);trackSelector=new DefaultTrackSelector(this);
        player=new ExoPlayer.Builder(this).setMediaSourceFactory(new DefaultMediaSourceFactory(dataSource)).setTrackSelector(trackSelector).build();
        PlayerView view=new PlayerView(this);view.setPlayer(player);view.setKeepScreenOn(true);view.setShowSubtitleButton(true);
        FrameLayout frame=new FrameLayout(this);frame.addView(view,new FrameLayout.LayoutParams(-1,-1));trackButtons=new LinearLayout(this);trackButtons.setOrientation(LinearLayout.HORIZONTAL);FrameLayout.LayoutParams controls=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.END);controls.topMargin=24;controls.rightMargin=24;
        Button audio=new Button(this);audio.setText("Áudio");audio.setFocusable(true);audio.setOnClickListener(v->showTrackDialog("Faixa de áudio",C.TRACK_TYPE_AUDIO,false));trackButtons.addView(audio);
        Button subtitles=new Button(this);subtitles.setText("Legendas");subtitles.setFocusable(true);subtitles.setOnClickListener(v->showTrackDialog("Legendas",C.TRACK_TYPE_TEXT,true));trackButtons.addView(subtitles);frame.addView(trackButtons,controls);setContentView(frame);
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)));player.addListener(new Player.Listener(){@Override public void onPlayerError(PlaybackException error){android.widget.Toast.makeText(VideoActivity.this,"Erro de reprodução: "+error.getErrorCodeName(),android.widget.Toast.LENGTH_LONG).show();}});player.prepare();if(store.preference("autoplay",true))player.play();
    }
    private void showTrackDialog(String title,int type,boolean disable){if(player==null)return;new TrackSelectionDialogBuilder(this,title,player,type).setShowDisableOption(disable).build().show();}
    @Override public void onUserLeaveHint(){super.onUserLeaveHint();if(android.os.Build.VERSION.SDK_INT>=26&&store!=null&&store.preference("pip",true)&&player!=null&&player.isPlaying()){try{enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());}catch(Exception ignored){}}}
    @Override public void onPictureInPictureModeChanged(boolean inPip,Configuration config){super.onPictureInPictureModeChanged(inPip,config);if(trackButtons!=null)trackButtons.setVisibility(inPip?View.GONE:View.VISIBLE);}
    @Override protected void onStop(){resumeOnStart=player!=null&&player.isPlaying();if(player!=null)player.pause();super.onStop();}
    @Override protected void onStart(){super.onStart();if(resumeOnStart&&player!=null){player.play();resumeOnStart=false;}}
    @Override protected void onDestroy(){super.onDestroy();if(player!=null){player.release();player=null;}if(trackSelector!=null){trackSelector.release();trackSelector=null;}}
}
