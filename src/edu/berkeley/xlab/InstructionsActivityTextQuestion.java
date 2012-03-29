package edu.berkeley.xlab;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/** 
 * TQInstructions set the instructions text for a Text Question experiment from values/strings.
 */
public class InstructionsActivityTextQuestion extends Activity {
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Bundle extras = getIntent().getExtras();
        //int id = extras.getInt("id");    
        
        //John, we'll probably make the instructions a server-specified textblock. That'll be in the SP-DV
        //At this point I haven't implemented the need for any experiment specific information for the Text questions activity instructions. -JG
        /*
        SharedPreferences expInfo = getApplicationContext().getSharedPreferences(ExperimentAbstract.makeSPName(id), Context.MODE_PRIVATE);
        String title = expInfo.getString("title","");
        */
        setContentView(R.layout.instructions_text_question);
        
        TextView instructions = (TextView) findViewById(R.id.instructionText);
        
        //add all other conditions here and then set text to appropriate string in Values/Strings.xml
        instructions.setText(R.string.text_question_instruction);       
    }   
}
