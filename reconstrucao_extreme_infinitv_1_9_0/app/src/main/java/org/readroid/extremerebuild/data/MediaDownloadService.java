package org.readroid.extremerebuild.data;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Foreground, resumable direct-file downloads stored in the app-private Downloads folder. */
public final class MediaDownloadService extends Service {
    public static final String ACTION_ENQUEUE="org.readroid.extremerebuild.DOWNLOAD_ENQUEUE",ACTION_PAUSE="org.readroid.extremerebuild.DOWNLOAD_PAUSE",ACTION_RESUME="org.readroid.extremerebuild.DOWNLOAD_RESUME",ACTION_DELETE="org.readroid.extremerebuild.DOWNLOAD_DELETE";
    private static final String CHANNEL="media-downloads";private static final int NOTIFICATION=810;
    private final ExecutorService io=Executors.newSingleThreadExecutor();private final AtomicInteger pending=new AtomicInteger();private final Set<String> pauseIds=new HashSet<>(),scheduled=new HashSet<>();
    private volatile HttpURLConnection activeConnection;private volatile String activeId="";private volatile int latestStartId;private LocalStore store;
    public static Intent command(android.content.Context context,String action,String id){return new Intent(context,MediaDownloadService.class).setAction(action).putExtra("id",id);}
    @Override public void onCreate(){super.onCreate();store=new LocalStore(this);createChannel();}
    @Override public int onStartCommand(Intent intent,int flags,int startId){latestStartId=startId;startInForeground("Preparando downloads…",0,-1);String action=intent==null?"":intent.getAction(),id=intent==null?"":intent.getStringExtra("id");
        if(ACTION_ENQUEUE.equals(action)||ACTION_RESUME.equals(action)){synchronized(pauseIds){pauseIds.remove(id);}submit(id);}
        else if(ACTION_PAUSE.equals(action)){synchronized(pauseIds){pauseIds.add(id);}if(id.equals(activeId)&&activeConnection!=null)activeConnection.disconnect();DownloadEntry entry=find(id);if(entry!=null)store.saveDownload(entry.state("paused",entry.bytes,entry.total));refreshNotification();idleIfDone();}
        else if(ACTION_DELETE.equals(action)){DownloadEntry entry=find(id);synchronized(pauseIds){pauseIds.add(id);}if(id.equals(activeId)&&activeConnection!=null)activeConnection.disconnect();store.removeDownload(id);if(entry!=null){File dir=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);if(dir==null)dir=new File(getFilesDir(),"downloads");new File(dir,entry.fileName+".part").delete();new File(dir,entry.fileName).delete();}idleIfDone();}
        else if(intent==null){for(DownloadEntry entry:store.downloads())if(entry.state.equals("queued")||entry.state.equals("downloading"))submit(entry.id);idleIfDone();}
        return START_STICKY;
    }
    private void submit(String id){if(id==null||id.isEmpty())return;synchronized(scheduled){if(!scheduled.add(id))return;}pending.incrementAndGet();io.execute(()->{try{download(id);}finally{synchronized(scheduled){scheduled.remove(id);}if(pending.decrementAndGet()==0)idleIfDone();}});}
    private void download(String id){DownloadEntry entry=find(id);if(entry==null)return;synchronized(pauseIds){if(pauseIds.contains(id))return;}activeId=id;File directory=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);if(directory==null)directory=new File(getFilesDir(),"downloads");if(!directory.exists()&&!directory.mkdirs()){saveIfPresent(entry.state("error",entry.bytes,entry.total));return;}File partial=new File(directory,entry.fileName+".part"),complete=new File(directory,entry.fileName);long received=partial.exists()?partial.length():0,total=-1;saveIfPresent(entry.state("downloading",received,total));
        try{HttpURLConnection connection=(HttpURLConnection)new URL(entry.url).openConnection();activeConnection=connection;connection.setConnectTimeout(15000);connection.setReadTimeout(30000);connection.setInstanceFollowRedirects(true);for(java.util.Map.Entry<String,String> h:entry.headers.entrySet())connection.setRequestProperty(h.getKey(),h.getValue());if(received>0)connection.setRequestProperty("Range","bytes="+received+"-");int status=connection.getResponseCode();if(status<200||status>=300)throw new java.io.IOException("HTTP "+status);boolean append=status==206;if(!append)received=0;long responseLength=connection.getContentLengthLong();total=responseLength<0?-1:received+responseLength;
            try(InputStream in=connection.getInputStream();FileOutputStream out=new FileOutputStream(partial,append)){byte[] buffer=new byte[64*1024];int count;long lastSave=0;while((count=in.read(buffer))!=-1){synchronized(pauseIds){if(pauseIds.contains(id))throw new InterruptedException("pause");}out.write(buffer,0,count);received+=count;if(received-lastSave>=4*1024*1024){saveIfPresent(entry.state("downloading",received,total));startInForeground(entry.title,received,total);lastSave=received;}}out.getFD().sync();}
            connection.disconnect();activeConnection=null;activeId="";synchronized(pauseIds){if(pauseIds.contains(id)){saveIfPresent(entry.state("paused",received,total));return;}}
            if(complete.exists()&&!complete.delete())throw new java.io.IOException("Não foi possível substituir o arquivo anterior.");if(!partial.renameTo(complete))throw new java.io.IOException("Não foi possível finalizar o arquivo baixado.");saveIfPresent(entry.state("complete",received,total));startInForeground("Download concluído: "+entry.title,received,total);
        }catch(InterruptedException paused){saveIfPresent(entry.state("paused",received,total));}
        catch(Exception error){boolean paused;synchronized(pauseIds){paused=pauseIds.contains(id);}DownloadEntry current=find(id);if(current!=null)store.saveDownload(current.state(paused?"paused":"error",received,total));}
        finally{if(activeConnection!=null&&id.equals(activeId)){activeConnection.disconnect();activeConnection=null;activeId="";}if(find(id)==null){partial.delete();complete.delete();}refreshNotification();}
    }
    private DownloadEntry find(String id){for(DownloadEntry d:store.downloads())if(d.id.equals(id))return d;return null;}
    private void saveIfPresent(DownloadEntry entry){if(find(entry.id)!=null)store.saveDownload(entry);}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationManager manager=getSystemService(NotificationManager.class);manager.createNotificationChannel(new NotificationChannel(CHANNEL,"Downloads de mídia",NotificationManager.IMPORTANCE_LOW));}}
    private void startInForeground(String text,long current,long total){Notification notification=notification(text,current,total);if(Build.VERSION.SDK_INT>=29)startForeground(NOTIFICATION,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);else startForeground(NOTIFICATION,notification);}
    private Notification notification(String text,long current,long total){NotificationCompat.Builder builder=new NotificationCompat.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("ReaDroid Stream").setContentText(text).setOngoing(pending.get()>0).setOnlyAlertOnce(true);if(total>0)builder.setProgress(100,(int)Math.min(100,current*100/total),false);else if(pending.get()>0)builder.setProgress(0,0,true);return builder.build();}
    private void refreshNotification(){String title=activeId.isEmpty()?"Downloads em pausa":"Baixando mídia…";startInForeground(title,0,-1);}
    private void idleIfDone(){if(pending.get()==0){stopForeground(STOP_FOREGROUND_REMOVE);stopSelfResult(latestStartId);}}
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onDestroy(){if(activeConnection!=null)activeConnection.disconnect();io.shutdownNow();super.onDestroy();}
}
