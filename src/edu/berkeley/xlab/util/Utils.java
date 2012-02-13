package edu.berkeley.xlab.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.util.Log;

public class Utils {
	
	public static final String TAG = "ModeChoice-UTIL";
	
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
    	  return "th";
    	 }
    	 
    	 switch (tenRemainder) {
    	  case 1:
    	   return "st";
    	  case 2:
    	   return "nd";
    	  case 3:
    	   return "rd";
    	  default:
    	   return "th";
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

}
