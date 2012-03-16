package edu.berkeley.xlab.xlab_objects;

import edu.berkeley.xlab.constants.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/** 
 * Object that information of chosen point
 * 
 * @author dvizzini
 *
 */
public class ResponseBL extends Response {

	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab-Chosen";

	private int sessionId; public int getSessionId() {return sessionId;}
	private int lineId; public int getLineId() {return lineId;}
	private float x_int; public float getX_int() {return x_int;}
	private float y_int; public float getY_int() {return y_int;}
	private float x_chosen; public float getX_chosen() {return x_chosen;}
	private float y_chosen; public float getY_chosen() {return y_chosen;}
	private char winner; public char getWinner() {return winner;}
	private boolean line_chosen_boolean; public boolean getLine_chosen_boolean() {return line_chosen_boolean;}
	
	public ResponseBL(Context context, int expId, int sessionId, int lineId, float x_int, float y_int, float x_chosen, float y_chosen, char winner, boolean line_chosen_boolean) {
		
		Log.d(TAG,"In Chosen explicit constructor");
		
		this.typeId = Constants.XLAB_BL_EXP;
		this.expId = expId;
		this.sessionId = sessionId;
		this.lineId = lineId;
		this.x_int = x_int;
		this.y_int = y_int;
		this.x_chosen = x_chosen;
		this.y_chosen = y_chosen;
		this.winner = winner;
		this.line_chosen_boolean = line_chosen_boolean;
		
		this.save(context);
		
	}

	public ResponseBL(SharedPreferences sharedPreferences) {
		
		Log.d(TAG,"In Chosen SharedPreferences constructor");
		
		this.typeId = Constants.XLAB_BL_EXP;
		this.expId = sharedPreferences.getInt("expId",-1);
		this.sessionId = sharedPreferences.getInt("sessionId",-1);
		this.lineId = sharedPreferences.getInt("lineId",-1);
		this.x_int = (float)sharedPreferences.getFloat("x_int",(float)-1);
		this.y_int = (float)sharedPreferences.getFloat("y_int",(float)-1);
		this.x_chosen = (float)sharedPreferences.getFloat("x_chosen",(float)-1);
		this.y_chosen = (float)sharedPreferences.getFloat("y_chosen",(float)-1);
		this.winner = sharedPreferences.getString("winner","").charAt(0);
		this.line_chosen_boolean = sharedPreferences.getBoolean("line_chosen_boolean",false);
		
	}

	@Override
	public void save(Context context) {
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		editor.putInt("typeId", typeId);
		editor.putInt("expId", expId);
		editor.putInt("lineId", lineId);
		editor.putInt("sessionId", sessionId);
		editor.putFloat("x_int",x_int);
		editor.putFloat("y_int",y_int);
		editor.putFloat("x_chosen",x_chosen);
		editor.putFloat("y_chosen",y_chosen);
		editor.putString("winner", String.valueOf(winner));
		editor.putBoolean("line_chosen_boolean",line_chosen_boolean);
		editor.commit();
		
		appendList(context, Response.RESPONSES_LIST);
		
	}
	
	
	@Override
	public String getSPName() {
		return(RESPONSE_PREFIX + typeId + "_"  + expId + "_" + sessionId + "_" + lineId);		
	}
	
	/**
	 * Creates name for SharedPreferences used to persistently store response
	 * @param typeId Type of experiment (XLAB_BL_EXP)
	 * @param expId Unique experiment id
	 * @param sessionId Identifier of Session of result
	 * @param lineId Identifier of Line of result
	 * @return name of SharedPreferences used to persistently store response
	 */
	public static String getSPName(int typeId, int expId, int sessionId, int lineId) {
		return(RESPONSE_PREFIX + typeId + "_"  + expId + "_" + sessionId + "_" + lineId);
	}
	
}