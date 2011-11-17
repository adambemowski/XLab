package edu.berkeley.xlab;

import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.util.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

public class BinaryQuestionActivity extends Activity implements OnClickListener {
	
	int bqId;
	private String question;
	private String answer;
	private EditText etAnswer;
	private Button btnSubmit;
	String username; 
	
	public static final String BQ_ACIVITY_TAG = "XLab-BQ";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.binary_question);
		
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		
		this.bqId = extras.getInt("bq_id");
		this.question = extras.getString("bq_question");
		
		Log.d(BQ_ACIVITY_TAG, "Received question onCreate - " + this.question);
		
		
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
    	
    	Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		
		this.bqId = extras.getInt("bq_id");
		this.question = extras.getString("bq_question");
		
		Log.d(BQ_ACIVITY_TAG, "Received question onResume - " + this.question);    	
    }

	@Override
	public void onClick(View v) {
		if (v == findViewById(R.id.submit_button)) {
            this.answer = this.etAnswer.getText().toString().trim();	
		try {
			String response = Utils.getData(Configuration.XLAB_API_ENDPOINT_BQ + 
					"?bq_id=" + this.bqId + "&bq_response=" + answer + "&bq_username=" + this.username);			
			Log.d(BQ_ACIVITY_TAG, response);
			
			if(null == response) {
				//TODO: Check for response and retry if it failed
				Log.e(BQ_ACIVITY_TAG, "Received null response");
			} else if(response.equalsIgnoreCase("1")) {
				Log.d(BQ_ACIVITY_TAG, "Received response from server - " + response);
				
				TextView questionSet = (TextView) findViewById(R.id.submit_status);
				questionSet.setText("Thank you for the response!");
			} else if(response.equalsIgnoreCase("0")) {
				Log.d(BQ_ACIVITY_TAG, "Received response from server - " + response);
				
				TextView questionSet = (TextView) findViewById(R.id.submit_status);
				questionSet.setText("Sorry, an error occurred!");
			}
			
		} catch (Exception e) {
			Log.e(BQ_ACIVITY_TAG, e.toString());
		}
		}
	}
}
