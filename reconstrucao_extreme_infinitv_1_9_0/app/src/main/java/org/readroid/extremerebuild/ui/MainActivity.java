package org.readroid.extremerebuild.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.readroid.extremerebuild.data.LocalStore;
import org.readroid.extremerebuild.data.BackupManager;
import org.readroid.extremerebuild.data.DownloadEntry;
import org.readroid.extremerebuild.data.MediaDownloadService;
import org.readroid.extremerebuild.data.M3uParser;
import org.readroid.extremerebuild.data.M3uSerializer;
import org.readroid.extremerebuild.data.PlaylistSource;
import org.readroid.extremerebuild.data.ProgramEntry;
import org.readroid.extremerebuild.data.StreamItem;
import org.readroid.extremerebuild.data.XtreamClient;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final int REQUEST_M3U_FILE=710;
    private static final int REQUEST_BACKUP_EXPORT=711,REQUEST_BACKUP_IMPORT=712;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private LocalStore store; private LinearLayout root,body,nav; private EditText search;
    private String section="TV ao vivo",category="Todas"; private List<StreamItem> entries=new ArrayList<>(),shown=new ArrayList<>(); private List<String> categories=new ArrayList<>();
    private int bg=Color.rgb(9,13,17), panel=Color.rgb(18,25,32), fg=Color.rgb(236,241,245), muted=Color.rgb(153,166,177), accent=Color.rgb(118,215,165);
    @Override public void onCreate(Bundle b){super.onCreate(b);store=new LocalStore(this);build();loadSavedSource();}
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
    private void build(){
        root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(bg);setContentView(root);
        TextView brand=text("STREAM LIBRARY",22,true);brand.setPadding(dp(22),dp(18),dp(20),dp(8));root.addView(brand);
        ScrollView nscroll=new ScrollView(this); nscroll.setHorizontalScrollBarEnabled(false); nav=new LinearLayout(this); nav.setPadding(dp(12),dp(4),dp(12),dp(8)); nscroll.addView(nav);root.addView(nscroll);
        for(String s:new String[]{"TV ao vivo","Filmes","Séries","Favoritos","Assistir depois","Histórico","Downloads","Editar M3U","Fontes","Configurações"}){Button x=button(s);x.setOnClickListener(v->{if(s.equals("Fontes")){manageSources();return;}if(s.equals("Editar M3U")&&(!store.get("type").equals("m3u")||entries.isEmpty())){toast("Carregue uma playlist M3U para editar.");return;}section=s;category="Todas";if(store.get("type").equals("xtream")&&(s.equals("TV ao vivo")||s.equals("Filmes")||s.equals("Séries")))loadXtream(false);else render();});nav.addView(x);}
        search=new EditText(this);search.setSingleLine(true);search.setHintTextColor(muted);search.setTextColor(fg);search.setHint("Pesquisar canais e títulos");search.setPadding(dp(15),dp(8),dp(15),dp(8));search.setBackgroundColor(panel);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(48));sp.setMargins(dp(18),dp(4),dp(18),dp(10));root.addView(search,sp);search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){renderRows();}public void afterTextChanged(android.text.Editable e){}});
        ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(14),0,dp(14),dp(16));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        search.setOnEditorActionListener((v,a,e)->{renderRows();return false;});render();
    }
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextColor(fg);v.setTextSize(size);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(fg);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel));b.setFocusable(true);b.setFocusableInTouchMode(true);b.setOnFocusChangeListener((v,focused)->b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(focused?accent:panel)));b.setPadding(dp(12),0,dp(12),0);b.setMinimumWidth(dp(72));return b;}
    private void loadSavedSource(){String type=store.get("type");if(type.isEmpty())return;if(type.equals("m3u")){loadM3u(store.get("url"),false);}else loadXtream(false);}
    private void addSource(){editSource(null);}
    private void manageSources(){
        LinearLayout list=new LinearLayout(this);list.setOrientation(1);list.setPadding(dp(12),dp(8),dp(12),dp(8));
        List<PlaylistSource> sources=store.sources();
        for(PlaylistSource source:sources){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView name=text(source.name+" · "+source.type.toUpperCase(),15,true);row.addView(name,new LinearLayout.LayoutParams(0,-2,1));Button select=button(source.id.equals(store.activeSource().id)?"Ativa":"Usar");select.setOnClickListener(v->{store.activateSource(source.id);section="TV ao vivo";loadSavedSource();});row.addView(select);Button edit=button("Editar");edit.setOnClickListener(v->editSource(source));row.addView(edit);Button remove=button("Excluir");remove.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Excluir "+source.name+"?").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,w)->{store.deleteSource(source.id);render();manageSources();}).show());row.addView(remove);list.addView(row);}
        if(sources.isEmpty())list.addView(text("Nenhuma fonte salva.",15,false));
        Button export=button("Exportar conteúdo carregado como M3U");export.setOnClickListener(v->exportM3u());list.addView(export);
        new AlertDialog.Builder(this).setTitle("Fontes de conteúdo").setView(list).setNegativeButton("Fechar",null).setNeutralButton("Importar arquivo M3U",(d,w)->openM3uFile()).setPositiveButton("Adicionar URL",(d,w)->addSource()).show();
    }
    private void editSource(PlaylistSource original){
        LinearLayout form=new LinearLayout(this);form.setOrientation(1);form.setPadding(dp(8),dp(4),dp(8),dp(4));
        EditText name=input("Nome da fonte",InputType.TYPE_CLASS_TEXT);name.setText(original==null?"":original.name);form.addView(name);
        String[] modes={"M3U/M3U8 URL","Xtream Codes"};android.widget.Spinner spinner=new android.widget.Spinner(this);spinner.setAdapter(new android.widget.ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,modes));form.addView(spinner);if(original!=null)spinner.setSelection(original.type.equals("xtream")?1:0);
        EditText host=input("Host URL (https://servidor:porta)",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI),user=input("Usuário",InputType.TYPE_CLASS_TEXT),pass=input("Senha",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD),url=input("URL da playlist M3U/M3U8",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        host.setText(original==null?"":original.host);user.setText(original==null?"":original.username);pass.setText(original==null?"":original.password);url.setText(original==null?"":original.url);form.addView(host);form.addView(user);form.addView(pass);form.addView(url);
        Runnable visibility=()->{boolean xt=spinner.getSelectedItemPosition()==1;host.setVisibility(xt?View.VISIBLE:View.GONE);user.setVisibility(xt?View.VISIBLE:View.GONE);pass.setVisibility(xt?View.VISIBLE:View.GONE);url.setVisibility(xt?View.GONE:View.VISIBLE);};spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?>p){}public void onItemSelected(android.widget.AdapterView<?>p,View v,int pos,long id){visibility.run();}});visibility.run();
        new AlertDialog.Builder(this).setTitle(original==null?"Adicionar playlist":"Editar playlist").setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->{
            String label=name.getText().toString().trim(),type=spinner.getSelectedItemPosition()==0?"m3u":"xtream",h=host.getText().toString().trim(),un=user.getText().toString().trim(),pw=pass.getText().toString(),u=url.getText().toString().trim();
            if(label.isEmpty())label="Playlist";if(type.equals("m3u")&&!isHttp(u)&&!u.startsWith("content://")){toast("Informe URL HTTP/HTTPS ou importe um arquivo M3U.");return;}if(type.equals("xtream")&&(!isHttp(h)||un.isEmpty()||pw.isEmpty())){toast("Informe host, usuário e senha Xtream.");return;}
            String id=store.saveSource(new PlaylistSource(original==null?"":original.id,label,type,h,un,pw,u));
            if(type.equals("m3u"))loadM3u(u,true);else loadXtream(true);
        }).show();
    }
    private boolean isHttp(String value){return value.startsWith("https://")||value.startsWith("http://");}
    private void openM3uFile(){Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("*/*");intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(intent,REQUEST_M3U_FILE);}
    private void exportM3u(){if(entries.isEmpty()){toast("Carregue conteúdo antes de exportar.");return;}io.execute(()->{try{File dir=new File(getCacheDir(),"exports");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Não foi possível criar a pasta de exportação.");File file=new File(dir,"playlist-"+System.currentTimeMillis()+".m3u");try(FileOutputStream out=new FileOutputStream(file)){out.write(M3uSerializer.serialize(entries).getBytes(java.nio.charset.StandardCharsets.UTF_8));}Uri uri=androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".files",file);Intent share=new Intent(Intent.ACTION_SEND);share.setType("audio/x-mpegurl");share.putExtra(Intent.EXTRA_STREAM,uri);share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);runOnUiThread(()->startActivity(Intent.createChooser(share,"Exportar playlist M3U")));}catch(Exception e){runOnUiThread(()->toast("Falha ao exportar: "+e.getMessage()));}});}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();if(requestCode==REQUEST_BACKUP_EXPORT){requestBackupPassphrase(uri,true);return;}if(requestCode==REQUEST_BACKUP_IMPORT){requestBackupPassphrase(uri,false);return;}if(requestCode!=REQUEST_M3U_FILE)return;try{getContentResolver().takePersistableUriPermission(uri,data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}String label="Playlist local";try(Cursor cursor=getContentResolver().query(uri,null,null,null,null)){if(cursor!=null&&cursor.moveToFirst()){int column=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(column>=0)label=cursor.getString(column);}}catch(Exception ignored){}store.saveSource(new PlaylistSource("",label,"m3u","","","",uri.toString()));section="TV ao vivo";category="Todas";loadM3u(uri.toString(),true);}
    private void createBackup(){Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/octet-stream");intent.putExtra(Intent.EXTRA_TITLE,"readroid-stream-backup.rdsb");startActivityForResult(intent,REQUEST_BACKUP_EXPORT);}
    private void restoreBackup(){Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/octet-stream");startActivityForResult(intent,REQUEST_BACKUP_IMPORT);}
    private void requestBackupPassphrase(Uri uri,boolean export){LinearLayout form=new LinearLayout(this);form.setOrientation(1);EditText pass=input("Frase secreta do backup (mínimo 8 caracteres)",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);form.addView(pass);EditText confirm=null;if(export){confirm=input("Confirme a frase secreta",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);form.addView(confirm);}EditText confirmation=confirm;new AlertDialog.Builder(this).setTitle(export?"Proteger backup":"Desbloquear backup").setView(form).setNegativeButton("Cancelar",null).setPositiveButton(export?"Exportar":"Restaurar",(d,w)->{String secret=pass.getText().toString();if(secret.length()<8||(export&&!secret.equals(confirmation.getText().toString()))){toast("Frase inválida ou confirmação diferente.");return;}io.execute(()->{try{if(export){try(java.io.OutputStream out=getContentResolver().openOutputStream(uri,"w")){if(out==null)throw new Exception("Não foi possível gravar o arquivo.");BackupManager.write(out,store,secret.toCharArray());}}else{try(InputStream in=getContentResolver().openInputStream(uri)){if(in==null)throw new Exception("Não foi possível abrir o backup.");BackupManager.restore(in,store,secret.toCharArray());}runOnUiThread(()->{section="TV ao vivo";loadSavedSource();toast("Backup restaurado.");});}}catch(Exception e){runOnUiThread(()->toast("Backup não processado: "+e.getMessage()));}});}).show();}
    private EditText input(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(fg);e.setHintTextColor(muted);e.setSingleLine(true);e.setInputType(type);return e;}
    private String endpoint(){return section.equals("Filmes")?"movie":section.equals("Séries")?"series":"live";}
    private void loadXtream(boolean announce){PlaylistSource source=store.activeSource();if(source==null)return;String kind=endpoint(),filter=category;io.execute(()->{try{XtreamClient c=new XtreamClient(source.host,source.username,source.password);c.validate();List<String> cats=c.categories(kind);List<StreamItem> rows=c.items(kind,filter);List<StreamItem> scoped=new ArrayList<>();for(StreamItem item:rows)scoped.add(item.withSource(source.id));runOnUiThread(()->{categories=cats;entries=scoped;render();if(announce)toast("Playlist conectada: "+source.name);});}catch(Exception e){runOnUiThread(()->toast("Falha ao consultar "+source.name+": "+e.getMessage()));}});}
    private void loadM3u(String location,boolean announce){PlaylistSource source=store.activeSource();String sourceId=source==null?"":source.id;io.execute(()->{HttpURLConnection connection=null;try{InputStream stream;if(location.startsWith("content://")){stream=getContentResolver().openInputStream(Uri.parse(location));if(stream==null)throw new Exception("open");}else{connection=(HttpURLConnection)new URL(location).openConnection();connection.setConnectTimeout(15000);connection.setReadTimeout(30000);int code=connection.getResponseCode();if(code<200||code>=300)throw new Exception("HTTP "+code);stream=connection.getInputStream();}try(InputStream in=stream;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] chunk=new byte[8192];int count,total=0;while((count=in.read(chunk))!=-1){total+=count;if(total>64*1024*1024)throw new Exception("playlist too large");out.write(chunk,0,count);}List<StreamItem> parsed=M3uParser.parse(new String(out.toByteArray(),java.nio.charset.StandardCharsets.UTF_8));if(parsed.isEmpty())throw new Exception("invalid playlist");List<StreamItem> rows=new ArrayList<>();for(StreamItem item:parsed)rows.add(item.withSource(sourceId));runOnUiThread(()->{entries=rows;categories=new ArrayList<>();categories.add("Todas");for(StreamItem x:rows)if(x.category!=null&&!x.category.trim().isEmpty()&&!categories.contains(x.category))categories.add(x.category);render();if(announce)toast(rows.size()+" entradas importadas.");});}}catch(Exception e){String message=e.getMessage()!=null&&e.getMessage().startsWith("HTTP ")?"Falha HTTP ao importar playlist.":"Não foi possível abrir a playlist. Verifique rede, acesso ao arquivo e formato.";runOnUiThread(()->toast(message));}finally{if(connection!=null)connection.disconnect();}});}
    private boolean isCatalogSection(){return section.equals("TV ao vivo")||section.equals("Filmes")||section.equals("Séries");}
    private void render(){body.removeAllViews();TextView heading=text(section,23,true);heading.setPadding(dp(6),dp(4),0,dp(8));body.addView(heading);
        if(isCatalogSection()&&!categories.isEmpty()){ScrollView cs=new ScrollView(this);cs.setHorizontalScrollBarEnabled(false);LinearLayout cr=new LinearLayout(this);for(String cat:categories){Button b=button(cat);b.setOnClickListener(v->{category=cat;if(store.get("type").equals("xtream"))loadXtream(false);else renderRows();});cr.addView(b);}cs.addView(cr);body.addView(cs);}
        if(section.equals("Configurações")){renderSettings();return;}
        if(section.equals("Downloads")){renderDownloads();return;}
        if(section.equals("Editar M3U")){Button export=button("Exportar playlist editada");export.setOnClickListener(v->exportM3u());body.addView(export);}
        if(isCatalogSection()&&entries.isEmpty()){TextView empty=text("Adicione uma fonte para carregar conteúdo.",16,false);empty.setTextColor(muted);empty.setPadding(dp(6),dp(26),dp(6),dp(20));body.addView(empty);Button add=button("Gerenciar fontes");add.setOnClickListener(v->manageSources());body.addView(add);}renderRows();
    }
    private void renderSettings(){Button autoplay=button("Iniciar reprodução automaticamente: "+(store.preference("autoplay",true)?"Ativado":"Desativado"));autoplay.setOnClickListener(v->{boolean next=!store.preference("autoplay",true);store.setPreference("autoplay",next);render();});body.addView(autoplay);Button pip=button("Picture-in-Picture automático: "+(store.preference("pip",true)?"Ativado":"Desativado"));pip.setOnClickListener(v->{boolean next=!store.preference("pip",true);store.setPreference("pip",next);render();});body.addView(pip);Button sources=button("Gerenciar fontes");sources.setOnClickListener(v->manageSources());body.addView(sources);Button backup=button("Criar backup criptografado");backup.setOnClickListener(v->createBackup());body.addView(backup);Button restore=button("Restaurar backup criptografado");restore.setOnClickListener(v->restoreBackup());body.addView(restore);Button clearHistory=button("Limpar histórico de reprodução");clearHistory.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Apagar o histórico deste dispositivo?").setNegativeButton("Cancelar",null).setPositiveButton("Apagar",(d,w)->{store.clearHistory();toast("Histórico apagado.");}).show());body.addView(clearHistory);Button clearWatchlist=button("Limpar lista Assistir depois");clearWatchlist.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Apagar a lista Assistir depois?").setNegativeButton("Cancelar",null).setPositiveButton("Apagar",(d,w)->{store.clearWatchlist();toast("Lista apagada.");}).show());body.addView(clearWatchlist);}
    private boolean isDirectDownload(String value){if(!isHttp(value))return false;String path=Uri.parse(value).getPath();if(path==null)return false;String lower=path.toLowerCase();return lower.endsWith(".mp4")||lower.endsWith(".mkv")||lower.endsWith(".mov")||lower.endsWith(".avi")||lower.endsWith(".webm")||lower.endsWith(".ts");}
    private void enqueueDownload(StreamItem item){String extension=Uri.parse(item.url).getPath();extension=extension==null?".media":extension.substring(extension.lastIndexOf('.')).toLowerCase();String id=UUID.randomUUID().toString(),base=item.name.replaceAll("[^A-Za-z0-9._-]","_");if(base.length()>60)base=base.substring(0,60);String fileName=(base.isEmpty()?id:base)+"-"+id.substring(0,8)+extension;store.saveDownload(new DownloadEntry(id,item.name,item.url,fileName,"queued",0,-1,item.sourceId,item.headers));Intent command=MediaDownloadService.command(this,MediaDownloadService.ACTION_ENQUEUE,id);if(android.os.Build.VERSION.SDK_INT>=26)startForegroundService(command);else startService(command);toast("Download adicionado à fila privada do app.");}
    private void renderDownloads(){List<DownloadEntry> downloads=store.downloads();if(downloads.isEmpty()){body.addView(text("Nenhum download salvo.",15,false));return;}for(DownloadEntry entry:downloads){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(8),dp(8),dp(8));row.setBackgroundColor(panel);row.addView(text(entry.title,16,true));String progress=entry.total>0?String.format(java.util.Locale.ROOT,"%.1f%% · %s / %s",entry.bytes*100.0/entry.total,android.text.format.Formatter.formatFileSize(this,entry.bytes),android.text.format.Formatter.formatFileSize(this,entry.total)):android.text.format.Formatter.formatFileSize(this,entry.bytes);row.addView(text(entry.state+" · "+progress,13,false));LinearLayout actions=new LinearLayout(this);if(entry.state.equals("complete")){Button play=button("Reproduzir offline");play.setOnClickListener(v->{File dir=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);if(dir==null)dir=new File(getFilesDir(),"downloads");File file=new File(dir,entry.fileName);if(file.isFile())VideoActivity.launch(this,entry.title,Uri.fromFile(file).toString());else toast("Arquivo de download não encontrado.");});actions.addView(play);}else if(entry.state.equals("downloading")){Button pause=button("Pausar");pause.setOnClickListener(v->sendDownloadCommand(MediaDownloadService.ACTION_PAUSE,entry.id));actions.addView(pause);}else{Button resume=button(entry.state.equals("error")?"Tentar novamente":"Retomar");resume.setOnClickListener(v->sendDownloadCommand(MediaDownloadService.ACTION_RESUME,entry.id));actions.addView(resume);}Button delete=button("Excluir");delete.setOnClickListener(v->sendDownloadCommand(MediaDownloadService.ACTION_DELETE,entry.id));actions.addView(delete);row.addView(actions);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));body.addView(row,lp);}}
    private void sendDownloadCommand(String action,String id){Intent command=MediaDownloadService.command(this,action,id);if(android.os.Build.VERSION.SDK_INT>=26)startForegroundService(command);else startService(command);body.postDelayed(()->{if(section.equals("Downloads"))render();},500);}
    private void renderRows(){
        if(body==null)return;
        for(int i=body.getChildCount()-1;i>=0;i--){View v=body.getChildAt(i);if("stream-row".equals(v.getTag()))body.removeViewAt(i);}
        String query=search==null?"":search.getText().toString().trim().toLowerCase();List<StreamItem> source;
        if(section.equals("Favoritos"))source=store.favorites();else if(section.equals("Assistir depois"))source=store.watchlist();else if(section.equals("Histórico"))source=store.history();else source=entries;
        shown=new ArrayList<>();
        for(StreamItem item:source){
            if(!section.equals("Favoritos")&&!section.equals("Assistir depois")&&!section.equals("Histórico")){
                if(store.get("type").equals("m3u")){if(section.equals("Filmes")&&!item.kind.equals("vod"))continue;if(section.equals("Séries"))continue;if(section.equals("TV ao vivo")&&item.kind.equals("vod"))continue;}
                if(!category.equals("Todas")&&!category.equals(item.category))continue;
            }
            if(!query.isEmpty()&&!item.name.toLowerCase().contains(query))continue;
            shown.add(item);LinearLayout row=new LinearLayout(this);row.setTag("stream-row");row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(8),dp(6),dp(8));row.setBackgroundColor(panel);row.setFocusable(true);row.setFocusableInTouchMode(true);row.setOnFocusChangeListener((v,focused)->row.setBackgroundColor(focused?Color.rgb(34,68,56):panel));
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(70));rp.setMargins(0,dp(4),0,dp(4));body.addView(row,rp);
            TextView title=text(item.name,16,true);row.addView(title,new LinearLayout.LayoutParams(0,-2,1));
            if(section.equals("Editar M3U")){Button edit=button("Editar");edit.setOnClickListener(v->editM3uItem(item));row.addView(edit);Button remove=button("Excluir");remove.setOnClickListener(v->{entries.remove(item);renderRows();});row.addView(remove);continue;}
            if((item.kind.equals("vod")||item.kind.equals("movie")||item.kind.equals("episode"))&&isDirectDownload(item.url)){Button download=button("Baixar");download.setOnClickListener(v->enqueueDownload(item));row.addView(download);}
            Button star=button(isFav(item)?"★":"☆");star.setContentDescription(isFav(item)?"Remover dos favoritos":"Adicionar aos favoritos");star.setOnClickListener(v->{toggleFav(item);star.setText(isFav(item)?"★":"☆");});row.addView(star);
            Button later=button(isInWatchlist(item)?"✓":"＋");later.setContentDescription(isInWatchlist(item)?"Remover da lista para assistir":"Salvar para assistir depois");later.setOnClickListener(v->{toggleWatchlist(item);later.setText(isInWatchlist(item)?"✓":"＋");});if(item.kind.equals("vod")||item.kind.equals("series"))row.addView(later);
            if(section.equals("TV ao vivo")&&store.get("type").equals("xtream")){Button epg=button("EPG");epg.setOnClickListener(v->loadEpg(item));row.addView(epg);}
            Button play=button(item.kind.equals("series")?"Episódios":(section.equals("Filmes")||item.kind.equals("vod"))?"Detalhes":"Reproduzir");play.setOnClickListener(v->{if(item.kind.equals("series"))loadEpisodes(item);else if(section.equals("Filmes")||item.kind.equals("vod"))loadFilmDetails(item);else playItem(item);});row.addView(play);
            row.setOnClickListener(v->{if(item.kind.equals("series"))loadEpisodes(item);else if(section.equals("Filmes")||item.kind.equals("vod"))loadFilmDetails(item);else playItem(item);});
        }
        if(shown.isEmpty()){TextView empty=text(query.isEmpty()?"Esta seção ainda não tem itens.":"Nenhum resultado para esta pesquisa.",15,false);empty.setTextColor(muted);body.addView(empty);}
    }
    private void playItem(StreamItem item){store.recordHistory(item);VideoActivity.launch(this,item.name,item.url,item.headers);}
    private void editM3uItem(StreamItem item){LinearLayout form=new LinearLayout(this);form.setOrientation(1);EditText name=input("Nome",InputType.TYPE_CLASS_TEXT);name.setText(item.name);EditText group=input("Categoria",InputType.TYPE_CLASS_TEXT);group.setText(item.category);form.addView(name);form.addView(group);new AlertDialog.Builder(this).setTitle("Editar entrada M3U").setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->{for(int i=0;i<entries.size();i++)if(entries.get(i)==item){entries.set(i,new StreamItem(item.id,name.getText().toString(),item.url,group.getText().toString(),item.kind,item.logo,item.sourceId,item.headers));break;}renderRows();}).show();}
    private PlaylistSource sourceFor(StreamItem item){for(PlaylistSource source:store.sources())if(source.id.equals(item.sourceId))return source;return store.activeSource();}
    private void loadEpg(StreamItem channel){PlaylistSource source=sourceFor(channel);if(source==null||!source.type.equals("xtream")){toast("O EPG está disponível para canais Xtream.");return;}io.execute(()->{try{List<ProgramEntry> programs=new XtreamClient(source.host,source.username,source.password).shortEpg(channel.id,5);runOnUiThread(()->{if(programs.isEmpty()){toast("O provedor não retornou programação para este canal.");return;}StringBuilder details=new StringBuilder();for(ProgramEntry p:programs){if(details.length()>0)details.append("\n\n");details.append(p.start);if(!p.end.isEmpty())details.append("–").append(p.end);details.append("  ").append(p.title);if(!p.description.isEmpty())details.append("\n").append(p.description);}new AlertDialog.Builder(this).setTitle("Programação · "+channel.name).setMessage(details.toString()).setPositiveButton("Fechar",null).show();});}catch(Exception e){runOnUiThread(()->toast("Falha ao carregar EPG: "+e.getMessage()));}});}
    private void loadFilmDetails(StreamItem item){PlaylistSource source=sourceFor(item);if(source==null||!source.type.equals("xtream")){new AlertDialog.Builder(this).setTitle(item.name).setMessage(item.category).setPositiveButton("Reproduzir",(d,w)->playItem(item)).setNegativeButton("Fechar",null).show();return;}io.execute(()->{try{org.json.JSONObject root=new XtreamClient(source.host,source.username,source.password).vodInfo(item.id);org.json.JSONObject info=root.optJSONObject("info");runOnUiThread(()->{String plot=info==null?"":info.optString("plot","");String genre=info==null?"":info.optString("genre","");String year=info==null?"":info.optString("releasedate",info.optString("releaseDate",""));String cast=info==null?"":info.optString("cast","");String director=info==null?"":info.optString("director","");StringBuilder message=new StringBuilder();for(String line:new String[]{year,genre,director,cast,plot})if(line!=null&&!line.isEmpty()){if(message.length()>0)message.append("\n\n");message.append(line);}new AlertDialog.Builder(this).setTitle(item.name).setMessage(message.length()==0?"Sem informações adicionais do provedor.":message.toString()).setPositiveButton("Reproduzir",(d,w)->playItem(item)).setNeutralButton(isInWatchlist(item)?"Na lista":"Assistir depois",(d,w)->toggleWatchlist(item)).setNegativeButton("Fechar",null).show();});}catch(Exception e){runOnUiThread(()->toast("Falha ao carregar detalhes: "+e.getMessage()));}});}
    private void loadEpisodes(StreamItem series){
        PlaylistSource source=sourceFor(series);if(source==null||!source.type.equals("xtream")){toast("Esta playlist não fornece dados de episódios.");return;}
        io.execute(()->{try{
            List<StreamItem> raw=new XtreamClient(source.host,source.username,source.password).episodes(series.id,series.name);List<StreamItem> episodes=new ArrayList<>();for(StreamItem item:raw)episodes.add(item.withSource(series.sourceId));
            runOnUiThread(()->{
                if(episodes.isEmpty()){toast("O provedor não retornou episódios para esta série.");return;}
                String[] labels=new String[episodes.size()];for(int i=0;i<episodes.size();i++)labels[i]=episodes.get(i).category+"  ·  "+episodes.get(i).name;
            new AlertDialog.Builder(this).setTitle(series.name).setItems(labels,(dialog,which)->{StreamItem episode=episodes.get(which).withSource(series.sourceId);playItem(episode);}).setNegativeButton("Fechar",null).show();
            });
        }catch(Exception e){runOnUiThread(()->toast("Não foi possível carregar episódios: "+e.getMessage()));}});
    }
    private boolean sameItem(StreamItem a,StreamItem b){return (a.sourceId.equals(b.sourceId)||a.sourceId.isEmpty()||b.sourceId.isEmpty())&&a.id.equals(b.id)&&a.kind.equals(b.kind);}
    private boolean isFav(StreamItem item){for(StreamItem x:store.favorites())if(sameItem(x,item))return true;return false;}
    private boolean isInWatchlist(StreamItem item){for(StreamItem x:store.watchlist())if(sameItem(x,item))return true;return false;}
    private void toggleFav(StreamItem item){List<StreamItem> f=store.favorites();boolean present=false;for(StreamItem x:f)if(sameItem(x,item)){present=true;break;}f.removeIf(x->sameItem(x,item));if(!present)f.add(item);store.favorites(f);if(section.equals("Favoritos"))renderRows();}
    private void toggleWatchlist(StreamItem item){List<StreamItem> f=store.watchlist();boolean present=false;for(StreamItem x:f)if(sameItem(x,item)){present=true;break;}f.removeIf(x->sameItem(x,item));if(!present)f.add(item);store.watchlist(f);if(section.equals("Assistir depois"))renderRows();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override protected void onDestroy(){super.onDestroy();io.shutdownNow();}
}
