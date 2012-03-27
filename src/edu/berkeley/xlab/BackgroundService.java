package edu.berkeley.xlab;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.*;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BackgroundService extends Service {
	
	String username;
	
	private LocationManager locationManager;
	private NotificationManager notificationManager;	
	
	private static double lastLat = 0;
	
	private Context context;

	
	/**
	 * @return last latitude read
	 */
	public static double getLastLat() {
		return lastLat;
	}
	
	private static double lastLon = 0;
	/**
	 * @return last longitude read
	 */
	public static double getLastLon() {
		return lastLon;
	}
		
	GPSListener gpsListener;
	long lastLocationUploadToServer = 0;
	public enum UploadStatus {NO_ATTEMPT, SUCCESS, FAIL};
	UploadStatus uploadStatus = UploadStatus.NO_ATTEMPT;
	public static final int DOWNLOAD_INTERVAL = 15 * 60 * 1000; //In milliseconds
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
		
		context = this.getApplicationContext();

		
		Log.d(TAG, "In BackgroundService - OnCreate");
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		this.username = Utils.getStringPreference(this, Constants.USERNAME, "anonymous");
		this.lastLocationUploadToServer = System.currentTimeMillis();
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		super.onStartCommand(intent, flags, startId);
				
		if (gpsListener == null) {
			gpsListener = new GPSListener();
			gpsListener.start();
		}
		
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

	public class GPSListener implements LocationListener {
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
			lastLat = location.getLatitude();
			lastLon = location.getLatitude();
			if (location.hasAccuracy()) {
				//Perform X-Lab checks
				doXLabChecks(location.getLatitude(), location.getLongitude(),location.getAccuracy());						
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG,"In onProviderDisabled");
			isRunning = false;
			notifyOnLocationDisable();
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG,"In onProviderEnabled");
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
		
	public String getLocationDataAsString(Location l) {
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
		int icon = R.drawable.ic_stat_x_notification;
		CharSequence tickerText = "XLab needs access to location";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		CharSequence contentTitle = "XLab Alert";
		CharSequence contentText = "Please turn on GPS.";
		Intent notificationIntent = new Intent(this, LoginActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(DISABLED_NOTIFICATION_ID, notification);
	}
	
	//Copy sensor code here
	private static final long MIN_TIME_BETWEEN_ALERTS = 10 * 60 * 1000;
	private ConcurrentHashMap<Integer, Long> lastTime = new ConcurrentHashMap<Integer, Long>();

	/**
	 * Checks if geofence has been breached
	 * @param lat latitude of center of geofence
	 * @param lon longitude of center of geofence
	 * @param accuracy accuracy of GPS listener 
	 */
	private void doXLabChecks(double lat, double lon, float accuracy) {

		Log.d(TAG,"In doXLabChecks");
		String[] expNames = context.getSharedPreferences(ExperimentAbstract.EXP_LIST, Context.MODE_PRIVATE).getString("SharedPreferences", "").split(",");
		for(String expName : expNames) {

			ExperimentAbstract exp;
			SharedPreferences expSP = context.getSharedPreferences(expName, Context.MODE_PRIVATE);
			
			if (expSP.getInt("typeId", -1) == Constants.XLAB_TQ_EXP) {				
				exp = new ExperimentTextQuestion(context, expSP);
			} else {
				exp = new ExperimentBudgetLine(context, expSP);
			}
			
			Log.d(TAG,"Examining " + exp.getTitle() + ": isAnswered = " + exp.isDone() + ": radius = " + exp.getRadius() + ", accuracy = " + accuracy + ", distance = " + Utils.getDistanceFromLatLon(exp.getLat(), exp.getLon(), lat, lon));
			//Consider this fix only if the accuracy is at least 100% of the radius specified in the geofence
			if(! exp.isDone() &&  (exp.getRadius() * 1.0f) >= accuracy && Utils.getDistanceFromLatLon(exp.getLat(), exp.getLon(), lat, lon) <= exp.getRadius()) {
				lastTime.putIfAbsent(exp.getExpId(), (long)0);
				Log.d(TAG,"a" + new Long(System.currentTimeMillis()).toString());
				Log.d(TAG,"b" + new Long(lastTime.get(exp.getExpId())).toString());
				Log.d(TAG,"c" + new Long(MIN_TIME_BETWEEN_ALERTS).toString());
				if (System.currentTimeMillis() - lastTime.get(exp.getExpId()) > MIN_TIME_BETWEEN_ALERTS) {
					
					lastTime.put(exp.getExpId(), System.currentTimeMillis());
					Log.d(TAG,"Starting thread for " + exp.getTitle());
					//Show a notification
					Thread t = new Thread( new Notifier(context, notificationManager, exp) );
					t.start();
				
				}
			}
		}
	}	
	
}