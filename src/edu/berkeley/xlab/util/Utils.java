package edu.berkeley.xlab.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

public class Utils {
	
	public static final String TAG = "ModeChoice-UTIL";
	
	/** decimal FORMATTER */
	public static final DecimalFormat FORMATTER_PERCENT = new DecimalFormat("0%");
	
	public static void setStringPreference(Context context, String prefKey, String prefValue) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(prefKey, prefValue);
		editor.commit();
	}
	
	public static String getStringPreference(Context context, String prefKey, String defValue) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getString(prefKey, defValue);		
	}
	
	public static void setBooleanPreference(Context context, String prefKey, boolean prefValue) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(prefKey, prefValue);
		editor.commit();
	}
	
	public static boolean getBooleanPreference(Context context, String prefKey, boolean defValue) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(prefKey, defValue);		
	}
	
    public static String getDateAsString(String strFormat, long epochTime) {

        Date now = new Date(epochTime);

        SimpleDateFormat format = new SimpleDateFormat(strFormat);

        return format.format(now);
    }
    
    public static String join(String sep, List<Object> pieces) {
        if (pieces.isEmpty())  return "";
        if (pieces.size() == 1)  return String.valueOf(pieces.get(0));

        StringBuilder buf = new StringBuilder(16*pieces.size());
        Iterator<Object> i = pieces.iterator();
        buf.append( String.valueOf(i.next()) );        // We already know that size > 1
        while (i.hasNext()) {
          buf.append(sep).append( String.valueOf(i.next()) );
        }
        return buf.toString();
    }
    
    /**
     * Given a string, returns the MD5 hash
     *
     * @param s String whose MD5 hash value is required
     * @return An MD5 hash string
     * @throws java.security.NoSuchAlgorithmException
     */
    public static String getMD5(String s) throws NoSuchAlgorithmException {

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update( s.getBytes(), 0, s.length() );

        String md5 = new BigInteger(1, m.digest()).toString(16);

        return md5;
    }
    
	public static String postData(String endpoint, Map<String, String> params) 
		throws ClientProtocolException, IOException {
		
		String responseBody = null;
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(endpoint);
	    
        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
        	nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
       
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);
        responseBody = EntityUtils.toString(response.getEntity());	    
	    
	    return responseBody;
	}
	
	public static String getData(String endpoint) 
		throws ClientProtocolException, IOException, NumberFormatException {
		
		String responseBody = null;
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpGet httpget = new HttpGet(endpoint);    
	
	    // Execute HTTP Get Request
	    HttpResponse response = httpclient.execute(httpget);
	    responseBody = EntityUtils.toString(response.getEntity());	    
	    
	    return responseBody;
	}
	
	public static double[] getDoubleArrayFromDoubleList(List<Double> l) {
		double[] a = new double[l.size()];
		for(int i = 0; i < l.size(); i++) {
			a[i] = l.get(i);
		}
		
		return a;
	}
	
	public static double[] getDoubleArrayFromIntegerList(List<Integer> l) {
		double[] a = new double[l.size()];
		for(int i = 0; i < l.size(); i++) {
			a[i] = l.get(i);
		}
		
		return a;
	}
	
    public static double getDistanceFromLatLon(double lat1, double lon1, double lat2, double lon2) {

        int R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * 
        	Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (R * c) * 1000;
    }
    
    //courtesy of http://www.javalobby.org/forums/thread.jspa?threadID=16906&tstart=0
    public static String getOrdinalFor(int value) {
    	 int hundredRemainder = value % 100;
    	 int tenRemainder = value % 10;
    	 if(hundredRemainder - tenRemainder == 10) {
    	  return value + "th";
    	 }
    	 
    	 switch (tenRemainder) {
    	  case 1:
    	   return value + "st";
    	  case 2:
    	   return value + "nd";
    	  case 3:
    	   return value + "rd";
    	  default:
    	   return value + "th";
    	 }
    	}
    
	/**
	 * Loads JSONObject
	 * @throws JSONException 
	 */
	public static JSONObject getJSONFromFile(FileInputStream fis) throws JSONException {
		try {
	        InputStreamReader isr = new InputStreamReader(fis);
	        BufferedReader br = new BufferedReader(isr);
	        return (JSONObject) new JSONTokener(br.readLine()).nextValue();
	        
		} catch (Exception e) {
			Log.d(TAG,"getJSONObject Exception: " + Log.getStackTraceString(e), e);
			JSONObject blank = new JSONObject();
			blank.put("valid", 0);
			return blank;
		}
	}
	
	/**
	 * Calculates the number of eligible days between two dates.
	 * @param startDate day from which to begin calculating
	 * @param endDate day from which to end calculating
	 * @param dayEligibility boolean array of length 7, with each day corresponding to a day of the week, where true indicates the day is eligible. Index 0 is Sunday.
	 * @return the number of eligible days between startDate and endDate, inclusively. Note that is startDate equals endDate, 1 will be returned
	 */
	public static int getEligiableDaysBetweenTwoDates(GregorianCalendar startDate, GregorianCalendar endDate, boolean[] dayEligibility) {
		
		checkDayEligibility(dayEligibility);
		
		//put earlier day first			    
	    if (startDate.getTimeInMillis() > endDate.getTimeInMillis()) {
	        GregorianCalendar tempDate = (GregorianCalendar) startDate.clone();
	    	startDate = (GregorianCalendar) endDate.clone();
	        endDate = (GregorianCalendar) tempDate.clone();
	    }

	    int eligiableDays = 0;
	    int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK) - 1;
	    int[] firstDate = {startDate.get(Calendar.YEAR),startDate.get(Calendar.MONTH), startDate.get(Calendar.DATE)};
	    GregorianCalendar dateBetween;
	    
	    for (int i = 0; i < 7; i ++) {
	    	dateBetween = new GregorianCalendar(firstDate[0], firstDate[1], firstDate[2] + i);

	    	Log.d(TAG,"dayEligibility: " + dayEligibility[0] + dayEligibility[1] + dayEligibility[2] + dayEligibility[3] + dayEligibility[4] + dayEligibility[5] + dayEligibility[6]);
	    	Log.d(TAG,"dayOfWeek: " + dayOfWeek);
	    	
	    	if (dayEligibility[dayOfWeek]) {
	    		while (dateBetween.getTimeInMillis() <= endDate.getTimeInMillis()) {
		            eligiableDays++;
		            dateBetween.add(Calendar.DAY_OF_MONTH, 7);
			    }
	    	}
	    	dayOfWeek = (dayOfWeek + 1) % 7;
	    }

	    return eligiableDays;
	}
	
	/**
	 * Calculates the day given number of days after a starting date, skipping ineligible days
	 * @param startDate day from which to begin calculating
	 * @param numDays eligible days after startDate that the function will return. Note that 0 will return 0 if this date is eligible and the first eligible date after startDate otherwise
	 * @param dayEligibility boolean array of length 7, with each day corresponding to a day of the week, where true indicates the day is eligible. Index 0 is Sunday.
	 * @return the date numDays eligible days after start date
	 */
	public static GregorianCalendar addEligibleDays(GregorianCalendar startDate, int numDays, boolean[] dayEligibility){
		
		checkDayEligibility(dayEligibility);
		
		GregorianCalendar date = (GregorianCalendar) startDate.clone();
		
		if (numDays < 0) {
			throw new IllegalArgumentException("numDays must be positive");
		}
		
	    int dayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1;
	    
	    while (numDays >= 0) {
	        Log.d(TAG,"date Now: " + date.get(Calendar.YEAR) + "/" + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.DATE) );
	    	if (dayEligibility[dayOfWeek]) {
	    		--numDays;
	    		Log.d(TAG,"numDays: "+ numDays);
	    		if (numDays < 0) {
	    			Log.d(TAG,"Breaking");
			    	break;	    			
	    		}
	    	} 
	    	date.add(Calendar.DATE,1);	    		
	    	dayOfWeek = (dayOfWeek + 1) % 7;
	    }
	    
	    return date;
	    
	}
	
	/** ensures that dayEligibility argument is of write length and has a true element */
	private static void checkDayEligibility(boolean[] dayEligibility) {
		if (dayEligibility.length != 7) {
			throw new IllegalArgumentException("dayEligibility must be of length 7, with index 0 corresponding to Sunday");
		}
		
		boolean atLeastOneDayIsEligible = false;
		for (boolean day : dayEligibility) {
			atLeastOneDayIsEligible |= day;
		}
		if (!atLeastOneDayIsEligible) {
			throw new IllegalArgumentException("At least one day must be eligible");
		}
	}
	
	/**
	 * Checks list to see if XLABSuperObject has been saved
	 * @param context Application context
	 * @param name name of SharedPreferences associated with XLABSuperObject specified
	 * @param list name of list of SharedPreferences, e.g. Experiment.EXP_LIST
	 * @return true if list contains this name, false otherwise
	 */
	public static boolean checkIfSaved(Context context, String name, String list) {
		
		String[] names = context.getSharedPreferences(list, Context.MODE_PRIVATE).getString("SharedPreferences", "").split(",");
		ArrayList<String> namesArrayList = new ArrayList<String>();
		
		if (!names[0].equals("")) {
			for (String expName : names) {
				if (!namesArrayList.contains(expName)) {
					namesArrayList.add(expName);
				}
			}
		}		
		
		return namesArrayList.contains(name);
		
	}
	
	/**
	 * @return string of time of next experiment segment in readable manner, e.g. 3:50 pm today, 3:50 pm tomorrow, 3:50 pm on Thursday, March 3
	 */
	public static String getRelativeTime(long timeInFuture) {
    	
        Long currentTime = System.currentTimeMillis();
        Time nextTimeObj = new Time();
	    nextTimeObj.set(timeInFuture);
	    
        //figure out relative day of next alert
        String relativeDay;
        Calendar rightNow = Calendar.getInstance();
        int dateDiff = (Time.getJulianDay(timeInFuture, (rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET)) / 1000) - Time.getJulianDay(currentTime, (rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET)) / 1000));
        if (dateDiff == 0) {
        	relativeDay = " today";
        } else if (dateDiff == 1) {
        	relativeDay = " tomorrow";
        } else {
        	relativeDay = " on " + nextTimeObj.format("%A") + ", " + nextTimeObj.format("%B") + " " + Utils.getOrdinalFor(Integer.valueOf(nextTimeObj.format("%d"))) + ", " + nextTimeObj.format("%Y");
        }
        
        return nextTimeObj.format("%r") + relativeDay;

	}
	

}
