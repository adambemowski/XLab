package edu.berkeley.xlab;

import java.util.concurrent.ConcurrentHashMap;

import android.app.Application;
import edu.berkeley.xlab.experiments.*;

public class App extends Application {
	
	private ConcurrentHashMap<Integer, XLabBinaryQuestion> xLabBinaryQuestions = new ConcurrentHashMap<Integer, XLabBinaryQuestion>();
	public ConcurrentHashMap<Integer, XLabBinaryQuestion> getXLabBinaryQuestions(){return xLabBinaryQuestions;} 
	public void setXLabBinaryQuestions(ConcurrentHashMap<Integer, XLabBinaryQuestion> xLabBinaryQuestions ){this.xLabBinaryQuestions = xLabBinaryQuestions;}

	private ConcurrentHashMap<Integer, XLabBudgetLineExp> xLabBudgetLineExps = new ConcurrentHashMap<Integer, XLabBudgetLineExp>();
	public ConcurrentHashMap<Integer, XLabBudgetLineExp> getXLabBudgetLineExps(){return xLabBudgetLineExps;} 
	public void setXLabBudgetLineExps(ConcurrentHashMap<Integer, XLabBudgetLineExp> xLabBudgetLineExps ){this.xLabBudgetLineExps = xLabBudgetLineExps;}

}