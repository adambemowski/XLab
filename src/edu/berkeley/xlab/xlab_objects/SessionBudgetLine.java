package edu.berkeley.xlab.xlab_objects;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionBudgetLine {
	
	/** TAG is an identifier for the log. */
	public static final String TAG = "X-Lab - Session";
		
	/** Filename prefix for persistent memory of session*/
	public static final String SESSION_PREFIX = "Session_Budget_Line_";

	private int expId;
	public int getExpId() {return expId;}
	
	private int sessionId;
	public int getSessionId() {return sessionId;}
	
	private int round_chosen;
	public int getRound_chosen() {return round_chosen;}
	
	private RoundBudgetLine[] rounds;
	public RoundBudgetLine[] getRounds() {return rounds;}
	public RoundBudgetLine getRound(int id) {return rounds[id];}

	public SessionBudgetLine(Context context, int expId, int sessionId, int round_chosen, RoundBudgetLine[] rounds) {
		
		this.expId = expId;
		this.sessionId = sessionId;
		this.round_chosen = round_chosen;
		this.rounds = rounds;
		
	}
	
	public SessionBudgetLine (Context context, JSONObject json) throws JSONException {

		JSONArray roundsJSON = json.getJSONArray("lines");
		RoundBudgetLine[] rounds = new RoundBudgetLine[roundsJSON.length()];
		Random r = new Random();
		
		for (int i = 0; i < roundsJSON.length(); i++) {
			rounds[i] = new RoundBudgetLine(context, roundsJSON.getJSONObject(i));
		}

		this.expId = json.getInt("expId");
		this.sessionId = json.getInt("sessionId");
		this.round_chosen = r.nextInt(roundsJSON.length());
		this.rounds = rounds;
		
	}
	
	public SessionBudgetLine(Context context, SharedPreferences sharedPreferences) {
				
		this.expId = sharedPreferences.getInt("expId",-1);
		this.sessionId = sharedPreferences.getInt("sessionId",-1);
		this.round_chosen = sharedPreferences.getInt("round_chosen",-1);

		String[] roundNames = sharedPreferences.getString("rounds", "").split(",");
		RoundBudgetLine[] rounds = new RoundBudgetLine[roundNames.length];

		for (int i = 0; i < rounds.length; i++) {
			Log.d(TAG, "roundNames[i]: " + roundNames[i]);
			rounds[i] = new RoundBudgetLine(context.getSharedPreferences(roundNames[i], Context.MODE_PRIVATE));
		}

		this.rounds = rounds;
		
	}
	
	/** saves instance to Shared Preferences */
	public void save(Context context) {
		
		String name = getSPName();
		String roundsString = "";
		
		for (RoundBudgetLine round : rounds){
			roundsString = roundsString + RoundBudgetLine.ROUND_PREFIX + expId + "_" + sessionId + "_" + round.getRoundId() + ",";
			round.save(context);
		}

		SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putInt("expId", expId);
		editor.putInt("sessionId", sessionId);
		editor.putInt("round_chosen", round_chosen);
		editor.putString("rounds",roundsString);
		editor.commit();
	}
	
	//* clears all SharedPreferences associated with Session, except those associated with its results */	
	public void clearSharedPreferences(Context context) {
		
		for (RoundBudgetLine round: rounds){
			round.clearSharedPreferences(context);
		}

		//new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + makeName(expId, sessionId) + ".xml").delete();		
		context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE).edit().clear();
	}
	
	/** returns name of SharedPreferences */
	public static String makeSPName(int expId, int sessionId) {
		return(SESSION_PREFIX + expId + "_" + sessionId);
	}
	
	/** returns name of SharedPreferences */
	public String getSPName() {
		return(SESSION_PREFIX + expId + "_" + sessionId);
	}
}