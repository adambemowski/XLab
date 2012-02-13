package edu.berkeley.xlab;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.Experiment;
import edu.berkeley.xlab.xlab_objects.ExperimentBudgetLine;
import edu.berkeley.xlab.xlab_objects.ExperimentTextQuestion;
import edu.berkeley.xlab.xlab_objects.Response;
import edu.berkeley.xlab.xlab_objects.Session;
import edu.berkeley.xlab.xlab_objects.XLabSuperObject;

/**
 * Posts all unreceived results to server
 * 
 * @author dvizzini
 */
public class RefreshExperiments extends AsyncTask<Void, Void, Void> {

	/** TAG is an identifier for the log. */
	public static final String TAG = "X-Lab - RefreshExperiments";
		
	/** dialog displayed as results upload */
	private ProgressDialog dialog;
	
	/** true if download and upload successful, false otherwise  */
	private boolean successful;
	
	/** number of experiments downloaded */
	private int downloaded;
	
	/** number of results uploaded */
	private int uploaded;
	
	/** true if task downloads new experiments, false otherwise */
	private boolean downloadFlag;
	
	/** outstanding asynchonous tasks */
	private int outstanding;
		
	/** persistent record of points chosen */
	private SharedPreferences responsesList;
	
	/** application context */
	private Context context;
	
	/** for http post, defined in onCreate */
	private String username;
	
	/** list of outstanding results to be downloaded */
	private ArrayList<String> responseNamesArrayList;
	
	/** calling Activity */
	private Activity activity;
	
	/** application-level array of experiment objects */
	private ConcurrentHashMap<Integer, Experiment> xLabExps;
	
	/** Application state to access "global" variables and methods */
	private App appState;
	
	/** Clears experiment from phone if called */
	private int toBeRemovedId;
	
	/** 
	 * @param context Application Context
	 * @param activity Calling Activity
	 * @param downloadFlag true if task also downloads experiments, false otherwise
	 */
	public RefreshExperiments(Context context, Activity activity, boolean downloadFlag, int toBeRemovedId) {
		this.context = context;
		this.activity = activity;
		this.downloadFlag = downloadFlag;
		this.appState = (App) context;
		this.toBeRemovedId = toBeRemovedId;
	}
		
	/** 
	 * @param context Application Context
	 * @param activity Calling Activity
	 * @param downloadFlag true if task also downloads experiments, false otherwise
	 */
	public RefreshExperiments(Context context, Activity activity, boolean downloadFlag) {
		this.context = context;
		this.activity = activity;
		this.downloadFlag = downloadFlag;
		this.appState = (App) context;
		this.toBeRemovedId = -1;
	}
	
	@Override
	protected void onPreExecute() {

		//Initialize values and show dialog
		dialog = new ProgressDialog(activity);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("Connecting to XLab server...");
		dialog.show();
		
		this.username = Utils.getStringPreference(context, Configuration.USERNAME, "anonymous");
		this.xLabExps = appState.getXLabExps();
		
		successful = true;
		downloaded = 0;
		uploaded = 0;
				
	}

	@Override
	protected Void doInBackground(Void... whatever) {

		//read SharedPreferences files of responses in order to upload and delete them via the UploadSingleResponse class
		responsesList = context.getSharedPreferences(Response.RESPONSES_LIST, Context.MODE_PRIVATE);
		
		Log.d(TAG, "Response SharedPreferences names: "+ responsesList.getString("SharedPreferences", ""));
		String[] responseNames = responsesList.getString("SharedPreferences", "").split(",");
		responseNamesArrayList = new ArrayList<String>();
		
		/** true if the server does not need to be called, false otherwise */
		boolean callServer = false;
		
		if (!responseNames[0].equals("")) {
			callServer = true;
			for (String responseName : responseNames) {
				responseNamesArrayList.add(responseName);
				Log.d(TAG,"Uploading " + responseName);
				new Thread( new UploadSingleResponse(responseName)).start();
			}
		}
		
		if (downloadFlag) {
			callServer = true;
			fetchXLabExps();
		}
		
		if (!callServer) {
			Log.d(TAG,"Not calling server");
			finishUp();
		}
		
		return null;
		
    }
	
	/**checks to see if there are any outstanding experiments */
	private void checkForDone() {
		
		outstanding--;
		
		Log.d(TAG, "Checking if done, outstanding = " + outstanding);

		if (outstanding == 0) {
			finishUp();
		}
				
	}
	
	/** displays finishing dialog and resets RESPONSES_LIST
	 * Deletes specified Experiment from App Class and ShaaredPreferences associated with the parameters of the experiment.
	 */
	private void finishUp() {
		
		//TODO: Get everything in if statement to Experiment objects after "done" variable is added to server
		//Remove Experiment from SharedPreferences
		if (toBeRemovedId != -1) {
			
			//retrieve experiment
			Experiment toBeRemoved = this.xLabExps.get(toBeRemovedId);
			
			//remove from application-level variable
			this.xLabExps.remove(toBeRemovedId);
			
			//delete SharedPreferences
			Log.d(TAG,"File to be deleted: " + "/data/data/" + context.getPackageName() + "/shared_prefs/" + toBeRemoved.getSPName() + ".xml");
			new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + toBeRemoved.getSPName() + ".xml").delete();
			
			//delete child SharedPreferences
			if (toBeRemoved.getTypeId() == Configuration.XLAB_BL_EXP) {

				for (Session session : ((ExperimentBudgetLine) toBeRemoved).getSessions()){
					session.deleteSharedPreferences(context);
				}

			}
			
			//remove from SharedPrefrences list of SharedPreferences
			SharedPreferences sharedPreferencesList = context.getSharedPreferences(Experiment.EXP_LIST, Context.MODE_PRIVATE);
			SharedPreferences.Editor listEditor = sharedPreferencesList.edit();

			String[] halfList = sharedPreferencesList.getString("SharedPreferences","").split(toBeRemoved.getSPName() + ",");
			
			if (halfList.length == 0) {
				Log.d(TAG,"Empty EXP_LIST");
				listEditor.putString("SharedPreferences", "");			
			} else if (halfList.length == 1) {
				Log.d(TAG,"EXP_LIST is " + halfList[0]);
				listEditor.putString("SharedPreferences", halfList[0]);						
			} else {
				Log.d(TAG,"EXP_LIST is " + halfList[0] + halfList[1]);
				listEditor.putString("SharedPreferences", halfList[0] + halfList[1]);
			}
			
			listEditor.commit();
			
			toBeRemoved = null;//good housekeeping
						
		}
		
		//God bless StackOverflow http://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
		Thread uiThread = new HandlerThread("UIHandler");
		uiThread.start();
	    UIHandler uiHandler = new UIHandler(((HandlerThread) uiThread).getLooper());
	    Message msg = uiHandler.obtainMessage();		    
	    
		XLabSuperObject.setList(context, Response.RESPONSES_LIST, responseNamesArrayList);
	    
		dialog.dismiss();
		
	    uiHandler.sendMessage(msg);
		
	}
	
	/**
	 * Creates handler to handle message
	 * 
	 * @author Daniel Vizzini
	 *
	 */
	final class UIHandler extends Handler {

		public UIHandler(Looper looper)
	    {
	        super(looper);
	    }

	    @Override
	    public void handleMessage(Message msg)
	    {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			String message = ((downloaded > 0) ? "Your phone downloaded " + downloaded + " experiment" + ((downloaded > 1) ? "s" : "") : ((successful) ? "Your phone conected to the the Xlab but no new experiments are available for download. Please try again later by restarting the app or selecting Refresh Experiments." : ""));
			message = ((uploaded > 0 ) ? "The XLab server received " + uploaded + " results." : "There were no results to upload.") + ((downloadFlag) ? "\n\n" + message : "");
			message = ((successful) ? "Refresh was succesful.\n\n" + message : "Refresh was not fully successful. Please try again later by selecting Refresh Experiments.");
			builder.setMessage(message);
			builder.setNeutralButton("OK",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					activity.startActivity(new Intent(context, MainActivity.class));
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
			
	    }
	}

	/**
	 * Uploads single response to server in cloud
	 */
	public class UploadSingleResponse implements Runnable {
		
		private String http_response;
		private String sharedPreferencesName;
		private SharedPreferences sharedPreferences;
		private int typeId;
		private int expId;
		
		//TQ-Specific
		private String answer;
		
		//BL-Specific
		private int lineId;
		private int sessionId;
		private float x_int;
		private float y_int;
		private float x_chosen;
		private float y_chosen;
		private char winner;
		private boolean line_chosen_boolean;		
		
		public UploadSingleResponse(String sharedPreferencesName) {
			
			super();
			Log.d(TAG,"In UploadSingleResponse constructor");
			
			this.sharedPreferencesName = sharedPreferencesName;
			this.sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
			this.typeId = sharedPreferences.getInt("typeId",-1);
			this.expId = sharedPreferences.getInt("expId",-1);

			switch (typeId) {
			case Configuration.XLAB_TQ_EXP:
				this.answer = sharedPreferences.getString("answer","");
				break;
			case Configuration.XLAB_BL_EXP:
				this.sessionId = sharedPreferences.getInt("sessionId",-1);
				this.lineId = sharedPreferences.getInt("lineId",-1);
				this.x_int = sharedPreferences.getFloat("x_int", (float)-1);
				this.y_int = sharedPreferences.getFloat("y_int", (float)-1);
				this.x_chosen = sharedPreferences.getFloat("x_chosen", (float)-1);
				this.y_chosen = sharedPreferences.getFloat("y_chosen", (float)-1);
				this.winner = sharedPreferences.getString("winner", "").charAt(0);
				this.line_chosen_boolean = sharedPreferences.getBoolean("line_chosen_boolean",false);
				break;
			}

		}

		@Override
		public void run() {
			
			outstanding++;
			Log.d(TAG, "outstanding: " + outstanding);

			//TODO: Set timeout
			try {
				
				Log.d(TAG, "typeId: " + typeId);
				
				switch (typeId) {
				
				case Configuration.XLAB_TQ_EXP:
					Log.d(TAG,Configuration.XLAB_API_ENDPOINTS[0] + "?tq_id=" + expId + "&tq_response=" + URLEncoder.encode(answer, "utf-8") + "&tq_username=" + username + "&tq_lat=" + BackgroundService.getLastLat() + "&tq_lon=" + BackgroundService.getLastLon());
					http_response = Utils.getData(Configuration.XLAB_API_ENDPOINTS[0] + "?tq_id=" + expId + "&tq_response=" + URLEncoder.encode(answer, "utf-8") + "&tq_username=" + username + "&tq_lat=" + BackgroundService.getLastLat() + "&tq_lon=" + BackgroundService.getLastLon());

					if(null == http_response) {
						Log.e(TAG, "Received null response");
						successful = false;
					} else if(http_response.equalsIgnoreCase("0")) {
						Log.d(TAG, "Received response from server - " + http_response);
						successful = false;
					} else if(http_response.equalsIgnoreCase("1")) {
						Log.d(TAG, "Received response from server - " + http_response);
						responseNamesArrayList.remove(sharedPreferencesName);
						new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + sharedPreferencesName + ".xml").delete();
						uploaded++;
					}
					
					break;

				case Configuration.XLAB_BL_EXP:
					Log.d(TAG,Configuration.XLAB_API_ENDPOINTS[1]
							+ "?bl_id=" + expId + "&bl_session=" + sessionId + "&bl_line=" + lineId + "&bl_username=" + username
							+ "&bl_lat=" + BackgroundService.getLastLat()
							+ "&bl_lon=" + BackgroundService.getLastLon()
							+ "&bl_x=" + x_chosen + "&bl_y=" + y_chosen
							+ "&bl_x_intercept=" + x_int + "&bl_y_intercept=" + y_int
							+ "&bl_winner=" + winner + "&bl_line_chosen_boolean=" + line_chosen_boolean);
					http_response = Utils.getData(Configuration.XLAB_API_ENDPOINTS[1]
							+ "?bl_id=" + expId + "&bl_session=" + sessionId + "&bl_line=" + lineId + "&bl_username=" + username
							+ "&bl_lat=" + BackgroundService.getLastLat()
							+ "&bl_lon=" + BackgroundService.getLastLon()
							+ "&bl_x=" + x_chosen + "&bl_y=" + y_chosen
							+ "&bl_x_intercept=" + x_int + "&bl_y_intercept=" + y_int
							+ "&bl_winner=" + winner + "&bl_line_chosen_boolean=" + line_chosen_boolean);
	
					if(null == http_response) {
						Log.e(TAG, "Received null response");
						successful = false;
					} else if(http_response.equalsIgnoreCase("0")) {
						Log.d(TAG, "Received response from server - " + http_response);
						successful = false;
					} else if(http_response.equalsIgnoreCase("1")) {
						Log.d(TAG, "Received response from server - " + http_response);
						responseNamesArrayList.remove(sharedPreferencesName);
						new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + sharedPreferencesName + ".xml").delete();
						uploaded++;
					}
					
					break;
					
				}
				
				checkForDone();
					
				
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				successful = false;
				checkForDone();
			}
	        	    			
		}

	}

	/*
	 * Checks for persistently stored Experiments in SharedPreferences and then on server
	 */
	private void fetchXLabExps() {
		
		Log.d(TAG, "outstanding: " + outstanding);
				
		uploadInternalExps();
		
		//fetch experiments from server		
		for (int i = 0; i < Configuration.XLAB_API_ENDPOINTS.length; i++)  {
			new Thread( new Fetcher(i) ).start();				
		}
			
	}
	
	private void uploadInternalExps() {

		SharedPreferences expSP;
		String[] expNames = context.getSharedPreferences(Experiment.EXP_LIST, Context.MODE_PRIVATE).getString("SharedPreferences", "").split(",");

		Log.d(TAG, "exp SharedPreferences: " + context.getSharedPreferences(Experiment.EXP_LIST, Context.MODE_PRIVATE).getString("SharedPreferences", ""));
		
		//Get persistently stored experiments
		if (!expNames[0].equals("")) {
			for (String expName : expNames) {
				
				Log.d(TAG,"Retrieving " + expName);
				
				expSP = context.getSharedPreferences(expName, Context.MODE_PRIVATE);
				
				switch (expSP.getInt("typeId", -1)) {
				
				case Configuration.XLAB_TQ_EXP:
					
					if (!xLabExps.containsKey(expSP.getInt("expId", -1))) {
						ExperimentTextQuestion tq = new ExperimentTextQuestion(context, context.getSharedPreferences(expName, Context.MODE_PRIVATE));
						appState.appendToXLabExps(tq);
					}

					break;
					
				case Configuration.XLAB_BL_EXP:
					
					if (!xLabExps.containsKey(expSP.getInt("expId", -1))) {
						ExperimentBudgetLine bl = new ExperimentBudgetLine(context, context.getSharedPreferences(expName, Context.MODE_PRIVATE));
						appState.appendToXLabExps(bl);
					}
					
					break;
					
				}
				
			}
		}
	}
	
	class Fetcher implements Runnable {
		
		public Fetcher(int index) {
			this.index = index;
		}
		
		private int index;
		private String endpoint;
		private String response;
		
		@Override
		public void run() {
			//TODO: Set timeout
			try {				

				outstanding++;

				endpoint = Configuration.XLAB_API_ENDPOINTS[index];
				Log.d(TAG, "Sending request to server: " + endpoint);

				response = Utils.getData(endpoint);
				Log.d(TAG, "response: " + response);
				
				if (null != response) {

					if (!response.equals("blank")) {

						// Parse the response
						String[] lines = response.split("\n");

						for (String line : lines) {

							switch (index) {
							
							case Configuration.XLAB_TQ_EXP:
								
								ExperimentTextQuestion tq = new ExperimentTextQuestion(context, line);

								if (!xLabExps.containsKey(tq.getExpId())) {
									appState.appendToXLabExps(tq);
									tq.save(context);
									downloaded++;
								}
								
								break;
								
							case Configuration.XLAB_BL_EXP:
								
								ExperimentBudgetLine bl = new ExperimentBudgetLine(context, line);

								if (!xLabExps.containsKey(bl.getExpId())) {
									appState.appendToXLabExps(bl);
									bl.save(context);
									downloaded++;
								}
								
								break;
								
							}
							
						}
						
					}
					
				} else {
					successful = false;
				}
				
				checkForDone();
				
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				successful = false;
				checkForDone();
			}

		}

	}
}