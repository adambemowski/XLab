package edu.berkeley.xlab;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.berkeley.xlab.constants.Constants;
import edu.berkeley.xlab.xlab_objects.Experiment;
import edu.berkeley.xlab.xlab_objects.ResponseBL;
import edu.berkeley.xlab.timers.TimerDynamic;
import edu.berkeley.xlab.timers.TimerStatic;
import edu.berkeley.xlab.timers.TimerSuperClass;
import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.*;

/**
 * An activity that controls choosing a point on a line and recording
 * that choice.
 * 
 * @author John Gunnison and Daniel Vizzini
 */
public class ExpActivityBudgetLine extends ExpActivitySuperclass implements SeekBar.OnSeekBarChangeListener, OnClickListener, OnTouchListener {

	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab-BL";

	/** The SharedPreferences associated with exp */
	private SharedPreferences sharedPreferences;

	/** exp is the ExperimentBudgetLine. */
	private ExperimentBudgetLine exp;

	/** Timer is the TimerSuperClass object associated with the Experiment. */
	private TimerSuperClass timer;

	/** probabilistic is true if the experiment is probabilistic, false otherwise */
	private boolean probabilistic;

	/** progress is the current value of the slider. */
	private static int progress;

	/** explanation is the displayed value of the explanation of what will happen if the line is selected. */
	private TextView explanation;

	/** layout is the custom view created to hold the graph. */
	private View layout;

	/** seekBar object that is controlled by the user on screen. */
	private static SeekBar seekBar;

	/** x is the value of the x intercept. */
	private static float x;

	/** y is the value of the y intercept. */
	private static float y;

	/** slope is the value of the slope of the line. */
	private static float slope;

	/** unique identifier of current session */
	private int currentSession;
	
	/** unique identifier of current line */
	private int currentLine;
	
	/** axis chosen in probabilistic experiment */
	private char winner;
	
	/** true if line is state should be saved in onStop, false otherwise, false otherwise */
	private boolean saveStateBoolean;
	
	/** true if line is confirmed done. */
	private boolean lineDone;
	
	/** true if line is selected in session to be the one from which subject wins goods, false otherwise */
	private boolean line_chosen_boolean;
	
	/** this activity to pass to methods in other objects */
	Activity activity;
	
	/** the currency character ($, €, or '-', where '-' indicates a non-monetary reward) */
	char currency;
	
	/** the maximum of X_max and Y_max. */
	float max;
	
	private boolean holdThreadRunning = false;
	private boolean cancelHoldThread = false;
	private Handler handler = new Handler();
	
	/** Called when the activity is first created. */
	@Override
	public void onStart() {
		
		super.onStart();
		
		this.activity = this;
		
		//call superclass method
		initialize(this);
		
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		
		sharedPreferences = context.getSharedPreferences(Experiment.makeSPName(extras.getInt("expId")), Context.MODE_PRIVATE);
		
		holdThreadRunning = false;
		cancelHoldThread = false;
		
		setContentView(R.layout.budget_line);

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
		explanation = (TextView) findViewById(R.id.explanation);

		exp = new ExperimentBudgetLine(context, sharedPreferences);
		currency = exp.getCurrency();

		Log.d(TAG,"exp.isDone(): "+ exp.isDone());
		
		//in case experiment has timed out
		if (exp.isDone()) {
			makeMessageAndClean((currentSession - 1) % exp.getSessions().length, true);									
		} else if (exp.getTimer_status() != Constants.TIMER_STATUS_NONE) {
			Log.d(TAG,"exp.getTimer_type() " + exp.getTimer_type() );
			switch(exp.getTimer_type()) {
			case Constants.TIMER_STATIC:
				
				TimerStatic timerStatic;
				
				Log.d(TAG,"About to instantiate TimerStatic for " + exp.getExpId());
				timer = new TimerStatic(context, exp, sharedPreferences, false);
				timerStatic = ((TimerStatic) timer);
				
				//update timer if restrictive
				if (exp.getTimer_status() == Constants.TIMER_STATUS_RESTRICTIVE) {
					exp = timerStatic.getExpBL();					
				}
				
				//check if done now
				if (exp.isDone()) {
					
					makeMessageAndClean((currentSession - 1) % exp.getSessions().length, true);						
					
				} else {
					
					int numSkipped = exp.getNumSkipped();
					Log.d(TAG, "numSkipped: " + numSkipped);
					
					//inform of skipped lines
					if(numSkipped > 0 && exp.getTimer_status() == Constants.TIMER_STATUS_RESTRICTIVE) {
						
						String message;
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						
						if (numSkipped == 1) {
							message = "You did not respond to 1 prompt. If the line associated with this prompt is chosen for your rewared, you will receive nothing.";
						} else {
							message = "You did not respond to " + numSkipped + " prompts. If any of the lines associated with these prompts are chosen for your rewared, you will receive nothing.";
						}
						
						builder.setMessage(message);
						builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								exp.resetNumSkipped(context);
								initialize();
							}
						});
					
						AlertDialog alert = builder.create();
						alert.show();
		
					} else {
						initialize();						
					}
					
				}
				break;
			case Constants.TIMER_DYNAMIC:
				Log.d(TAG,"About to instantiate TimerDynamic for " + exp.getExpId());
				timer = new TimerDynamic(context, activity, exp, sharedPreferences);
				initialize();
				break;
			}
		} else {
			initialize();
		}
	}
	
	private void initialize() {
		probabilistic = exp.getProbabilistic();
		Log.d(TAG, "probabilistic = " + probabilistic);
		
		TextView title = (TextView) findViewById(R.id.title);

		title.setText(exp.getTitle());
		DrawView.setLabels(exp.getX_label(), exp.getY_label(),
				exp.getX_units(), exp.getY_units(), exp.getCurrency());

		saveStateBoolean = true;
		lineDone = false;
		currentSession = exp.getCurrSession();
		currentLine = exp.getCurrLine();

		displayNewLine();
	
		Log.d(TAG,"exp.getProgress() = " + exp.getProgress());
		if (exp.getProgress() != -1) {
			changeProgress(exp.getProgress());
			seekBar.setProgress(exp.getProgress());
		}
	}
	
	@Override
	public void onStop() {
		
		super.onStop();
		Log.d(TAG,"In onStop, progress = " + progress);
		Log.d(TAG,"exp.isDone() = " + exp.isDone());
		if (saveStateBoolean && !exp.isDone() && !lineDone) {
			exp.saveState(context, progress);
		}
			
	}
	
	@Override
	public void onProgressChanged(SeekBar seekbar, int progress,
			boolean fromUser) {
		changeProgress(progress);
	}
	
	/** Changes sets dot based on progress given */
	public void changeProgress(int progressInput) {
		progress = progressInput;
		DrawView.setDotValue((int) (progress * x / max * 4));
		layout.invalidate();
		explanation.setText(getExplanation());
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
			
			Log.d(TAG,"In onClick");
			
			Log.d(TAG,"currentSession: " + currentSession);
			Random r = new Random();
			winner = (probabilistic ? (r.nextDouble() < exp.getProb_x() ? 'x' : 'y') : '-');
			Log.d(TAG, "exp.getSession(currentSession).getLine_chosen(): " + exp.getSession(currentSession).getLine_chosen());
			Log.d(TAG, "currentLine: " + currentLine);
			this.line_chosen_boolean = (exp.getSession(currentSession).getLine_chosen() == currentLine);
			Log.d(TAG, "line_chosen_boolean: " + this.line_chosen_boolean);
			
			String confirmationMessage = "You have chosen " 
					+ _getXRewardString(getX()) + " and " 
					+ _getYRewardString(getY())
					+ ". Select Confirm to save the selection or Cancel to select another point on this line.";
			
			
			AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(ExpActivityBudgetLine.this);
			confirmationBuilder.setMessage(confirmationMessage);
			
			confirmationBuilder.setNegativeButton("Cancel", null);

			confirmationBuilder.setPositiveButton("Confirm",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
							
						new ResponseBL(context, expId, currentSession, currentLine, x, y, getX(), getY(), winner, line_chosen_boolean);
	
						//very important line of code
						exp.nextLine(context);
						currentSession = exp.getCurrSession();
						currentLine = exp.getCurrLine();
						

						
						if (currentLine == 0) {
							makeMessageAndClean((currentSession - 1) % exp.getSessions().length, false);
						} else { 
						    switch (exp.getTimer_status()) {
						    case Constants.TIMER_STATUS_NONE:  
						        displayNewLine();
						        break;
						    case Constants.TIMER_STATUS_REMINDER:
						    	timer.onFinish();
						        displayNewLine();
						        break;
						    case Constants.TIMER_STATUS_RESTRICTIVE:
						    	timer.onFinish();
						        AlertDialog.Builder timeBuilder = new AlertDialog.Builder(ExpActivityBudgetLine.this);
						        
						        timeBuilder.setMessage(timer.getClosingMessage());
						        
						        timeBuilder.setCancelable(false);
						        
						        timeBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {

						                @Override
						                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						                    if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
						                        return true;
						                    }
						                    return false;
						                }
						        });
						        
						        timeBuilder.setPositiveButton("OK",
						                        new DialogInterface.OnClickListener() {
						            public void onClick(DialogInterface dialog, int which) {finish();}});
						        AlertDialog timeAlert =  timeBuilder.create();
						        timeAlert.show();	
						        
						        exp.saveState(context, -1);
						        lineDone = true;
						    		
						    }

						}
					}
				}
			);
			
			AlertDialog confirmationAlert = confirmationBuilder.create();
			confirmationAlert.show();
			
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
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

	/**
	 * handleHoldDown is the method that is called when a button is pushed down.
	 * It check to see if the hold thread is running and if it isn't, calls
	 * startHoldThread which starts the thread.
	 * 
	 * @param v
	 *            A view object that knows what button has been pushed.
	 */
	private void handleHoldDown(View v) {
		if (!holdThreadRunning) {
			startHoldThread(v);
		}
	}

	/**
	 * startHoldThread defines a thread that runs an inner thread. The inner
	 * thread controls what happens when a button is in the down position and
	 * how often that action occurs. In this case, when the left and right
	 * buttons are held down 1 gets subtracted or added, respectively, to the x
	 * value of the graph every 40 milliseconds. The outer thread initially
	 * sleeps for 300 milliseconds to give the user a chance to just click the
	 * button without causing continuous scrolling.
	 * 
	 * @param v
	 *            A view object that knows what button has been pushed.
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
	                                if (progress > 0) {
	                                    DrawView.addToX(-1);
	                                    progress -= 1;
	                                    seekBar.setProgress(progress);
	                                    explanation.setText(getExplanation());
	                                }
	                                break;
	                            case R.id.right_button:
	                                if (progress < 100) {
	                                    DrawView.addToX(1);
	                                    progress += 1;
	                                    seekBar.setProgress(progress);
	                                    explanation.setText(getExplanation());
	                                }
	                                break;
	                            }
	                            layout.invalidate();
	                        }
	                    });
	                    try {
	                        Thread.sleep(35);
	                    } catch (InterruptedException e) {
	                        throw new RuntimeException(
	                                        "Could not wait between adding 1 to x.", e);
	                    }
	                }
	            } catch (InterruptedException e) {
	                throw new RuntimeException(
	                                "Could not initially wait for button hold");
	            } finally {
	                holdThreadRunning = false;
	                cancelHoldThread = false;
	            }
	        }
	    };
	    // actually start the thread, after defining it
	    r.start();
	}

	/**
	 * handleHoldUp is the method is called when a button is released. It sets
	 * the cancelHoldThread boolean to true, which tells the thread in
	 * startHoldThread that the button has been released so it should stop
	 * changing the x value of the graph. It then proceeds to modify the x axis
	 * one more unit to the left or right, depending on which button was pushed.
	 * 
	 * @param v
	 *            A view object that knows what button has been pushed.
	 */
	private void handleHoldUp(View v) {
		cancelHoldThread = true;
		switch (v.getId()) {
		case R.id.left_button:
			if (progress > 0) {
				DrawView.addToX(-1);
				progress -= 1;
				seekBar.setProgress(progress);
                explanation.setText(getExplanation());
			}
			break;
		case R.id.right_button:
			if (progress < 100) {
				DrawView.addToX(1);
				progress += 1;
				seekBar.setProgress(progress);
                explanation.setText(getExplanation());
			}
			break;
		}
		layout.invalidate();
	}
	
	/**
	 * Returns string that clarifies what will be awarded if the line is chosen
	 * 
	 * @param currProgress
	 *            Current progress value of SeekBar
	 * @return message The message to be displayed under the graph
	 */
	private String getExplanation() {
		
		String message;
		
		if (probabilistic) {
			message = "If this line is chosen, you will get " + _getXRewardString((float) progress * x / (float) seekBar.getMax()) + " if the x-axis is chosen and " + _getYRewardString(-slope * ((float) progress * x / (float) seekBar.getMax()) + y) + " if the y-axis is chosen.";
		} else {
			message = "If this line is chosen, you will get " + _getXRewardString((float) progress * x / (float) seekBar.getMax()) + " and " + _getYRewardString(-slope * ((float) progress * x / (float) seekBar.getMax()) + y) + ".";
		}
		
		return message;
	}

	/**
	 * Converts current SeekBar progress value to X-axis value
	 * 
	 * @param currProgress
	 *            Current progress value of SeekBar
	 * @return x_value X value corresponding to current progress value of
	 *         SeekBar
	 */
	public static float getX() {
		return (float) (progress * x) / (float) seekBar.getMax();
	}

	/**
	 * Converts current SeekBar progress value to Y-axis value
	 * 
	 * @param currProgress
	 *            Current progress value of SeekBar
	 * @return y_value Y value corresponding to current progress value of
	 *         SeekBar
	 */
	public static float getY() {
		return (-slope * ((float) progress * x / (float) seekBar.getMax()) + y);
		
	}

	/**
	 * Displays new budgetLine
	 */
	private void displayNewLine() {
		/** intercept is the x-y intercepts of the new line, in that order. */
		float[] intercepts = new float[2];

		Log.d(TAG, "Creating new line");
		x = (float) exp.getSession(currentSession).getLine(currentLine)
				.getX_int();
		y = (float) exp.getSession(currentSession).getLine(currentLine)
				.getY_int();
		currency = '$';
		if (currency == '-') {//cannot directly compare apples and oranges
            intercepts[0] = (float) (x * seekBar.getMax() / exp.getX_max() * 4);
            intercepts[1] = (float) (y * seekBar.getMax() / exp.getY_max() * 4);            
        } else {//can directly compare dollars and dollars
            max = (float) Math.max(exp.getX_max(),exp.getY_max());
            intercepts[0] = (float) (x * seekBar.getMax() / max * 4);
            intercepts[1] = (float) (y * seekBar.getMax() / max * 4);
            //x =  x * (float) (exp.getX_max() / max);
            //y =  y * (float) (exp.getY_max() / max);
        }
		/*
		intercepts[0] = (float) (x * seekBar.getMax() / exp.getX_max() * 4);
		intercepts[1] = (float) (y * seekBar.getMax() / exp.getY_max() * 4);			
        */
		//TODO: this boolean is for cases where x and y max are different. Make it work with progress increments
		/*
		
		*/
		
		slope = y / x;
		seekBar.setProgress(50);
		progress = 50;
		DrawView.loadLineValues(intercepts[0], intercepts[1]);
		DrawView.setDotValue((int) Math.round(intercepts[0] / 2));
        explanation.setText(getExplanation());
		layout.invalidate();
	}
	
	/**
	 * Finish up session
	 * @param timedOut true if called because activity has been started after time out, false otherwise
	 */
	private void makeMessageAndClean(int session, boolean timedOut) {
		
		int lineChosen = exp.getSession(session).getLine_chosen();
		
		//cancel status bar notification
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(exp.getExpId());
		
		//make it so progress is not saved
		saveStateBoolean = false;
		
		//Winning line chosen for message that informs subjects of their rewards
		SharedPreferences winningSharedPreferences = context.getSharedPreferences(ResponseBL.getSPName(Constants.XLAB_BL_EXP, expId, currentSession - 1, lineChosen), Context.MODE_PRIVATE);
		Log.d(TAG, "SharedPreferences name: " + ResponseBL.getSPName(Constants.XLAB_BL_EXP, expId, currentSession - 1, lineChosen));
		Log.d(TAG, "SharedPreferences x_int: " + winningSharedPreferences.getFloat("x_int",(float)-99));
		
		
		String message = ((timedOut) ? "Your session has timed out." : "Thank you for completing a session.") + "\n\n"
				+ "The " + Utils.getOrdinalFor(lineChosen + 1) + " ";
		
		
		if (winningSharedPreferences.getFloat("x_chosen", -1) == -1) {
			
			message = message + "line was chosen. Unfortunately, you did not respond to the prompt for this line in a timely manner. You will receive nothing.";
			
		} else {

			if (probabilistic) {
								
				message = message
						+ "line and "
						+ winner
						+ "-axis were chosen.\n\nYou have won "
						+ ((winner == 'X' || winner == 'x') ? 
								_getXRewardString(winningSharedPreferences.getFloat("x_chosen", -1)) : 
									_getYRewardString(winningSharedPreferences.getFloat("y_chosen", -1))) + ".";
				
			} else {
				
				message = message + "line was chosen.\n\nYou have won "
						+ _getXRewardString(winningSharedPreferences.getFloat("x_chosen", -1)) + " and "
						+ _getYRewardString(winningSharedPreferences.getFloat("y_chosen", -1)) + ".";
				
			}
			
		}
		
		cleanUpExp(exp, message);

	}

	private String _getXRewardString(float x){
		String xFormatted = FORMATTER.format(x);
		return (currency == '-') ? (xFormatted + " " + exp.getX_units() + " of " + exp.getX_label()) : (currency + xFormatted);
	}

	private String _getYRewardString(float y){
		String yFormatted = FORMATTER.format(y);
		return (currency == '-') ? (yFormatted + " " + exp.getY_units() + " of " + exp.getY_label()) : (currency + yFormatted);
	}

    /*@Override
    public void instructionsSelected() {
        // TODO Auto-generated method stub
        
    }*/
	
}