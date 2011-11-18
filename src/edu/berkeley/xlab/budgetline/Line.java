package edu.berkeley.xlab.budgetline;
public class Line {
	
	private int id; public int getId() {return id;}
	private double x_int; public double getX_int() {return x_int;}//server-side Monte Carlo
	private double y_int; public double getY_int() {return y_int;}//server-side Monte Carlo
	private char winner; public char getWinner() {return winner;}//server-side Monte Carlo
	
	public Line (int id, double x_int, double y_int, char winner) {
		this.id = id;
		this.x_int = x_int;
		this.y_int = y_int;
		this.winner = winner;//for risk-reward studies
	}
	
}
