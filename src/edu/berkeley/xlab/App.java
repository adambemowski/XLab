package edu.berkeley.xlab;

import java.util.concurrent.ConcurrentHashMap;

import android.app.Application;
import edu.berkeley.xlab.xlab_objects.*;

public class App extends Application {
	
	//IMPORTANT! These variables are only used in BackgroundService and MainActivity, the ExpActivities deal directly with SharedPreferences
	
	public static final String TAG = "XLab-App";

	private ConcurrentHashMap<Integer, Experiment> xLabExps;
	
	public Experiment getXLabExp(int id) {
		return xLabExps.get(id);
	}
	
	public ConcurrentHashMap<Integer, Experiment> getXLabExps() {return xLabExps;}
	
	public void appendToXLabExps(Experiment xLabExp){
		this.xLabExps.putIfAbsent(xLabExp.getExpId(), xLabExp);
	}
	
	@Override
	public void onCreate() {
		xLabExps = new ConcurrentHashMap<Integer, Experiment>();
	}
	
}