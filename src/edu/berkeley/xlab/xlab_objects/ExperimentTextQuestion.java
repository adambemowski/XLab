package edu.berkeley.xlab.xlab_objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import edu.berkeley.xlab.ExpActivityTextQuestion;
import edu.berkeley.xlab.constants.Configuration;

public class ExperimentTextQuestion extends Experiment {
	
	/** TAG is an identifier for the log. */
	public static final String TAG = "X-Lab - XLabTextQuestion";
	
	/** answer that subject enters into textbox */
	private String answer; public String getAnswer() {return answer;}
	
	public ExperimentTextQuestion(Context context, String line) {
		
		this.identify();
		
		String[] parts = line.split(",");
		this.expId = Integer.valueOf(parts[0]);
		this.location = String.valueOf(parts[1]);
		this.lat = Float.valueOf(parts[2]);
		this.lon = Float.valueOf(parts[3]);
		this.radius = Integer.valueOf(parts[4]);
		this.title = String.valueOf(parts[5]);
		this.answer = "";

	}
	
	public ExperimentTextQuestion(Context context, SharedPreferences sharedPreferences) {
		
		Log.d(TAG,"In XLabTextQuestion SharedPreferences constructor");
		
		this.identify();
		this.expId = sharedPreferences.getInt("expId",-1);
		this.title = sharedPreferences.getString("title","");
		this.location = sharedPreferences.getString("location","");
		this.lat = sharedPreferences.getFloat("lat",(float) -1);
		this.lon = sharedPreferences.getFloat("lon",(float) -1);
		this.radius = sharedPreferences.getInt("radius", -1);
		this.answer = sharedPreferences.getString("answer", "");
				
	}
	
	@Override
	public void save(Context context) {//would prefer protected, limitation of Interface
		
		Log.d(TAG, "Saving " + title);
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putInt("typeId", typeId);
		editor.putInt("expId", expId);
		editor.putString("title", title);
		editor.putString("location", location);
		editor.putFloat("lat", lat);
		editor.putFloat("lon", lon); 
		editor.putInt("radius", radius);
		editor.commit();
		
		appendList(context, EXP_LIST);
		
	}
	
	public void saveState(Context context, String currentAnswer) {
		
		this.answer = currentAnswer;
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putString("answer", currentAnswer);
		editor.commit();

	}
	
	/** method of instantiations common to all constructors */
	private void identify() {
		this.typeId = Configuration.XLAB_TQ_EXP;
		this.activity = ExpActivityTextQuestion.class;		
	}

}