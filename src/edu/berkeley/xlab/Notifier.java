package edu.berkeley.xlab;

import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Upload XLab task status to the server 
 * 
 * @author Daniel Vizzini
 */
public class Notifier implements Runnable {
	/* for log */
	public static final String TAG = "XLab-BACKGROUND";

	private NotificationManager notificationManager;
	private Context context;
	private ExperimentAbstract exp;
	private Class<?> activity;
	
	public Notifier(Context context, NotificationManager notificationManager, ExperimentAbstract exp) {
		Log.d(TAG,"In UploadXlabBL constructor");
		this.context = context;
		this.notificationManager = notificationManager;
		this.exp = exp;
		this.activity = exp.getActivity();
	}

	@Override
	public void run() {
		
		try{
			
			String title = exp.getTitle();
			int expId = exp.getExpId();
			int icon = R.drawable.ic_stat_x_notification;
			CharSequence tickerText = "X-Lab Alert";
			long when = System.currentTimeMillis();
			
			Log.d(TAG,"Running UploadXlabBL");
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			
			CharSequence contentTitle = "X-Lab Alert";
			CharSequence contentText = title;
			Intent notificationIntent = new Intent(context, activity);
			notificationIntent.putExtra("expId", expId);
			PendingIntent contentIntent = PendingIntent.getActivity(context, expId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			Log.d(TAG,"UploadXlabBL notify for " + title);
			notificationManager.cancel(expId);
			notificationManager.notify(expId, notification);

		}
		catch(Exception e){
			Log.e(TAG,e.toString());
		}
	}
}