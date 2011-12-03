package edu.berkeley.xlab;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;  
import java.util.concurrent.TimeUnit;  

import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.experiments.*;
import edu.berkeley.xlab.util.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BackgroundService extends Service {
	
	//TODO:GET THIS CLASS IN A DIFFERNT PROCESS WITH SOME SORT OF LOCAL STORE.
	String username;
	
	private LocationManager locationManager;
	private NotificationManager notificationManager;
	
	GPSListener gpsListener;
	long lastLocationUploadToServer = 0;
	public enum UploadStatus {NO_ATTEMPT, SUCCESS, FAIL};
	UploadStatus uploadStatus = UploadStatus.NO_ATTEMPT;
	public static final int DOWNLOAD_INTERVAL = 15 * 60 * 1000; //In milliseconds
	//TODO: Make this changeable in settings
	public static final long SENSOR_CHECK_INTERVAL = 0;//5 * 60 * 1000;
	public static final long GPS_MIN_ON = 5 * 1000;
	public static final long GPS_MAX_ON = 60 * 1000;
	public static final long GPS_DESIRED_ACCURACY = 100;//In meters. Stops GPS if achieved
	public static final int DISABLED_NOTIFICATION_ID = -1;
	/******** CONSTANTS *********/
	
	//Logging
	public static final String TAG = "XLab-BACKGROUND";
	
	//Location Manager settings
	static final int MIN_DISTANCE = 0;//DV: CHANGED FROM 5 Note that usage is controlled with other parameters
	static final int MIN_TIME = 0;//DV: CHANGED FROM 5 Seconds Note that usage is controlled with other parameters
	
	//Platform ID (defined in Django app)
	public static final int PLATFORM_ID = 11;
	
	//File names
	public static final String GPS_DATA_FILE = "gps_data_file";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "In BackgroundService - OnCreate");
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		this.username = Utils.getStringPreference(this, Configuration.USERNAME, "anonymous");
		this.lastLocationUploadToServer = System.currentTimeMillis();
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		super.onStartCommand(intent, flags, startId);
		
		if (gpsListener == null) {
			gpsListener = new GPSListener();
			gpsListener.start();
		}
		
		/**
		 * Periodically takes GPS Readings
		 * 
		 * @author dvizzini
		 */
		//DV: DO NOT DELETE. SENSOR_CHECK_INTERVAL will not be zero
/*		if (SENSOR_CHECK_INTERVAL != 0) {
			ScheduledThreadPoolExecutor locationTimer= new ScheduledThreadPoolExecutor(5);//5 threads? 2? Anyone have any idea?
			locationTimer.scheduleAtFixedRate(new Runnable() {
				
				private long interval = System.currentTimeMillis() - gpsListener.getLastSwitchedOn();
				
				@Override
				public void run() {
					if( interval > SENSOR_CHECK_INTERVAL ) {
						if( !gpsListener.isRunning() ) {
							gpsListener.start();
						}
					} else {
						Location location = gpsListener.getLocation();
						if (location.hasAccuracy() && 
								(location.getAccuracy() <= GPS_DESIRED_ACCURACY) && 
								(location.getTime() >= gpsListener.getLastSwitchedOn())) {
							gpsListener.stop();
							//Perform X-Lab checks
							doXLabChecks(location.getLatitude(), location.getLongitude(),location.getAccuracy(), location.getSpeed(), location.getProvider());						
						}
					}				
				}
				
			},0, 1000,TimeUnit.MILLISECONDS );  			
		}
*/		
		
		/**
		 * Periodically uploads data
		 * 
		 * @author dvizzini
		 */
		//TODO: Implement this: http://android-developers.blogspot.com/2010/05/android-cloud-to-device-messaging.html
		/*DV: Commenting out for demo (and perhaps forever). Will get called at startup.
		ScheduledThreadPoolExecutor downloader= new ScheduledThreadPoolExecutor(2);//5 threads? 2? Anyone have any idea?
		downloader.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				Thread t = new Thread( new FetchXLabTask() );
				t.start();
			}
		},DOWNLOAD_INTERVAL, DOWNLOAD_INTERVAL,TimeUnit.MILLISECONDS );
		*/
		
		// We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		return START_STICKY;		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		//  Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
		gpsListener.stop();
	}

	private class GPSListener implements LocationListener {
		private boolean isRunning;
		private long lastSwitchedOn;
		
		public void start() {
			Log.d(TAG, "Starting GPS Provider");
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					MIN_TIME, MIN_DISTANCE, this);
			isRunning = true;
			this.lastSwitchedOn = System.currentTimeMillis();
		}
		
		public void stop() {
			Log.d(TAG, "Stopping GPS Provider");
			locationManager.removeUpdates(this);
			isRunning = false;
		}
		
		@Override
		public void onLocationChanged(Location location) {
			//DO NOT DELETE. FOR WHEN SENSOR_CHECK_INTERVAL is 0
			Log.d(TAG, "New location: " + location.getLatitude() + location.getLongitude());
			if (location.hasAccuracy() && (SENSOR_CHECK_INTERVAL == 0)) {
				//Perform X-Lab checks
				doXLabChecks(location.getLatitude(), location.getLongitude(),location.getAccuracy());						
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			isRunning = false;
			notifyOnLocationDisable();
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			this.start();
			isRunning = true;
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		
		public boolean isRunning() {
			return this.isRunning;
		}
		
		public long getLastSwitchedOn() {
			return this.lastSwitchedOn;
		}
		
		public Location getLocation() {
			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);				
		}
	}
		
	private String readFromFile(String fileName) throws IOException {
		StringBuilder strContent = new StringBuilder("");
		int ch;
		FileInputStream fin = openFileInput(fileName);
		
		while ((ch = fin.read()) != -1) {
			strContent.append((char)ch);
		}
		fin.close();
		
		return strContent.toString();		
	}
	
	private String getLocationDataAsString(Location l) {
		//Format - identifier, platform, lat, lon, gps_time, sample_time, velocity, 
		//	haccuracy, vaccuracy, altitude, course, hdop, vdop, service_provider, has_speed, 
		//  has_accuracy, has_velocity, has_altitude, has_course, satellites_in_view, satellite_count, 
		//  travel_mode
		
		long sampleTime = System.currentTimeMillis();
		//Unique Device ID
		TelephonyManager tm = (TelephonyManager) this.getApplicationContext().
			getSystemService(Context.TELEPHONY_SERVICE);
		
		List<Object> elems = new ArrayList<Object>();
		elems.add(tm.getDeviceId()); //identifier
		elems.add(PLATFORM_ID); //platform
		elems.add(l.getLatitude()); //lat
		elems.add(l.getLongitude()); //lon
		
		//TODO: These should be in milli seconds for higher precision
		elems.add(sampleTime / 1000); //gps_time
		elems.add(l.getTime() / 1000); //sample_time
		
		elems.add(l.getSpeed()); //velocity
		elems.add(l.getAccuracy()); //haccuracy
		elems.add(""); //vaccuracy
		elems.add(l.getAltitude()); //altitude
		elems.add(l.getBearing()); //course
		elems.add(""); //hdop
		elems.add(""); //vdop
		elems.add(l.getProvider()); //service_provider
		elems.add( l.hasSpeed() ? "1" : "0" ); //has_speed
		elems.add(l.hasAccuracy() ? "1" : "0" ); //has_accuracy
		elems.add(""); //has_velocity
		elems.add(l.hasAltitude() ? "1" : "0" ); //has_altitude
		elems.add(l.hasBearing() ? "1" : "0" ); //has_course
		elems.add(""); //satellites_in_view
		elems.add(""); //satellite_count
		
		return Utils.join(",", elems) + "\n";		
	}
	
	/**
	 * Display a notification to the user if the GPS sensor is switched off
	 */
	private void notifyOnLocationDisable() {
		int icon = R.drawable.androidmarker;
		CharSequence tickerText = "XLab needs access to location";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "XLab Alert";
		CharSequence contentText = "Please turn on GPS.";
		Intent notificationIntent = new Intent(this, LoginActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(DISABLED_NOTIFICATION_ID, notification);
	}
	
	//Copy sensor code here
	private static final long MIN_TIME_BETWEEN_ALERTS = 15 * 60 * 1000;
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
				Log.d(TAG,"Starting thread for " + exp.isDone());
				//Show a notification
				Thread t = new Thread( new UploadXLabTask(exp, lat,lon, accuracy) );
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

		public UploadXLabTask(Experiment exp, double lat, double lon, double accuracy ) {
			super();
			Log.d(TAG,"In UploadXlabBL constructor");
			this.exp = exp;
		}

		@Override
		public void run() {
			
			try{
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
					Intent notificationIntent = new Intent(getApplicationContext(), BudgetLineActivity.class);
					notificationIntent.putExtra("bl_id", this.exp.getId());
					PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					Log.d(TAG,"UploadXlabBL notify for " + this.exp.getTitle());
					notificationManager.cancelAll();
					notificationManager.notify(exp.getId(), notification);
					//TODO: Figure out how to increment sessions
					//this.xlabBL.nextCurrSession();

				}
			}
			catch(Exception e){}
		}
	}
}