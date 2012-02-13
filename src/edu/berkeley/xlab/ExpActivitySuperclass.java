package edu.berkeley.xlab;

import java.text.DecimalFormat;

import edu.berkeley.xlab.xlab_objects.Experiment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;


/**
 * Draw is an activity that controls choosing a point on a line and recording
 * that choice.
 * 
 * @author Daniel Vizzini
 */
public abstract class ExpActivitySuperclass extends Activity {

	//TODO: Put instruction in Menu
	
	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab - ExpActivitySuperclass";
	
	/** decimal FORMATTER */
	public static final DecimalFormat FORMATTER = new DecimalFormat("###,###,###.00");

	/** application context for Shared Preferences */
	protected Context context;
	
	/** application object for calling methods with a UI thread */
	protected Activity activity;
	
	/** unique identifier of Experiment */
	protected int expId;
	
	/** Called when the activity is first created. */
	protected void initialize(Activity activity) {
		
		super.onStart();
		
		Log.d(TAG, "In initialize() method");

		context = getApplicationContext();
		
		this.activity = activity;
		
		expId = (int) getIntent().getExtras().getInt("expId");
		Log.d(TAG, "expId = " + expId);
		
	}

	/**
	 * Displays message at the conclusion of session (e.g. to inform the subject what has been won) and cleans the Result's shared preferences from the phone
	 * @param activity current activity
	 * @param dialogMessage message for dialog box
	 */
	protected void cleanUpExp(final Experiment exp, String dialogMessage) {		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);				
		builder.setMessage(dialogMessage);
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				cleanUpExp(exp);
			}
		});
	
		AlertDialog alert = builder.create();
		alert.show();
		
	}
	

	
	/**
	 * Cleans the Result's SharedPreferences from the phone
	 * @param activity current activity	 
	 */
	protected void cleanUpExp(Experiment exp) {		
		
		if (exp.isDone()) {
			Log.d(TAG, "Calling Refresh experiments with hit order");
			new RefreshExperiments(context, activity, false, expId).execute();
		} else {
			Log.d(TAG, "Calling Refresh experiments without hit order");
			new RefreshExperiments(context, activity, false).execute();			
		}
		
	}
	
}