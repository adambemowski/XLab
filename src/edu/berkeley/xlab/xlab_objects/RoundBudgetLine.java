package edu.berkeley.xlab.xlab_objects;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class RoundBudgetLine {
	
	/** Filename prefix for persistent memory of session*/
	public static final String ROUND_PREFIX = "Round_Budget_Line_";

	private int roundId; public int getRoundId() {return roundId;}
	private int sessionId; public int getSessionId() {return sessionId;}
	private int expId; public int getExpId() {return expId;}
	private float x_int; public float getX_int() {return x_int;}
	private float y_int; public float getY_int() {return y_int;}
	private char winner; public char getWinner() {return winner;}
	
	public RoundBudgetLine (Context context, int expId, int sessionId, int roundId, float x_int, float y_int, char winner) {

		this.expId = expId;
		this.sessionId = sessionId;
		this.roundId = roundId;
		this.x_int = x_int;
		this.y_int = y_int;
		this.winner = winner;//for risk-reward studies
		
	}
		
	public RoundBudgetLine (Context context, JSONObject json) throws JSONException {
		
		this.expId = json.getInt("id");
		this.sessionId = json.getInt("sessionId");
		this.roundId = json.getInt("lineId");
		this.x_int = (float) json.getDouble("x_int");
		this.y_int = (float) json.getDouble("y_int");
		this.winner = json.getString("winner").charAt(0);//for risk-reward studies

	}
	
	public RoundBudgetLine(SharedPreferences sharedPreferences) {
		
		this.expId = sharedPreferences.getInt("expId",-1);
		this.sessionId = sharedPreferences.getInt("sessionId",-1);
		this.roundId = sharedPreferences.getInt("roundId",-1);
		this.x_int = sharedPreferences.getFloat("x_int",(float) -1);
		this.y_int = sharedPreferences.getFloat("y_int", (float) -1);
		this.winner = sharedPreferences.getString("winner", "").charAt(0);//for risk-reward studies
		
		Log.d("Round Debug", "x_int is " + x_int);
		Log.d("Round Debug", "y_int is " + y_int);
		
	}
	
	/** saves instance to Shared Preferences */
	public void save(Context context) {
		
		String name = makeSPName(expId, sessionId, roundId);
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putInt("expId", expId);
		editor.putInt("sessionId", sessionId);
		editor.putInt("roundId", roundId);
		editor.putFloat("x_int", x_int);
		editor.putFloat("y_int", y_int);
		editor.putString("winner", String.valueOf(winner));
		editor.commit();
		
	}
	
	//* clears all SharedPreferences associated with Session, except those associated with its results */	
	public void clearSharedPreferences(Context context) {
		
		//new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + makeSPName(expId, sessionId, roundId) + ".xml").delete();		
		context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE).edit().clear();
		
	}
	
	/** returns name of SharedPreferences */
	public String getSPName() {
		return(ROUND_PREFIX + expId + "_" + sessionId + "_" + roundId);
	}

	/** returns name of SharedPreferences */
	public static String makeSPName(int expId, int sessionId, int roundId) {
		return(ROUND_PREFIX + expId + "_" + sessionId + "_" + roundId);
	}

}
