/*TÜRKAY BİLİYOR   turkaybiliyor@hotmail.com*/
package com.sensbox;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.location.Location;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import com.nutiteq.MapView;
import com.nutiteq.components.Components;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.Options;
import com.nutiteq.geometry.Line;
import com.nutiteq.geometry.Marker;
import com.nutiteq.layers.Layer;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.projections.Projection;
import com.nutiteq.rasterlayers.TMSMapLayer;
import com.nutiteq.style.LabelStyle;
import com.nutiteq.style.LineStyle;
import com.nutiteq.style.MarkerStyle;
import com.nutiteq.style.StyleSet;
import com.nutiteq.ui.DefaultLabel;
import com.nutiteq.ui.Label;
import com.nutiteq.utils.UnscaledBitmapLoader;
import com.nutiteq.vectorlayers.GeometryLayer;
import com.nutiteq.vectorlayers.MarkerLayer;

public class SensBoxActivity extends Activity  {	
	private PowerManager.WakeLock wl;
	public static MapView mapView;
	private MarkerLayer routemarkerLayer=null;
	private MarkerLayer trckmarkerLayer=null;
	private Layer baselayer=null;
	private double slp_inHg_=29.92;	
	private int interval=250;
	private boolean init = false;
	private static double pressure_hPa_= 1013.0912;	
	private static final double SLT_K = 288.15;  // Sea level temperature.
	private static final double TLAPSE_K_PER_M = -0.0065;  // Linear temperature atmospheric lapse rate.
	private static final double G_M_PER_S_PER_S = 9.80665;  // Acceleration from gravity.
	private static final double R_J_PER_KG_PER_K = 287.052;  // Specific gas constant for air, US Standard Atmosphere edition.	
	private static final double PA_PER_INHG = 3386;  // Pascals per inch of mercury.
	private static final double FT_PER_M = 3.2808399;  // Feet per meter.
	private static final double KF_VAR_ACCEL = 0.0075;  // Variance of pressure acceleration noise input.
	private static final double KF_VAR_MEASUREMENT = 0.05;  // Variance of pressure measurement noise.	
	LabelStyle labelStyle=null;
	MapPos currentpoint;
	MapEventListener mapListener = null;
    private final static String TAG = SensBoxActivity.class.getSimpleName();   
    private BeepThread beeps=null;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static String UUIDNAME;
    public final static UUID UUID_FLYTEC_MANUFACTURER =UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_FLYTEC_SERVICE =UUID.fromString("aba27100-143b-4b81-a444-edcd0000f020");
    public final static UUID UUID_FLYTEC_SENSOR_NAVIGATION =UUID.fromString("aba27100-143b-4b81-a444-edcd0000f022");
    public final static UUID UUID_FLYTEC_SENSOR_MOVEMENT =UUID.fromString("aba27100-143b-4b81-a444-edcd0000f023");
    public final static UUID UUID_FLYTEC_SCOND_GPS =UUID.fromString("aba27100-143b-4b81-a444-edcd0000f024");
    public final static UUID UUID_FLYTEC_SYSTEM =UUID.fromString("aba27100-143b-4b81-a444-edcd0000f025");
    public final static UUID UUID_COMMSERVICE =UUID.fromString("aba27100-143b-4b81-a444-edcd0000f011");
    public final static UUID UUID_TRANSFER =UUID.fromString("aba27100-143b-4b81-a444-edcd0000f012");
    public final static UUID UUID_FILETRANSFER =UUID.fromString("aba27100-143b-4b81-a444-edcd0000f013");    
    public static UUID CURRENTUUID=null;
    private TextView connectionState;
    private TextView txtdatetime,txtlatitude,txtlongitude,txtalt,AvgVarTxt,AltTxt, SpeedTxt,DTkf,TrackTxt,TimeTxt,  
    txtspeed,txtvario,txtsats,txttemp,txtstatus,txtheading,TempTxt;   
    private String pilotname, wingmodel, pilotid, logfilename = null;
    private RelativeLayout footer,header;
    private ScrollView sview;
    private String deviceName;
    private String deviceAddress;   
    boolean isConnected=false,sendcommand=false;
    private int gpsalt=0,gpsfix=0,numberofsats=0,sdfix=0,loglevel=0,logstart=0,intlogtime=3000,trackcount=0,battstatus,battlevel,soundtype=2;
    double dbvario=0,oldvario=0,senslat=0,senslng=0,baroalt=0,speed=0,heading=0,dtemp=0,startlatitude=0,startlongitude=0;
    String sgpstime;   
    int whichCommand=0;
    int color=Color.RED;
    private double sinkalarm=2.5,pressure; 
    String status1,status2;
    private long dv,senstime;
    private float markersize=0.5f;
    Date df;    
    private String mapPath=null;
	private VerticalProgressBar_Reverse avgprogbar_reverse;
	private VerticalProgressBar avgprogbar;
	private static boolean igcstart=false,logheader=false,enablelog=false,highress=false,
			logfooter=false,drawgpstrack=false,getfirstfix=false,touched=false,mapupdate=true;
   public static boolean showdata=false;
   ArrayList<String> igclog = new ArrayList<String>();
   ArrayList<MapPos> routePoints = new ArrayList<MapPos>();
   ArrayList<MapPos> trackPoints = new ArrayList<MapPos>();
   private static Projection proj=new EPSG3857();
   private float zoomlevel=14,linewidth=0.05f,tiltlevel=0.0f;
   private Button altinc,altdec;
   static PositionWriter liveWriter;
   static String username,password,serverUrl,stime,errorinfo;
   static boolean loginLW=false,error=false,livetrackenabled=false;
   static int vechiletype=1,LWcount=0,type=0;
   static Context basecontext;
   static SimpleDateFormat sdf;
   private Handler loghandler = new Handler();	
   private Handler sensboxhandler = new Handler();	
   private SenseBoxAdapter senseBoxAdapter = null;
   private static final int REQUEST_ENABLE_BT = 0;
   private final static int IGCFILE = 1;
   private final static int SETALT = 2;  
   private BluetoothAdapter bluetoothAdapter;
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);      
       PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	   wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
	   wl.acquire();
	   setContentView(R.layout.main);
	   if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
           Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
           finish();
           return;
       }
	   final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
       bluetoothAdapter = bluetoothManager.getAdapter();

       // Checks if Bluetooth is supported on the device.
       if (bluetoothAdapter == null) {
           Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
           finish();
           return;
       }  
	   connectionState = (TextView) findViewById(R.id.connection_state);       
	   txtdatetime= (TextView) findViewById(R.id.date_time);
	   txtlatitude= (TextView) findViewById(R.id.lat);
	   txtlongitude= (TextView) findViewById(R.id.lng);
	   txtalt= (TextView) findViewById(R.id.alt);
	   txtspeed= (TextView) findViewById(R.id.speed);
	   txtheading=(TextView) findViewById(R.id.heading);
	   txtvario= (TextView) findViewById(R.id.sensvario);
	   txtsats=(TextView) findViewById(R.id.sats);
	   txttemp=(TextView) findViewById(R.id.temp);       
	   txtstatus=(TextView) findViewById(R.id.status);
	   AvgVarTxt=(TextView) findViewById(R.id.avgvario);
	   AltTxt = (TextView) findViewById(R.id.map_alt);
		SpeedTxt = (TextView) findViewById(R.id.map_speed);		
		DTkf = (TextView) findViewById(R.id.map_disttakeoff);
		TrackTxt=(TextView) findViewById(R.id.trackcount);
		TimeTxt=(TextView) findViewById(R.id.map_time);
		TempTxt=(TextView) findViewById(R.id.map_temp);
	   avgprogbar_reverse=(VerticalProgressBar_Reverse) findViewById(R.id.avgprogbar_reverse);
	   avgprogbar=(VerticalProgressBar) findViewById(R.id.avgprogbar);	
	   header=(RelativeLayout)findViewById(R.id.header); 
	   footer=(RelativeLayout)findViewById(R.id.footer); 
	   sview=(ScrollView)findViewById(R.id.scrollableContents); 	  
	   getActionBar().setDisplayHomeAsUpEnabled(true);
	   routemarkerLayer = new MarkerLayer(proj); 
	   trckmarkerLayer = new MarkerLayer(proj);	   	   
	   startMap(); 
	   setScreen();	        
       if (!bluetoothAdapter.isEnabled()) {
           final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
           startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
           return;
       }else
    	   init();
    		
	   altinc= (Button)findViewById(R.id.altinc);
	   altdec= (Button) findViewById(R.id.altdec);	
	   altinc.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {									
				long slp_inHg_long = Math.round(100.0 * slp_inHg_);
				if (slp_inHg_long < 3100) ++slp_inHg_long;	
		    	slp_inHg_ = slp_inHg_long / 100.0;	
		    	pressure=altTohPa(baroalt);					    				
				AltTxt.setText(String.format("Alt: %1.1f m",hPaToMeter(slp_inHg_,pressure))); 
			}
		});		
	   altdec.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {	
		    	long slp_inHg_long = Math.round(100.0 * slp_inHg_);
				if (slp_inHg_long > 2810) --slp_inHg_long;	 
		    	slp_inHg_ = slp_inHg_long / 100.0;		
		    	pressure=altTohPa(baroalt);					    				
				AltTxt.setText(String.format("Alt: %1.1f m",hPaToMeter(slp_inHg_,pressure))); 
			}								
		});	   
   } 
   private void init()
   {
	   loginLW=false;
	   type=0;	
	   LWcount=0;		  
	   new startSensBox(this).execute();	
	   loghandler.postDelayed(logrunnable, intlogtime);	 	   
   }
   @Override
   protected void onResume() {
    super.onResume();       	 
   	startProperties();   
	 if(!enablelog)
	  {			  
		  if(igcstart)
		  {
			  callIgcFunc("normal");
		  }	
	  }			
	 if(!livetrackenabled && loginLW)
	  {				  
		setLivePos emitPos = new setLivePos();
		emitPos.execute(3);								 
	  }		 
   }
  
   @Override
   public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
       //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);       
   }
   private void setScreen()
   {
	   avgprogbar_reverse.setMax(80);
	   avgprogbar.setMax(80);
	   SetProgressColor("#0000FF",avgprogbar);
	   SetProgressColor("#ff0000",avgprogbar_reverse);		
	   avgprogbar_reverse.setProgress(0);
	   avgprogbar.setProgress(0);	
	   Display display = getWindowManager().getDefaultDisplay();
	   Point size = new Point();
	   display.getSize(size);
	   int width = size.x;
	   int height = size.y;   
	   if(width>1300 || height>1300)
	   {
		   highress=true;
		   labelStyle = LabelStyle.builder()
			.setEdgePadding(15)
			.setLinePadding(15)
			.setTitleFont(Typeface.create("Arial", Typeface.BOLD), 30)
			.setDescriptionFont(Typeface.create("Arial", Typeface.BOLD), 28)
			.setBackgroundColor(Color.BLACK)
			.setTitleColor(Color.WHITE)
			.setDescriptionColor(Color.WHITE)
			.setBorderColor(Color.LTGRAY)
			.build();
		   markersize=0.9f;
	   }else
	   {
		   highress=false;
		   labelStyle = LabelStyle.builder()
			.setEdgePadding(15)
			.setLinePadding(15)
			.setTitleFont(Typeface.create("Arial", Typeface.BOLD), 15)
			.setDescriptionFont(Typeface.create("Arial", Typeface.BOLD), 14)
			.setBackgroundColor(Color.BLACK)
			.setTitleColor(Color.WHITE)
			.setDescriptionColor(Color.WHITE)
			.setBorderColor(Color.LTGRAY)
			.build();      
		   markersize=0.4f;
	   }       
   }
   public void startMap(){			
	// 1. Get the MapView from the Layout xml - mandatory
       mapView = (MapView) findViewById(R.id.mapView);
       mapView.setComponents(new Components());
       // add event listener
       mapListener = new MapEventListener(this, mapView);
       mapView.getOptions().setMapListener(mapListener);    
      
       getvalues();  
       setOnlineMap(mapPath,0,18,6); 
       mapView.setZoom(zoomlevel);
	   mapView.setTilt(tiltlevel);
    // preload map tiles outside current view. Makes map panning faster, with cost of extra bandwidth usage
       mapView.getOptions().setPreloading(true);
       // repeating world view if you are panning out of map to east or west. Usually important for most general zooms only.
       mapView.getOptions().setSeamlessHorizontalPan(true);
       // blending animation for tile replacement
       mapView.getOptions().setTileFading(true);
       // pan/scroll map dynamically
       mapView.getOptions().setKineticPanning(true);
       // enable doubleclick gesture for zoom in
       mapView.getOptions().setDoubleClickZoomIn(true);
       // enable two-finger touch gesture to zoom out
       mapView.getOptions().setDualClickZoomOut(true);
       // set sky bitmap - optional, default - white
       mapView.getOptions().setSkyDrawMode(Options.DRAW_BITMAP);
       mapView.getOptions().setSkyOffset(4.86f);
       // see sample image for good size ideas
       mapView.getOptions().setSkyBitmap(
               UnscaledBitmapLoader.decodeResource(getResources(),R.drawable.sky_small));
       // Map background, visible if no map tiles loaded - optional, default - white
       mapView.getOptions().setBackgroundPlaneDrawMode(Options.DRAW_BITMAP);
       mapView.getOptions().setBackgroundPlaneBitmap(
               UnscaledBitmapLoader.decodeResource(getResources(),
                       R.drawable.background_plane));
       mapView.getOptions().setClearColor(Color.WHITE);
      
       // configure texture caching. Following are usually good values
       mapView.getOptions().setTextureMemoryCacheSize(40 * 1024 * 1024);
       mapView.getOptions().setCompressedMemoryCacheSize(8 * 1024 * 1024);    
       // define online map persistent caching. 
       mapView.getOptions().setPersistentCachePath(
                this.getDatabasePath("mapcache").getPath());
       // set persistent raster cache limit to 100MB. Use any value you feel appropriate
       mapView.getOptions().setPersistentCacheSize(100 * 1024 * 1024);
       mapView.getOptions().setPersistentCachePath ("/sdcard/nmlcache.db");
       mapView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
       
       mapView.getOptions().setMapListener(mapListener); 
       mapListener = (MapEventListener) mapView.getOptions().getMapListener();	
       adjustMapDpi(); 
 	}
  
   @Override
   protected void onPause() {
       super.onPause();
       //unregisterReceiver(gattUpdateReceiver);
   }
   
   @Override
   protected void onDestroy() {
       super.onDestroy();        
       wl.release();
   }
  
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {	   
       getMenuInflater().inflate(R.menu.gatt_services, menu);       
       if(!init)
	   	{    
    	   menu.findItem(R.id.menu_connect).setTitle("Connect"); 
	   	}else
	   	{            	  
	   	   menu.findItem(R.id.menu_connect).setTitle("Disconnect");
	   	}
       this.menu = menu;
       return true;      
   }
  
   private void setOnlineMap(String map,int minzoom,int maxzoom,int id) {      	
   	TMSMapLayer onlinemapplayer = new TMSMapLayer(proj, minzoom, maxzoom, id,map, "/", ".png");
	   baselayer=onlinemapplayer;  
	   mapView.getLayers().setBaseLayer(baselayer); 
       adjustMapDpi();    
   }
   private void adjustMapDpi() {
       DisplayMetrics metrics = new DisplayMetrics();
       getWindowManager().getDefaultDisplay().getMetrics(metrics);
       float dpi = metrics.densityDpi;
       // following is equal to  -log2(dpi / DEFAULT_DPI)
       float adjustment = (float) - (Math.log(dpi / DisplayMetrics.DENSITY_HIGH) / Math.log(2));
       mapView.getOptions().setTileZoomLevelBias(adjustment / 2.0f);
   }
   public void getvalues()
   {
   	String sFileName="sensbox_settings.txt";
		try {
			File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");
			File settingsfile = new File(root, sFileName);
			if (settingsfile.exists()) 
			{				
	    		    BufferedReader br = new BufferedReader(new FileReader(settingsfile));
	    		    String line;
	    		    line = br.readLine();
	    		    String[] parts = line.split(";");
	    		    this.mapPath = parts[0]; 
	    		    this.zoomlevel = Float.parseFloat(parts[1]); 
	    		    this.tiltlevel = Float.parseFloat(parts[2]); 
	    		    this.slp_inHg_ = Double.parseDouble(parts[3]);	    		    
	    		    br.close();
	    	}else
	    	{
	    		this.mapPath ="http://a.tile.thunderforest.com/outdoors/"; 
	    		setOnlineMap(mapPath,0,18,6); 
	    		this.zoomlevel = 13; 
	    		this.tiltlevel = 0.0f; 
	    		this.slp_inHg_ = 29.92;
	    	}
		}catch(Exception e){}
   }
   public void savevalues()
   {
   	String sFileName="sensbox_settings.txt";
		try {
			File root = new File(Environment.getExternalStorageDirectory(),
					"VarioLog");
			if (!root.exists()) {
				root.mkdirs();
			}
			File settingsfile = new File(root, sFileName);
			FileWriter writer = new FileWriter(settingsfile);
			writer.write(mapPath +";");	
			writer.write(String.valueOf(mapView.getZoom())+";");	
			writer.write(String.valueOf(mapView.getTilt())+";");
			writer.write(String.valueOf(slp_inHg_)+";");	
			writer.flush();
			writer.close();	
		}catch(Exception e){}
   }
  
   @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
		 switch (keyCode) {
		    case KeyEvent.KEYCODE_BACK:
		    	Toast.makeText(getApplicationContext(),
						"Please use Menu - Exit Button!...", Toast.LENGTH_SHORT).show();
		        return true;
		    default:
		        return false;
		    }
		}
    void playsound(double vario){	
		if(beeps!=null)				
		beeps.setAvgVario(vario);			
	}
	private void Exit()
	{
		if(beeps!=null)
		{
			beeps.stop();
			beeps.onDestroy();
		}
		if(livetrackenabled && loginLW)
			{					
				setLivePos emitPos = new setLivePos();
				emitPos.execute(3);				
				}  
		if(loghandler!=null)  
		loghandler.removeCallbacks(logrunnable);
		  if (senseBoxAdapter != null) {
			  sensboxhandler.removeCallbacks(sensrunnable);	
			  senseBoxAdapter.onDestroy();				  
		  }
		savevalues();	
		 if(enablelog && igcstart)
				callIgcFunc("exit");
		 else if(livetrackenabled)				
				finish();
		 else
				android.os.Process.killProcess(android.os.Process.myPid()); 
	}
	 private Menu menu;
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {           
            case R.id.menu_exit:               
            	Exit();    	
            	return true;    
            case R.id.menu_settings:               
            	Intent i = new Intent(this, EditPreferences.class);
    			this.startActivity(i); 	
            	return true;  
            case R.id.menu_mapclear:               
            	clearMap();
            	return true;  
            case R.id.menu_maptopo:               
            	this.mapPath ="http://a.tile.thunderforest.com/outdoors/"; 
	    		setOnlineMap(mapPath,0,18,6); 
            	return true;  
            case R.id.menu_mapcity:   
	    		this.mapPath="http://a.tile.openstreetmap.org/";	
				setOnlineMap(mapPath,0,18,6); 
            	return true;  
            case R.id.menu_igc:               
            	startActivityForResult(new Intent(this,IgcLoad.class),IGCFILE);
            	return true;
            case R.id.menu_connect:               	
            	if(item.getTitle().equals("Disconnect"))
            	{ 
            	   if (senseBoxAdapter != null) {
          			  sensboxhandler.removeCallbacks(sensrunnable);	
          			  senseBoxAdapter.onDestroy();				  
          		   }
            	   init=false;
            	   senseBoxAdapter=null;
            	   mapupdate=false;      
            	   updateConnectionState("Disconnected" + " : " + deviceName +" "+ deviceAddress);	
            	   messageToast("SensBox Disconnected!");
	               item.setTitle("Connect");
            	}else
            	{
            	   new startSensBox(this).execute();	
            	   mapupdate=true;  
 	               item.setTitle("Disconnect");
            	}
            	return true;
            case R.id.menu_sensdata:             	
            	if(item.getTitle().equals("Sens Box Data"))
            	{   
            	   showdata=true;
            	   showData();            	   
	               item.setTitle("Show Map");
            	}else
            	{
            	   showdata=false;
            	   hideData();
 	               item.setTitle("Sens Box Data");
            	}            	
            	return true;
            
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
   
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);	
		 if (requestCode == REQUEST_ENABLE_BT) {
	           if (resultCode == Activity.RESULT_CANCELED) {
	               finish();
	           } else {
	               init();                
	           }
	       }
		switch (requestCode) {
		case IGCFILE:					
			if (resultCode == Activity.RESULT_OK) {					
				String string = data.getStringExtra("file");
				if (string.length() != 0)				
				{
					ReadIgc(string.trim());					
				}
			}
			break;						
		}		
	}
    protected double DDMMmmmToDecimalLat(String coord,String hems)
	{
		String coorddegree=coord.substring(0,2);
		String coordminute=coord.substring(2,7);
		coordminute=coordminute.substring(0,2)+"."+coordminute.substring(2,5);
		double latcoordminute= Double.parseDouble(coordminute);
		latcoordminute=latcoordminute/60;
		DecimalFormat df = new DecimalFormat("0.000000");			
		coord=df.format(latcoordminute).substring(2);
		double result=Double.parseDouble(coorddegree + "." + coord);
		if(hems.equals("S"))
			result=-1*result;
		return result;	
	}
	protected double DDMMmmmToDecimalLon(String coord,String hems)
	{
		String coorddegree=coord.substring(0,3);
		String coordminute=coord.substring(3,8);
		coordminute=coordminute.substring(0,2)+"."+coordminute.substring(2,5);
		double latcoordminute= Double.parseDouble(coordminute);
		latcoordminute=latcoordminute/60;
		DecimalFormat df = new DecimalFormat("0.000000");			
		coord=df.format(latcoordminute).substring(2);
		double result=Double.parseDouble(coorddegree + "." + coord);
		if(hems.equals("W"))
			result=-1*result;
		return result;
	}
    
    private void updateConnectionState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionState.setText(state);
            }
        });
    }     
    public static String ConvertDecimalToDegMinSec(double coord)
    {
    	String output, degrees, minutes, seconds;   
    	double mod = coord % 1;
    	int intPart = (int)coord;     
    	degrees = String.valueOf(intPart);
      	coord = mod * 60;
    	mod = coord % 1;
    	intPart = (int)coord;
            if (intPart < 0) {
               // Convert number to positive if it's negative.
               intPart *= -1;
            }     
    	minutes = String.format("%02d",intPart);     
    	coord = mod * 60;
    	intPart = (int)coord;
            if (intPart < 0) {
               // Convert number to positive if it's negative.
               intPart *= -1;
            }
    	seconds = String.format("%02d",intPart);     
    	//Standard output of D°M′S″
    	output = degrees + "° " + minutes + "' " + seconds + "\"";
    	return output;
    }   
    
    public String getHemisphereLat(double coord) {
		if (coord < 0) {
			return "S";
		} else {
			return "N";
		}
	}
	public String getHemisphereLon(double coord) {
		if (coord < 0) {
			return "W";
		} else {
			return "E";
		}
	}	
   
   
	private String Getinfo(MapPos point)
	{
		String info = String.format("Lat: %.6f", Getlat(point)).replace(",",".")
				+ String.format(" Lon: %.6f", Getlon(point)).replace(",", ".");
		return info;
	}
	
	private void distancetotakeoff(MapPos point) {    	
    	try{
		Location currentLocation = new Location("reverseGeocoded");
		currentLocation.setLatitude(startlatitude);
		currentLocation.setLongitude(startlongitude);

		Location targetLocation = new Location("reverseGeocoded");
		targetLocation.setLatitude(Getlat(point));
		targetLocation.setLongitude(Getlon(point));
		String str = null;
		double distance = (int) currentLocation.distanceTo(targetLocation);
		if (distance <= 999)
			str = String.valueOf(String.format("DTkf: %.0f", distance)) + " m";
		else
			str = String.valueOf(String.format("DTkf: %.1f", distance / 1000))
					+ " Km";
		DTkf.setText(str);
    	}catch(Exception e){Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_SHORT).show(); 
		}
	}
	static double Getlat(MapPos pos){
		return proj.toWgs84(pos.x, pos.y).y;
	}
	static double Getlon(MapPos pos){
		return proj.toWgs84(pos.x, pos.y).x;
	}
	private double distancetolocation(MapPos pointtarget,MapPos pointcurrent) {    	
    	try{
			Location currentLocation = new Location("reverseGeocoded");
			currentLocation.setLatitude(Getlat(pointcurrent));
			currentLocation.setLongitude(Getlon(pointcurrent));
	
			Location targetLocation = new Location("reverseGeocoded");
			targetLocation.setLatitude(Getlat(pointtarget));
			targetLocation.setLongitude(Getlon(pointtarget));		
			double distance = (int) currentLocation.distanceTo(targetLocation);
			return distance;
    	}catch(Exception e){ 
		}
		return 0;
	}
	 private Runnable logrunnable = new Runnable() {
		   @Override
		   public void run() {				  
			   if(enablelog && gpsfix!=0)
				{
				   setigcfile();	
				}
			   if(livetrackenabled && gpsfix!=0 && username!="" && password!="")
				{	
					if(!loginLW)
					{
						TrackTxt.setText("Live: trying");					
						setLivePos emitPos = new setLivePos();
						emitPos.execute(1);		
					}else
					{							
						setLivePos emitPos = new setLivePos();
						emitPos.execute(2);			            		
					}
				}
			    loghandler.postDelayed(this,intlogtime);			  
		   }
		};	
		private void setigcfile() {
			if (!logheader) 
			preparelogheader();
			// B
			Date date = new Date(senstime);
			String igcval = null;
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String igcgpstime = sdf.format(date);
			String igclat = decimalToDMSLat(senslat);
			String igclon = decimalToDMSLon(senslng);
			// A
			String igcaltpressure = String.format("%05d", (int) baroalt);
			String igcaltgps = String.format("%05d", (int) gpsalt);		
				igcval = "B" + igcgpstime.replace(":", "") + igclat + igclon + "A"	+ igcaltpressure + igcaltgps;
				igclog.add(igcval + "\r\n");				
				trackcount++;
				if(!livetrackenabled)
				TrackTxt.setText("Trck: " + String.valueOf(trackcount));		
					if (!igcstart) {
						Toast.makeText(getApplicationContext(),
								"IGC LOG START...", Toast.LENGTH_LONG).show();				
						igcstart=true;	
					}			
		}
		private void preparelogheader() {		
			Calendar c = Calendar.getInstance();
			SimpleDateFormat dfdetail = new SimpleDateFormat("ddMMyy");
			String formattedDate = dfdetail.format(c.getTime());
			String value = "AXSB SENSBOX" + "\r\n";
			value = value + "HFDTE" + formattedDate + "\r\n";
			value = value + "HOPLTPILOT:" + pilotname + "\r\n";
			value = value + "HOGTYGLIDERTYPE: " + wingmodel + "\r\n";			
			value = value + "HOCIDCOMPETITIONID:" + pilotid + "\r\n";
			value = value + "HODTM100GPSDATUM: WGS84" + "\r\n";				
			igclog.add(value);
			logheader = true;		
		}
		private void preparelogfooter() {
			if (!logfooter) {
				Calendar c = Calendar.getInstance();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = df.format(c.getTime());
				String value = "LXGD Turkay Biliyor ANDROID SENSBOX"
						+ "\r\n";
				value = value + ("LXGD Downloaded " + formattedDate);
				igclog.add(value);
				df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				formattedDate = df.format(c.getTime());
				logfilename = "FlightLog_" + formattedDate.replace(" ", "_");
				logfooter = true;
			}
		}			
		public String decimalToDMSLat(double coord) {
			try {
				String output, degrees, minutes, hemisphere;
				if (coord < 0) {
					coord = -1 * coord;
					hemisphere = "S";
				} else {
					hemisphere = "N";
				}
				double mod = coord % 1;
				int intPart = (int) coord;
				degrees = String.format("%02d", intPart);
				coord = mod * 60;
				DecimalFormat df = new DecimalFormat("00.000");
				minutes = df.format(coord).replace(".", "");
				minutes = minutes.replace(",", "");
				output = degrees + minutes + hemisphere;
				return output;
			} catch (Exception e) {
				return null;
			}
		}
		public String decimalToDMSLon(double coord) {
			try {
				String output, degrees, minutes, hemisphere;
				if (coord < 0) {
					coord = -1 * coord;
					hemisphere = "W";
				} else {
					hemisphere = "E";
				}
				double mod = coord % 1;
				int intPart = (int) coord;
				degrees = String.format("%03d", intPart);
				coord = mod * 60;
				DecimalFormat df = new DecimalFormat("00.000");
				minutes = df.format(coord).replace(".", "");
				minutes = minutes.replace(",", "");
				output = degrees + minutes + hemisphere;
				return output;
			} catch (Exception e) {
				return null;
			}
		}
	  
	   public void SetProgressColor(String color,ProgressBar bar){
			final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
			ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
			String MyColor =color;
			pgDrawable.getPaint().setColor(Color.parseColor(MyColor));
			ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
			bar.setProgressDrawable(progress);   
			bar.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));	
			bar.refreshDrawableState();
		}
	   public void hideData(){	
		   header.setVisibility(View.INVISIBLE);
	       footer.setVisibility(View.INVISIBLE);
	       sview.setVisibility(View.INVISIBLE);
	   }
	   public void showData(){	
		   header.setVisibility(View.VISIBLE);
	       footer.setVisibility(View.VISIBLE);
	       sview.setVisibility(View.VISIBLE);
	   }
	Timer timer;
	public void Reminder(int seconds) {
         timer = new Timer();
         timer.schedule(new RemindTask(), seconds*1000);
    }

	class RemindTask extends TimerTask {
     public void run() {
    	 touched=false;
         timer.cancel(); //Terminate the timer thread
     }
	}  
	
	GeometryLayer geomLayer_route = new GeometryLayer(proj);
	private void drawLine(ArrayList<MapPos> list,int color)	 {
		 if(list.size()>1)
		 {	
			 geomLayer_route.clear();
			 StyleSet<LineStyle> lineStyleColor= new StyleSet<LineStyle>();
			 lineStyleColor.setZoomStyle(0, LineStyle.builder().setWidth(linewidth).setColor(color).build());	
			 geomLayer_route.add(new Line(list, new DefaultLabel("Route Line"), lineStyleColor, null));	  
		     mapView.getLayers().removeLayer(geomLayer_route);
		     mapView.getLayers().addLayer(geomLayer_route);  
		 }	     
		} 	
	GeometryLayer geomLayer_track = new GeometryLayer(proj);
	private void drawTrack(ArrayList<MapPos> Points,int color)	 {
		 if(Points.size()>1)
		 {	
			 geomLayer_track.clear();
			 StyleSet<LineStyle> lineStyleColor= new StyleSet<LineStyle>();
			 lineStyleColor.setZoomStyle(0, LineStyle.builder().setWidth(linewidth).setColor(color).build());	
			 geomLayer_track.add(new Line(Points, new DefaultLabel("Track Line"), lineStyleColor, null));	  
		     mapView.getLayers().removeLayer(geomLayer_track);
		     mapView.getLayers().addLayer(geomLayer_track); 
		     MapPos start=Points.get(0);
			 MapPos end=Points.get(Points.size()-1);		 
			 double distance=getDistance(start,end);	
			 trckmarkerLayer.clear();	  
			 drawTrckMarker(start,"Start Point",Getinfo(start));
			 drawTrckMarker(end,"End Point","Distance:" + String.format("%.1f",distance/1000) + " Km");	
	         GotoMitPosition(start,end,distance);	
		 }	     
		} 
	 public void drawRouteMarker(MapPos markerLocation,Layer layer)
		{
		 try{
			Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.plane);
		    MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(markersize).setColor(Color.YELLOW).build();		    
		    routemarkerLayer.clear();
		    Marker marker=new Marker(markerLocation, null, markerStyle, null);
		    routemarkerLayer.add(marker);
		    mapView.getLayers().addLayer(routemarkerLayer);	
		 }catch(Exception e){}
		}	  
	 private void drawTrckMarker(MapPos markerLocation,String desc1,String desc2 )
	 {
	    Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.marker);
     MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(markersize).setColor(Color.WHITE).build();
     Label markerLabel = new DefaultLabel(desc1,desc2,labelStyle);   
     Marker marker=new Marker(markerLocation, markerLabel, markerStyle, null);           
 	   trckmarkerLayer.add(marker);
 	   mapView.getLayers().addLayer(trckmarkerLayer);
   
     mapView.setFocusPoint(markerLocation); 
     mapView.selectVectorElement(marker);
	 }
	 private static double hPaToMeter(double slp_inHg, double pressure_hPa) {
		   	// Algebraically unoptimized computations---let the compiler sort it out.
		   	double factor_m = SLT_K / TLAPSE_K_PER_M;
		   	double exponent = -TLAPSE_K_PER_M * R_J_PER_KG_PER_K / G_M_PER_S_PER_S;    	
		   	double current_sea_level_pressure_Pa = slp_inHg * PA_PER_INHG;
		   	double altitude_m =
		   			factor_m *
		   			(Math.pow(100.0 * pressure_hPa / current_sea_level_pressure_Pa, exponent) - 1.0);
		   	return altitude_m;
		   }
		   public static double altTohPa(double altitude) {
			     return Math.pow(((44330.8 - altitude) / 4946.54), 5.25588) / pressure_hPa_* 1013.25 /100.0;	   
			  }
	 public static double getDistance(MapPos StartP, MapPos EndP) {
		    double lat1 = Getlat(StartP);
		    double lat2 = Getlat(EndP);
		    double lon1 = Getlon(StartP);
		    double lon2 = Getlon(EndP);
		    double dLat = Math.toRadians(lat2-lat1);
		    double dLon = Math.toRadians(lon2-lon1);
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		    Math.sin(dLon/2) * Math.sin(dLon/2);
		    double c = 2 * Math.asin(Math.sqrt(a));
		    return 6366000 * c;
		}
	 protected float getbearing(MapPos start, MapPos end){
		 float bearing=0;
		 try{
		  double longitude1 = Getlon(start);
		  double longitude2 = Getlon(end);
		  double latitude1 = Math.toRadians(Getlat(start));
		  double latitude2 = Math.toRadians(Getlat(end));
		  double longDiff= Math.toRadians(longitude2-longitude1);
		  double y= Math.sin(longDiff)*Math.cos(latitude2);
		  double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);		 
		  bearing=(float) ((Math.toDegrees(Math.atan2(y, x))+360)%360);
		 }catch(Exception e){}
		 return bearing;
		}
	 void GotoMitPosition(MapPos StartP, MapPos EndP,double distance) {		
		 double lat1 = Getlat(StartP);
		 double lat2 = Getlat(EndP);
		 double lon1 = Getlon(StartP);
		 double lon2 = Getlon(EndP);
		 double finalLat,finalLng;		 
		 double dLon = Math.toRadians(lon2 - lon1);

	        lat1 = Math.toRadians(lat1);
	        lat2 = Math.toRadians(lat2);
	        lon1 = Math.toRadians(lon1);

	        double Bx = Math.cos(lat2) * Math.cos(dLon);
	        double By = Math.cos(lat2) * Math.sin(dLon);
	        finalLat= Math.toDegrees(Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By)));
	        finalLng= Math.toDegrees(lon1 + Math.atan2(By, Math.cos(lat1) + Bx));
		    mapView.setFocusPoint(baselayer.getProjection().fromWgs84(finalLng,finalLat));
		    if(highress)
		        mapView.setZoom(zoomLevelbyDist(distance)*4/3); 
		    else
		    	mapView.setZoom(zoomLevelbyDist(distance)); 		   
		    float bearingtrck=getbearing(StartP,EndP);
			mapView.setRotation(-1*bearingtrck);
	}
	 private float zoomLevelbyDist(double distance) {
		 double equatorLength = 40075004; // in meters
		 WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		           DisplayMetrics metrics = new DisplayMetrics();
		           wm.getDefaultDisplay().getMetrics(metrics);
		            double heightInPixels = metrics.heightPixels / metrics.density;
		             double metersPerPixel = equatorLength / 256 / metrics.density;
		             // 68 is percent
		             double diameter = (distance / 55 * 100) * 2;
		             int zoomLevel = 1;
		             while ((metersPerPixel * heightInPixels) > diameter) {
		                 metersPerPixel /= 2;
		                 ++zoomLevel;
		            }			             
		       return zoomLevel;  
		}	
	
	public static void zoom_map(float value){	
		mapView.setZoom(value);
	}
	private void clearMap()
	{
    	geomLayer_route.clear();
    	geomLayer_track.clear();
    	mapView.getLayers().removeLayer(geomLayer_route);   
    	mapView.getLayers().removeLayer(geomLayer_track);  
    	routemarkerLayer.clear();		
		trckmarkerLayer.clear();
		routePoints.clear();
		trackPoints.clear();
		mapView.clearFocus();
		mapView.clearAnimation();			
	}
	public void ReadIgc(String path) {		
		new DownloadFileAsync(this).execute(path);			
	}	
	 private void callIgcFunc(String type)
		{
			new CreateIgc(this).execute(type);	
		}
	    public class CreateIgc extends AsyncTask<String, String, String> {
			public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
			private ProgressDialog mProgressDialog;	
			private String cmdtype=null;
			public CreateIgc(Context context) 
			{			
			     mProgressDialog = new ProgressDialog(context);
			     mProgressDialog.setMessage("Creating Igc File..Total tracks: "+String.valueOf(igclog.size()));
			     mProgressDialog.setIndeterminate(false);
			     mProgressDialog.setMax(igclog.size());
			     mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			     mProgressDialog.setCancelable(true);
			}
			@Override
			protected void onPreExecute() {
			    super.onPreExecute();			   
			    mProgressDialog.show();
			}
			@Override
			protected String doInBackground(String... aurl) {
					int i=0;
					cmdtype=aurl[0];
					try {
						preparelogfooter();						
						String sFileName=logfilename + ".igc";
						try {
							File root = new File(Environment.getExternalStorageDirectory(),
									"VarioLog");
							if (!root.exists()) {
								root.mkdirs();
							}
							File igcfile = new File(root, sFileName);
							FileWriter writer = new FileWriter(igcfile);
							for (String str : igclog) {
								writer.write(str);
								i++;
								mProgressDialog.setProgress(i);
							}
							writer.flush();
							writer.close();
							igclog.clear();
							Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
							Uri uri = Uri.fromFile(igcfile);
							intent.setData(uri);
							sendBroadcast(intent);							
						} catch (IOException e) {
						}
					} catch (Exception e) {						
					}				
			    return null;
			}	
			protected void onProgressUpdate(String... progress) {
			    mProgressDialog.setProgress(Integer.parseInt(progress[0]));
			}		
			@Override
			protected void onPostExecute(String unused) {			
				mProgressDialog.dismiss();			
				if(cmdtype.equals("exit"))
				{
					igclog.clear();
					igcstart=false;
					trackcount=0;
					if(livetrackenabled)				
						finish();
					else
						android.os.Process.killProcess(android.os.Process.myPid()); 	
				}else
				{
				  igclog.clear();
				  igcstart=false;
				  trackcount=0;
				  TrackTxt.setText("Trck: "+trackcount);				  
				}					
			}			
		}	   
	private String root = Environment.getExternalStorageDirectory().getPath();
	 public class DownloadFileAsync extends AsyncTask<String, String, String> {
			private ProgressDialog mProgressDialog;				
			MapPos pos;
			public DownloadFileAsync(Context context) 
			{				 
			     mProgressDialog = new ProgressDialog(context);
			     mProgressDialog.setMessage("Reading Tracks..Please Wait");
			     mProgressDialog.setIndeterminate(false);
			     mProgressDialog.setMax(0);
			     mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			     mProgressDialog.setCancelable(true);		
			     trackPoints.clear();		
			}
			@Override
			protected void onPreExecute() {
			    super.onPreExecute();			   
			    mProgressDialog.show();
			}
			@Override
			protected String doInBackground(String... aurl) {			
				String path=aurl[0];
				int i=0,count=0;				
				String lat=null,lathems=null,lon=null,lonhems=null;				
				File file = new File(root,path);
				if (file.exists()) 
				{
					try {
		    		    BufferedReader br = new BufferedReader(new FileReader(file));
		    		    String line;
		    		    while ((line = br.readLine()) != null) {
		    		    	if(line.startsWith("B"))	    		    		
		    		    	{		    		    		 
		    		    		count++;
		    		    	}
		    		    }
		    		}
		    		catch (IOException e) {	    		   
		    		}	
					mProgressDialog.setMax(count);						
					file = new File(root,path);		
					try {
			    		   BufferedReader br = new BufferedReader(new FileReader(file));
			    		    String line;			    		 
			    		    while ((line = br.readLine()) != null) {
			    		    try{
			    		    	if(line.startsWith("B"))
			    		    	{			    		    		 
			    		    		lat=line.substring(7, 14);
			    		    		lathems=line.substring(14, 15);
			    		    		lon=line.substring(15, 24);
			    		    		lonhems=line.substring(24, 25);	
			    		    		double dlat=DDMMmmmToDecimalLat(lat,lathems);
			    		    		double dlon=DDMMmmmToDecimalLon(lon,lonhems);
			    		    		pos=new MapPos(
			    		    				(float)proj.fromWgs84(dlon,dlat).x,
			    		    				(float)proj.fromWgs84(dlon,dlat).y,0);
			    		    		trackPoints.add(pos);
			    		    		i++;
	    		    			    mProgressDialog.setProgress(i);	
			    		    	}		    		    	
			    		    }catch(Exception e){			    		    	
			    		    }	
			    		   }
			    		   br.close();	    		   
			    		}
			    		catch (IOException e) {			    			
			    		}						
				}
			    return null;
			}				
			protected void onProgressUpdate(String... progress) {
			    mProgressDialog.setProgress(Integer.parseInt(progress[0]));
			}		
			@Override
			protected void onPostExecute(String unused) {	
				drawTrack(trackPoints,Color.BLUE);
				mProgressDialog.dismiss();					
			}		
		}
	
public void startProperties() {		
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());	
		basecontext=getBaseContext();
		//serverUrl="http://test.livetrack24.com/";
		serverUrl="http://www.livetrack24.com/";
		String sinkLevelStr = preferences.getString("sinkLevel", "2.5");
		sinkalarm = Double.parseDouble(sinkLevelStr);
		String strlogtime=preferences.getString("logtime", "3000");		
		intlogtime=Integer.parseInt(strlogtime);
		username= preferences.getString("liveusername", "").trim();			
		password = preferences.getString("livepassword", "").trim();
		String strwechiletype=preferences.getString("vehicletype", "1");		
		vechiletype=Integer.parseInt(strwechiletype);	
		pilotname = preferences.getString("pilotname", "n/a");
		wingmodel = preferences.getString("wingmodel", "n/a");		
		pilotid = preferences.getString("wingid", "n/a");			
		drawgpstrack = preferences.getBoolean("checkboxgpstrack", false);				
		enablelog = preferences.getBoolean("checkboxigcrecord", false);			
		livetrackenabled = preferences.getBoolean("livetrackenabled", false);	
		String soundfreqstr=preferences.getString("soundfreq", "2"); 	        
	    soundtype=Integer.parseInt(soundfreqstr); 
	    if(beeps==null)
		   {       
			beeps = new BeepThread(SensBoxActivity.this);				
		   } 
	    beeps.start(getApplicationContext(), soundtype, sinkalarm);
	}
private class setLivePos extends AsyncTask<Object, Void, Boolean>{			
	 @Override
	    protected void onPreExecute() {
	        super.onPreExecute();	               	        
	        errorinfo="";
	        error=false;
	 }
	@Override
	protected Boolean doInBackground(Object... params)  {					
		try {				 
			 type = (Integer) params[0];	
			 if(!loginLW && type==1)
			 {
            liveWriter = new LeonardoLiveWriter(
           		  basecontext,
           		  serverUrl,
           		  username,
           		  password,
           		  wingmodel,
           		  vechiletype,
           		  intlogtime/1000);	
            	 liveWriter.emitProlog();
			 }else if(loginLW && type==2)
			 {					 
				 liveWriter.emitPosition(dv, senslat, senslng, (float)gpsalt, (int)heading, (float)speed);	
			 }else if(loginLW && type==3)
			 {	
				 liveWriter.emitEpilog();  	
				 loginLW=false;					
			 }				 
            return true;
       }	      
       catch (Exception e) {	            
       	errorinfo="Live Connection Error";		
       }
		return false;
	}			
	@Override
	protected void onPostExecute(Boolean result) {			
	        super.onPostExecute(result);
	        if(result)
	        {	error=false;		        	
	        	if(type==1)
		        {
		        	loginLW=true;				        		        	
		        }	
	        	else if(type==3)
	        	{
	        		loginLW=false;
					type=0;	
					LWcount=0;
					TrackTxt.setText("Trck: 0");
	        	}else{		        		
	        	LWcount=liveWriter.getLWCount();	
	        	if(livetrackenabled)
					TrackTxt.setText("Live: " + String.valueOf(LWcount));
	        	}
	        	
	        }else
	        {
	        	error=true;	
	        	TrackTxt.setText("Live: trying");
	        }	      
	    }
}	
public boolean isInit() {
	return init;
}
private Runnable sensrunnable = new Runnable() {
	   @Override
	   public void run() {	
	   if(isInit())
		{
		   senseBoxAdapter.readAllNewValues();			   
           dbvario=senseBoxAdapter.getVario();             
           senstime=senseBoxAdapter.getGpsTime();           
           senslat = senseBoxAdapter.getLat();    
           senslng = senseBoxAdapter.getLng();   	
           gpsalt = senseBoxAdapter.getGpsAlt();   	
           baroalt = senseBoxAdapter.getPressureAttitude();           
           speed = senseBoxAdapter.getGroundSpeed(); 
           heading = senseBoxAdapter.getHeading();
           dtemp= senseBoxAdapter.getTemp();  
           numberofsats=senseBoxAdapter.getSats();
           loglevel=senseBoxAdapter.getLogLevel();
           battlevel=senseBoxAdapter.getBatteryLevel();     
           battstatus=senseBoxAdapter.getBatteryStatus(); 
           sdfix=senseBoxAdapter.getSdFix();
           gpsfix=senseBoxAdapter.getGpsFix();   
           logstart=senseBoxAdapter.getLogStatus();
		   if(showdata)		
		   {	deviceName = senseBoxAdapter.getDeviceName();
			    deviceAddress = senseBoxAdapter.getDeviceAddress();  
				updateConnectionState("Connected" + " : " + deviceName +" "+ deviceAddress);			   
		   }
		   updateSensData(); 
		}
	   sensboxhandler.postDelayed(this,interval);
	   }
	};	
	
	private void updateSensData() {
	        runOnUiThread(new Runnable() {
	            @Override
	            public void run() {	            	
	            	try{
	            		playsound(dbvario);
	            	}catch(Exception e){Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();}
	            	currentpoint=proj.fromWgs84(senslng,senslat);  	            	
	            	if (!getfirstfix && senslat!=0 ) {   				   
	    				startlatitude = senslat;
	    				startlongitude = senslng;					
	    				getfirstfix = true;							
	    			}  
	            	dv = Long.valueOf(senstime);
                    df = new java.util.Date(dv);
	                if(showdata)
	    		    {
	                    sgpstime = new SimpleDateFormat("dd-MM-yyyy' / 'HH:mm:ss").format(df);
	    	            txtvario.setText(String.format("%1.1f m/s",dbvario)); 
	    	            txtdatetime.setText("Time: " + String.valueOf(sgpstime));
	    	            txtlatitude.setText("Lat: " + ConvertDecimalToDegMinSec(senslat) + " " + getHemisphereLat(senslat));
	    	            txtlongitude.setText("Lng: " + ConvertDecimalToDegMinSec(senslng) + " " +  getHemisphereLon(senslng));	 
	    	            txtalt.setText("BaroAlt: " +String.format("%1.1f m",baroalt) +" / GpsAlt: " + String.valueOf(gpsalt)+" m");
	    	            txtspeed.setText("Speed: " + String.format("%1.1f km/h",speed));
	    	            txtheading.setText("Heading: " + String.format("%1.0f°",heading)); 	  
	    	            txttemp.setText("Temp: " + String.format("%1.1f°C", dtemp)); 
	    	            String logstatus;
	    	            if(logstart==0)
	    	            	logstatus="Stop";
	    	            else
	    	            	logstatus="Logging";
	    	         	txtsats.setText("Sats: " + String.valueOf(numberofsats) + " / GpsFix: " + getGpsFix(gpsfix) 
	    	         			+ " / Log: "+logstatus + " " +loglevel  + " %");  
	    	         	
	    	         	if(battstatus==1)
	    	         		txtstatus.setText("Sdcard: " + getSdCard(sdfix) + " / " + "Battery: Charging"); 
	    	         	else
	    	         		txtstatus.setText("Sdcard: " + getSdCard(sdfix) + " / " + "Battery: " + battlevel + " %"); 
	    	         	
	    		    }
	                else
	    		    {
	    		    	if(dbvario>=0)
	    			    {			    	
	    			    	AvgVarTxt.setTextColor(Color.BLUE);
	    			    	avgprogbar.setProgress((int) (dbvario*10));
	    			    	avgprogbar_reverse.setProgress(0);
	    			    }
	    			    else if(dbvario<0)
	    			    {			    	
	    			    	AvgVarTxt.setTextColor(Color.RED);
	    			    	avgprogbar_reverse.setProgress((int) (-1*dbvario*10));
	    			    	avgprogbar.setProgress(0);
	    			    }	
	    		    	AvgVarTxt.setText(String.format("%.1f m/s",dbvario));
	    		    	if(!touched && mapupdate)
	    		    	{
	    		    		 mapView.setFocusPoint(currentpoint);				         
	    					 mapView.setRotation(-1* (int) heading); 
	    		    	}
	    	            drawRouteMarker(currentpoint,baselayer);
	    	            if(getfirstfix)
	    		            distancetotakeoff(currentpoint);		   
	    				if(drawgpstrack && gpsfix!=0 && senslat!=0 )
	    				{
	    					routePoints.add(currentpoint);
	    					drawLine(routePoints,Color.RED);
	    				}
	    				pressure=altTohPa(baroalt);
	    				String gpstime = new SimpleDateFormat("HH:mm:ss").format(df);	    				
	    				AltTxt.setText(String.format("Alt: %1.1f m",hPaToMeter(slp_inHg_,pressure))); 
	    				SpeedTxt.setText(String.format("Spd: %1.0f m",speed)); 		    		     		   
	    				TimeTxt.setText(gpstime);
	    				TempTxt.setText(String.format("%1.1f°C", dtemp));
	    		    }
	            }
	        });	        
	    }  
	private void messageToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {	            	
            	Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });	        
    }  
 private String getGpsFix(int gpsfix)
    {
         String gpsString;
         switch (gpsfix) {
             case 0:  gpsString = "no fix";
                      break;
             case 1:  gpsString = "2D fix";
                      break;
             case 2:  gpsString = "3D fix";
                      break;
             case 3:  gpsString = "2D + DPGS";
                      break;
             case 4:  gpsString = "3D + DPGS";
                      break;             
             default: gpsString = "no fix";
                      break;
         }
		return gpsString;    	
    }
 private String getSdCard(int status)
    {
         String sdString;
         switch (status) {
             case 0:  sdString = "Sd Off";
                      break;
             case 1:  sdString = "Sd On";
                      break;            
             default: sdString = "Sd Off";
                      break;
         }
		return sdString;    	
    }
 private class startSensBox extends AsyncTask<Object, Void, Boolean>{	
	 private ProgressDialog mProgressDialog;	
	 public startSensBox(Context context) 
		{				 
		     mProgressDialog = new ProgressDialog(context);
		     mProgressDialog.setMessage("Connecting to SensBox..Please Wait");		     		    	
		}
	 @Override
	    protected void onPreExecute() {
	        super.onPreExecute();	               	        
	        mProgressDialog.show();
	 }
	@Override
	protected Boolean doInBackground(Object... params)  {					
		    if(senseBoxAdapter==null)
			senseBoxAdapter = new SenseBoxAdapter();		
			init = senseBoxAdapter.connect();	
		return init;
	}			
	@Override
	protected void onPostExecute(Boolean result) {			
	        super.onPostExecute(result);
	        mProgressDialog.dismiss();
	        MenuItem connetItem = menu.findItem(R.id.menu_connect);	       
	        if(result)
	    	{
	    		senseBoxAdapter.readAllNewValues();
	    		deviceName = senseBoxAdapter.getDeviceName();
	    	    deviceAddress = senseBoxAdapter.getDeviceAddress();  
	    	    getActionBar().setTitle("Flytec " + deviceName);
	    		sensboxhandler.postDelayed(sensrunnable,interval);	
	    		 Toast.makeText(getApplicationContext(), "SensBox Connected", Toast.LENGTH_LONG).show();
	    		 connetItem.setTitle("Disconnect");
	    	} else
	    	{		 
	    		 Toast.makeText(getApplicationContext(), "SensBox Not Found!", Toast.LENGTH_LONG).show();
	    		 connetItem.setTitle("Connect");
	    	}
	    }
 }	 
 
}
