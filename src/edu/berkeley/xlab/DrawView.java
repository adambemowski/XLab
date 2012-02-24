package edu.berkeley.xlab;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** DrawView controls what is displayed on the graph portion of the screen.
 * @author John Gunnison
 */
public class DrawView extends View {

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** slope is the value of the slope of the line. */
    private static double slope;

    /** x is the value of the x intercept. */
    private static double x;

    /** y is the value of the y intercept. */
    private static double y;

    /** x value of moving dot. */
    private static int dotX;

    /** y value of moving dot. */
    private static int dotY;
    
    private DecimalFormat formatter = new DecimalFormat("###,###,##0.00");
    
    private static String xLabel;
    
    private static String yLabel;
    
    private static String xUnit;
    
    private static String yUnit;

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        
        //draw axes.
        canvas.drawLine(30, 400, 30, 0, paint);
        canvas.drawLine(30, 400, 430, 400, paint);
        
        //draw the graph labels.
        paint.setTextSize(23);
        canvas.rotate(-90);
        canvas.drawText(yLabel + ": " + formatter.format(ExpActivityBudgetLine.getY()) + " " + yUnit, -300, 20, paint);
        canvas.rotate(90);
        canvas.drawText( xLabel + ": " + formatter.format(ExpActivityBudgetLine.getX()) + " " + xUnit, 140, 430, paint);
        
        //draw the budget line.
        paint.setColor(Color.RED);
        canvas.drawLine(30, 400 - (int) Math.round(y), 30 + (int) Math.round(x), 400, paint);
        
        //draw the dot.
        paint.setColor(Color.BLACK);
        canvas.drawCircle(dotX, dotY, 7, paint);
    }
    
    public static void setLabels(String x, String y, String xCurrency, String yCurrency) {
        xLabel = x;
        yLabel = y;
        xUnit = xCurrency;
        yUnit = yCurrency;
    }

    /** Sets the x and y value of the dot.
     * @param x1 the new x value of the dot
     */
    public static void setDotValue(int x1) {
        dotX = x1 + 30;
        dotY = (int) (slope * (x1) + 400 - y);
    }

    /** Adds a value to the x coordinates and then
     * changes the y coordinate accordingly.
     * @param add amount being added to the x value of the dot.
     */
    public static void addToX(double add) {
        dotX += add;
        dotY = (int) Math.round(slope * (dotX - 30) + 400 - y);
    }

    /** assigns the slope and x & y intercepts from the Draw class so
     * they can be put on the screen.
     * @param xIntercept value of the x intercept.
     * @param yIntercept value of the y intercept.
     */
    public static void loadLineValues(double xIntercept, double yIntercept) {
        x = xIntercept;
        y = yIntercept;
        slope = y / x;
    }

}
