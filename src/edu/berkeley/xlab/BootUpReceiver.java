package edu.berkeley.xlab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.xlab_objects.ExperimentAbstract;
import edu.berkeley.xlab.xlab_objects.ExperimentTextQuestion;

public class BootUpReceiver extends BroadcastReceiver{

	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab - BootUpReceiver";

	//TODO: Implement this? https://github.com/commonsguy/cwac-wakeful
	
	@Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(TAG,"Boot up received");
		String[] names = context.getSharedPreferences(ExperimentAbstract.EXP_LIST, Context.MODE_PRIVATE).getString("SharedPreferences", "").split(",");
		
		if (!names[0].equals("")) {
			for (String expName : names) {
				SharedPreferences sharedPreferences = context.getSharedPreferences(expName,Context.MODE_PRIVATE);
				switch(sharedPreferences.getInt("typeId", -1)){
				case Constants.XLAB_TQ_EXP:
					Log.d(TAG, "About to create tq " + expName + " object from SP");
					new ExperimentTextQuestion(context, sharedPreferences);
					break;
				case Constants.XLAB_BL_EXP:
					Log.d(TAG, "About to create bl " + expName + " object from SP");
					Communicator.createBudgetLineAndTimerFromSharedPrefereces(context, context.getSharedPreferences(expName,Context.MODE_PRIVATE));
					break;
				}
			}
		}			
    }
}