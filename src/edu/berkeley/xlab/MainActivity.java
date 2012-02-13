package edu.berkeley.xlab;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.berkeley.xlab.BackgroundService;
import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.*;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends ListActivity {

	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab-MAIN";
	
	/** instance of (single-task) class for inner class*/
	private final ListActivity LIST_ACTIVITY = MainActivity.this;
	
	/** application context for Shared Preferences */
	private Context context;

	/** Application state to access "global" variables and methods */
	private App appState;
	
	/** boolean for if GPS is currently operating */
	private boolean backgroundRunning;
	
	/** xLabExps are the "global" ConcurrentHashMap of Experiments */
	private ConcurrentHashMap<Integer, Experiment> xLabExps;

	@Override
	public void onCreate(Bundle bundle) {

		super.onCreate(bundle);
		Log.d(TAG, "In MainActivity -- OnCreate");
		context = getApplicationContext();
		appState = (App) context;
		new RefreshExperiments(context, LIST_ACTIVITY, true).execute();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		xLabExps = appState.getXLabExps();
		populate();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		backgroundRunning = false;
		getMenuInflater().inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	public void onBackPressed()
	{
		moveTaskToBack(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.download:
				new RefreshExperiments(getApplicationContext(), MainActivity.this, true).execute();
				return true;
			case R.id.gpsToggle:
				ComponentName comp = new ComponentName(context.getPackageName(),BackgroundService.class.getName());
				Log.d(TAG, "In GPS Toggle");				
				if (backgroundRunning) {
					Log.d(TAG, "backgroundRunning was true");					
					backgroundRunning = false;
					context.stopService(new Intent().setComponent(comp));
					item.setTitle(getString(R.string.menu_turnon));
					return true;				
				} else {
					Log.d(TAG, "backgroundRunning was false");					
					backgroundRunning = true;
					context.startService(new Intent().setComponent(comp));
					item.setTitle(getString(R.string.menu_turnoff));
					return true;
				}
			case R.id.debugBL:
				doXLabChecks(37.87557,-122.260108,10);
				return true;
			case R.id.debugTQ:
				doXLabChecks(37.87213, -122.25787,10);
				return true;
			default:
				return super.onOptionsItemSelected(item);
	    }
	}
	

	/**
	* populate
	*
	* @author Daniel Vizzini
	* 
	* Populates ListAcitvity with Experiments
	* Big up http://www.tutomobile.fr/personnaliser-une-listview-tutoriel-android-n%C2%B07/04/07/2010/ 
	*/
	public void populate() {
		
		ArrayList<ConcurrentHashMap<String, String>> listItem = new ArrayList<ConcurrentHashMap<String, String>>();			 
	    ConcurrentHashMap<String, String> map;
		final int[] expIds = new int[xLabExps.size() + 1];
		int i = 0;
	    
	    for (Experiment exp : xLabExps.values()) {
		    map = new ConcurrentHashMap<String, String>();
		    map.put("title", exp.getTitle());
		    map.put("location", exp.getLocation());
		    if (exp.getActivity().getName().equals(ExpActivityTextQuestion.class.getName())) {
		    	map.put("img", String.valueOf(R.drawable.ic_tq));
		    } else if (exp.getActivity().getName().equals(ExpActivityBudgetLine.class.getName())) {
		    	map.put("img", String.valueOf(R.drawable.ic_bl));
		    }
		    listItem.add(map);		    	
			expIds[i] = exp.getExpId();
			i++;
	    }
	    
	    map = new ConcurrentHashMap<String, String>();
	    map.put("title", "Refresh Experiments");
	    map.put("location", "");
	    map.put("img", String.valueOf(R.drawable.ic_menu_refresh));
	    listItem.add(map);
	    expIds[i] = 0;

	    LIST_ACTIVITY.setListAdapter(new SimpleAdapter (LIST_ACTIVITY, listItem, R.layout.main, new String[] {"img", "title", "location"}, new int[] {R.id.img, R.id.title, R.id.location}));
	    
	    LIST_ACTIVITY.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if (expIds[position] != 0) {
					
					Intent intent = new Intent(context, xLabExps.get(expIds[position]).getActivity());
					intent.putExtra("expId", expIds[position]);
					
					LIST_ACTIVITY.startActivity(intent);				
					
				} else {
					new RefreshExperiments(context, LIST_ACTIVITY, true).execute();
				}
				
		    }
		});		
	}
	
	//TODO: After working, copy following classes into BackgroundService and delete
	//DV: For Debugging. Will only be in BackgroundService in Production Version
	//Notification ID
	private static final long MIN_TIME_BETWEEN_ALERTS = 0;//10 * 60 * 1000;//TODO: Change to something reasonable
	private ConcurrentHashMap<Integer, Long> lastTime = new ConcurrentHashMap<Integer, Long>();

	private void doXLabChecks(double lat, double lon, float accuracy) {

		Log.d(TAG,"In doXLabChecks");
		for(Map.Entry<Integer, Experiment> entry : appState.getXLabExps().entrySet()) {
			Experiment exp = entry.getValue();

			Log.d(TAG,"Examining " + exp.getTitle() + ": isAnswered = " + exp.isDone() + ": radius = " + exp.getRadius() + ", accuracy = " + accuracy + ", distance = " + Utils.getDistanceFromLatLon(exp.getLat(), exp.getLon(), lat, lon));
			//Consider this fix only if the accuracy is at least 100% of the radius specified in the geofence
			if(! exp.isDone() &&  (exp.getRadius() * 1.0f) >= accuracy && 
					Utils.getDistanceFromLatLon(exp.getLat(), exp.getLon(), lat, lon) <= exp.getRadius()) {
				Log.d(TAG,"Starting thread for " + exp.getTitle());
				//Show a notification
				Thread t = new Thread( new UploadXLabTask(exp,false) );
				t.start();
			}
			
		}

	}
	
	/**
	 * Upload XLab task status to the server 
	 * 
	 * @author Daniel Vizzini
	 */
	private class UploadXLabTask implements Runnable {
		private NotificationManager notificationManager  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		private Experiment exp;
		private boolean timed;

		public UploadXLabTask(Experiment exp, boolean timed) {
			super();
			Log.d(TAG,"In UploadXlabBL constructor without default timed");
			this.exp = exp;
			this.timed = timed;
		}

		@Override
		public void run() {
			
			try{
				lastTime.putIfAbsent(exp.getExpId(), (long)0);
				Log.d(TAG,"a" + new Long(System.currentTimeMillis()).toString());
				Log.d(TAG,"b" + new Long(lastTime.get(exp.getExpId())).toString());
				Log.d(TAG,"c" + new Long(MIN_TIME_BETWEEN_ALERTS).toString());
				if (!timed || (System.currentTimeMillis() - lastTime.get(exp.getExpId()) > MIN_TIME_BETWEEN_ALERTS)) {
					
					lastTime.put(exp.getExpId(), System.currentTimeMillis());
					int icon = R.drawable.ic_stat_x_notification;
					CharSequence tickerText = "X-Lab Alert";
					long when = System.currentTimeMillis();
					
					Log.d(TAG,"Running UploadXlab");
					Notification notification = new Notification(icon, tickerText, when);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					notification.defaults |= Notification.DEFAULT_SOUND;
					notification.defaults |= Notification.DEFAULT_VIBRATE;
					notification.defaults |= Notification.DEFAULT_LIGHTS;
					
					CharSequence contentTitle = "X-Lab Alert";
					CharSequence contentText = this.exp.getTitle();
					Intent notificationIntent = new Intent(context, exp.getActivity());
					notificationIntent.putExtra("expId", this.exp.getExpId());
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					Log.d(TAG,"UploadXlab notify for " + this.exp.getTitle());
					notificationManager.cancel(exp.getExpId());
					notificationManager.notify(exp.getExpId(), notification);

				}
			}
			catch(Exception e){
				Log.e(TAG,e.toString());
			}
		}
	}	
}