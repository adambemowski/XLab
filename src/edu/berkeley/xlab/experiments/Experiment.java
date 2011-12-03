package edu.berkeley.xlab.experiments;

public class Experiment {
	protected int id;	public int getId() {return id;}
	protected double lat;	public double getLat() {return lat;}
	protected double lon;	public double getLon() {return lon;}
	protected int radius;	public int getRadius() {return radius;}
	protected Class activity; public Class getActivity() {return activity;}
	protected String title; public String getTitle() {return title;}
	protected boolean done; public boolean isDone() {return this.done;} 
}
