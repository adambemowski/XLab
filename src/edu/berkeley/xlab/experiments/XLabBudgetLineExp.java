package edu.berkeley.xlab.experiments;

/*
 * Defines budget-line experiment
 * 
 * @author Daniel Vizzini
 * 
 * The following is an example of a string array that may be received, to illustrate how the objects in the package work:
 * 
 * For each experiment (Java object XLabBudgetLineExperiement):
 * 	id (integer), title, lat, lon, radius, probabilistic (true if you get either X or Y, false if you get both),  prob_x (probability of getting X, only applicable if probabilistic is true), x_label (label of x axis), x_units (units of x axis), x_max (maximum x intercept), x_min (minimum x intercept), y_label (label of y axis), y_uints (units of y axis), y_max (maximum y intercept), y_min (minimum y intercept), 
 * 
 * Then, for each experiment, a number of sessions for the set of budget lines subjects get at a given time (Java object Session)
 * 	"session_parser" (for parsing), id (1 through number of Sessions), line_chosen (which line in the session will actually dictate rewards),
 * 
 * Then, for each session,  a number of lines (Java object Line):
 * 	"line_parser" (for parsing), id (1 through number of Lines), x_int (x-intercept of line), y_int (y-intercept of line), winner ("X" if only X is rewarded, "Y" otherwise, only applicable if probabilistic is true)
 * 
 * Example encompassing a two-session probabilistic experiment in which line and a three-session non-probabilistic experiment (note it will come as one continuous string, with a newline between the experiments):
 * 
 * 14,Muscovite Risk/Reward,55.75,37.70,200,1,0.5,Reward if X chosen,Rubles,1500,750,Reward if Y chosen,Rubles,1500,750,
 * 		session_parser,1,3,
 * 			line_parser,1,800,1000,X,
 * 			line_parser,2,1350,850,X,
 * 			line_parser,3,1150,1250,Y,
 * 			line_parser,4,1150,1250,Y,
 * 		session_parser,2,2,
 * 			line_parser,1,1100,1000,Y,
 * 			line_parser,2,750,1150,X,
 * 			line_parser,3,1450,850,X,
 * 			line_parser,4,850,1050,Y,
 * 16,Kamchatkan Diet Selector,53.01,158.65,200,0,0.5,Regional Fried Dough,Rubles,1000,500,Pickled Produce,Rubles,1000,500,
 * 		session_parser,1,1,
 * 			line_parser,1,800,700,X,
 * 			line_parser,2,750,850,X,
 * 			line_parser,3,550,500,Y,
 * 			line_parser,4,600,750,Y,
 * 		session_parser,2,4,
 * 			line_parser,1,500,600,Y,
 * 			line_parser,2,750,650,X,
 * 			line_parser,3,650,850,X,
 * 			line_parser,4,850,950,Y,
 * 		session_parser,3,1,
 * 			line_parser,1,600,600,Y,
 * 			line_parser,2,650,650,X,
 * 			line_parser,3,650,950,X,
 * 			line_parser,4,750,950,Y,"
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

	public XLabBudgetLineExp(String exp) {
			
		String[] ses = exp.split("session_parser,");
	    Session[] sessions = new Session[ses.length - 1];
		
		String[] header = ses[0].split(",");
		
		int id = Integer.valueOf(header[0]);
		String title = header[1];
		double lat = Double.valueOf(header[2]);
		double lon = Double.valueOf(header[3]);
		int radius = Integer.valueOf(header[4]);
		
		boolean probabilistic = ((header[5] == "1") ? true : false);
		double prob_x = Double.valueOf(header[6]);
		
	    String x_label = header[7];
	    String x_units = header[8];
	    int x_max = Integer.valueOf(header[9]);
	    int x_min = Integer.valueOf(header[10]);
	    
	    String y_label = header[11];
	    String y_units = header[12];
	    int y_max = Integer.valueOf(header[13]);
	    int y_min = Integer.valueOf(header[14]);
	    		    
	    for (int i = 1; i < sessions.length; i++) {
	    	
	    	String[] lines = ses[i].split("line_parser,");//line as in budget line, not line of text
		    Line[] lineInput = new Line[lines.length];
		    
		    header = lines[0].split(",");
		    
		    for (int j = 1; j < lineInput.length; j++) {
		    	String[] parts = lines[j].split(",");
		    	lineInput[j - 1] = new Line(Integer.valueOf(parts[0]),Double.valueOf(parts[1]),Double.valueOf(parts[2]),parts[3].charAt(0));
		    }
		    
		    sessions[i-1] = new Session(Integer.valueOf(header[0]),Integer.valueOf(header[1]),lineInput);
			
	    }
	    
		this.id = id; this.title = title;
		this.lat = lat; this.lon = lon; this.radius = radius;
		this.probabilistic = probabilistic; this.prob_x = prob_x;
		this.x_label = x_label; this.x_units = x_units; this.x_max = x_max; this.x_min = x_min;
		this.y_label = y_label; this.y_units = y_units; this.y_max = y_max; this.y_min = y_min;

	}

}