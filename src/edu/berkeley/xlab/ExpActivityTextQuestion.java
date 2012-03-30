package edu.berkeley.xlab;

import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;
import edu.berkeley.xlab.xlab_objects.ExperimentTextQuestion;
import edu.berkeley.xlab.xlab_objects.ResponseTextQuestion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

public class ExpActivityTextQuestion extends ExpActivityAbstract implements OnClickListener {
	
	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab-TQ";
	
	/** exp is the ExperimentTextQuestion. */
	private ExperimentTextQuestion exp;

	/** unique identifier of experiment */
	private String question;
	private String answer;
	private EditText etAnswer;
	private Button btnSubmit;

	@Override
	protected void onStart() {

		super.onStart();
		
		Bundle extras = this.getIntent().getExtras();
		
		initialize(this);
		
		exp = new ExperimentTextQuestion(context, context.getSharedPreferences(ExperimentAbstract.makeSPName(extras.getInt("expId")), Context.MODE_PRIVATE));

		setContentView(R.layout.experiment_text_question);
		
		this.question = exp.getTitle();
		
		Log.d(TAG, "Received question onCreate - " + this.question);
		
		etAnswer = (EditText)findViewById(R.id.answer);
		btnSubmit = (Button)findViewById(R.id.submit_button);
		btnSubmit.setOnClickListener(this);
		
		TextView questionSet = (TextView) findViewById(R.id.question);
		questionSet.setText(this.question);
		
		Log.d(TAG, "Current Answer: " + exp.getAnswer());
		this.etAnswer.setText(exp.getAnswer());

	}
	
	@Override
	protected void onStop() {
	
		super.onStop();
		
		Log.d(TAG, "ET contents: " + this.etAnswer.getText().toString());
		Log.d(TAG, "exp.isDone(): " + exp.isDone());
		
		if (!exp.isDone()) {
			exp.saveState(context, this.etAnswer.getText().toString());			
		}
		
	}
	
	@Override
	public void onClick(View v) {
		if (v == findViewById(R.id.submit_button)) {
            this.answer = this.etAnswer.getText().toString();
			Log.d(TAG,"In onClick");
			
			String confirmationMessage = "\"" + answer + "\" was your answer. Select Confirm to save the selection or Cancel to select another point on this line.";

			AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(ExpActivityTextQuestion.this);
			confirmationBuilder.setMessage(confirmationMessage);
			
			confirmationBuilder.setNegativeButton("Cancel", null);

			confirmationBuilder.setPositiveButton("Confirm",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
							exp.makeDone(context);
							
							new ResponseTextQuestion(context, expId, answer);	
							cleanUpExp(exp);
							
						}
			});
			
			AlertDialog confirmationAlert = confirmationBuilder.create();
			confirmationAlert.show();
			
		}
	}

    @Override
    public void instructionsSelected() {
        Intent i = new Intent("edu.berkeley.xlab.TQINSTRUCTIONS");
        Bundle data = new Bundle();
        data.putInt("ID", exp.getExpId());
        i.putExtras(data);
        startActivity(i);
        
    }	
}
