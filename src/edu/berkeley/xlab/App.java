package edu.berkeley.xlab;

import java.util.concurrent.ConcurrentHashMap;

import android.app.Application;
import android.util.Log;
import edu.berkeley.xlab.experiments.*;

public class App extends Application {
	
	public static final String TAG = "XLab-App";

	private ConcurrentHashMap<Integer, Experiment> xLabExps = new ConcurrentHashMap<Integer, Experiment>();
	public ConcurrentHashMap<Integer, Experiment> getXLabExps(){return xLabExps;} 
	public void setXLabExps(ConcurrentHashMap<Integer, Experiment> xLabExps){
		this.xLabExps = xLabExps;
	}
}