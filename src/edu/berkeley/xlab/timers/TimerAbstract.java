package edu.berkeley.xlab.timers;

import java.util.GregorianCalendar;

import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 
 * Abstract class shared by all timers
 *
 */
public abstract class TimerAbstract {
	
	/**
	 * For log
	 */
	public String TAG = "TimerSuperClass";
	
	/**
	 * Application context
	 */
	protected Context context;
	
	/**
	 * Experiment associated with instance of TimerSuperClass
	 */
	protected ExperimentAbstract exp;
	
	/**
	 * Activity that instantiates TimerSuperClass object
	 */
	protected Activity activity;
	
	/**
	 * fetches exp object associated with experiment
	 * @return Experiment exp
	 */
	public ExperimentAbstract getExp() {
		return exp;
	}
	/**
	 * True if timer is active on a day of the week, false otherwise.
	 * Index 0 is Sunday
	 */
	protected boolean[] dayEligibility;
	
	/**
	 * First date that timer is active, if eligible, note that if timerStatus is 2, subjects cannot receive new experiment segments before this date
	 */
	protected GregorianCalendar startDate;
	
	/**
	 * Last date that timer is active, if eligible, note that if timerStatus is 2, subjects cannot receive new experiment segments after this date
	 */
	protected GregorianCalendar endDate;

	/**
	 * First eligible date on or after startDate
	 * First date that timer is active, note that if timerStatus is 2, subjects cannot receive new experiment segments before this date
	 */
	protected GregorianCalendar firstEligibleDate;
	
	/**
	 * Last eligible date on or before endDate
	 * Last date that timer is active, note that if timerStatus is 2, subjects cannot receive new experiment segments after this date
	 */
	protected GregorianCalendar lastEligibleDate;

	/**
	 * First time of day, in minutes since Midnight, that the timer is active. Note that if timerStatus is 2, subjects cannot receive new experiment segments before this time of day
	 */
	protected int startTime;
	
	/**
	 * Last time of day, in minutes since Midnight, that the timer is active. Note that if timerStatus is 2, subjects cannot receive new experiment segments after this time of day
	 */
	protected int endTime;	
	
	/**
	 * Experiment next unanswered experiment segment
	 */
	protected long nextTime;
	
	public long getNextTime() {
		return nextTime;
	}
	
	public TimerAbstract (Context context, ExperimentAbstract exp, long nextTime, boolean[] dayEligibility) {
		this.dayEligibility = dayEligibility;
		this.context = context;
		this.exp = exp;
		this.nextTime = nextTime;
		TAG = TAG + " " + exp.getExpId();
	}
	
	public TimerAbstract (Context context, ExperimentAbstract exp, long nextTime) {
		this.context = context;
		this.exp = exp;
		this.nextTime = nextTime;
		TAG = TAG + " " + exp.getExpId();
	}
	
	public TimerAbstract (Context context, ExperimentAbstract exp, boolean[] dayEligibility) {
		this.dayEligibility = dayEligibility;
		this.context = context;
		this.exp = exp;
		TAG = TAG + " " + exp.getExpId();
	}
	
	public TimerAbstract (Context context, ExperimentAbstract exp) {
		this.context = context;
		this.exp = exp;
		TAG = TAG + " " + exp.getExpId();
	}
	
	
	/**
	 * Run after experiment segment is completed
	 */
	public abstract String onFinish();
	
	
	/**
	 * 
	 * @return message to be displayed at end of experiment segment
	 */
	public String getClosingMessage() {
		
		return "Thank you for participating. You will not be able to participate again until " + Utils.getRelativeTime(nextTime) + ". You will receive a notification at this time.";
    	
	}
	
	protected void saveNextTime() {
		saveNextTime(false);
	}
	
	protected void saveNextTime(boolean done) {
		
		Log.d(TAG,"in saveNextTime()");
		Log.d(TAG,"done parameter: " + done);
		Log.d(TAG,"exp.getSPName(): " + exp.getSPName());

		//save to persistent memory
		SharedPreferences sharedPreferences = context.getSharedPreferences(exp.getSPName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		if (done){
			editor.putBoolean("done", true);
		} else {
			editor.putBoolean("done", false);//can be handled through defaults, but why not be explicit?
			editor.putLong("nextTime", this.nextTime);			
		}
		editor.commit();

	}

}