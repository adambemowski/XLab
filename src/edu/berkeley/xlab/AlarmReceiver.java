package edu.berkeley.xlab;

import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;
import edu.berkeley.xlab.xlab_objects.ExperimentBudgetLine;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
			
			//set next Alarm. If no nextNextTime exists or last alarm has been set, intent will return 0L
			if (intent.getExtras().getLong("nextNextTime") != 0L) {
				PendingIntent sender = PendingIntent.getBroadcast(context, intent.getExtras().getInt("expId"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, intent.getExtras().getLong("nextNextTime"), sender);				
			}
			
			Log.d(TAG,"String.valueOf(intent.getExtras().getInt(\"expID\"): " + String.valueOf(intent.getExtras().getInt("expId")));
			new Notifier(context, (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE), new ExperimentBudgetLine(context, context.getSharedPreferences(ExperimentAbstract.makeSPName(intent.getExtras().getInt("expId")),Context.MODE_PRIVATE))).run();
		} catch (Exception e) {	
			e.printStackTrace();
		}
	}
}