package edu.berkeley.xlab.xlab_objects;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public abstract class XLabSuperObject {
	
	/** TAG is an identifier for the log. */
	public static final String TAG = "X-Lab - SuperObject";
		
	/** Integer identifying type of experiment (XLAB_TQ_EXP for XLabTextQuestion, XLABTBQ_EXP for XLabBudgetLineExp, etc...) */
	protected int typeId; public int getTypeId() {return typeId;}
	
	/** Unique id for experiment (made unique by server) */
	protected int expId; public int getExpId() {return expId;}
	
	/**
	 * persistently saves response information for future upload
	 * @param context application context
	 */
	public abstract void save(Context context);

	/** return name of shared Preference */	
	public abstract String getSPName();
	
	/**
	 * Sets list of names of SharedPreferences for responses to another SharedPreferences for persistent store
	 * @param context application context
	 * @param listName name of SharedPreferences that lists other SharedPrefernces of a given type
	 * @param chosenNamesArrayList names of responses' SharedPreferences
	 */
	public static void setList(Context context, String listName, ArrayList<String> chosenNamesArrayList) {
		
		Log.d(TAG, "setList parameters: context: " + context + ", listName: " + listName + ", chosenNamesArrayList: " + chosenNamesArrayList);
		
		SharedPreferences sharedPreferencesList = context.getSharedPreferences(listName, Context.MODE_PRIVATE);
		SharedPreferences.Editor listEditor = sharedPreferencesList.edit();
		
		//clear list before repopulating
		listEditor.clear();
		listEditor.commit();
		
		//repopulate list with outstanding results
		for (String chosenName : chosenNamesArrayList) {
			Log.d(TAG, chosenName);
			listEditor.putString("SharedPreferences", sharedPreferencesList.getString("SharedPreferences","") + chosenName + ",");
			listEditor.commit();
		}
		
	}
		
	/** 
	 * Appends name of SharedPreferences for a response to another SharedPreferences for persistent store
	 * @param context application context
	 * @param listName name of SharedPreferences that lists other SharedPrefernces of a given type
	 */
	protected void appendList(Context context, String listName) {
		SharedPreferences sharedPreferencesList = context.getSharedPreferences(listName, Context.MODE_PRIVATE);
		SharedPreferences.Editor listEditor = sharedPreferencesList.edit();
		
		listEditor.putString("SharedPreferences", sharedPreferencesList.getString("SharedPreferences","") + getSPName() + ",");
		listEditor.commit();
	}	
	
}
