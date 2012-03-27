package edu.berkeley.xlab.xlab_objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public abstract class ExperimentAbstract extends XLabSuperObject {
	
	/** Filename of list of SharedPreferences of responses given*/
	public static final String EXP_LIST = "experiment_shared_preference_list";
	
	/** Filename prefix for persistent memory of Experiment*/
	public static final String EXP_PREFIX = "Experiment_";
	
	protected String location; public String getLocation() {return location;}
	protected float lat;	public float getLat() {return lat;}
	protected float lon;	public float getLon() {return lon;}
	protected int radius;	public int getRadius() {return radius;}
	protected Class<?> activity; public Class<?> getActivity() {return activity;}
	protected String title; public String getTitle() {return title;}
	protected boolean done; public boolean isDone() {return this.done;}
	
	/** number of units skipped by the timer */
	protected int numSkipped;
	
	/** @return the number of units skipped by the timer */
	public int getNumSkipped() {
		return numSkipped;
	}

	/**
	 * Add specified int to numSkipped
	 * @param context Application context
	 * @param numSkipped int to be added
	 */
	public void addNumSkipped(Context context, int numSkipped) {
		this.numSkipped = numSkipped;
		SharedPreferences.Editor editor = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE).edit();
		editor.putInt("numSkipped", numSkipped);
		editor.commit();
	}
	
	/**
	 * Sets numSkipped to 0
	 * @param context Application context
	 */
	public void resetNumSkipped(Context context) {
		this.numSkipped = 0;
		SharedPreferences.Editor editor = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE).edit();
		editor.putInt("numSkipped", numSkipped);
		editor.commit();
	}
	
	/** 0 if no time restrictions, 1 if notification is given but subject can answer at any time, 2 if notification is given and subject must wait for it */
	protected int timer_status;
	public int getTimer_status() {
		return timer_status;
	}

	/** Static or Dynamic Timer */
	protected int timer_type;
	public int getTimer_type() {
		return timer_type;
	}
	
	//** sets done to true */
	public void makeDone(Context context) {
		context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE).edit().putString("done","true");
		this.done = true;
	}
	
	@Override
	public String getSPName() {
		return (EXP_PREFIX + expId);
	}

	/**
	 * returns the name of the SharedPreferences that persistently store data for a specified experiment
	 * @param expId unique id of experiment for which the experiment returns the name of the SharedPreferences
	 * @return name of SharedPreferences
	 */
	public static String makeSPName(int expId) {
		return (EXP_PREFIX + expId);
	}
	
	/**
	 * Clears shared preferences associated with the Experiment
	 * @param context Application context
	 */
	public void clearSharedPreferences(Context context) {

		//clear SharedPreferences
		Log.d(TAG,"File to be cleared: " + this.getSPName());
		//new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + this.getSPName() + ".xml").delete();
		context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE).edit().clear();
		
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