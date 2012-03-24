package edu.berkeley.xlab;

import edu.berkeley.xlab.xlab_objects.Experiment;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

/** TQInstructions set the instructions text for a Text Question experiment from values/strings.
 * @author John Gunnison
 */
public class TQInstructions extends Activity {
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        int id = extras.getInt("ID");    
        
        //At this point I haven't implemented the need for any experiment specific information for the Text questions activity instructions.
        SharedPreferences expInfo = getApplicationContext().getSharedPreferences(Experiment.makeSPName(id), Context.MODE_PRIVATE);
        String title = expInfo.getString("title","");
        
        setContentView(R.layout.instructions);
        
        TextView instructions = (TextView) findViewById(R.id.instructionText);
        
        //add all other conditions here and then set text to appropriate string in Values/Strings.xml
        instructions.setText(R.string.TQinstruction);       
    }   
}
