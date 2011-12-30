package edu.berkeley.xlab;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.berkeley.xlab.BackgroundService;
import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.experiments.*;
import edu.berkeley.xlab.util.Utils;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends ListActivity {

	public static final String TAG = "XLab-MAIN";
	private boolean backgroundRunning = false;
	private boolean uploaded = false;
	private MenuInflater inflater = getMenuInflater();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "In MainActivity -- OnCreate");
		super.onCreate(savedInstanceState);
		if (!uploaded) {
			new FetchXLabTask().execute();
		} else {
			populateListView();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG,"In onSaveInstanceState");
		savedInstanceState.putBoolean("uploaded", uploaded);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG,"In onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		uploaded = savedInstanceState.getBoolean("uploaded");
	}
	
	/**
	 * Fetch XLab tasks from the server
	 * 
	 * @author Daniel Vizzini
	 */
	private class FetchXLabTask extends AsyncTask<Void, Void, Void> {

		private App appState = ((App) getApplicationContext());
		private ConcurrentHashMap<Integer, Experiment> xLabExps = appState.getXLabExps();

		private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

		@Override
		protected void onPreExecute() {

			Log.d(TAG, "In MainActivity -- FetchXLabTask -- onPreExecute");

			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage("XLab is loading...");
			dialog.show();
		}

		protected Void doInBackground(Void... voids) {

			Log.d(TAG,"In MainActivity -- FetchXLabTask -- doInBackground");

			try {
				String responseBQ = Utils
						.getData(Configuration.XLAB_API_ENDPOINT_BQ);
				Log.d(TAG, responseBQ);

				if (null != responseBQ) {

					// Parse the response
					String[] geofencesBQ = responseBQ.split("\n");

					for (String line : geofencesBQ) {

						XLabBinaryQuestion bq = new XLabBinaryQuestion(line);
						int id = bq.getId();

						// TODO: The xLabBinaryQuestions map will keep growing
						// if we do this.
						// Clear old values.
						xLabExps.putIfAbsent(id, bq);
					}
				}

				String responseBL = Utils.getData(Configuration.XLAB_API_ENDPOINT_BL);
				Log.d(TAG, responseBL);

				if (null != responseBL) {

					// Parse the response
					String[] geofencesBL = responseBL.split("\n");

					for (String exp : geofencesBL) {

						XLabBudgetLineExp bl = new XLabBudgetLineExp(exp);
						Log.d(TAG, bl.getTitle());

						xLabExps.putIfAbsent(bl.getId(), bl);

					}
					// TODO: The xLabBinaryQuestions map will keep growing if we
					// do this.
					// Clear old values.

					appState.setXLabExps(xLabExps);
					Log.d(TAG, "Downloaded "
							+ geofencesBL.length + " geofences.");
				}

			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}

			return null;
			
		}

		@Override
		protected void onPostExecute(Void voids) {

			Log.d(TAG,"In MainActivity -- FetchXLabTask -- onPostActivity");
			dialog.dismiss();
			populateListView(); 
			uploaded = true;
			
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		backgroundRunning = true;
		inflater.inflate(R.menu.menu, menu);
	    return true;
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.download:
				new FetchXLabTask().execute();
				return true;
			case R.id.gpsToggle:
				ComponentName comp = new ComponentName(this.getApplicationContext().getPackageName(),BackgroundService.class.getName());
				Log.d(TAG, "In GPS Toggle");				
				if (backgroundRunning) {
					Log.d(TAG, "backgroundRunning was true");					
					backgroundRunning = false;
					this.getApplicationContext().stopService(new Intent().setComponent(comp));
					item.setTitle(getString(R.string.menu_turnon));
					return true;				
				} else {
					Log.d(TAG, "backgroundRunning was false");					
					backgroundRunning = true;
					this.getApplicationContext().startService(new Intent().setComponent(comp));
					item.setTitle(getString(R.string.menu_turnoff));
					return true;
				}
			case R.id.debugBL:
				doXLabChecks(37.87213, -122.25787,10);
				return true;
			case R.id.debugBQ:
				doXLabChecks(37.87557,-122.260108,10);
				return true;
			default:
				return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * Populate ListView
	 *
	 * @author Daniel Vizzini
	 * 
	 * Big up http://www.tutomobile.fr/personnaliser-une-listview-tutoriel-android-n%C2%B07/04/07/2010/ 
	 */
	private void populateListView() {
		
		App appState = ((App) getApplicationContext());
		final ConcurrentHashMap<Integer, Experiment> xLabExps = appState.getXLabExps();
		ArrayList<ConcurrentHashMap<String, String>> listItem = new ArrayList<ConcurrentHashMap<String, String>>();			 
	    ConcurrentHashMap<String, String> map;
		final int[] ids = new int[xLabExps.size()];
		int i = 0;
	    
	    for (Experiment exp : xLabExps.values()) {
		    map = new ConcurrentHashMap<String, String>();
		    map.put("title", exp.getTitle());
		    map.put("location", exp.getLocation());
		    Log.d(TAG,exp.getActivity().getName());
		    Log.d(TAG,BinaryQuestionActivity.class.getName());
		    Log.d(TAG,String.valueOf(exp.getActivity().getName().equals(BinaryQuestionActivity.class.getName())));
		    Log.d(TAG,BudgetLineActivity.class.getName());
		    Log.d(TAG,String.valueOf(exp.getActivity().getName().equals(BudgetLineActivity.class.getName())));
		    if (exp.getActivity().getName().equals(BinaryQuestionActivity.class.getName())) {
		    	Log.d(TAG,"ic_bq");
		    	map.put("img", String.valueOf(R.drawable.ic_bq));
		    } else if (exp.getActivity().getName().equals(BudgetLineActivity.class.getName())) {
		    	Log.d(TAG,"ic_bl");
		    	map.put("img", String.valueOf(R.drawable.ic_bl));
		    }
		    listItem.add(map);		    	
			ids[i] = exp.getId();
			i++;
	    }

	    setListAdapter(new SimpleAdapter (this.getBaseContext(), listItem, R.layout.main, new String[] {"img", "title", "location"}, new int[] {R.id.img, R.id.title, R.id.location}));
	    
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				Intent intent = new Intent(getApplicationContext(), xLabExps.get(ids[position]).getActivity());
				intent.putExtra("id", ids[position]);
				
				startActivity(intent);
				
		    }
		});		
	}

	//TODO: After working, copy following classes into BackgroundService and delete
	//DV: For Debugging. Will only be in BackgroundService in Production Version
	//Notification ID
	private static final long MIN_TIME_BETWEEN_ALERTS = 0;//15 * 60 * 1000;//TODO: Change to something reasonable
	private ConcurrentHashMap<Integer, Long> lastTime = new ConcurrentHashMap<Integer, Long>();

	private void doXLabChecks(double lat, double lon, float accuracy) {

		App appState = ((App)getApplicationContext());
		Log.d(TAG,"In doXLabChecks");
		for(Map.Entry<Integer, Experiment> entry : appState.getXLabExps().entrySet()) {
			Experiment exp = entry.getValue();

			Log.d(TAG,"Examining " + exp.getTitle() + ": isAnswered = " + exp.isDone() + ": radius = " + exp.getRadius() + ", accuracy = " + accuracy + ", distance = " + Utils.GetDistanceFromLatLon(exp.getLat(), exp.getLon(), lat, lon));
			//Consider this fix only if the accuracy is at least 100% of the radius specified in the geofence
			if(! exp.isDone() &&  (exp.getRadius() * 1.0f) >= accuracy && 
					Utils.GetDistanceFromLatLon(exp.getLat(), exp.getLon(), lat, lon) <= exp.getRadius()) {
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
				lastTime.putIfAbsent(exp.getId(), (long)0);
				Log.d(TAG,"a" + new Long(System.currentTimeMillis()).toString());
				Log.d(TAG,"b" + new Long(lastTime.get(exp.getId())).toString());
				Log.d(TAG,"c" + new Long(MIN_TIME_BETWEEN_ALERTS).toString());
				if (!timed || (System.currentTimeMillis() - lastTime.get(exp.getId()) > MIN_TIME_BETWEEN_ALERTS)) {
					
					lastTime.put(exp.getId(), System.currentTimeMillis());
					int icon = R.drawable.ic_stat_x_notification;
					CharSequence tickerText = "X-Lab Alert";
					long when = System.currentTimeMillis();
					
					Log.d(TAG,"Running UploadXlab");
					Notification notification = new Notification(icon, tickerText, when);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					notification.defaults |= Notification.DEFAULT_SOUND;
					notification.defaults |= Notification.DEFAULT_VIBRATE;
					notification.defaults |= Notification.DEFAULT_LIGHTS;
					
					Context context = getApplicationContext();
					CharSequence contentTitle = "X-Lab Alert";
					CharSequence contentText = this.exp.getTitle();
					Intent notificationIntent = new Intent(getApplicationContext(), exp.getActivity());
					notificationIntent.putExtra("id", this.exp.getId());
					PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					Log.d(TAG,"UploadXlab notify for " + this.exp.getTitle());
					notificationManager.cancel(exp.getId());
					notificationManager.notify(exp.getId(), notification);

				}
			}
			catch(Exception e){}
		}
	}	
}