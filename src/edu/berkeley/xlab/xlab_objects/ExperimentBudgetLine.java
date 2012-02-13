package edu.berkeley.xlab.xlab_objects;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import edu.berkeley.xlab.ExpActivityBudgetLine;
import edu.berkeley.xlab.constants.Configuration;

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
			this.makeDone();
		}
	}	
	
	private int currLine; public int getCurrLine() {return currLine;}
	private int progress; public int getProgress() {return progress;}
	
	/**
	 *  Iterates to next line. Will set currLine to 0 and iterate to next session if necessary.
	 *  Will set done to true if all sessions have been completed.
	 */
	public void nextLine(Context context) {
		
		currLine = (currLine + 1) % sessions[this.getCurrSession()].getLines().length; 
		Log.d(TAG,"Current Line: "+currLine);
		if (currLine == 0) {
			this.nextSession(context);
		}
		
	}
	
	public ExperimentBudgetLine(Context context, JSONObject json) throws NumberFormatException, JSONException {

		JSONArray sessionsJSON = json.getJSONArray("sessions");
		Session[] sessions = new Session[sessionsJSON.length()];
		
		for (int i = 0; i < sessionsJSON.length(); i++) {
			sessions[i] = new Session(context, sessionsJSON.getJSONObject(i));
		}

		this.identify();
		this.expId = json.getInt("expId"); this.title = json.getString("title");
		this.location = json.getString("location"); this.lat = (float) json.getDouble("lat"); this.lon = (float) json.getDouble("lon"); this.radius = json.getInt("radius");
		this.probabilistic = json.getBoolean("probabilistic"); this.prob_x = (float) json.getDouble("prob_x");
		this.x_label = json.getString("x_label"); this.x_units = json.getString("x_units"); this.x_max = (float) json.getDouble("x_max"); this.x_min = (float) json.getDouble("x_min");
		this.y_label = json.getString("y_label"); this.y_units = json.getString("y_units"); this.y_max = (float) json.getDouble("y_max"); this.y_min = (float) json.getDouble("y_min");
		this.sessions = sessions;
		this.initialize();
	}
	
	public ExperimentBudgetLine(Context context, String exp) {
				
		String[] ses = exp.split("session_parser,");
	    Session[] sessions = new Session[ses.length - 1];
		
		String[] header = ses[0].split(",");
		
		int expId = Integer.valueOf(header[0]);
		String title = header[1];
		String location = header[2];
		float lat = Float.valueOf(header[3]);
		float lon = Float.valueOf(header[4]);
		int radius = Integer.valueOf(header[5]);
		
		boolean probabilistic = (header[6].equalsIgnoreCase("1") ? true : false);
		float prob_x = Float.valueOf(header[7]);
		
	    String x_label = header[8];
	    String x_units = header[9];
	    float x_max = Float.valueOf(header[10]);
	    float x_min = Float.valueOf(header[11]);
	    
	    String y_label = header[12];
	    String y_units = header[13];
	    float y_max = Float.valueOf(header[14]);
	    float y_min = Float.valueOf(header[15]);
	    		    
	    for (int i = 0; i < sessions.length; i++) {
	    	
	    	String[] lines = ses[i+1].split("line_parser,");//line as in budget line, not line of text
		    Line[] lineInput = new Line[lines.length - 1];
		    Log.d(TAG,"lineInput array has length " + lineInput.length);
		    header = lines[0].split(",");
		    int sessionId = Integer.valueOf(header[0]);
		    int line_chosen = Integer.valueOf(header[1]);
		    
		    for (int j = 0; j < lineInput.length; j++) {
		    	String[] parts = lines[j+1].split(",");
		    	lineInput[j] = new Line(context, expId, sessionId, Integer.valueOf(parts[0]),Float.valueOf(parts[1]),Float.valueOf(parts[2]),parts[3].charAt(0));
		    }
		    
		    sessions[i] = new Session(context, expId, sessionId, line_chosen, lineInput);
			
	    }
	    
		this.identify();
		this.expId = expId; this.title = title;
		this.location = location; this.lat = lat; this.lon = lon; this.radius = radius;
		this.probabilistic = probabilistic; this.prob_x = prob_x;
		this.x_label = x_label; this.x_units = x_units; this.x_max = x_max; this.x_min = x_min;
		this.y_label = y_label; this.y_units = y_units; this.y_max = y_max; this.y_min = y_min;
		this.sessions = sessions;
		this.initialize();
	}
	
	public ExperimentBudgetLine(Context context, SharedPreferences sharedPreferences) {
		
		Log.d(TAG,"In XLabBudgetLineExp SharedPreferences constructor");
		
		this.identify();
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
		this.currSession = sharedPreferences.getInt("currSession", -1);
		this.currLine = sharedPreferences.getInt("currLine", -1);
		this.progress = sharedPreferences.getInt("progress", -1);
		
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
		this.typeId = Configuration.XLAB_BL_EXP;
		this.activity = ExpActivityBudgetLine.class;		
	}

	/** sets currLine and currSession to zero */
	private void initialize() {
		this.progress = -1;
		this.currSession = 0;
		this.currLine = 0;
	}
	
	@Override
	public void save(Context context) {
		
		Log.d(TAG, "Saving " + title);
		
		String sessionsString = "";
		
		for (Session session : sessions){
			sessionsString = sessionsString + Session.SESSION_PREFIX + expId + "_" + session.getSessionId() + ",";
			session.save(context);
		}

		SharedPreferences sharedPreferences = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putInt("typeId", typeId);
		editor.putInt("expId", expId);
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
		editor.putInt("currSession", currSession);
		editor.putInt("currLine", currLine);
		editor.putString("sessions",sessionsString);
		editor.putInt("currSession", currSession);
		editor.putInt("currLine", currLine);
		editor.putInt("progress", progress);
		editor.commit();
		
		appendList(context, EXP_LIST);
		
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
	
}