package edu.berkeley.xlab.budgetline;

public class Session {
	
	private int id; public int getId() {return id;}
	private int line_chosen; public int getLine_chosen() {return line_chosen;}//server-side Monte Carlo
	private Line[] lines; public Line[] getLines() {return lines;}
	
	public Session (int id, int line_chosen, Line[] lines) {
		this.id = id;
		this.line_chosen = line_chosen;
		this.lines = lines;
	}
	
}
