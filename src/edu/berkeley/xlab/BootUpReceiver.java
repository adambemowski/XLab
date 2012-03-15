package edu.berkeley.xlab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d("BootUpReceiver","Boot up received");
    	new RefreshExperiments(context, true).execute();  
    }

}