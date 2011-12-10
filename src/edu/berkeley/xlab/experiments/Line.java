package edu.berkeley.xlab.experiments;
public class Line {
	
	private int id; public int getId() {return id;}
	private float x_int; public float getX_int() {return x_int;}//server-side Monte Carlo
	private float y_int; public float getY_int() {return y_int;}//server-side Monte Carlo
	private char winner; public char getWinner() {return winner;}//server-side Monte Carlo
	
	public Line (int id, float x_int, float y_int, char winner) {
		this.id = id;
		this.x_int = x_int;
		this.y_int = y_int;
		this.winner = winner;//for risk-reward studies
	}
	
}
