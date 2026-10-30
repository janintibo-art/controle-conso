package fr.tibofou.controleconso;

import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
 final int bg=0xff0d1017, panel=0xff1b202c, red=0xffff3c50, pale=0xffc8cdd8;
 LinearLayout root, listing, header; TextView summary, status; EditText search;
 int days=7; String filter=""; ArrayList<Item> items=new ArrayList<>();
 static class Item {String name, pkg; long fg,bg,total; int uid; Item(String n,String p,int u){name=n;pkg=p;uid=u;} }
 int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 TextView txt(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextColor(color);v.setTextSize(size);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
 LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 void pad(View v,int a){v.setPadding(dp(a),dp(a),dp(a),dp(a));}
 void add(LinearLayout parent,View view){parent.addView(view,new LinearLayout.LayoutParams(-1,-2));}
 TextView button(String label,Runnable action){TextView b=txt(label,13,Color.WHITE,true);b.setGravity(Gravity.CENTER);pad(b,11);b.setBackground(shape(0xffaf2336,12));b.setOnClickListener(v->action.run());return b;}
 @Override public void onCreate(Bundle b){super.onCreate(b);build();}
 void build(){
  ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(bg);root=vertical();pad(root,16);sc.addView(root);setContentView(sc);
  LinearLayout brand=new LinearLayout(this);brand.setGravity(Gravity.CENTER_VERTICAL);
  ImageView logo=new ImageView(this);logo.setImageResource(R.mipmap.ic_launcher);brand.addView(logo,new LinearLayout.LayoutParams(dp(68),dp(68)));
  LinearLayout title=vertical();TextView h=txt("CONTRÔLE CONSO",24,Color.WHITE,true);add(title,h);add(title,txt("LIBÈRE TES DONNÉES • SURVEILLE LE RÉSEAU",10,red,true));brand.addView(title);add(root,brand);
  TextView desc=txt("Trafic mobile par application • premier plan / arrière-plan",13,pale,false);desc.setPadding(0,dp(15),0,dp(15));add(root,desc);
  summary=txt("",17,Color.WHITE,true);pad(summary,16);summary.setBackground(shape(panel,18));add(root,summary);
  LinearLayout tabs=new LinearLayout(this);tabs.setPadding(0,dp(14),0,dp(12)); for(int d:new int[]{1,7,30}) { TextView t=button(d==1?"24 H":d+" J",()->{days=d;load();});LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(0,-2,1);a.setMargins(0,0,dp(8),0);tabs.addView(t,a);}add(root,tabs);
  TextView allow=button("AUTORISER L'ACCÈS AUX STATISTIQUES",()->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));add(root,allow);
  TextView data=button("RÉGLAGES GÉNÉRAUX DES DONNÉES",()->safeStart(new Intent(Settings.ACTION_DATA_USAGE_SETTINGS),new Intent(Settings.ACTION_SETTINGS)));data.setBackground(shape(panel,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(8);root.addView(data,lp);
  search=new EditText(this);search.setSingleLine(true);search.setHint("Rechercher une application...");search.setHintTextColor(0xff8e98aa);search.setTextColor(Color.WHITE);search.setTextSize(15);search.setBackground(shape(panel,12));pad(search,12);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(15);root.addView(search,sp);search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){filter=s.toString().toLowerCase(Locale.ROOT);render();}public void afterTextChanged(android.text.Editable e){}});
  status=txt("",13,pale,false);status.setPadding(0,dp(12),0,dp(12));add(root,status);listing=vertical();add(root,listing);load();
 }
 boolean allowed(){AppOpsManager ops=(AppOpsManager)getSystemService(APP_OPS_SERVICE);return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),getPackageName())==AppOpsManager.MODE_ALLOWED;}
 void load(){if(!allowed()){summary.setText("Accès aux statistiques requis");status.setText("Active l'autorisation, puis reviens ici.");listing.removeAllViews();return;}status.setText("Lecture des statistiques en cours...");new Thread(()->{
 ArrayList<Item> out=new ArrayList<>();String error=null;
 try{
  PackageManager pm=getPackageManager();HashMap<Integer,Item> map=new HashMap<>();
  for(ApplicationInfo ai:pm.getInstalledApplications(0)){if(ai.uid<10000)continue;Item old=map.get(ai.uid);if(old==null){Item item=new Item(pm.getApplicationLabel(ai).toString(),ai.packageName,ai.uid);map.put(ai.uid,item);}}
  NetworkStatsManager mgr=(NetworkStatsManager)getSystemService(NETWORK_STATS_SERVICE);
  long end=System.currentTimeMillis(),start=end-days*86400000L;
  NetworkStats stats=mgr.querySummary(ConnectivityManager.TYPE_MOBILE,null,start,end);
  if(stats!=null){NetworkStats.Bucket bucket=new NetworkStats.Bucket();try{while(stats.hasNextBucket()){stats.getNextBucket(bucket);Item it=map.get(bucket.getUid());if(it==null){it=new Item("Système / UID "+bucket.getUid(),"",bucket.getUid());map.put(bucket.getUid(),it);}long n=Math.max(0,bucket.getRxBytes())+Math.max(0,bucket.getTxBytes());if(bucket.getState()==NetworkStats.Bucket.STATE_FOREGROUND)it.fg+=n;else it.bg+=n;it.total+=n;}}finally{stats.close();}}
  out.addAll(map.values());out.sort((a,b)->Long.compare(b.total,a.total));
 }catch(Exception e){error=e.getClass().getSimpleName()+" : "+e.getMessage();}
 final String err=error;runOnUiThread(()->{if(err!=null){summary.setText("Lecture impossible");status.setText(err+". Vérifie l’autorisation et les restrictions du téléphone.");listing.removeAllViews();return;}items=out;render();});
 }).start();}
 String fmt(long b){if(b>=1073741824)return String.format(Locale.FRANCE,"%.2f Go",b/1073741824.0);if(b>=1048576)return String.format(Locale.FRANCE,"%.1f Mo",b/1048576.0);if(b>=1024)return String.format(Locale.FRANCE,"%.1f Ko",b/1024.0);return b+" o";}
 void render(){if(listing==null)return;listing.removeAllViews();long sum=0,bgs=0;for(Item i:items){sum+=i.total;bgs+=i.bg;}summary.setText("DONNÉES MOBILES • "+(days==1?"24 H":days+" J")+"\n"+fmt(sum)+" consommés\n"+fmt(bgs)+" en arrière-plan");int visible=0;
  for(Item it:items){if(it.total==0 || !(it.name+" "+it.pkg).toLowerCase(Locale.ROOT).contains(filter))continue;visible++;LinearLayout card=vertical();pad(card,14);card.setBackground(shape(panel,15));TextView title=txt(it.name,17,Color.WHITE,true);add(card,title);add(card,txt(it.pkg.isEmpty()?"UID "+it.uid:it.pkg,11,0xffa6afc0,false));TextView usage=txt("TOTAL   "+fmt(it.total)+"\nACTIVITÉ   "+fmt(it.fg)+"     ARRIÈRE-PLAN   "+fmt(it.bg),13,pale,false);usage.setPadding(0,dp(10),0,dp(10));add(card,usage);
  if(!it.pkg.isEmpty()){TextView action=button("GÉRER LES DONNÉES EN ARRIÈRE-PLAN  ›",()->openAppSettings(it.pkg));add(card,action);}LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.bottomMargin=dp(10);listing.addView(card,cp);
  }status.setText(visible+" applications avec trafic visible • données estimées par Android. Certains UID sont partagés ou non attribués.");}
 void safeStart(Intent primary,Intent fallback){try{startActivity(primary);}catch(Exception x){try{startActivity(fallback);}catch(Exception y){Toast.makeText(this,"Réglages indisponibles",Toast.LENGTH_SHORT).show();}}}
 void openAppSettings(String pkg){Intent direct=new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,Uri.parse("package:"+pkg));Intent app=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+pkg));safeStart(direct,app);}
 @Override protected void onResume(){super.onResume();if(root!=null)load();}
}
