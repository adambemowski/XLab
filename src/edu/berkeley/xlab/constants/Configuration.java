package edu.berkeley.xlab.constants;

public class Configuration {
	public static final boolean IS_DEV_MODE = false;
	public static final boolean ENABLE_ACCEL_UPLOAD = false;
	
	public static final String AUTH_API_ENDPOINT = "http://ec2-107-20-49-145.compute-1.amazonaws.com/api/v2/auth/?format=json";
	public static final String XLAB_API_ENDPOINT_BL = "http://ec2-107-20-49-145.compute-1.amazonaws.com/xlab/api/budget/";
	public static final String XLAB_API_ENDPOINT_BQ = "http://ec2-107-20-49-145.compute-1.amazonaws.com/xlab/api/binary/";
	
	public static final int APPLICATION_ID = 1;
	public static final String SECRET_KEY = "fswehw7et912ur2rf7#Y@^nfhfbqwme34f&HB&T24gvdkk";
	
	//Shared preference keys
	public static final String USERNAME = "username";
	public static final String IS_LOGGED_IN = "is_logged_in";
	public static final String CURRENT_MODE = "current_mode";
	public static final String POINTS_SAVED = "points_saved";
	public static final String ACCEL_DATA_SAVE = "accel_data_save";

}
