package edu.berkeley.xlab;

import java.util.concurrent.ConcurrentHashMap;

import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.experiments.*;
import edu.berkeley.xlab.util.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String BACKGROUND_SERVICE_TAG = "XLab-MAIN";

	private TextView textView;

	// private ProgressDialog mSplashDialog = new
	// ProgressDialog(getApplicationContext());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(BACKGROUND_SERVICE_TAG, "In MainActivity -- OnCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		textView = (TextView) findViewById(R.id.TextView01);
		new FetchXLabTask().execute();
	}

	/**
	 * Fetch XLab tasks from the server
	 * 
	 * @author Daniel Vizzini
	 */
	private class FetchXLabTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {

			Log.d(BACKGROUND_SERVICE_TAG,
					"In MainActivity -- FetchXLabTask -- onPreExecute");

			// mSplashDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// mSplashDialog.setMessage("XLab is Loading...");
			// mSplashDialog.setCancelable(false);
			// mSplashDialog.show();
		}

		protected String doInBackground(Void... voids) {

			Log.d(BACKGROUND_SERVICE_TAG,
					"In MainActivity -- FetchXLabTask -- doInBackground");

			String message = "Welcome to XLab.\nA nifty menu is coming soon.\n";

			try {

				App appState = ((App) getApplicationContext());
				String responseBQ = Utils
						.getData(Configuration.XLAB_API_ENDPOINT_BQ);
				Log.d(BACKGROUND_SERVICE_TAG, responseBQ);

				// X-Lab related...
				ConcurrentHashMap<Integer, XLabBinaryQuestion> xLabBinaryQuestions = appState
						.getXLabBinaryQuestions();
				ConcurrentHashMap<Integer, XLabBudgetLineExp> xLabBudgetLineExps = appState
						.getXLabBudgetLineExps();

				if (null != responseBQ) {

					// Parse the response
					String[] geofencesBQ = responseBQ.split("\n");
					message += "\nYou have received " + geofencesBQ.length
							+ " questions:\nYour phone currently contains:\n\n";

					for (String line : geofencesBQ) {

						XLabBinaryQuestion bq = new XLabBinaryQuestion(line);
						int id = bq.getId();
						message += bq.getQuestion() + "\n";

						// TODO: The xLabBinaryQuestions map will keep growing
						// if we do this.
						// Clear old values.
						if (!xLabBinaryQuestions.containsKey(id)) {
							xLabBinaryQuestions.put(id, bq);
						}
					}

					appState.setXLabBinaryQuestions(xLabBinaryQuestions);
					Log.d(BACKGROUND_SERVICE_TAG, "Downloaded "
							+ geofencesBQ.length + " BQ geofences.");

				}

				String responseBL = Utils
						.getData(Configuration.XLAB_API_ENDPOINT_BL);
				Log.d(BACKGROUND_SERVICE_TAG, responseBL);

				if (null != responseBL) {

					// Parse the response
					String[] geofencesBL = responseBL.split("\n");
					message += "\nYou have received "
							+ geofencesBL.length
							+ " budget lines:\nYour phone currently contains:\n\n";

					for (String exp : geofencesBL) {

						XLabBudgetLineExp bl = new XLabBudgetLineExp(exp);
						int id = bl.getId();
						message += bl.getTitle() + "\n";
						Log.d(BACKGROUND_SERVICE_TAG, bl.getTitle());

						if (!xLabBudgetLineExps.containsKey(id)) {
							xLabBudgetLineExps.put(id, bl);
						}

					}
					// TODO: The xLabBinaryQuestions map will keep growing if we
					// do this.
					// Clear old values.

					appState.setXLabBudgetLineExps(xLabBudgetLineExps);
					Log.d(BACKGROUND_SERVICE_TAG, "Downloaded "
							+ geofencesBL.length + " geofences.");
				}

			} catch (Exception e) {
				Log.e(BACKGROUND_SERVICE_TAG, e.toString());
			}

			return message;

		}

		@Override
		protected void onPostExecute(String message) {

			Log.d(BACKGROUND_SERVICE_TAG,
					"In MainActivity -- FetchXLabTask -- onPostActivity");

			textView.setText(message);
		}
	}
}