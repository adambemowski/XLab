package edu.berkeley.xlab.xlab_objects;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Session {
	
	/** TAG is an identifier for the log. */
	public static final String TAG = "X-Lab - Session";
		
	/** Filename prefix for persistent memory of session*/
	public static final String SESSION_PREFIX = "Session_";

	private int expId;
	public int getExpId() {return expId;}
	
	private int sessionId;
	public int getSessionId() {return sessionId;}
	
	private int line_chosen;
	public int getLine_chosen() {return line_chosen;}//server-side Monte Carlo
	
	private Line[] lines;
	public Line[] getLines() {return lines;}
	public Line getLine(int id) {return lines[id];}

	public Session (Context context, int expId, int sessionId, int line_chosen, Line[] lines) {
		
		this.expId = expId;
		this.sessionId = sessionId;
		this.line_chosen = line_chosen;
		this.lines = lines;
		
	}
	
	public Session (Context context, JSONObject json) throws JSONException {

		JSONArray linesJSON = json.getJSONArray("lines");
		Line[] lines = new Line[linesJSON.length()];
		
		for (int i = 0; i < linesJSON.length(); i++) {
			lines[i] = new Line(context, linesJSON.getJSONObject(i));
		}

		this.expId = json.getInt("expId");
		this.sessionId = json.getInt("sessionId");
		this.line_chosen = json.getInt("line_chosen");
		this.lines = lines;
		
	}
	
	public Session(Context context, SharedPreferences sharedPreferences) {
				
		this.expId = sharedPreferences.getInt("expId",-1);
		this.sessionId = sharedPreferences.getInt("sessionId",-1);
		this.line_chosen = sharedPreferences.getInt("line_chosen",-1);

		String[] lineNames = sharedPreferences.getString("lines", "").split(",");
		Line[] lines = new Line[lineNames.length];

		for (int i = 0; i < lines.length; i++) {
			Log.d(TAG, "lineNames[i]: " + lineNames[i]);
			lines[i] = new Line(context.getSharedPreferences(lineNames[i], Context.MODE_PRIVATE));
		}

		this.lines = lines;
		
	}
	
	/** saves instance to Shared Preferences */
	public void save(Context context) {
		
		String name = makeName(expId, sessionId);
		String linesString = "";
		
		for (Line line : lines){
			linesString = linesString + Line.LINE_PREFIX + expId + "_" + sessionId + "_" + line.getLineId() + ",";
			line.save(context);
		}

		SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putInt("expId", expId);
		editor.putInt("sessionId", sessionId);
		editor.putInt("line_chosen", line_chosen);
		editor.putString("lines",linesString);
		editor.commit();
	}
	
	//* deletes all SharedPreferences associated with Session, except those associated with its results */	
	public void deleteSharedPreferences(Context context) {
		
		for (Line line : lines){
			line.deleteSharedPreferences(context);
		}

		new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + makeName(expId, sessionId) + ".xml").delete();		
		
	}
	
	/** returns name of SharedPreferences */
	public static String makeName(int expId, int sessionId) {
		return(SESSION_PREFIX + expId + "_" + sessionId);
	}
	
}