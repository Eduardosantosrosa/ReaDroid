package org.readroid.extremerebuild.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import org.readroid.extremerebuild.data.M3uParser;
import org.readroid.extremerebuild.data.StreamItem;
import org.readroid.extremerebuild.data.XtreamClient;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
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
        for(String s:new String[]{"TV ao vivo","Filmes","Séries","Favoritos","Adicionar"}){Button x=button(s);x.setOnClickListener(v->{if(s.equals("Adicionar")){addSource();return;}section=s;category="Todas";if(store.get("type").equals("xtream")&&!s.equals("Favoritos"))loadXtream(false);else render();});nav.addView(x);}
        search=new EditText(this);search.setSingleLine(true);search.setHintTextColor(muted);search.setTextColor(fg);search.setHint("Pesquisar canais e títulos");search.setPadding(dp(15),dp(8),dp(15),dp(8));search.setBackgroundColor(panel);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(48));sp.setMargins(dp(18),dp(4),dp(18),dp(10));root.addView(search,sp);search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){renderRows();}public void afterTextChanged(android.text.Editable e){}});
        ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(14),0,dp(14),dp(16));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        search.setOnEditorActionListener((v,a,e)->{renderRows();return false;});render();
    }
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextColor(fg);v.setTextSize(size);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(fg);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel));b.setFocusable(true);b.setPadding(dp(12),0,dp(12),0);b.setMinimumWidth(dp(72));return b;}
    private void loadSavedSource(){String type=store.get("type");if(type.isEmpty())return;if(type.equals("m3u")){loadM3u(store.get("url"),false);}else loadXtream(false);}
    private void addSource(){
        LinearLayout form=new LinearLayout(this);form.setOrientation(1);form.setPadding(dp(8),dp(4),dp(8),dp(4));
        String[] modes={"M3U/M3U8 URL","Xtream Codes"};android.widget.Spinner spinner=new android.widget.Spinner(this);spinner.setAdapter(new android.widget.ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,modes));form.addView(spinner);
        EditText host=input("Host URL (https://servidor:porta)",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI),user=input("Usuário",InputType.TYPE_CLASS_TEXT),pass=input("Senha",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD),url=input("URL da playlist M3U/M3U8",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        form.addView(host);form.addView(user);form.addView(pass);form.addView(url);
        Runnable visibility=()->{boolean xt=spinner.getSelectedItemPosition()==1;host.setVisibility(xt?View.VISIBLE:View.GONE);user.setVisibility(xt?View.VISIBLE:View.GONE);pass.setVisibility(xt?View.VISIBLE:View.GONE);url.setVisibility(xt?View.GONE:View.VISIBLE);};spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?>p){}public void onItemSelected(android.widget.AdapterView<?>p,View v,int pos,long id){visibility.run();}});visibility.run();
        new AlertDialog.Builder(this).setTitle("Adicionar playlist").setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Conectar",(d,w)->{
            if(spinner.getSelectedItemPosition()==0){String u=url.getText().toString().trim();if(!u.startsWith("https://")&&!u.startsWith("http://")){toast("Informe uma URL HTTP ou HTTPS válida.");return;}store.source("m3u","","","",u);loadM3u(u,true);}
            else {String h=host.getText().toString().trim(),un=user.getText().toString(),pw=pass.getText().toString();if(!h.startsWith("https://")&&!h.startsWith("http://")){toast("Informe o host do provedor.");return;}store.source("xtream",h,un,pw,"");loadXtream(true);}
        }).show();
    }
    private EditText input(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(fg);e.setHintTextColor(muted);e.setSingleLine(true);e.setInputType(type);return e;}
    private String endpoint(){return section.equals("Filmes")?"movie":section.equals("Séries")?"series":"live";}
    private void loadXtream(boolean announce){io.execute(()->{try{XtreamClient c=new XtreamClient(store.get("host"),store.get("user"),store.get("pass"));c.validate();String kind=endpoint();List<String> cats=c.categories(kind);List<StreamItem> rows=c.items(kind,category);runOnUiThread(()->{categories=cats;entries=rows;render();if(announce)toast("Playlist conectada.");});}catch(Exception e){runOnUiThread(()->toast("Falha ao consultar o provedor: "+e.getMessage()));}});}
    private void loadM3u(String url,boolean announce){io.execute(()->{HttpURLConnection c=null;try{c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(30000);try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] chunk=new byte[8192];int count;while((count=in.read(chunk))!=-1)out.write(chunk,0,count);List<StreamItem> rows=M3uParser.parse(new String(out.toByteArray(),java.nio.charset.StandardCharsets.UTF_8));if(rows.isEmpty())throw new Exception("A playlist não contém entradas válidas.");runOnUiThread(()->{entries=rows;categories=new ArrayList<>();categories.add("Todas");for(StreamItem x:rows)if(x.category!=null&&!x.category.trim().isEmpty()&&!categories.contains(x.category))categories.add(x.category);render();if(announce)toast(rows.size()+" entradas importadas.");});}}catch(Exception e){runOnUiThread(()->toast("Não foi possível importar: "+e.getMessage()));}finally{if(c!=null)c.disconnect();}});}
    private void render(){body.removeAllViews();TextView heading=text(section,23,true);heading.setPadding(dp(6),dp(4),0,dp(8));body.addView(heading);
        if(!categories.isEmpty()){ScrollView cs=new ScrollView(this);cs.setHorizontalScrollBarEnabled(false);LinearLayout cr=new LinearLayout(this);for(String cat:categories){Button b=button(cat);b.setOnClickListener(v->{category=cat;if(store.get("type").equals("xtream"))loadXtream(false);else renderRows();});cr.addView(b);}cs.addView(cr);body.addView(cs);}
        if(entries.isEmpty()){TextView empty=text("Adicione uma playlist para carregar conteúdo do seu provedor.",16,false);empty.setTextColor(muted);empty.setPadding(dp(6),dp(26),dp(6),dp(20));body.addView(empty);Button add=button("Adicionar playlist");add.setOnClickListener(v->addSource());body.addView(add);}renderRows();
    }
    private void renderRows(){if(body==null)return;for(int i=body.getChildCount()-1;i>=0;i--){View v=body.getChildAt(i);if("stream-row".equals(v.getTag()))body.removeViewAt(i);}String query=search==null?"":search.getText().toString().toLowerCase();List<StreamItem> source=section.equals("Favoritos")?store.favorites():entries;shown=new ArrayList<>();
        for(StreamItem item:source){if(!section.equals("Favoritos")&&store.get("type").equals("m3u")){if(section.equals("Filmes")&&!item.kind.equals("vod"))continue;if(section.equals("Séries"))continue;if(section.equals("TV ao vivo")&&item.kind.equals("vod"))continue;}if(!category.equals("Todas")&&!category.equals(item.category))continue;if(!query.trim().isEmpty()&&!item.name.toLowerCase().contains(query))continue;shown.add(item);LinearLayout row=new LinearLayout(this);row.setTag("stream-row");row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(8),dp(8),dp(8));row.setBackgroundColor(panel);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(64));rp.setMargins(0,dp(4),0,dp(4));body.addView(row,rp);TextView title=text(item.name,16,true);row.addView(title,new LinearLayout.LayoutParams(0,-2,1));Button star=button(isFav(item)?"★":"☆");star.setOnClickListener(v->{toggleFav(item);star.setText(isFav(item)?"★":"☆");});row.addView(star);Button play=button(item.kind.equals("series")?"Abrir":"Reproduzir");play.setOnClickListener(v->{if(item.kind.equals("series")){toast("Detalhes de série ainda não estão disponíveis.");return;}VideoActivity.launch(this,item.name,item.url);});row.addView(play);row.setOnClickListener(v->{if(!item.kind.equals("series"))VideoActivity.launch(this,item.name,item.url);});}
        if(shown.isEmpty()&&!entries.isEmpty()){TextView empty=text("Nenhum resultado para este filtro.",15,false);empty.setTextColor(muted);body.addView(empty);}}
    private boolean isFav(StreamItem item){for(StreamItem x:store.favorites())if(x.id.equals(item.id)&&x.url.equals(item.url))return true;return false;}
    private void toggleFav(StreamItem item){List<StreamItem> f=store.favorites();boolean present=false;for(StreamItem x:f)if(x.id.equals(item.id)&&x.url.equals(item.url)){present=true;break;}f.removeIf(x->x.id.equals(item.id)&&x.url.equals(item.url));if(!present)f.add(item);store.favorites(f);if(section.equals("Favoritos"))renderRows();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override protected void onDestroy(){super.onDestroy();io.shutdownNow();}
}
