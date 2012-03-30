package edu.berkeley.xlab;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import edu.berkeley.xlab.BackgroundService;
import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.*;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends ListActivity {

	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab-MAIN";
	
	/** instance of (single-task) class for inner class*/
	private final ListActivity LIST_ACTIVITY = MainActivity.this;
	
	/** application context for Shared Preferences */
	private Context context;

	/** boolean for if GPS is currently operating */
	private boolean backgroundRunning;
	
	/** expNames is an array of names of all the SharedPreferences of saved experiments*/
	String[] expNames;

	@Override
	public void onCreate(Bundle bundle) {

		super.onCreate(bundle);
		Log.d(TAG, "In MainActivity -- OnCreate");
		context = getApplicationContext();
		new RefreshExperiments(context, LIST_ACTIVITY).execute();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		expNames = context.getSharedPreferences(ExperimentAbstract.EXP_LIST, Context.MODE_PRIVATE).getString("SharedPreferences", "").split(",");
		populate();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		backgroundRunning = false;
		getMenuInflater().inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	public void onBackPressed()
	{
		moveTaskToBack(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.download:
				new RefreshExperiments(getApplicationContext(), MainActivity.this).execute();
				return true;
			case R.id.gpsToggle:
				ComponentName comp = new ComponentName(context.getPackageName(),BackgroundService.class.getName());
				Log.d(TAG, "In GPS Toggle");				
				if (backgroundRunning) {
					Log.d(TAG, "backgroundRunning was true");					
					backgroundRunning = false;
					context.stopService(new Intent().setComponent(comp));
					item.setTitle(getString(R.string.menu_turnon));
					return true;				
				} else {
					Log.d(TAG, "backgroundRunning was false");					
					backgroundRunning = true;
					context.startService(new Intent().setComponent(comp));
					item.setTitle(getString(R.string.menu_turnoff));
					return true;
				}
			case R.id.clearSPs:
				
				String[] expNames = context.getSharedPreferences(ExperimentAbstract.EXP_LIST, Context.MODE_PRIVATE).getString("SharedPreferences", "").split(",");
				
				if (!expNames[0].equals("")) {
					SharedPreferences sharedPreferences;
					for (String expName : expNames) {
						sharedPreferences = context.getSharedPreferences(expName, Context.MODE_PRIVATE);
						switch(sharedPreferences.getInt("typeId", -1)) {
						case Constants.XLAB_TQ_EXP:
							new ExperimentTextQuestion(context,sharedPreferences).clearSharedPreferences(context);
							break;
						case Constants.XLAB_BL_EXP:
							new ExperimentBudgetLine(context,sharedPreferences).clearSharedPreferences(context);
							break;
						}
					}
				}		

				//new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + ExperimentAbstract.EXP_LIST + ".xml").delete();//unnecessary, but good housekeeping
				context.getSharedPreferences(ExperimentAbstract.EXP_LIST, Context.MODE_PRIVATE).edit().clear();//unnecessary, but good housekeeping

				String[] responseNames = context.getSharedPreferences(ResponseAbstract.RESPONSES_LIST, Context.MODE_PRIVATE).getString("SharedPreferences", "").split(",");
				
				if (!responseNames[0].equals("")) {
					for (String responseName : responseNames) {
						//new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + responseName + ".xml").delete();
						context.getSharedPreferences(responseName, Context.MODE_PRIVATE).edit().clear();
					}
				}		

				//new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + ResponseAbstract.RESPONSES_LIST + ".xml").delete();
				context.getSharedPreferences(ResponseAbstract.RESPONSES_LIST, Context.MODE_PRIVATE).edit().clear();

				new RefreshExperiments(context, this, false).execute();			

				return true;

			case R.id.debugTQ:
				doXLabChecks(37.87213, -122.25787,10);
				return true;
			default:
				return super.onOptionsItemSelected(item);
	    }
	}
	

	/**
	* populate
	*  
	* Populates ListAcitvity with Experiments
	* Big up http://www.tutomobile.fr/personnaliser-une-listview-tutoriel-android-n%C2%B07/04/07/2010/ 
	*/
	public void populate() {
		
		ArrayList<ConcurrentHashMap<String, String>> listItem = new ArrayList<ConcurrentHashMap<String, String>>();			 
	    ConcurrentHashMap<String, String> map;
	    Log.d(TAG, "expNames.length: " + expNames.length);
		final int[] expIds = new int[expNames.length + 1];
		int i = 0;
	    SharedPreferences sharedPreferences;
	    
	    if (!expNames[0].equals("")) {
		    for (String expName : expNames) {
		    	
		    	sharedPreferences = context.getSharedPreferences(expName, Context.MODE_PRIVATE);
		    	
			    map = new ConcurrentHashMap<String, String>();
			    map.put("title", sharedPreferences.getString("title", ""));
			    
			    String[] sessionNames = sharedPreferences.getString("sessions", "").split(",");
			    String[] roundNames = context.getSharedPreferences(sessionNames[0], Context.MODE_PRIVATE).getString("rounds", "").split(",");
			    Log.d(TAG,"currSession for rounds left: " + sharedPreferences.getInt("currSession", 0));
			    Log.d(TAG,"currRound for rounds left: " + sharedPreferences.getInt("currRound", 0));
			    int unitsLeft = (sessionNames.length - 1 - sharedPreferences.getInt("currSession", 0)) * roundNames.length + roundNames.length - sharedPreferences.getInt("currRound", 0);
			    map.put("location", (sharedPreferences.getInt("typeId", 0) != 1) ? sharedPreferences.getString("location", "") : unitsLeft + ((unitsLeft == 1) ? " round" : " rounds") + " left");
			    switch(sharedPreferences.getInt("typeId", Constants.XLAB_TQ_EXP)) {
			    case Constants.XLAB_TQ_EXP:
			    	map.put("img", String.valueOf(R.drawable.ic_tq));
			    	break;
			    case Constants.XLAB_BL_EXP:
			    	map.put("img", String.valueOf(R.drawable.ic_bl));
			    	break;
			    }
			    listItem.add(map);		    	
				expIds[i] = sharedPreferences.getInt("expId", -1);
				i++;
		    }	    	
	    }
	    
	    map = new ConcurrentHashMap<String, String>();
	    map.put("title", "Refresh Experiments");
	    map.put("location", "");
	    map.put("img", String.valueOf(R.drawable.ic_menu_refresh));
	    listItem.add(map);
	    expIds[i] = 0;

	    LIST_ACTIVITY.setListAdapter(new SimpleAdapter (LIST_ACTIVITY, listItem, R.layout.main, new String[] {"img", "title", "location"}, new int[] {R.id.img, R.id.title, R.id.location}));
	    
	    LIST_ACTIVITY.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if (expIds[position] != 0) {
					
					SharedPreferences clickedSP = context.getSharedPreferences(ExperimentAbstract.makeSPName(expIds[position]), Context.MODE_PRIVATE);
					Class<?> activity = null;
					int typeId = clickedSP.getInt("typeId", Constants.XLAB_BL_EXP);
					
					switch(typeId) {
					case Constants.XLAB_TQ_EXP:
				    	activity = ExpActivityTextQuestion.class;
				    	break;
					case Constants.XLAB_BL_EXP:
				    	Log.d(TAG,"Declaring BL Activity: currSession: " + clickedSP.getInt("currSession", -1) + ", currRound: " + clickedSP.getInt("currRound", -1));
						if (clickedSP.getInt("currSession", -1) == 0 && clickedSP.getInt("currRound", -1) == 0) {
					    	activity = InstructionsActivityBudgetLine.class;
						} else {
					    	activity = ExpActivityBudgetLine.class;							
						}
						break;
					}
					
					Intent intent = new Intent(context, activity);
					
					intent.putExtra("expId", expIds[position]);
					
					if (clickedSP.getInt("currSession", -1) == 0 && clickedSP.getInt("currRound", -1) == 0) {
						intent.putExtra("firstRound", true);//will return false if it does not exist
					}
					
					Log.d(TAG, "Experiment.makeSPName(expIds[position]: " + ExperimentAbstract.makeSPName(expIds[position]));
					long nextTime = clickedSP.getLong("nextTime", 2000000000000L);//default timestamp is in the year 2033
										
					if (typeId == Constants.XLAB_BL_EXP) {
						
						if (clickedSP.getInt("timer_status", -1) == Constants.TIMER_STATUS_RESTRICTIVE && System.currentTimeMillis() < nextTime) {
					        
							AlertDialog.Builder timeBuilder = new AlertDialog.Builder(MainActivity.this);

					        timeBuilder.setMessage("You cannot participate in this experiment until " + Utils.getRelativeTime(nextTime) + ". Please try again at this time.");

					        timeBuilder.setPositiveButton("OK", null);
					        AlertDialog timeAlert = timeBuilder.create();
					        timeAlert.show();
					        
						} else {
							LIST_ACTIVITY.startActivity(intent);
						}
					} else {
						LIST_ACTIVITY.startActivity(intent);						
					}
					
				} else {
					new RefreshExperiments(context, LIST_ACTIVITY).execute();
				}
				
		    }
		});		
	}
	
	//TODO: Delete this!
	//DV: For Debugging. Will only be in BackgroundService in Production Version
	private void doXLabChecks(double lat, double lon, float accuracy) {

		Log.d(TAG,"In doXLabChecks");
		for(String expName : expNames) {

			ExperimentAbstract exp;
			SharedPreferences expSP = context.getSharedPreferences(expName, Context.MODE_PRIVATE);
			
			if (expSP.getInt("typeId", -1) == Constants.XLAB_TQ_EXP) {				
				exp = new ExperimentTextQuestion(context, expSP);
			} else {
				exp = new ExperimentBudgetLine(context, expSP);
			}
			
			Log.d(TAG,"Examining " + exp.getTitle() + ": isAnswered = " + exp.isDone() + ": radius = " + exp.getRadius() + ", accuracy = " + accuracy + ", distance = " + Utils.getDistanceFromLatLon(exp.getLat(), exp.getLon(), lat, lon));
			//Consider this fix only if the accuracy is at least 100% of the radius specified in the geofence
			if(! exp.isDone() &&  (exp.getRadius() * 1.0f) >= accuracy && Utils.getDistanceFromLatLon(exp.getLat(), exp.getLon(), lat, lon) <= exp.getRadius()) {

				Log.d(TAG,"Starting thread for " + exp.getTitle());
				//Show a notification
				Thread t = new Thread( new Notifier(context, (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE), exp) );
				t.start();
				
			}
		}
	}
	
}