package edu.berkeley.xlab.timers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Random;

import edu.berkeley.xlab.AlarmReceiver;
import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.Experiment;
import edu.berkeley.xlab.xlab_objects.ExperimentBudgetLine;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Sets times at beginning of experiment within range of dates and day-time combinations.
 * If timerStatus is 2 and subject does not answer within specified time, he or she cannot participate in this segment of the experiment and will receive no reward if applicable
 * @author dvizzini
 */
public class TimerStatic extends TimerSuperClass {
	
	protected ArrayList<Long> times;
	protected long currentTime;
	//TODO: For now just the number of lines in a session for ExperimentBudgetLines. In the future make this flexible enough for all types of experiments and either session or line (or line's generalization)
	//TODO: When above TODO is addressed delete following line and use exp
	protected ExperimentBudgetLine expBL;
	public ExperimentBudgetLine getExpBL() {
		return expBL;
	}
	protected int numExpUnits;
	protected int numDays;
	protected boolean createAlarmsBool;
	
	public TimerStatic(Context context, Experiment exp, boolean[] dayEligibility, GregorianCalendar startDate, GregorianCalendar endDate, int startTime, int endTime) {
		
		super(context, exp, dayEligibility);
		
		Log.d(TAG,"In TimerStatic Constructor");
		
		this.times = new ArrayList<Long>();
		this.currentTime = System.currentTimeMillis();
		this.expBL = (ExperimentBudgetLine) exp;
		this.startDate = startDate;
		this.endDate = endDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.createAlarmsBool = true;
		
		numExpUnits = expBL.getSession(expBL.getCurrSession()).getLines().length;
		
		GregorianCalendar date;
		
		numDays = Utils.getEligiableDaysBetweenTwoDates(startDate, endDate, dayEligibility);
		firstEligibleDate = Utils.addEligibleDays(startDate, 0, dayEligibility);
		lastEligibleDate = Utils.addEligibleDays(firstEligibleDate, numDays - 1, dayEligibility);

		date = firstEligibleDate;
	    date.add(Calendar.MINUTE, startTime);
		times.add(date.getTimeInMillis());//first experiment segment can be done as soon as eligibility begins
		
	    date = lastEligibleDate;
	    date.add(Calendar.MINUTE, endTime);
		times.add(date.getTimeInMillis());//last experiment segment can be done up until eligibility ends
				
		String toBeSaved = "";

		Log.d(TAG,"startDate: " + (startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DATE) + "/" + startDate.get(Calendar.YEAR));
		Log.d(TAG,"endDate: " + (endDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DATE) + "/" + endDate.get(Calendar.YEAR));
		Log.d(TAG,"first eligiable date: " + (firstEligibleDate.get(Calendar.MONTH) + 1) + "/" + firstEligibleDate.get(Calendar.DATE) + "/" + firstEligibleDate.get(Calendar.YEAR));
		Log.d(TAG,"last eligiable date: " + (lastEligibleDate.get(Calendar.MONTH) + 1) + "/" + lastEligibleDate.get(Calendar.DATE) + "/" + lastEligibleDate.get(Calendar.YEAR));
		Log.d(TAG,"numDays: " + numDays);
		
		/**
		 * assign uniform random times
		 * We need n - 1 times between the beginning of the experiment, when the first prompt can first be answered
		 * and the end of the experiment, when the last prompt can last be answered.
		 */
		for (int i = 0; i < numExpUnits - 1; i++) {
			pickDate();   
		}
		
		//sort collections
		Collections.sort(times);
	    
		//save to SharedPreferences
		for (Long time : times) {
			toBeSaved = toBeSaved + time + ",";
		}

		SharedPreferences.Editor editor = context.getSharedPreferences(expBL.getSPName(), Context.MODE_PRIVATE).edit();
		editor.putLong("nextTime", times.get(0));
		editor.putString("times", toBeSaved);
		editor.putInt("timer_type", Constants.TIMER_STATIC);
		editor.commit();

		initialize();
	}
	
	public TimerStatic(Context context, Experiment exp, SharedPreferences sharedPreferences, boolean createAlarmsBool) {
		
		super(context, exp);
		
		constructFromSharedPreferences(sharedPreferences);
		
		this.createAlarmsBool = createAlarmsBool;
		
		initialize();

	}
	
	private void constructFromSharedPreferences(SharedPreferences sharedPreferences) {
		this.expBL = (ExperimentBudgetLine) exp;
		this.times = new ArrayList<Long>();
		this.numExpUnits = expBL.getSession(expBL.getCurrSession()).getLines().length;
		this.currentTime = System.currentTimeMillis();
		
		//get times from sharedPreferences
		String[] rawArray = sharedPreferences.getString("times", "").split(",");
		
		for (String rawElem : rawArray) {
			times.add(Long.valueOf(rawElem));
		}

	}
	
	/**
	 * Creates GregorianCalendar object by adding a uniform random number between 0 and numDays of days and a uniform random number between the start time and (endTime - 1) to Date, and then adds to the times ArrayList
	 * If times already contains the GregorianCalendar object generated, a new GregorianCalendar object will be generated and added to times
	 */
	private void pickDate() {
		
		//Monte Carlo engine
		Random r = new Random();
	    
	    //pick date
	    GregorianCalendar date = Utils.addEligibleDays(firstEligibleDate,r.nextInt(numDays),dayEligibility);
    	Log.d(TAG,"Current time of date: " + date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE) );
		Log.d(TAG,"date: " + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.DATE) + "/" + date.get(Calendar.YEAR));
	    
	    //pick time
    	Log.d(TAG,"endTime - startTime: " + (endTime - startTime));
    	int timeAdded = r.nextInt(endTime - startTime);
    	Log.d(TAG,"timeAdded: " + timeAdded);    	
	    date.add(Calendar.MINUTE, timeAdded);
	    
	    //add to ArrayList if not present, else generate new object
	    if (!times.contains(date.getTimeInMillis())){
		    times.add(date.getTimeInMillis());					    	
	    } else {
	    	pickDate();
	    }
	    
	}
	
	private void initialize() {
		
		expBL = updateExp();
		
		Log.d(TAG, "this.createAlarmsBool: " + this.createAlarmsBool);
		Log.d(TAG, "expBL.isDone(): " + expBL.isDone());
		
		//create timers
		if (!expBL.isDone() && this.createAlarmsBool) {
			setAlarm();			
		}
	}
	
	@Override
	public String onFinish() {
	    
		Log.d(TAG,"In TimerStatic onFinish()");
		
		setAlarm();

		this.nextTime = times.get(expBL.getCurrLine());
		saveNextTime(expBL.isDone());
		
		return getClosingMessage();
		
	}
	
	private void setAlarm() {
		
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(exp.getExpId());

		// create the pending intent
		Log.d(TAG, "creating timer: times.get(" + expBL.getCurrLine() + "): " + times.get(expBL.getCurrLine()));
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("expId", expBL.getExpId());
		intent.putExtra("nextNextTime", times.get(expBL.getCurrLine() + 1) < times.size() ? times.get(expBL.getCurrLine() + 1) : 0L);//so next alarm can be set immediately
		PendingIntent sender = PendingIntent.getBroadcast(context, expBL.getExpId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, times.get(expBL.getCurrLine()), sender);
		
	}
	
	/**
	 * Updates Experiment object by iterating through undone experiments. 
	 * @return Updated Experiment object
	 */
	public ExperimentBudgetLine updateExp() {
		
		Log.d(TAG, "expBL.getCurrLine() before for: " + expBL.getCurrLine());
		Log.d(TAG, "numExpUnits: " + numExpUnits);
		Log.d(TAG, "currentTime: " + currentTime);
		Log.d(TAG, "times.get(0): " + times.get(0));
		
		int numSkipped = expBL.getNumSkipped();
		
		//set nextTime variable, numSkipped and currentExperiment
		for (int i = expBL.getCurrLine(); i < numExpUnits; i++){
			Log.d(TAG, "times.get(" + (i + 1) + "): " + times.get(i + 1));
			if (times.get(i + 1) < currentTime) {//times.get(i + 1) is the last possible time that experiment segment i can be done. The first time is there for the reminder.
				Log.d(TAG, "Skipping to line " + (i + 1));				
				expBL.nextLine(context);//iteration is saved in SharedPreferences
				numSkipped++;
			} else {
				break;
			}
		}
		
		Log.d(TAG,"numSkipped: " + numSkipped);
		expBL.addNumSkipped(context, numSkipped);

		if (expBL.isDone()) {
			Log.d(TAG, "expBL is done");			
		} else {
			Log.d(TAG, "expBL.getCurrLine() after for: " + expBL.getCurrLine());			
		}

		this.nextTime = times.get(expBL.getCurrLine());
		
		saveNextTime(expBL.isDone());

		return expBL;
		
	}
	
}