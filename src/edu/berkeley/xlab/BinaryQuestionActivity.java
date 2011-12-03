package edu.berkeley.xlab;

import java.net.URLEncoder;

import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.util.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class BinaryQuestionActivity extends Activity implements OnClickListener {
	
	private int bqId;
	private String question;
	private String answer;
	private EditText etAnswer;
	private Button btnSubmit;
	private String username; 
	
	public static final String TAG = "XLab-BQ";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		App appState = ((App) getApplicationContext());
		setContentView(R.layout.binary_question);
		
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		
		this.bqId = extras.getInt("id");
		this.question = appState.getXLabExps().get(bqId).getTitle();
		
		Log.d(TAG, "Received question onCreate - " + this.question);
		
		
		etAnswer = (EditText)findViewById(R.id.answer);
		btnSubmit = (Button)findViewById(R.id.submit_button);
		btnSubmit.setOnClickListener(this);
		
		
		TextView questionSet = (TextView) findViewById(R.id.question);
		questionSet.setText(this.question);
		this.username= Utils.getStringPreference(this, Configuration.USERNAME, "anonymous");
	}
		
	
	@Override
    protected void onResume() {
    	super.onResume();
		App appState = ((App) getApplicationContext());
    	
    	Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		
		this.bqId = extras.getInt("id");
		this.question = appState.getXLabExps().get(bqId).getTitle();
		
		Log.d(TAG, "Received question onResume - " + this.question);    	
    }

	@Override
	public void onClick(View v) {
		if (v == findViewById(R.id.submit_button)) {
            this.answer = this.etAnswer.getText().toString().trim();
            new PostBQ().execute();
		}
	}
	
	private class PostBQ extends AsyncTask<Void, Void, String> {
		
		private int duration = Toast.LENGTH_LONG;
        private ProgressDialog dialog = new ProgressDialog(BinaryQuestionActivity.this);
        private String response;
		private String message;
		private Context context = getApplicationContext();
		private Toast toast;

		@Override
		protected void onPreExecute() {
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        dialog.setMessage("Sending answer to server...");
	        dialog.show();
	    }
		
		@Override	
	    protected String doInBackground(Void... whatever) {
			
			
			try {
				
				response = Utils.getData(Configuration.XLAB_API_ENDPOINT_BQ + "?bq_id=" + bqId + "&bq_response=" + URLEncoder.encode(answer, "utf-8") + "&bq_username=" + username);
				Log.d(TAG, response);
				
				if(null == response) {
					//TODO: Check for response and retry if it failed
					Log.e(TAG, "Received null response");
					message = "Sorry, an error occurred!";
				} else if(response.equalsIgnoreCase("1")) {
					Log.d(TAG, "Received response from server - " + response);
					message = "Thank you for your response.";
				} else if(response.equalsIgnoreCase("0")) {
					Log.d(TAG, "Received response from server - " + response);
					message = "Sorry, an error occurred!";
				}
				
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
	        
	        return message;
	    }
		
		@Override
		protected void onPostExecute(String message) {
			Log.d(TAG,"In onPostExecute");
			dialog.dismiss();
			toast = Toast.makeText(context, message, duration);
			toast.setGravity(Gravity.CENTER, 0,0);
			toast.show();
		}
	    	    
	}
}
