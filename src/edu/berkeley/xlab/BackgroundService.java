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
	String username;
	
	private LocationManager locationManager;
	private NotificationManager notificationManager;
	
	GPSListener gpsListener;
	
	long lastLocationUploadToServer = 0;
	public enum UploadStatus {NO_ATTEMPT, SUCCESS, FAIL};
	UploadStatus uploadStatus = UploadStatus.NO_ATTEMPT;
	public static final int DOWNLOAD_INTERVAL = 15 * 60 * 1000; //In milliseconds
	//TODO: Make this changeable in settings
	public static final long SENSOR_CHECK_INTERVAL = 5 * 60 * 1000;
	public static final long GPS_MIN_ON = 5 * 1000;
	public static final long GPS_MAX_ON = 60 * 1000;
	public static final long GPS_DESIRED_ACCURACY = 100;//In meters. Stops GPS if achieved
	
	//X-Lab related...
	//TODO: Having a separate hashmap for each type of q could get out of hand. How to generalize?

	Map<Integer, XLabBinaryQuestion> xLabBinaryQuestions = 
		new ConcurrentHashMap<Integer, BackgroundService.XLabBinaryQuestion>();
	Map<Integer, XLabBudgetLineExp> xLabBudgetLines = 
		new ConcurrentHashMap<Integer, BackgroundService.XLabBudgetLineExp>();
	
	/******** CONSTANTS *********/
	
	//Logging
	public static final String BACKGROUND_SERVICE_TAG = "XLab-BACKGROUND";
	
	//Location Manager settings
	static final int MIN_DISTANCE = 0;//DV: CHANGED FROM 5 Note that usage is controlled with other parameters
	static final int MIN_TIME = 0;//DV: CHANGED FROM 5 Seconds Note that usage is controlled with other parameters
	
	//Platform ID (defined in Django app)
	public static final int PLATFORM_ID = 11;
	
	//File names
	public static final String GPS_DATA_FILE = "gps_data_file";
	
	//Notification ID
	static final int NOTIFICATION_ID = 1;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(BACKGROUND_SERVICE_TAG, "In BackgroundService - OnCreate");
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
		
		
		/**
		 * Periodically uploads data
		 * 
		 * @author dvizzini
		 */
		//TODO: Make button for debugging
		//TODO: Implement this: http://android-developers.blogspot.com/2010/05/android-cloud-to-device-messaging.html
		ScheduledThreadPoolExecutor downloader= new ScheduledThreadPoolExecutor(2);//5 threads? 2? Anyone have any idea?
		downloader.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				Thread t = new Thread( new FetchXLabTask() );
				t.start();
			}
		},0, DOWNLOAD_INTERVAL,TimeUnit.MILLISECONDS );
		
		// We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		return START_STICKY;		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class GPSListener implements LocationListener {
		private boolean isRunning;
		private long lastSwitchedOn;
		
		public void start() {
			Log.d(BACKGROUND_SERVICE_TAG, "Starting GPS Provider");
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					MIN_TIME, MIN_DISTANCE, this);
			isRunning = true;
			this.lastSwitchedOn = System.currentTimeMillis();
		}
		
		public void stop() {
			Log.d(BACKGROUND_SERVICE_TAG, "Stopping GPS Provider");
			locationManager.removeUpdates(this);
			isRunning = false;
		}
		
		@Override
		@SuppressWarnings("all")
		public void onLocationChanged(Location location) {
			//DO NOT DELETE. FOR WHEN SENSOR_CHECK_INTERVAL is 0
			if (location.hasAccuracy() && (SENSOR_CHECK_INTERVAL == 0)) {
				//Perform X-Lab checks
				doXLabChecks(location.getLatitude(), location.getLongitude(),location.getAccuracy(), location.getSpeed(), location.getProvider());						
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

	/**
	 * Defines budget-line experiment
	 * 
	 * @author Daniel Vizzini
	 */
	private class Session {
		
		public Line[] lines;
		
		
		public Session (Line[] lines) {
			this.lines = lines;
		}
		
	}
	
	/**
	 * For budget-line experiment
	 * 
	 * @author Daniel Vizzini
	 */
	private class Line {
		
		int id; double x_int; double y_int;
		
		public Line (int id, double x_int, double y_int) {
			this.id = id;
			this.x_int = x_int;
			this.y_int = y_int;
		}
		
	}

	/**
	 * Defines budget-line experiment
	 * 
	 * @author Daniel Vizzini
	 */
	private class XLabBudgetLineExp{
						
		private int id; public int getId() {return id;}
		private String title; public String getTitle() {return title;}
		private double lat; public double getLat() {return lat;}
		private double lon; public double getLon() {return lon;}
		private int radius; public int getRadius() {return radius;}
		
		private String x_label; public String getX_label() {return x_label;}
		private String x_units; public String getX_units() {return x_units;}
		private int x_max; public int getX_max() {return x_max;}
		private int x_min; public int getX_min() {return x_min;}
	    
		private String y_label; public String getY_label() {return y_label;}
		private String y_units; public String getY_units() {return y_units;}
		private int y_max; public int getY_may() {return y_max;}
		private int y_min; public int getY_min() {return y_min;}

		private Session[] sessions; public Session[] getSessions() {return sessions;}
		
		private int currSession = 0; public int getCurrSession() {return currSession;} public int nextCurrSession() {currSession++; return currSession;}
		private int currLine = 0; public int getCurrLine() {return currLine;} public int nextCurrLine() {currLine++; return currLine;}
		
		public XLabBudgetLineExp(int id, String title,
				double lat, double lon, int radius, 
				String x_label, String x_units, int x_max, int x_min,
				String y_label, String y_units, int y_max, int y_min,
				Session[] sessions) {
			this.id = id; this.title = title;
			this.lat = lat; this.lon = lon; this.radius = radius;
			this.x_label = x_label; this.x_units = x_units; this.x_max = x_max; this.x_min = x_min;
			this.y_label = y_label; this.y_units = y_units; this.y_max = y_max; this.y_min = y_min;
		}

	}
	
	private class XLabBinaryQuestion {
		int id;
		double lat;
		double lon;
		int radius;
		String question;
		boolean answered;
		
		public XLabBinaryQuestion(int id, double lat, double lon,
				int radius, String question) {
			super();
			this.id = id;
			this.lat = lat;
			this.lon = lon;
			this.radius = radius;
			this.question = question;
		}		

		public int getId() {
			return id;
		}

		public double getLat() {
			return lat;
		}

		public double getLon() {
			return lon;
		}

		public int getRadius() {
			return radius;
		}

		public String getQuestion() {
			return question;
		}
		
		public boolean isAnswered() {
			return this.answered;
		}

		public void setAnswered(boolean answered) {
			this.answered = answered;
		}
			
	}
		
	/**
	 * Fetch XLab tasks from the server
	 * 
	 * @author thejo and Daniel Vizzini
	 */
	private class FetchXLabTask implements Runnable {		
		
		@Override
		public void run() {
			try {
				
				String responseBQ = Utils.getData(Configuration.XLAB_API_ENDPOINT_BQ);
				Log.d(BACKGROUND_SERVICE_TAG, responseBQ);
				
				if(null != responseBQ) {
					
					//Parse the response
					String[] geofencesBQ = responseBQ.split("\n");
					
					for (String line : geofencesBQ) {
						String[] parts = line.split(",");
						int idBQ = Integer.valueOf(parts[0]);
						
						XLabBinaryQuestion bq = new XLabBinaryQuestion(idBQ, 
								Double.valueOf(parts[1]), Double.valueOf(parts[2]), 
								Integer.valueOf(parts[3]), String.valueOf(parts[4]));
						
						//TODO: The xLabBinaryQuestions map will keep growing if we do this.
						//Clear old values.
						if (! xLabBinaryQuestions.containsKey(idBQ)) {
							xLabBinaryQuestions.put(Integer.valueOf(parts[0]), bq);
						}
					}
					
				Log.d(BACKGROUND_SERVICE_TAG, "Downloaded " + geofencesBQ.length + "BQ geofences.");
				}
				
				String responseBL = Utils.getData(Configuration.XLAB_API_ENDPOINT_BL);
				Log.d(BACKGROUND_SERVICE_TAG, responseBL);

				if(null != responseBL) {
					
					//Parse the response
					String[] geofencesBL = responseBL.split("\n");
										    	
					for (String exp : geofencesBL) {
						
						String[] ses = exp.split("session_parser,");
					    Session[] sessions = new Session[ses.length - 1];
						
						String[] header = ses[0].split(",");
						
						int idBL = Integer.valueOf(header[0]);
						String title = header[1];
						double lat = Double.valueOf(header[2]);
						double lon = Double.valueOf(header[3]);
						int radius = Integer.valueOf(header[4]);
						
					    String x_label = header[5];
					    String x_units = header[6];
					    int x_max = Integer.valueOf(header[7]);
					    int x_min = Integer.valueOf(header[8]);
					    
					    String y_label = header[9];
					    String y_units = header[10];
					    int y_max = Integer.valueOf(header[11]);
					    int y_min = Integer.valueOf(header[12]);
					    		    
					    for (int i = 1; i < sessions.length; i++) {
					    	
					    	String[] lines = ses[i].split("line_parser,");//line as in budget line, not line of text
						    Line[] lineInput = new Line[lines.length];

						    for (int j = 0; j < lineInput.length; j++) {
						    	String[] parts = lines[j].split(",");
						    	lineInput[j] = new Line(Integer.valueOf(parts[0]),Double.valueOf(parts[1]),Double.valueOf(parts[2]));			    	
						    }
							
					    }
					    
					    XLabBudgetLineExp bl = new XLabBudgetLineExp(idBL, title,
								lat, lon, radius, 
								x_label, x_units, x_max, x_min,
								y_label, y_units, y_max, y_min,
								sessions);

						if (! xLabBudgetLines.containsKey(idBL)) {
							xLabBudgetLines.put(idBL, bl);
						}

					}
						
				//TODO: The xLabBinaryQuestions map will keep growing if we do this.
				//Clear old values.
				
				Log.d(BACKGROUND_SERVICE_TAG, "Downloaded " + geofencesBL.length + " geofences.");
				}
				
			} catch (Exception e) {
				Log.e(BACKGROUND_SERVICE_TAG, e.toString());
			}			
		}		
	}
	
	/**
	 * Upload XLab task status to the server 
	 * 
	 * @author thejo
	 */
	private class UploadXLabBQ implements Runnable {
		private XLabBinaryQuestion xlabQ;	
		private double lat;
		private double lon;
		private double accuracy;

		public UploadXLabBQ(XLabBinaryQuestion xlabQ,double lat, double lon, double accuracy ) {
			super();
			this.xlabQ = xlabQ;
			this.lat=lat;
			this.lon=lon;
			this.accuracy=accuracy;
		}

		@Override
		public void run() {
			try{
			int icon = R.drawable.androidmarker;
			CharSequence tickerText = "X-Lab Alert";
			long when = System.currentTimeMillis();
			long timeDelay = 1000;

			Thread.sleep(timeDelay);

			Notification notification = new Notification(icon, tickerText, when);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			
			Context context = getApplicationContext();
			CharSequence contentTitle = "X-Lab Alert";
			CharSequence contentText = this.xlabQ.getQuestion();
			Intent notificationIntent = new Intent(getApplicationContext(), BinaryQuestionActivity.class);
			notificationIntent.putExtra("bq_id", this.xlabQ.getId());
			notificationIntent.putExtra("bq_question", this.xlabQ.getQuestion());
			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			notificationManager.notify(NOTIFICATION_ID, notification);
			this.xlabQ.setAnswered(true);
			//}
			}
			catch(Exception e){}
		}
	}
	
	/**
	 * Upload XLab task status to the server 
	 * 
	 */
	//TODO: John, I imagine you will need to change this class extensively
	private class UploadXLabBL implements Runnable {
		private XLabBudgetLineExp xlabBL;	
		private double lat;
		private double lon;
		private double accuracy;

		public UploadXLabBL(XLabBudgetLineExp xlabBL,double lat, double lon, double accuracy ) {
			super();
			this.xlabBL = xlabBL;
			this.lat=lat;
			this.lon=lon;
			this.accuracy=accuracy;
		}

		@Override
		public void run() {
			try{
			int icon = R.drawable.androidmarker;
			CharSequence tickerText = "X-Lab Alert";
			long when = System.currentTimeMillis();
			long timeDelay = 1000;

			Thread.sleep(timeDelay);

			Notification notification = new Notification(icon, tickerText, when);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			
			Context context = getApplicationContext();
			CharSequence contentTitle = "X-Lab Alert";
			//CharSequence contentText = this.xlabBL.getQuestion();
			Intent notificationIntent = new Intent(getApplicationContext(), BinaryQuestionActivity.class);
			notificationIntent.putExtra("bq_id", this.xlabBL.getId());
			//notificationIntent.putExtra("bq_question", this.xlabQ.getQuestion());
			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

			//notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			notificationManager.notify(NOTIFICATION_ID, notification);
			this.xlabBL.nextCurrSession();
			}
			catch(Exception e){}
		}
	}
	
	public void doXLabChecks(double lat, double lon, float accuracy, float speed, String provider) {

		
		for(Map.Entry<Integer, XLabBinaryQuestion> entry : xLabBinaryQuestions.entrySet()) {
			XLabBinaryQuestion bq = entry.getValue();

			//Consider this fix only if the accuracy is at least 20% of the radius specified in the geofence
			if(! bq.isAnswered() &&  (bq.getRadius() * 0.2f) >= accuracy && 
					Utils.GetDistanceFromLatLon(bq.getLat(), bq.getLon(), lat, lon) <= bq.getRadius()) {
				//Show a notification
				Thread t = new Thread( new UploadXLabBQ(bq, lat,lon, accuracy) );
				t.start();
			}
			
			Thread t = new Thread( new FetchXLabTask() );
			t.start();
		
		}

		for(Map.Entry<Integer, XLabBudgetLineExp> entry : xLabBudgetLines.entrySet()) {
			XLabBudgetLineExp bl = entry.getValue();

			//Consider this fix only if the accuracy is at least 20% of the radius specified in the geofence
			if( (bl.getCurrSession() > 0) && (bl.getRadius() * 0.2f) >= accuracy && 
					Utils.GetDistanceFromLatLon(bl.getLat(), bl.getLon(), lat, lon) <= bl.getRadius()) {
				//Show a notification
				Thread t = new Thread( new UploadXLabBL(bl, lat,lon, accuracy) );
				t.start();
			}
			
			Thread t = new Thread( new FetchXLabTask() );
			t.start();
		
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
		CharSequence tickerText = "Mode Choice needs access to location";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "Mode Choice Alert";
		CharSequence contentText = "Please turn on GPS.";
		Intent notificationIntent = new Intent(this, LoginActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
}