package edu.berkeley.xlab;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.util.Utils;

public class LoginActivity extends Activity implements OnClickListener {
	
	private EditText etUsername;
	private EditText etPassword;
	private Button btnLogin;
	private TextView lblResult;
	
	public static final String LOGIN_ACTIVITY_TAG = "XLab-LOGIN";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(LOGIN_ACTIVITY_TAG, "In LoginActivity - onCreate");
        ComponentName comp = new ComponentName(this.getApplicationContext().getPackageName(),BackgroundService.class.getName());
		this.getApplicationContext().startService(new Intent().setComponent(comp));

		if(Utils.getBooleanPreference(this, Configuration.IS_LOGGED_IN, false) || 
				Configuration.IS_DEV_MODE) {
			Log.d(LOGIN_ACTIVITY_TAG, "User is logged in - redirect to main screen");
        	Intent intent = new Intent(this, MainActivity.class);
        	startActivity(intent);
			
        } else {
        	Log.d(LOGIN_ACTIVITY_TAG, "User is not logged in - display login form");
			setContentView(R.layout.login);
			
			// Get the EditText and Button References
			etUsername = (EditText)findViewById(R.id.username);
			etPassword = (EditText)findViewById(R.id.password);
			btnLogin = (Button)findViewById(R.id.login_button);
			lblResult = (TextView)findViewById(R.id.result);
			
			// Set Click Listener
			btnLogin.setOnClickListener(this);
        }
	}

	@Override
	public void onClick(View v) {
		if (v == findViewById(R.id.login_button)) {
			String username = this.etUsername.getText().toString().trim();
			String password = this.etPassword.getText().toString().trim();
			
			if(username.equalsIgnoreCase("") || password.equalsIgnoreCase("")) {
				this.lblResult.setText("Please provide a username and password");
			} else {
				Map<String, String> params = new HashMap<String, String>();
				TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
				TimeZone tz = TimeZone.getDefault();
				params.put("api_version", "2");
				params.put("username", username);
				params.put("password", password);
				params.put("platform", "0");
				params.put("identifier", tm.getDeviceId());
				params.put("location_services","none");
				params.put("timezone", tz.getID());
				params.put("key", md5(Configuration.SECRET_KEY + username));
				Log.d(LOGIN_ACTIVITY_TAG, "Hashkey is " + md5(Configuration.SECRET_KEY + username));
				try {
					//TODO: Move network call out of UI thread (use AsyncTask)
					String response = Utils.postData(Configuration.AUTH_API_ENDPOINT, params);
					Log.d(LOGIN_ACTIVITY_TAG, response);
					if(null != response) {
						//Parse the JSON response
						JSONObject jsonObj = new JSONObject(response);
						int responseCode = (Integer) jsonObj.get("code");
						
						if(responseCode == 1) {
							//Save the username in shared preferences
							Utils.setStringPreference(this, Configuration.USERNAME, username);
							Utils.setBooleanPreference(this, Configuration.IS_LOGGED_IN, true);
							
							//Redirect to main screen
				        	Intent intent = new Intent(this, MainActivity.class);
				        	startActivity(intent);
				        	
						} else {
							this.lblResult.setText("The username or password is incorrect.");
						}
					}
				} catch (Exception e) {
					String errStr = "Could not connect to server. " +
							"Please check if you are connected to the internet.";
					this.lblResult.setText(errStr);
					Log.e(LOGIN_ACTIVITY_TAG, e.toString());
				}
			}
			
		}		
	}

	//Courtesy of http://p-xr.com/android-snippet-making-a-md5-hash-from-a-string-in-java/
	private String md5(String in) {
	    MessageDigest digest;
	    try {
	        digest = MessageDigest.getInstance("MD5");
	        digest.reset();
	        digest.update(in.getBytes());
	        byte[] a = digest.digest();
	        int len = a.length;
	        StringBuilder sb = new StringBuilder(len << 1);
	        for (int i = 0; i < len; i++) {
	            sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
	            sb.append(Character.forDigit(a[i] & 0x0f, 16));
	        }
	        return sb.toString();
	    } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
	    return null;
	}
	
}
