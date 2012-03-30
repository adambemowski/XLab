package edu.berkeley.xlab;

import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;
import edu.berkeley.xlab.xlab_objects.ExperimentBudgetLine;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
 
public class AlarmReceiver extends BroadcastReceiver {
 
	
	/**
	 * For log
	 */
	public static final String TAG = "AlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			
			if (intent.getExtras().getLong("nextRound") != -1) {
				
				int expId = intent.getExtras().getInt("expId");
				int nextRound = intent.getExtras().getInt("nextRound");
				
				SharedPreferences sharedPreferences = context.getSharedPreferences(ExperimentAbstract.makeSPName(expId),Context.MODE_PRIVATE);
				
				String[] times = sharedPreferences.getString("times", "").split(",");
				intent.putExtra("expId", expId);
				Log.d(TAG,"nextRound: " + nextRound);
				intent.putExtra("nextRound", nextRound + 1);//so next alarm can be set immediately
				PendingIntent sender = PendingIntent.getBroadcast(context, expId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				// Get the AlarmManager service
				AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, Long.valueOf(times[nextRound]), sender);
				
			}

			Log.d(TAG,"String.valueOf(intent.getExtras().getInt(\"expID\")): " + String.valueOf(intent.getExtras().getInt("expId")));
			new Notifier(context, (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE), new ExperimentBudgetLine(context, context.getSharedPreferences(ExperimentAbstract.makeSPName(intent.getExtras().getInt("expId")),Context.MODE_PRIVATE))).run();
			
		} catch (Exception e) {	
			e.printStackTrace();
		}
	}
}