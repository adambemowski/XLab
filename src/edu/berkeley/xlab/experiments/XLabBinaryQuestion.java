package edu.berkeley.xlab.experiments;

import edu.berkeley.xlab.BinaryQuestionActivity;

public class XLabBinaryQuestion extends Experiment {
	
	public XLabBinaryQuestion(String line) {
		
		String[] parts = line.split(",");
		this.id = Integer.valueOf(parts[0]);
		this.location = String.valueOf(parts[1]);
		this.lat = Double.valueOf(parts[2]);
		this.lon = Double.valueOf(parts[3]);
		this.radius = Integer.valueOf(parts[4]);
		this.title = String.valueOf(parts[5]);
		this.activity = BinaryQuestionActivity.class;
			
	}
	
}