package edu.berkeley.xlab.experiments;

public class XLabBinaryQuestion {
	int id;
	double lat;
	double lon;
	int radius;
	String question;
	boolean answered;
	
	public XLabBinaryQuestion(int id, double lat, double lon,
			int radius, String question) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.radius = radius;
		this.question = question;
	}		

	public XLabBinaryQuestion(String line) {
		
		String[] parts = line.split(",");
		this.id = Integer.valueOf(parts[0]);
		this.lat = Double.valueOf(parts[1]);
		this.lon = Double.valueOf(parts[2]);
		this.radius = Integer.valueOf(parts[3]);
		this.question = String.valueOf(parts[4]);
			
		//TODO: The xLabBinaryQuestions map will keep growing if we do this.
		//Clear old values.
			
	}

	public int getId() {
		return id;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public int getRadius() {
		return radius;
	}

	public String getQuestion() {
		return question;
	}
	
	public boolean isAnswered() {
		return this.answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}
		
}