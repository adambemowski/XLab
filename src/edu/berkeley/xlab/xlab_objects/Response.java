package edu.berkeley.xlab.xlab_objects;

/** 
 * Object that information of chosen point
 * 
 * @author dvizzini
 *
 */
public abstract class Response extends XLabSuperObject {
	
	/** Filename of list of SharedPreferences of responses given*/
	public static final String RESPONSES_LIST = "response_shared_preference_list";
	
	/** Filename prefix for persistent memory of Response*/
	public static final String RESPONSE_PREFIX = "Response_";
	
	@Override
	public String getSPName() {
		return (RESPONSE_PREFIX + expId);
	}
	
}