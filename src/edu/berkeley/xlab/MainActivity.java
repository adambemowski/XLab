package edu.berkeley.xlab;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.berkeley.xlab.BackgroundService;
import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.experiments.*;
import edu.berkeley.xlab.util.Utils;

import android.app.Activity;
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
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String TAG = "XLab-MAIN";
	private boolean backgroundRunning = false;
	private TextView textView;
	private MenuInflater inflater = getMenuInflater();


	// private ProgressDialog mSplashDialog = new
	// ProgressDialog(getApplicationContext());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "In MainActivity -- OnCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		textView = (TextView) findViewById(R.id.TextView01);
		new FetchXLabTask().execute();
	}

	/**
	 * Fetch XLab tasks from the server
	 * 
	 * @author Daniel Vizzini
	 */
	private class FetchXLabTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {

			Log.d(TAG,
					"In MainActivity -- FetchXLabTask -- onPreExecute");

			// mSplashDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// mSplashDialog.setMessage("XLab is Loading...");
			// mSplashDialog.setCancelable(false);
			// mSplashDialog.show();
		}

		protected String doInBackground(Void... voids) {

			Log.d(TAG,"In MainActivity -- FetchXLabTask -- doInBackground");

			String message = "Welcome to XLab.\nA nifty menu is coming soon.\n";

			try {
				App appState = ((App) getApplicationContext());
				String responseBQ = Utils
						.getData(Configuration.XLAB_API_ENDPOINT_BQ);
				Log.d(TAG, responseBQ);

				// X-Lab related...
				ConcurrentHashMap<Integer, Experiment> xLabExps = appState.getXLabExps();

				if (null != responseBQ) {

					// Parse the response
					String[] geofencesBQ = responseBQ.split("\n");
					message += "\nYou have received " + geofencesBQ.length
							+ " question" + ((geofencesBQ.length != 1)?"s":"") + ".\nYour phone currently contains:\n\n";

					for (String line : geofencesBQ) {

						XLabBinaryQuestion bq = new XLabBinaryQuestion(line);
						int id = bq.getId();
						message += bq.getTitle() + "\n";

						// TODO: The xLabBinaryQuestions map will keep growing
						// if we do this.
						// Clear old values.
						xLabExps.putIfAbsent(XLabBinaryQuestion.CONSTANT_ID + id, bq);
					}
				}

				String responseBL = Utils.getData(Configuration.XLAB_API_ENDPOINT_BL);
				Log.d(TAG, responseBL);

				if (null != responseBL) {

					// Parse the response
					String[] geofencesBL = responseBL.split("\n");
					message += "\nYou have received "
							+ geofencesBL.length
							+ " budget line" + ((geofencesBL.length != 1)?"s":"") + ".\nYour phone currently contains:\n\n";

					for (String exp : geofencesBL) {

						XLabBudgetLineExp bl = new XLabBudgetLineExp(exp);
						message += bl.getTitle() + "\n";
						Log.d(TAG, bl.getTitle());

						xLabExps.putIfAbsent(XLabBudgetLineExp.CONSTANT_ID + bl.getId(), bl);

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

			return message;

		}

		@Override
		protected void onPostExecute(String message) {

			Log.d(TAG,"In MainActivity -- FetchXLabTask -- onPostActivity");

			textView.setText(message);
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
				doXLabChecks(37.875576,-122.260841,10);
				return true;
			default:
				return super.onOptionsItemSelected(item);
	    }
	}
	
	//TODO: After working, copy following classes ingo BackgroundService and delete
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
				Thread t = new Thread( new UploadXLabTask(exp) );
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

		public UploadXLabTask(Experiment exp) {
			super();
			Log.d(TAG,"In UploadXlabBL constructor");
			this.exp = exp;
		}

		@Override
		public void run() {
			
			try{
				lastTime.putIfAbsent(exp.getId(), (long)0);
				Log.d(TAG,"a" + new Long(System.currentTimeMillis()).toString());
				Log.d(TAG,"b" + new Long(lastTime.get(exp.getId())).toString());
				Log.d(TAG,"c" + new Long(MIN_TIME_BETWEEN_ALERTS).toString());
				if (System.currentTimeMillis() - lastTime.get(exp.getId()) > MIN_TIME_BETWEEN_ALERTS) {
					
					lastTime.put(exp.getId(), System.currentTimeMillis());
					int icon = R.drawable.androidmarker;
					CharSequence tickerText = "X-Lab Alert";
					long when = System.currentTimeMillis();
					
					Log.d(TAG,"Running UploadXlabBL");
					Notification notification = new Notification(icon, tickerText, when);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					notification.defaults |= Notification.DEFAULT_SOUND;
					notification.defaults |= Notification.DEFAULT_VIBRATE;
					notification.defaults |= Notification.DEFAULT_LIGHTS;
					
					Context context = getApplicationContext();
					CharSequence contentTitle = "X-Lab Alert";
					CharSequence contentText = this.exp.getTitle();
					Intent notificationIntent = new Intent(getApplicationContext(), exp.getActivity());
					notificationIntent.putExtra("id", this.exp.getId() + this.exp.getClassId());
					PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					Log.d(TAG,"UploadXlabBL notify for " + this.exp.getTitle());
					notificationManager.cancel(exp.getId());
					notificationManager.notify(exp.getId(), notification);
					//TODO: Figure out how to increment sessions
					//this.xlabBL.nextCurrSession();

				}
			}
			catch(Exception e){}
		}
	}
}