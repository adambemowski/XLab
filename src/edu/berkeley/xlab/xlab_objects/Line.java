package edu.berkeley.xlab.xlab_objects;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

public class Line {
	
	/** Filename prefix for persistent memory of session*/
	public static final String LINE_PREFIX = "Line_";

	private int lineId;public int getLineId() {return lineId;}
	private int sessionId; public int getSessionId() {return sessionId;}
	private int expId; public int getExpId() {return expId;}
	private float x_int; public float getX_int() {return x_int;}//server-side Monte Carlo
	private float y_int; public float getY_int() {return y_int;}//server-side Monte Carlo
	private char winner; public char getWinner() {return winner;}//server-side Monte Carlo
	
	public Line (Context context, int expId, int sessionId, int lineId, float x_int, float y_int, char winner) {

		this.expId = expId;
		this.sessionId = sessionId;
		this.lineId = lineId;
		this.x_int = x_int;
		this.y_int = y_int;
		this.winner = winner;//for risk-reward studies
		
	}
		
	public Line (Context context, JSONObject json) throws JSONException {
		
		this.expId = json.getInt("expId");
		this.sessionId = json.getInt("sessionId");
		this.lineId = json.getInt("lineId");
		this.x_int = (float) json.getDouble("x_int");
		this.y_int = (float) json.getDouble("y_int");
		this.winner = json.getString("winner").charAt(0);//for risk-reward studies

	}
	
	public Line(SharedPreferences sharedPreferences) {
		
		this.expId = sharedPreferences.getInt("expId",-1);
		this.sessionId = sharedPreferences.getInt("sessionId",-1);
		this.lineId = sharedPreferences.getInt("lineId",-1);
		this.x_int = sharedPreferences.getFloat("x_int",(float) -1);
		this.y_int = sharedPreferences.getFloat("y_int", (float) -1);
		this.winner = sharedPreferences.getString("winner", "").charAt(0);//for risk-reward studies
		
	}
	
	/** saves instance to Shared Preferences */
	public void save(Context context) {
		
		String name = makeName(expId, sessionId, lineId);
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putInt("expId", expId);
		editor.putInt("sessionId", sessionId);
		editor.putInt("lineId", lineId);
		editor.putFloat("x_int", x_int);
		editor.putFloat("y_int", y_int);
		editor.putString("winner", String.valueOf(winner));
		editor.commit();
		
	}
	
	//* deletes all SharedPreferences associated with Session, except those associated with its results */	
	public void deleteSharedPreferences(Context context) {
		
		new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + makeName(expId, sessionId, lineId) + ".xml").delete();		
		
	}
	
	/** returns name of SharedPreferences */
	public static String makeName(int expId, int sessionId, int lineId) {
		return(LINE_PREFIX + expId + "_" + sessionId + "_" + lineId);
	}

}
