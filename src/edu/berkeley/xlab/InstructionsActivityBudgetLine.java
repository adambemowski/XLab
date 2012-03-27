package edu.berkeley.xlab;

import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;
import edu.berkeley.xlab.xlab_objects.ExperimentBudgetLine;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/** 
 * BLInstructions set the instructions text for a BudgetLine experiment from values/strings
 * based on experiment specific information from shared preferences.
 */
public class InstructionsActivityBudgetLine extends Activity implements OnClickListener {
    
	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab-InstructionsActivityBudgetLine";
	
    private ExperimentBudgetLine exp;
    private String currency;
    private Bundle extras;
    private int numSessions;
    private int numRounds;
    private boolean probabilistic;
    private int expId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        extras = getIntent().getExtras();
        expId = extras.getInt("expId");    	
        exp = new ExperimentBudgetLine (getApplicationContext(), getApplicationContext().getSharedPreferences(ExperimentAbstract.makeSPName(expId), Context.MODE_PRIVATE));
        currency = exp.getCurrency();
        numSessions = exp.getSessions().length;
        numRounds = exp.getSession(0).getRounds().length;
        probabilistic = exp.getProbabilistic();
        
        setContentView(R.layout.instructions_budget_line);
    	findViewById(R.id.procede_button).setOnClickListener(this);
        
        //add all other conditions here and then set text to appropriate string in Values/Strings.xml
    	((TextView) findViewById(R.id.text_top)).setText(
    			getString(R.string.budget_line_instruction_top));

		if (probabilistic) {
        	((TextView) findViewById(R.id.text_main_block_0)).setText(
        			getString(R.string.budget_line_instruction_main_prob_00) +
        			_getRewardAsText() + 
        			getString(R.string.budget_line_instruction_main_all_01) + 
        			_getSessionsAndRoundAsText() +
        			getString(R.string.budget_line_instruction_main_all_02));
        } else {
        	((TextView) findViewById(R.id.text_main_block_0)).setText(
        			getString(R.string.budget_line_instruction_main_nonprob_00) +
        			_getRewardAsText() + 
        			getString(R.string.budget_line_instruction_main_all_01) + 
        			_getSessionsAndRoundAsText() +
        			getString(R.string.budget_line_instruction_main_all_02));        	
        }
        
    	((TextView) findViewById(R.id.text_main_block_1)).setText(
    			getString(R.string.budget_line_instruction_main_all_03) +
    			//TODO: add currency plural
    			(!currency.equalsIgnoreCase("-") ? "units of cash-money" : exp.getX_units()) + 
    			getString(R.string.budget_line_instruction_main_all_04) + 
    			//TODO: add currency plural
    			(!currency.equalsIgnoreCase("-") ? "units of cash-money" : exp.getY_units()) + 
    			getString(R.string.budget_line_instruction_main_all_05) +
				//TODO: add currency plural
				(!currency.equalsIgnoreCase("-") ? "units of cash-money" : exp.getX_units()) + 
				getString(R.string.budget_line_instruction_main_all_06) + 
				//TODO: add currency plural
				(!currency.equalsIgnoreCase("-") ? "units of cash-money" : exp.getY_units()) + 
				getString(R.string.budget_line_instruction_main_all_07));
    	
        if (probabilistic) {    	
        	((TextView) findViewById(R.id.text_main_block_2)).setText(
					getString(R.string.budget_line_instruction_main_prob_08) +
					Utils.FORMATTER_PERCENT.format(exp.getProb_x()) +
					getString(R.string.budget_line_instruction_main_prob_09) +
					Utils.FORMATTER_PERCENT.format(1 - exp.getProb_x()) +
					getString(R.string.budget_line_instruction_main_prob_10) +
        			_getSessionsAndRoundAsText() +
					getString(R.string.budget_line_instruction_main_all_11) +
					_getSessionAsText() + 
					getString(R.string.budget_line_instruction_main_all_12));
        } else {
        	((TextView) findViewById(R.id.text_main_block_2)).setText(
					getString(R.string.budget_line_instruction_main_nonprob_08) +
					getString(R.string.budget_line_instruction_main_nonprob_10) +
        			_getSessionsAndRoundAsText() +
					getString(R.string.budget_line_instruction_main_all_11) +
					_getSessionAsText() + 
					getString(R.string.budget_line_instruction_main_all_12));
        }
        
        ((TextView) findViewById(R.id.text_earnings_title)).setText(
    			getString(R.string.budget_line_instruction_earnings_title));

        ((TextView) findViewById(R.id.text_earnings_block_0)).setText(
    			getString(R.string.budget_line_instruction_earnings_00) + 
    			_getSessionAsText() + 
    			getString(R.string.budget_line_instruction_earnings_01) + 
    			numRounds + 
    			getString(R.string.budget_line_instruction_earnings_02));

        ((TextView) findViewById(R.id.text_confidentiality_title)).setText(
    			getString(R.string.budget_line_instruction_confidentiality_title));

        ((TextView) findViewById(R.id.text_confidentiality_block_0)).setText(
    			getString(R.string.budget_line_instruction_confidentiality_00));

    	((TextView) findViewById(R.id.text_bottom)).setText(
    			getString(R.string.budget_line_instruction_bottom));

    }   

    private String _getSessionsAndRoundAsText() {
    	return numSessions + " session" + (numSessions > 1 ? "s" : "") +  " of " + numRounds + " round" + (numRounds > 1 ? "s" : "");
    }

    private String _getSessionAsText() {
    	return (numSessions != 1 ? "each session" : "the experiment");
    }

    private String _getRewardAsText() {
    	return (!currency.equalsIgnoreCase("-") ? "money is " : exp.getX_label() + " and " + exp.getY_label() + " are");
    }
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.d(TAG,"extras.getBoolean(\"firstRound\"): " + extras.getBoolean("firstRound"));
		if (v.getId() == R.id.procede_button) {
			if (extras.getBoolean("firstRound")) {
				Log.d(TAG,"expId: " + expId);
				Intent intent = new Intent(getApplicationContext(), ExpActivityBudgetLine.class);
				intent.putExtra("expId", expId);
				startActivity(intent);
			} else {
				Log.d(TAG,"Finishing InstructionsActivityBudgetLine");
				InstructionsActivityBudgetLine.this.finish();
			}
		}		
	}
}