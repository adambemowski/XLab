package edu.berkeley.xlab;

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

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        
        //draw axes.
        canvas.drawLine(25, 250, 25, 0, paint);
        canvas.drawLine(25, 250, 275, 250, paint);
        
        //draw the graph labels.
        paint.setTextSize(17);
        canvas.rotate(-90);
        canvas.drawText("other stuff: " + (int) Math.round(-(slope * (dotX - 25) - y)) + " rubles", -200, 15, paint);
        canvas.rotate(90);
        canvas.drawText("stuff: " + (dotX - 25) + " rubles", 80, 280, paint);
        
        //draw the budget line.
        paint.setColor(Color.RED);
        canvas.drawLine(25, 250 - Math.round(y), 25 + (int) Math.round(x), 250, paint);
        
        //draw the dot.
        paint.setColor(Color.BLACK);
        canvas.drawCircle(dotX, dotY, 5, paint);
    }

    /** Sets the x and y value of the dot.
* @param x1 the new x value of the dot
*/
    public static void setDotValue(int x1) {
        dotX = x1 + 25;
        dotY = (int) (slope * (x1) + 250 - y);
    }

    /** Adds a value to the x coordinates and then
* changes the y coordinate accordingly.
* @param add amount being added to the x value of the dot.
*/
    public static void addToX(int add) {
        dotX += add;
        dotY = (int) Math.round(slope * (dotX - 25) + 250 - y);
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