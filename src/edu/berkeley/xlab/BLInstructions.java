package edu.berkeley.xlab;

import edu.berkeley.xlab.xlab_objects.Experiment;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

/** BLInstructions set the instructions text for a BudgetLine experiment from values/strings
 * based on experiment specific information from shared preferences.
 * @author John Gunnison
 */
public class BLInstructions extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        int id = extras.getInt("ID");    
        SharedPreferences expInfo = getApplicationContext().getSharedPreferences(Experiment.makeSPName(id), Context.MODE_PRIVATE);
        boolean probabalistic = expInfo.getBoolean("probabalistic", false);
        
        setContentView(R.layout.instructions);
        
        TextView instructions = (TextView) findViewById(R.id.instructionText);
        
        //add all other conditions here and then set text to appropriate string in Values/Strings.xml
        if (probabalistic) {
            instructions.setText(R.string.BLinstrction_prob);
        } else {
            instructions.setText(R.string.BLinstrction_NoProb);
        }       
    }   
}
