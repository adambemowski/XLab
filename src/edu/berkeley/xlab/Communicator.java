package edu.berkeley.xlab;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.timers.TimerDynamic;
import edu.berkeley.xlab.timers.TimerStatic;
import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;
import edu.berkeley.xlab.xlab_objects.ExperimentBudgetLine;
import edu.berkeley.xlab.xlab_objects.ExperimentTextQuestion;
import edu.berkeley.xlab.xlab_objects.ResponseAbstract;
import edu.berkeley.xlab.xlab_objects.XLabSuperObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class Communicator {
	
	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab - Communicator";

	/** persistent record of points chosen */
	private SharedPreferences responsesList;
	
	/** True if interacts with UI, false otherwise */
	private boolean showMessages;

	/** true if download and upload successful, false otherwise */
	private boolean successful;

	/** application context */
	private Context context;

	/** outstanding asynchonous tasks */
	private int outstanding;

	/** list of outstanding results to be downloaded */
	private ArrayList<String> responseNamesArrayList;

	/** true if task downloads new experiments, false otherwise */
	private boolean downloadFlag;

	/** calling Activity */
	private Activity activity;

	/** dialog displayed as results upload */
	private ProgressDialog dialog;

	/** number of experiments downloaded */
	private int downloaded;

	/** number of results uploaded */
	private int uploaded;

	/** for http post, defined in onCreate */
	private String username;
	
	public Communicator(Context context, boolean downloadFlag, Activity activity, ProgressDialog dialog) {
		this.context = context;
		this.downloadFlag = downloadFlag;
		this.activity = activity;
		this.dialog = dialog;
		this.username = Utils.getStringPreference(context, Constants.USERNAME, "anonymous");
		this.showMessages = true;
		this.successful = true;//innocent until proven guilty
		this.outstanding = 0;
		this.downloaded = 0;
		this.uploaded = 0;
	}

	public void communicate() {
		// read SharedPreferences files of responses in order to upload and
		// delete them via the UploadSingleResponse class
		responsesList = context.getSharedPreferences(ResponseAbstract.RESPONSES_LIST, Context.MODE_PRIVATE);

		Log.d(TAG,"Response SharedPreferences names: " + responsesList.getString("SharedPreferences", ""));
		String[] responseNames = responsesList.getString("SharedPreferences",
				"").split(",");
		responseNamesArrayList = new ArrayList<String>();

		/** true if the server does not need to be called, false otherwise */
		boolean callServer = false;

		if (!responseNames[0].equals("")) {
			callServer = true;
			for (String responseName : responseNames) {
				responseNamesArrayList.add(responseName);
				Log.d(TAG, "Uploading " + responseName);
				new Thread(new UploadSingleResponse(responseName)).start();
			}
		}

		if (downloadFlag) {
			callServer = true;
			fetchXLabExps();
		}

		if (!callServer) {
			Log.d(TAG, "Not calling server");
			finishUp();
		}

	}

	/** checks to see if there are any outstanding experiments */
	private void checkForDone() {

		outstanding--;

		Log.d(TAG, "Checking if done, outstanding = " + outstanding);

		if (outstanding == 0) {
			finishUp();
		}

	}

	/**
	 * Displays finishing dialog and resets RESPONSES_LIST Deletes specified
	 * Experiment from App Class and ShaaredPreferences associated with the
	 * parameters of the experiment.
	 */
	private void finishUp() {

		XLabSuperObject.setList(context, ResponseAbstract.RESPONSES_LIST,responseNamesArrayList);

		if (showMessages) {
			// God bless StackOverflow
			// http://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
			Thread uiThread = new HandlerThread("UIHandler");
			uiThread.start();
			UIHandler uiHandler = new UIHandler(((HandlerThread) uiThread).getLooper());
			Message msg = uiHandler.obtainMessage();

			dialog.dismiss();

			uiHandler.sendMessage(msg);
		}

		Log.d(TAG,"EXP_LIST: " + context.getSharedPreferences(ExperimentAbstract.EXP_LIST,Context.MODE_PRIVATE).getString("SharedPreferences", "Not good"));
	}

	/**
	 * Creates handler to handle message 
	 */
	final class UIHandler extends Handler {

		public UIHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			String message = ((downloaded > 0) ? "Your phone downloaded "
					+ downloaded + " experiment"
					+ ((downloaded > 1) ? "s" : "")
					: ((successful) ? "Your phone conected to the the Xlab but no new experiments are available for download. You may want to check in the future by selecting Refresh Experiments."
							: ""));
			message = ((uploaded > 0) ? "The XLab server received " + uploaded
					+ " results." : "There were no results to upload.")
					+ ((downloadFlag) ? "\n\n" + message : "");
			message = ((successful) ? "Refresh was succesful.\n\n" + message
					: "Refresh was not fully successful. Please try again later by selecting Refresh Experiments.");
			builder.setMessage(message);
			builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							activity.startActivity(new Intent(context,
									MainActivity.class));
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

		// TQ-Specific
		private String answer;

		// BL-Specific
		private int roundId;
		private int sessionId;
		private float x_int;
		private float y_int;
		private float x_chosen;
		private float y_chosen;
		private char winner;
		private boolean round_chosen_boolean;

		public UploadSingleResponse(String sharedPreferencesName) {

			super();
			Log.d(TAG, "In UploadSingleResponse constructor");

			this.sharedPreferencesName = sharedPreferencesName;
			this.sharedPreferences = context.getSharedPreferences(
					sharedPreferencesName, Context.MODE_PRIVATE);
			this.typeId = sharedPreferences.getInt("typeId", -1);
			this.expId = sharedPreferences.getInt("expId", -1);

			switch (typeId) {
			case Constants.XLAB_TQ_EXP:
				this.answer = sharedPreferences.getString("answer", "");
				break;
			case Constants.XLAB_BL_EXP:
				this.sessionId = sharedPreferences.getInt("sessionId", -1);
				this.roundId = sharedPreferences.getInt("roundId", -1);
				this.x_int = sharedPreferences.getFloat("x_int", (float) -1);
				this.y_int = sharedPreferences.getFloat("y_int", (float) -1);
				this.x_chosen = sharedPreferences.getFloat("x_chosen",
						(float) -1);
				this.y_chosen = sharedPreferences.getFloat("y_chosen",
						(float) -1);
				this.winner = sharedPreferences.getString("winner", "").charAt(
						0);
				this.round_chosen_boolean = sharedPreferences.getBoolean(
						"round_chosen_boolean", false);
				break;
			}

		}

		@Override
		public void run() {

			outstanding++;
			Log.d(TAG, "just added to outstanding, now " + outstanding);

			try {

				Log.d(TAG, "typeId: " + typeId);

				switch (typeId) {

				case Constants.XLAB_TQ_EXP:
					Log.d(TAG, Constants.XLAB_API_ENDPOINTS[Constants.XLAB_TQ_EXP]
									+ "?tq_id=" + expId + "&tq_response="
									+ URLEncoder.encode(answer, "utf-8")
									+ "&tq_username=" + username + "&tq_lat="
									+ BackgroundService.getLastLat()
									+ "&tq_lon="
									+ BackgroundService.getLastLon());
					http_response = Utils.getData(Constants.XLAB_API_ENDPOINTS[0]
									+ "?tq_id=" + expId + "&tq_response="
									+ URLEncoder.encode(answer, "utf-8")
									+ "&tq_username=" + username + "&tq_lat="
									+ BackgroundService.getLastLat()
									+ "&tq_lon="
									+ BackgroundService.getLastLon());

					if (null == http_response) {
						Log.e(TAG, "Received null response");
						successful = false;
					} else if (http_response.equalsIgnoreCase("0")) {
						Log.d(TAG, "Received response from server - "
								+ http_response);
						successful = false;
					} else if (http_response.equalsIgnoreCase("1")) {
						Log.d(TAG, "Received response from server - "
								+ http_response);
						responseNamesArrayList.remove(sharedPreferencesName);
						//new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + sharedPreferencesName + ".xml").delete();
						context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE).edit().clear();
						uploaded++;
					}

					break;

				case Constants.XLAB_BL_EXP:
					Log.d(TAG,
							Constants.XLAB_API_ENDPOINTS[Constants.XLAB_BL_EXP]
									+ "?bl_id=" + expId + "&bl_session="
									+ sessionId + "&bl_line=" + roundId
									+ "&bl_username=" + username + "&bl_lat="
									+ BackgroundService.getLastLat()
									+ "&bl_lon="
									+ BackgroundService.getLastLon() + "&bl_x="
									+ x_chosen + "&bl_y=" + y_chosen
									+ "&bl_x_intercept=" + x_int
									+ "&bl_y_intercept=" + y_int
									+ "&bl_winner=" + winner
									+ "&bl_line_chosen_boolean="
									+ round_chosen_boolean);
					http_response = Utils
							.getData(Constants.XLAB_API_ENDPOINTS[Constants.XLAB_BL_EXP]
									+ "?bl_id="
									+ expId
									+ "&bl_session="
									+ sessionId
									+ "&bl_line="
									+ roundId
									+ "&bl_username="
									+ username
									+ "&bl_lat="
									+ BackgroundService.getLastLat()
									+ "&bl_lon="
									+ BackgroundService.getLastLon()
									+ "&bl_x="
									+ x_chosen
									+ "&bl_y="
									+ y_chosen
									+ "&bl_x_intercept="
									+ x_int
									+ "&bl_y_intercept="
									+ y_int
									+ "&bl_winner="
									+ winner
									+ "&bl_line_chosen_boolean="
									+ round_chosen_boolean);

					if (null == http_response) {
						Log.e(TAG, "Received null response");
						successful = false;
					} else if (http_response.equalsIgnoreCase("0")) {
						Log.d(TAG, "Received response from server - "
								+ http_response);
						successful = false;
					} else if (http_response.equalsIgnoreCase("1")) {
						Log.d(TAG, "Received response from server - "
								+ http_response);
						responseNamesArrayList.remove(sharedPreferencesName);
						//new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + sharedPreferencesName + ".xml").delete();
						context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
						
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
	 * Checks for persistently stored Experiments in SharedPreferences and then
	 * on server
	 */
	private void fetchXLabExps() {

		Log.d(TAG, "outstanding: " + outstanding);
		Log.d(TAG, "Constants.XLAB_API_ENDPOINTS.length: "
				+ Constants.XLAB_API_ENDPOINTS.length);

		// fetch experiments from server
		for (int i = 0; i < Constants.XLAB_API_ENDPOINTS.length; i++) {
			Log.d(TAG, Constants.XLAB_API_ENDPOINTS[i]);
			new Thread(new Fetcher(i)).start();
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

			outstanding++;
			Log.d(TAG, "Outstanding in Fetcher: " + outstanding);

			try {

				endpoint = Constants.XLAB_API_ENDPOINTS[index] + "?format=json";
				Log.d(TAG, "Sending request to server: " + endpoint);

				response = Utils.getData(endpoint);
				Log.d(TAG, "response: " + response);

				if (null != response) {

					if (!response.equals("blank")) {

						// Parse the response
						// String[] lines = response.split("\n");
						JSONArray jsonArray = new JSONArray(response);
						JSONObject json;
						Log.d(TAG,
								"jsonArray.toString(): " + jsonArray.toString());

						for (int i = 0; i < jsonArray.length(); i++) {

							json = jsonArray.getJSONObject(i);
							Log.d(TAG, "json " + i + ": " + json);

							switch (index) {

							case Constants.XLAB_TQ_EXP:

								Log.d(TAG,
										"json.getInt(\"id\"):"
												+ json.getInt("id"));

								if (!Utils.checkIfSaved(context,
										ExperimentAbstract.makeSPName(json
												.getInt("id")),
										ExperimentAbstract.EXP_LIST)) {
									new ExperimentTextQuestion(context, json);
									downloaded++;
								}

								break;

							case Constants.XLAB_BL_EXP:
								ExperimentBudgetLine bl;
								if (Utils.checkIfSaved(context,
										ExperimentAbstract.makeSPName(json
												.getInt("id")),
										ExperimentAbstract.EXP_LIST)) {
									Log.d(TAG, "About to create bl object from JSON from SP");
									createBudgetLineAndTimerFromSharedPrefereces(context, context.getSharedPreferences(ExperimentAbstract.makeSPName(json.getInt("id")),Context.MODE_PRIVATE));

								} else {

									Log.d(TAG, "About to create bl object");
									bl = new ExperimentBudgetLine(context, json);
									downloaded++;
									Log.d(TAG, "downloaded: " + downloaded);

									// construct timer if necessary
									Log.d(TAG,
											"bl.getTimer_status(): "
													+ bl.getTimer_status());
									switch (bl.getTimer_status()) {
									case Constants.TIMER_STATUS_REMINDER:
									case Constants.TIMER_STATUS_RESTRICTIVE:
										JSONObject timerJson = json
												.getJSONObject("timer");
										boolean[] dayEligibility = {
												timerJson
														.getBoolean("boolSunday"),
												timerJson
														.getBoolean("boolMonday"),
												timerJson
														.getBoolean("boolTuesday"),
												timerJson
														.getBoolean("boolWednesday"),
												timerJson
														.getBoolean("boolThursday"),
												timerJson
														.getBoolean("boolFriday"),
												timerJson
														.getBoolean("boolSaturday") };
										switch (bl.getTimer_type()) {
										case Constants.TIMER_STATIC:

											JSONObject startDateJson = timerJson
													.getJSONObject("startDate");
											JSONObject endDateJson = timerJson
													.getJSONObject("endDate");

											new TimerStatic(
													context,
													bl,
													dayEligibility,
													new GregorianCalendar(
															startDateJson
																	.getInt("year"),
															startDateJson
																	.getInt("month") - 1,
															startDateJson
																	.getInt("date")),
													new GregorianCalendar(
															endDateJson
																	.getInt("year"),
															endDateJson
																	.getInt("month") - 1,
															endDateJson
																	.getInt("date")),
													timerJson
															.getInt("startTime"),
													timerJson.getInt("endTime"));
											break;
										case Constants.TIMER_DYNAMIC:
											new TimerDynamic(
													context,
													bl,
													dayEligibility,
													timerJson
															.getInt("min_interval"),
													timerJson
															.getInt("max_interval"));
											break;
										}

									}

								}

							}

						}

					}

				} else {
					successful = false;
				}

				checkForDone();

			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.toString());
				successful = false;
				checkForDone();
			}

		}

	}
	
	public static void createBudgetLineAndTimerFromSharedPrefereces(Context context, SharedPreferences sharedPreferences) {
		
		ExperimentBudgetLine bl = new ExperimentBudgetLine(context, sharedPreferences);

		// construct timer if necessary
		Log.d(TAG, "bl.getTimer_status(): " + bl.getTimer_status());
		switch (bl.getTimer_status()) {
			case Constants.TIMER_STATUS_REMINDER:
			case Constants.TIMER_STATUS_RESTRICTIVE:

			switch (bl.getTimer_type()) {

			case Constants.TIMER_STATIC:

				new TimerStatic(context, bl,
						sharedPreferences, true);
				break;

			case Constants.TIMER_DYNAMIC:
				new TimerDynamic(context, bl, sharedPreferences);
				break;

			}

			break;

		}
		
	}


}
