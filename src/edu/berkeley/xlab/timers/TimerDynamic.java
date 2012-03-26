package edu.berkeley.xlab.timers;

import java.util.Random;

import edu.berkeley.xlab.AlarmReceiver;
import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Sets times at for next experiment segment within range of dates and day-time combinations.
 * Sets this time after last experiment segment has been completed.
 * If timerStatus is 2 and subject does not answer all experiment segments within specified time, he or she receives nothing.
 * @author dvizzini
 */
public class TimerDynamic extends TimerAbstract {
	
	protected final int intervalMax;
	
	protected final int intervalMin;
	
	private SharedPreferences sharedPreferences;
	
	private SharedPreferences.Editor editor;

	
	public TimerDynamic(Context context, ExperimentAbstract exp, boolean[] dayEligibility, int intervalMin, int intervalMax) {
		
		super(context, exp, -1L, dayEligibility);
		
		Log.d(TAG,"In TimerStatic exp Constructor");
				
		this.intervalMin = intervalMin;
		this.intervalMax = intervalMax;
		
		sharedPreferences = context.getSharedPreferences(exp.getSPName(), Context.MODE_PRIVATE);
		editor = sharedPreferences .edit();

		editor.putInt("timer_type", Constants.TIMER_DYNAMIC);
		editor.putInt("intervalMin", intervalMin);
		editor.putInt("intervalMax", intervalMax);
		editor.commit();
		
		initialize();
		
	}
		
	public TimerDynamic(Context context, Activity activity, ExperimentAbstract exp, SharedPreferences sharedPreferences) {
		
		super(context, exp, sharedPreferences.getLong("nextTime", -1L));
		
		Log.d(TAG,"In TimerStatic SP Constructor");

		this.sharedPreferences = sharedPreferences;

		this.intervalMin = sharedPreferences.getInt("intervalMin", -1);
		this.intervalMax = sharedPreferences.getInt("intervalMax", -1);
		
		initialize();

	}
	
	private void initialize () {
		
		this.nextTime = sharedPreferences.getLong("nextTime", -1L);
		
		if (nextTime == -1L) {
			//no nextTime so generate one
			onFinish();				
		}
	}
	
	@Override
	public String onFinish() {

		Log.d(TAG,"In TimerDynamic onFinish()");
		
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(exp.getExpId());

		int timeWindow = (intervalMax - intervalMin);//in minutes
	    Random r = new Random();
	    int time = r.nextInt(timeWindow);
	    Log.d(TAG,"time: " + time);
	    this.nextTime = (((long) time) * 60L * 1000L) + System.currentTimeMillis() + (((long)intervalMin) * 60L * 1000L);
	    Log.d(TAG, "(nextTime - System.currentTimeMillis()) / (60L * 1000L): " + ((nextTime - System.currentTimeMillis())/ (60L * 1000L)));
	    Log.d(TAG,"nextTime: " + nextTime);
	    
		saveNextTime();
		
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("expId", exp.getExpId());
		PendingIntent sender = PendingIntent.getBroadcast(context, exp.getExpId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, nextTime, sender);
		 
		return getClosingMessage();
		
	}
	
}