package edu.berkeley.xlab.xlab_objects;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import edu.berkeley.xlab.ExpActivityBudgetLine;
import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.util.Utils;

public class ExperimentBudgetLine extends ExperimentAbstract {

	/** TAG is an identifier for the log. */
	public static final String TAG = "X-Lab - XLabBudgetLineExp";
	
	private boolean probabilistic; public boolean getProbabilistic() {return probabilistic;}
	private float prob_x; public float getProb_x() {return prob_x;}
	
	private String x_label; public String getX_label() {return x_label;}
	private String x_units; public String getX_units() {return x_units;}
	private float x_max; public double getX_max() {return x_max;}
	private float x_min; public double getX_min() {return x_min;}
    
	private String y_label; public String getY_label() {return y_label;}
	private String y_units; public String getY_units() {return y_units;}
	private float y_max; public double getY_max() {return y_max;}
	private float y_min; public double getY_min() {return y_min;}
	
	private String currency; public String getCurrency() {return currency;}

	private SessionBudgetLine[] sessions; public SessionBudgetLine[] getSessions() {return sessions;} public SessionBudgetLine getSession(int id) {return sessions[id];}

	private int currSession; public int getCurrSession() {return currSession;} 
	public void nextSession(Context context) {
		currSession++;
		if (currSession == sessions.length) {
			Log.d(TAG,"Timing out");
			saveState(context, progress);
			this.makeDone(context);
		} else {
			saveState(context, progress);
		}
	}	
	
	private int currRound; public int getCurrRound() {return currRound;}
	private int progress; public int getProgress() {return progress;}
	
	/**
	 * Constructor based on JSON from server. Preferred (and in the future, hopefully exclusive) way to pull info from server
	 * @param context Application context
	 * @param exp JSON constructed from JSON-formatted string from server
	 */
	public ExperimentBudgetLine(Context context, JSONObject json) throws NumberFormatException, JSONException {

		Log.d(TAG,"In XLabBudgetLine JSON constructor");

		this.identify();

		JSONObject info;
		JSONObject geofense;
		Random r = new Random();

		int numSessions;
		
		int numRounds;
		int round_chosen;
		
		char winner;
		float x_int;
		float y_int;

		try {
						
			info = json.getJSONObject("budget_line_info");
			geofense = json.getJSONObject("geofence");
			
			this.done = false;
			this.expId = json.getInt("id"); this.title = info.getString("title");
			this.location = geofense.getString("title"); this.lat = (float) geofense.getDouble("lat"); this.lon = (float) geofense.getDouble("lon"); this.radius = geofense.getInt("radius");
			this.probabilistic = info.getBoolean("probabilistic"); this.prob_x = (float) info.getDouble("prob_x");
			this.x_label = info.getString("x_label"); this.x_units = info.getString("x_units"); this.x_max = (float) info.getDouble("x_max"); this.x_min = (float) info.getDouble("x_min");
			this.y_label = info.getString("y_label"); this.y_units = info.getString("y_units"); this.y_max = (float) info.getDouble("y_max"); this.y_min = (float) info.getDouble("y_min");
			this.timer_status = json.getInt("timer_status");
			this.currency = info.getString("currency");
			
			if (this.timer_status != Constants.TIMER_STATUS_NONE) {
				this.timer_type = json.getJSONObject("timer").getInt("timer_type");
			}
						
			numSessions = info.getInt("number_sessions");
			numRounds = info.getInt("lines_per_session");
			
			SessionBudgetLine[] sessions = new SessionBudgetLine[numSessions];
			
			Log.d(TAG,"Beginning of outer for");
			
			for (int i = 0; i < numSessions; i++) {

				RoundBudgetLine[] rounds = new RoundBudgetLine[numRounds];
				round_chosen = r.nextInt(numRounds);
				Log.d(TAG,"round_chosen: " + round_chosen);
				
				for (int j = 0; j < numRounds; j++) {
					
					winner = (r.nextFloat() < prob_x) ? 'x' : 'y';
					x_int = x_min + r.nextFloat() * (x_max - x_min);
					y_int = y_min + r.nextFloat() * (y_max - y_min);
					
					rounds[j] = new RoundBudgetLine(context, expId, i, j, x_int, y_int, winner);

				}
				
				sessions[i] = new SessionBudgetLine(context, expId, i, round_chosen, rounds);
			}
			
			Log.d(TAG,"End of outer for");
			
			this.sessions = sessions;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		this.save(context);

	}	
	
	public ExperimentBudgetLine(Context context, SharedPreferences sharedPreferences) {
		
		Log.d(TAG,"In XLabBudgetLineExp SharedPreferences constructor");
		
		this.identify();
		this.numSkipped = 0;
		this.done = sharedPreferences.getBoolean("done", false);
		this.expId = sharedPreferences.getInt("expId",-1);
		this.title = sharedPreferences.getString("title","");
		this.location = sharedPreferences.getString("location","");
		this.lat = sharedPreferences.getFloat("lat",(float) -1);
		this.lon = sharedPreferences.getFloat("lon",(float) -1);
		this.radius = sharedPreferences.getInt("radius", -1);
		this.probabilistic = sharedPreferences.getBoolean("probabilistic", false);
		this.prob_x = sharedPreferences.getFloat("prob_x", (float) -1);
		this.x_label = sharedPreferences.getString("x_label", "");
		this.x_units = sharedPreferences.getString("x_units", "");
		this.x_max = sharedPreferences.getFloat("x_max", (float) -1); 
		this.x_min = sharedPreferences.getFloat("x_min", (float) -1); 
		this.y_label = sharedPreferences.getString("y_label", "");
		this.y_units = sharedPreferences.getString("y_units", "");
		this.y_max = sharedPreferences.getFloat("y_max", (float) -1); 
		this.y_min = sharedPreferences.getFloat("y_min", (float) -1);
		this.timer_status = sharedPreferences.getInt("timer_status", -1);
		this.currSession = sharedPreferences.getInt("currSession", -1);
		this.currRound = sharedPreferences.getInt("currRound", -1);
		this.progress = sharedPreferences.getInt("progress", -1);
		this.numSkipped = sharedPreferences.getInt("numSkipped", -1);
		this.currency = sharedPreferences.getString("currency", "-");
		
		Log.d(TAG,"The x min value is  " + x_min);
		Log.d(TAG,"The x max value is  " + x_max);
		Log.d(TAG,"The y min value is  " + y_min);
		Log.d(TAG,"The y max value is  " + y_max);
		
		if (this.timer_status != Constants.TIMER_STATUS_NONE) {
			this.timer_type = sharedPreferences.getInt("timer_type", -1);
		}
					
		String[] sessionNames = sharedPreferences.getString("sessions", "").split(",");
		SessionBudgetLine[] sessions = new SessionBudgetLine[sessionNames.length];
		for (int i = 0; i < sessions.length; i++) {
			Log.d(TAG,"Instantiating " + sessionNames[i]);
			sessions[i] = new SessionBudgetLine(context, context.getSharedPreferences(sessionNames[i], Context.MODE_PRIVATE));
		}

		this.sessions = sessions;
		
	}
	
	/** method of instantiations common to all constructors */
	private void identify() {
		this.typeId = Constants.XLAB_BL_EXP;
		this.activity = ExpActivityBudgetLine.class;		
	}

	/** sets currRound and currSession to zero */
	private void initialize() {
		this.progress = -1;
		this.currSession = 0;
		this.currRound = 0;
	}
	
	/**
	 *  Iterates to next round. Will set currRound to 0 and iterate to next session if necessary.
	 *  Will set done to true if all sessions have been completed.
	 */
	public void nextRound(Context context) {
		
		currRound = (currRound + 1) % sessions[0].getRounds().length; 
		Log.d(TAG,"Current Round: " + currRound);
		if (currRound == 0) {
			this.nextSession(context);
		} else {
			saveState(context, progress);
		}
		
	}
	
	@Override
	protected void save(Context context) {
		
		Log.d(TAG, "In save of " + title);

		if (!Utils.checkIfSaved(context, getSPName(), ExperimentAbstract.EXP_LIST)) {
			
			Log.d(TAG, "Saving " + title);
			
			String sessionsString = "";
			
			this.initialize();
			
			for (SessionBudgetLine session : sessions) {
				sessionsString = sessionsString + SessionBudgetLine.SESSION_PREFIX + expId + "_" + session.getSessionId() + ",";
				session.save(context);
			}

			SharedPreferences sharedPreferences = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			
			editor.putInt("typeId", typeId);
			editor.putInt("expId", expId);
			editor.putBoolean("done", done);
			editor.putString("title", title);
			editor.putString("location", location);
			editor.putFloat("lat", lat);
			editor.putFloat("lon", lon); 
			editor.putInt("radius", radius);
			editor.putBoolean("probabilistic", probabilistic);
			editor.putFloat("prob_x", prob_x);
			editor.putString("x_label", x_label);
			editor.putString("x_units", x_units);
			editor.putFloat("x_max", x_max); 
			editor.putFloat("x_min", x_min);
			editor.putString("y_label", y_label);
			editor.putString("y_units", y_units);
			editor.putFloat("y_max", y_max); 
			editor.putFloat("y_min", y_min);
			editor.putInt("timer_status", timer_status);
			editor.putInt("currSession", currSession);
			editor.putInt("currRound", currRound);
			editor.putString("sessions",sessionsString);
			editor.putInt("currSession", currSession);
			editor.putInt("progress", progress);
			editor.putInt("numSkipped", numSkipped);
			editor.putString("currency", String.valueOf(currency));
			editor.commit();
			
			appendList(context, EXP_LIST);

		}
				
	}
	
	/**
	 * Only updates necessary field in mid-response
	 * @param context application context
	 * @param progress progress of slider
	 */
	public void saveState(Context context, int progress) {
		
		this.progress = progress;
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		Log.d(TAG,"Saving currSession as: " + editor.putInt("currSession", currSession));
		editor.putInt("currSession", currSession);
		editor.putInt("currRound", currRound);
		editor.putInt("progress", progress);
		editor.commit();
	
	}
	
	@Override
	public void clearSharedPreferences(Context context) {

		//clear child SharedPreferences
		for (SessionBudgetLine session : this.sessions){
			session.clearSharedPreferences(context);
		}
		
		//clear SharedPreferences
		Log.d(TAG,"File to be cleared: " + this.getSPName());
		context.getSharedPreferences(this.getSPName(), Context.MODE_PRIVATE).edit().clear();
		
		//remove from SharedPrefrences list of SharedPreferences
		SharedPreferences sharedPreferencesList = context.getSharedPreferences(ExperimentAbstract.EXP_LIST, Context.MODE_PRIVATE);
		SharedPreferences.Editor listEditor = sharedPreferencesList.edit();

		String[] halfList = sharedPreferencesList.getString("SharedPreferences","").split(this.getSPName() + ",");
		
		if (halfList.length == 0) {
			Log.d(TAG,"Empty EXP_LIST");
			listEditor.putString("SharedPreferences", "");			
		} else if (halfList.length == 1) {
			Log.d(TAG,"EXP_LIST is " + halfList[0]);
			listEditor.putString("SharedPreferences", halfList[0]);						
		} else {
			Log.d(TAG,"EXP_LIST is " + halfList[0] + halfList[1]);
			listEditor.putString("SharedPreferences", halfList[0] + halfList[1]);
		}
		
		listEditor.commit();

	}
	
}