package edu.berkeley.xlab.experiments;

import edu.berkeley.xlab.BinaryQuestionActivity;

public class XLabBinaryQuestion extends Experiment {
	
	public static final int CONSTANT_ID = 0;
	
	public XLabBinaryQuestion(String line) {
		
		String[] parts = line.split(",");
		this.id = Integer.valueOf(parts[0]);
		this.classId = CONSTANT_ID;
		this.lat = Double.valueOf(parts[1]);
		this.lon = Double.valueOf(parts[2]);
		this.radius = Integer.valueOf(parts[3]);
		this.title = String.valueOf(parts[4]);
		this.activity = BinaryQuestionActivity.class;
			
	}
	
}