package edu.berkeley.xlab.xlab_objects;

public abstract class Experiment extends XLabSuperObject {
	
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
	
	//** sets done to true */
	public void makeDone() {
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
}
