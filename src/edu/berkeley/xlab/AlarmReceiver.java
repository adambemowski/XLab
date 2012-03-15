package edu.berkeley.xlab;

import edu.berkeley.xlab.xlab_objects.Experiment;
import edu.berkeley.xlab.xlab_objects.ExperimentBudgetLine;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
 
public class AlarmReceiver extends BroadcastReceiver {
 
	
	/**
	 * For log
	 */
	public static final String TAG = "TimerSuperClass";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Log.d(TAG,"String.valueOf(intent.getExtras().getInt(\"expID\"): " + String.valueOf(intent.getExtras().getInt("expId")));
			new Notifier(context, (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE), new ExperimentBudgetLine(context, context.getSharedPreferences(Experiment.makeSPName(intent.getExtras().getInt("expId")),-1))).run();
		} catch (Exception e) {
			
			e.printStackTrace();
		 
		}
	}
}