package edu.berkeley.xlab;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;

/**
 * Posts all unreceived results to server
 * 
 */
public class RefreshExperiments extends AsyncTask<Void, Void, Void> {

	/** TAG is an identifier for the log. */
	public static final String TAG = "X-Lab - RefreshExperiments";

	/** application context */
	private Context context;

	/** Clears experiment from phone if called */
	private ExperimentAbstract toBeRemoved;

	/** True is there is an Experiment object toBeRemove, false otherwise */
	private boolean toBeRemovedBool;

	/** true if task downloads new experiments, false otherwise */
	private boolean downloadFlag;	
	
	/** Object that actually communicates with server */
	private Communicator communicator;

	/** calling Activity */
	private Activity activity;

	/** dialog displayed as results upload */
	private ProgressDialog dialog;

	/**
	 * 
	 * @param context
	 *            Application Context
	 * @param activity
	 *            Calling Activity
	 * @param downloadFlag
	 *            true if task also downloads experiments, false otherwise
	 * @param toBeRemovedId
	 *            experiment to be removed from "global" map and
	 *            SharedPreferences. The constructor with this is a crutch and
	 *            should be deleted after TODO below is addressed
	 */
	public RefreshExperiments(Context context, Activity activity, ExperimentAbstract toBeRemoved) {
		this.context = context;
		this.activity = activity;
		this.toBeRemoved = toBeRemoved;
		this.downloadFlag = false;
		this.toBeRemovedBool = true;
	}

	/**
	 * @param context
	 *            Application Context
	 * @param activity
	 *            Calling Activity
	 * @param downloadFlag
	 *            true if task also downloads experiments, false otherwise
	 */
	public RefreshExperiments(Context context, Activity activity) {
		this.activity = activity;
		this.context = context;
		this.downloadFlag = true;
		this.toBeRemovedBool = false;
	}

	/**
	 * @param context
	 *            Application Context
	 * @param activity
	 *            Calling Activity
	 * @param downloadFlag
	 *            true if task also downloads experiments, false otherwise
	 */
	public RefreshExperiments(Context context, Activity activity, boolean downloadFlag) {
		this.activity = activity;
		this.context = context;
		this.downloadFlag = downloadFlag;
		this.toBeRemovedBool = false;
	}

	@Override
	protected void onPreExecute() {

		// Initialize values and show dialog
		dialog = new ProgressDialog(activity);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("Connecting to XLab server...");
		dialog.show();

	}

	@Override
	protected Void doInBackground(Void... whatever) {

		// TODO: Get everything in if statement to Experiment objects after
		// "done" variable is added to server
		// Remove Experiment from SharedPreferences
		if (toBeRemovedBool) {
			toBeRemoved.clearSharedPreferences(context);
		}
		
		communicator = new Communicator(context, downloadFlag, activity, dialog);
			
		communicator.communicate();
		
		return null;

	}
	
}