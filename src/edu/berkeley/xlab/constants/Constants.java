package edu.berkeley.xlab.constants;

public class Constants {
	
	public static final boolean IS_DEV_MODE = false;
	public static final boolean ENABLE_ACCEL_UPLOAD = false;
	
	public static final String AUTH_API_ENDPOINT = "http://ec2-184-72-152-196.compute-1.amazonaws.com/api/v2/auth/?format=json";
	
	public static final int XLAB_TQ_EXP = 0;
	public static final int XLAB_BL_EXP = 1;
	public static final String XLAB_API_ENDPOINT_PREFIX = "http://ec2-184-72-152-196.compute-1.amazonaws.com/xlab/api/";
	//array must be indexed by constants (text question api at XLAB_TQ_EXP index, bl exp api at XLAB_TQ_EXP index, etc..)
	public static final String[] XLAB_API_ENDPOINTS = {XLAB_API_ENDPOINT_PREFIX + "text/", XLAB_API_ENDPOINT_PREFIX + "budget/"};
	
	public static final int TIMER_STATIC = 0;
	public static final int TIMER_DYNAMIC = 1;

	public static final int TIMER_STATUS_NONE = 0;
	public static final int TIMER_STATUS_REMINDER= 1;
	public static final int TIMER_STATUS_RESTRICTIVE = 2;

	public static final int APPLICATION_ID = 1;
	public static final String SECRET_KEY = "fswehw7et912ur2rf7#Y@^nfhfbqwme34f&HB&T24gvdkk";
	
	//Shared preference keys
	public static final String USERNAME = "username";
	public static final String IS_LOGGED_IN = "is_logged_in";
	public static final String CURRENT_MODE = "current_mode";
	public static final String POINTS_SAVED = "points_saved";
	public static final String ACCEL_DATA_SAVE = "accel_data_save";

}