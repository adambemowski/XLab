package edu.berkeley.xlab.xlab_objects;

import java.io.File;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import edu.berkeley.xlab.ExpActivityBudgetLine;
import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.util.Utils;

/*
 * Defines budget-line experiment
 * 
 * @author Daniel Vizzini
 * 
 * The following is an example of a string array that may be received, to illustrate how the objects in the package work:
 * 
 * For each experiment (Java object XLabBudgetLineExperiement):
 * 	id (integer), title, lat, lon, radius, probabilistic (true if you get either X or Y, false if you get both),  prob_x (probability of getting X, only applicable if probabilistic is true), x_label (label of x axis), x_units (units of x axis), x_max (maximum x intercept), x_min (minimum x intercept), y_label (label of y axis), y_uints (units of y axis), y_max (maximum y intercept), y_min (minimum y intercept), 
 * 
 * Then, for each experiment, a number of sessions for the set of budget lines subjects get at a given time (Java object Session)
 * 	"session_parser" (for parsing), id (1 through number of Sessions), line_chosen (which line in the session will actually dictate rewards),
 * 
 * Then, for each session,  a number of lines (Java object Line):
 * 	"line_parser" (for parsing), id (1 through number of Lines), x_int (x-intercept of line), y_int (y-intercept of line), winner ("X" if only X is rewarded, "Y" otherwise, only applicable if probabilistic is true)
 * 
 * Example encompassing a two-session probabilistic experiment in which line and a three-session non-probabilistic experiment (note it will come as one continuous string, with a newline between the experiments):
 * 
 * 14,Muscovite Risk/Reward,55.75,37.70,200,1,0.5,Reward if X chosen,Rubles,1500,750,Reward if Y chosen,Rubles,1500,750,
 * 		session_parser,1,3,
 * 			line_parser,1,800,1000,X,
 * 			line_parser,2,1350,850,X,
 * 			line_parser,3,1150,1250,Y,
 * 			line_parser,4,1150,1250,Y,
 * 		session_parser,2,2,
 * 			line_parser,1,1100,1000,Y,
 * 			line_parser,2,750,1150,X,
 * 			line_parser,3,1450,850,X,
 * 			line_parser,4,850,1050,Y,
 * 16,Kamchatkan Diet Selector,53.01,158.65,200,0,0.5,Regional Fried Dough,Rubles,1000,500,Pickled Produce,Rubles,1000,500,
 * 		session_parser,1,1,
 * 			line_parser,1,800,700,X,
 * 			line_parser,2,750,850,X,
 * 			line_parser,3,550,500,Y,
 * 			line_parser,4,600,750,Y,
 * 		session_parser,2,4,
 * 			line_parser,1,500,600,Y,
 * 			line_parser,2,750,650,X,
 * 			line_parser,3,650,850,X,
 * 			line_parser,4,850,950,Y,
 * 		session_parser,3,1,
 * 			line_parser,1,600,600,Y,
 * 			line_parser,2,650,650,X,
 * 			line_parser,3,650,950,X,
 * 			line_parser,4,750,950,Y,"
 */

public class ExperimentBudgetLine extends Experiment {

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

	private Session[] sessions; public Session[] getSessions() {return sessions;} public Session getSession(int id) {return sessions[id];}

	private int currSession; public int getCurrSession() {return currSession;} 
	public void nextSession(Context context) {
		currSession++;
		if (currSession == sessions.length) {
			saveState(context, progress);
			this.makeDone(context);
		} else {
			saveState(context, progress);
		}
	}	
	
	private int currLine; public int getCurrLine() {return currLine;}
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
		
		int numLines;
		int line_chosen;
		
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
			
			if (this.timer_status != Constants.TIMER_STATUS_NONE) {
				this.timer_type = json.getJSONObject("timer").getInt("timer_type");
			}
						
			numSessions = info.getInt("number_sessions");
			numLines = info.getInt("lines_per_session");
			
			Session[] sessions = new Session[numSessions];
			
			Log.d(TAG,"Beginning of outer for");
			
			for (int i = 0; i < numSessions; i++) {

				Line[] lines = new Line[numLines];
				line_chosen = r.nextInt(numLines);
				Log.d(TAG,"line_chosen: " + line_chosen);
				
				for (int j = 0; j < numLines; j++) {
					
					winner = (r.nextFloat() < prob_x) ? 'x' : 'y';
					x_int = x_min + r.nextFloat() * (x_max - x_min);
					y_int = y_min + r.nextFloat() * (y_max - y_min);
					
					lines[j] = new Line(context, expId, i, j, x_int, y_int, winner);

				}
				
				sessions[i] = new Session(context, expId, i, line_chosen, lines);
			}
			
			Log.d(TAG,"End of outer for");
			
			this.sessions = sessions;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		this.save(context);

	}	
	
	public ExperimentBudgetLine(Context context, SharedPreferences sharedPreferences) {
		this.numSkipped = 0;
		constructFromSharedPreferences(context, sharedPreferences);
	}
	
	public ExperimentBudgetLine(Context context, SharedPreferences sharedPreferences, int numSkipped) {
		this.numSkipped = numSkipped;
		constructFromSharedPreferences(context, sharedPreferences);
	}
	
	private void constructFromSharedPreferences(Context context, SharedPreferences sharedPreferences) {
		
		Log.d(TAG,"In XLabBudgetLineExp SharedPreferences constructor");
		
		this.identify();
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
		this.currLine = sharedPreferences.getInt("currLine", -1);
		this.progress = sharedPreferences.getInt("progress", -1);
		this.numSkipped = sharedPreferences.getInt("numSkipped", -1);
		
		Log.d(TAG,"The x min value is  " + x_min);
		Log.d(TAG,"The x max value is  " + x_max);
		Log.d(TAG,"The y min value is  " + y_min);
		Log.d(TAG,"The y max value is  " + y_max);
		
		
		if (this.timer_status != Constants.TIMER_STATUS_NONE) {
			this.timer_type = sharedPreferences.getInt("timer_type", -1);
		}
					
		String[] sessionNames = sharedPreferences.getString("sessions", "").split(",");
		Session[] sessions = new Session[sessionNames.length];
		for (int i = 0; i < sessions.length; i++) {
			Log.d(TAG,"Instantiating " + sessionNames[i]);
			sessions[i] = new Session(context, context.getSharedPreferences(sessionNames[i], Context.MODE_PRIVATE));
		}

		this.sessions = sessions;		

	}
	
	/** method of instantiations common to all constructors */
	private void identify() {
		this.typeId = Constants.XLAB_BL_EXP;
		this.activity = ExpActivityBudgetLine.class;		
	}

	/** sets currLine and currSession to zero */
	private void initialize() {
		this.progress = -1;
		this.currSession = 0;
		this.currLine = 0;
	}
	
	/**
	 *  Iterates to next line. Will set currLine to 0 and iterate to next session if necessary.
	 *  Will set done to true if all sessions have been completed.
	 */
	public void nextLine(Context context) {
		
		currLine = (currLine + 1) % sessions[this.getCurrSession()].getLines().length; 
		Log.d(TAG,"Current Line: " + currLine);
		if (currLine == 0) {
			this.nextSession(context);
		} else {
			saveState(context, progress);
		}
		
	}
	
	@Override
	protected void save(Context context) {
		
		Log.d(TAG, "In save of " + title);

		if (!Utils.checkIfSaved(context, getSPName(), Experiment.EXP_LIST)) {
			
			Log.d(TAG, "Saving " + title);
			
			String sessionsString = "";
			
			this.initialize();
			
			for (Session session : sessions) {
				sessionsString = sessionsString + Session.SESSION_PREFIX + expId + "_" + session.getSessionId() + ",";
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
			editor.putInt("currLine", currLine);
			editor.putString("sessions",sessionsString);
			editor.putInt("currSession", currSession);
			editor.putInt("currLine", currLine);
			editor.putInt("progress", progress);
			editor.putInt("numSkipped", numSkipped);
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
		
		editor.putInt("currSession", currSession);
		editor.putInt("currLine", currLine);
		editor.putInt("progress", progress);
		editor.commit();
	
	}
	
	@Override
	public void deleteSharedPreferences(Context context) {

		//delete child SharedPreferences
		for (Session session : this.sessions){
			session.deleteSharedPreferences(context);
		}
		
		//delete SharedPreferences
		Log.d(TAG,"File to be deleted: " + "/data/data/" + context.getPackageName() + "/shared_prefs/" + this.getSPName() + ".xml");
		if (new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + this.getSPName() + ".xml").delete()) {
			Log.d(TAG,"File apparently deleted");
		} else {
			Log.d(TAG,"File apparently did not delete");
		}
		
		//remove from SharedPrefrences list of SharedPreferences
		SharedPreferences sharedPreferencesList = context.getSharedPreferences(Experiment.EXP_LIST, Context.MODE_PRIVATE);
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