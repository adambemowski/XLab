package edu.berkeley.xlab;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.SeekBar;
import android.widget.TextView;

/**
* Draw is an activity that controls choosing a point on a line and recording
* that choice.
*
* @author John Gunnison
*/
public class BudgetLineActivity extends Activity implements SeekBar.OnSeekBarChangeListener,
                OnClickListener, OnTouchListener {

    /** BQ_AVTIVITY_TAG is an identifier for the log. */
    static final String BL_ACIVITY_TAG = "XLab-BL";

    /** _progress is the current value of the slider. */
    private int _progress;

    /** xValue is the displayed value of the x coordinate of the dot. */
    private TextView xValue;

    /** yValue is the displayed value of the y coordinate of the dot. */
    private TextView yValue;

    /** layout is the custom view created to hold the graph. */
    private View layout;

    /** seekBar object that is controlled by the user on screen. */
    private SeekBar seekBar;

    /** x is the value of the x intercept. */
    private double x;

    /** y is the value of the y intercept. */
    private double y;

    /** slope is the value of the slope of the line. */
    private double slope;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_line);
        Log.d(BL_ACIVITY_TAG, "In onCreate method");

        layout = findViewById(R.id.layout);
        layout.setBackgroundColor(Color.WHITE);

        View leftButton = findViewById(R.id.left_button);
        leftButton.setOnTouchListener(this);

        View rightButton = findViewById(R.id.right_button);
        rightButton.setOnTouchListener(this);

        View selectButton = findViewById(R.id.select_button);
        selectButton.setOnClickListener(this);

        seekBar = (SeekBar) findViewById(R.id.slider);
        seekBar.setOnSeekBarChangeListener(this);
        xValue = (TextView) findViewById(R.id.x_value);
        yValue = (TextView) findViewById(R.id.y_value);

        newLine();
        layout.invalidate();
    }

    @Override
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
        _progress = progress;
        DrawView.setDotValue(progress);
        layout.invalidate();
        xValue.setText("X = " + progress);
        yValue.setText("Y = " + (int) (-slope * progress + y));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekbar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.select_button) {
            newLine();
        }
    }

    private boolean holdThreadRunning = false;
    private boolean cancelHoldThread = false;
    private Handler handler = new Handler();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            handleHoldDown(v);
            return true;
        case MotionEvent.ACTION_UP:
            handleHoldUp(v);
            return true;
        default:
            return false;
        }
    }

/* handleHoldDown is the method that is called when a button
* is pushed down. It check to see if the hold thread is running
* and if it isn't, calls startHoldThread which starts the thread.
* @param v A view object that knows what button has been pushed.
*/
    private void handleHoldDown(View v) {
        if (!holdThreadRunning) {
            startHoldThread(v);
        }
    }
    
    /** startHoldThread defines a thread that runs an inner thread. The inner thread controls what
* happens when a button is in the down position and how often that action occurs. In this case,
* when the left and right buttons are held down 1 gets subtracted or added, respectively, to
* the x value of the graph every 40 milliseconds. The outer thread initially sleeps for 300
* milliseconds to give the user a chance to just click the button without causing continuous scrolling.
* @param v A view object that knows what button has been pushed.
*/
    private void startHoldThread(final View v) {
        Thread r = new Thread() {

            @Override
            public void run() {
                try {
                    holdThreadRunning = true;
                    Thread.sleep(300);
                    while (!cancelHoldThread) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                switch (v.getId()) {
                                case R.id.left_button:
                                    if (_progress > 0) {
                                        DrawView.addToX(-1);
                                        _progress -= 1;
                                        seekBar.setProgress(_progress);
                                        xValue.setText("X = " + _progress);
                                        yValue.setText("Y = " + (int) Math.round(-slope * _progress + y));
                                    }
                                    break;
                                case R.id.right_button:
                                    if (_progress < x) {
                                        DrawView.addToX(1);
                                        _progress += 1;
                                        seekBar.setProgress(_progress);
                                        xValue.setText("X = " + _progress);
                                        yValue.setText("Y = " + (int) Math.round(-slope * _progress + y));
                                    }
                                    break;
                                }
                                layout.invalidate();
                            }
                        });

                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(
                                            "Could not wait between adding 1 to x.", e);
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException ("Could not initially wait for button hold");
                }
                finally
                {
                    holdThreadRunning = false;
                    cancelHoldThread = false;
                }
            }
        };
        // actually start the thread, after defining it
        r.start();
    }
    
    /** handleHoldUp is the method is called when a button is released. It sets the
* cancelHoldThread boolean to true, which tells the thread in startHoldThread
* that the button has been released so it should stop changing the x value of
* the graph. It then proceeds to modify the x axis one more unit to the left
* or right, depending on which button was pushed.
* @param v A view object that knows what button has been pushed.
*/
    private void handleHoldUp(View v) {
        cancelHoldThread = true;
        switch (v.getId()) {
        case R.id.left_button:
            if (_progress > 0) {
                DrawView.addToX(-1);
                _progress -= 1;
                seekBar.setProgress(_progress);
                xValue.setText("X = " + _progress);
                yValue.setText("Y = " + (int) Math.round(-slope * _progress + y));
            }
            break;
        case R.id.right_button:
            if (_progress < x) {
                DrawView.addToX(1);
                _progress += 1;
                seekBar.setProgress(_progress);
                xValue.setText("X = " + _progress);
                yValue.setText("Y = " + (int) Math.round(-slope * _progress + y));
            }
            break;
        }
        layout.invalidate();
    }


    /** newLine applies values for a new graph and resets the slider. */
    public void newLine() {
        Log.d(BL_ACIVITY_TAG, "NewLine method, creating new line");
        double[] coor = getInterceptCoordinates();
        seekBar.setMax((int) coor[0]);
        _progress = (int) Math.round(coor[0] / 2);
        seekBar.setProgress(_progress);
        DrawView.loadLineValues(coor[0], coor[1]);
        DrawView.setDotValue(_progress);
        xValue.setText("X = " + _progress);
        yValue.setText("Y = " + (int) Math.round(-slope * _progress + y));
        layout.invalidate();
    }

/* getInterceptCoordinates generates a random x and y intercept.
* @return integer array containing x and y intercepts.
*/
    public double[] getInterceptCoordinates() {
        double a = Math.random();
        double b = Math.random();

        double[] interceptCoordinates = new double[2];
        x = (25 + Math.round(a * 200));
        interceptCoordinates[0] = x;
        y = (25 + Math.round(b * 200));
        interceptCoordinates[1] = y;

        slope = y / x;
        return interceptCoordinates;
    }
}