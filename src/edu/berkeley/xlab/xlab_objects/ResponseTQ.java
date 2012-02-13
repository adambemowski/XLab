package edu.berkeley.xlab.xlab_objects;

import edu.berkeley.xlab.constants.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/** 
 * Object that conveys information about answer
 * 
 * @author dvizzini
 *
 */
public class ResponseTQ extends Response {

	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab-Chosen";

	private String answer; public String getAnswer() {return answer;}
	
	public ResponseTQ(Context context, int expId, String answer) {
		
		Log.d(TAG,"In Chosen explicit constructor");
		
		this.typeId = Configuration.XLAB_TQ_EXP;
		this.expId = expId;
		this.answer = answer;
		
		this.save(context);
		
	}

	/** 
	 * @param sharedPreferences
	 */
	public ResponseTQ(SharedPreferences sharedPreferences) {
		
		Log.d(TAG,"In Chosen SharedPreferences constructor");
		
		this.typeId = Configuration.XLAB_TQ_EXP;
		this.expId = sharedPreferences.getInt("expId",-1);
		this.answer = sharedPreferences.getString("answer","");
		
	}

	@Override
	public void save(Context context) {
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putInt("typeId", typeId);
		editor.putInt("expId", expId);
		editor.putString("answer", answer);
		editor.commit();
		
		appendList(context, Response.RESPONSES_LIST);
		
	}

}