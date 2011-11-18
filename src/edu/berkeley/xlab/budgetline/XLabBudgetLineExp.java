package edu.berkeley.xlab.budgetline;

/**
 * Defines budget-line experiment
 * 
 * @author Daniel Vizzini
 */
public class XLabBudgetLineExp {

	private int id; public int getId() {return id;}
	private String title; public String getTitle() {return title;}
	private double lat; public double getLat() {return lat;}
	private double lon; public double getLon() {return lon;}
	private int radius; public int getRadius() {return radius;}
	
	private boolean probabilistic; public boolean getProbabilistic() {return probabilistic;}
	private double prob_x; public double getProb_x() {return prob_x;}
	
	private String x_label; public String getX_label() {return x_label;}
	private String x_units; public String getX_units() {return x_units;}
	private int x_max; public int getX_max() {return x_max;}
	private int x_min; public int getX_min() {return x_min;}
    
	private String y_label; public String getY_label() {return y_label;}
	private String y_units; public String getY_units() {return y_units;}
	private int y_max; public int getY_may() {return y_max;}
	private int y_min; public int getY_min() {return y_min;}

	private Session[] sessions; public Session[] getSessions() {return sessions;}
	
	private int currSession = 0; public int getCurrSession() {return currSession;} public int nextCurrSession() {currSession++; return (currSession - 1);}
	private int currLine = 0; public int getCurrLine() {return currLine;} public int nextCurrLine() {currLine++; return (currLine - 1);}
	
	public XLabBudgetLineExp(int id, String title,
			double lat, double lon, int radius, 
			boolean probabilistic, double prob_x,
			String x_label, String x_units, int x_max, int x_min,
			String y_label, String y_units, int y_max, int y_min,
			Session[] sessions) {
		this.id = id; this.title = title;
		this.lat = lat; this.lon = lon; this.radius = radius;
		this.probabilistic = probabilistic; this.prob_x = prob_x;
		this.x_label = x_label; this.x_units = x_units; this.x_max = x_max; this.x_min = x_min;
		this.y_label = y_label; this.y_units = y_units; this.y_max = y_max; this.y_min = y_min;
	}

}