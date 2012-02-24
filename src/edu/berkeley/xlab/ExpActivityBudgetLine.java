package edu.berkeley.xlab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.berkeley.xlab.constants.Configuration;
import edu.berkeley.xlab.xlab_objects.ResponseBL;
import edu.berkeley.xlab.util.Utils;
import edu.berkeley.xlab.xlab_objects.*;

/**
 * Draw is an activity that controls choosing a point on a line and recording
 * that choice.
 * 
 * @author John Gunnison and Daniel Vizzini
 */
public class ExpActivityBudgetLine extends ExpActivitySuperclass implements
		SeekBar.OnSeekBarChangeListener, OnClickListener, OnTouchListener {

	/** TAG is an identifier for the log. */
	public static final String TAG = "XLab-BL";

	/** exp is the ExperimentBudgetLine. */
	private ExperimentBudgetLine exp;

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
	
	/** true if line is selected in session to be the one from which subject wins goods, false otherwise */
	private boolean line_chosen_boolean;
	
	private boolean holdThreadRunning = false;
	private boolean cancelHoldThread = false;
	private Handler handler = new Handler();
	
	/** Called when the activity is first created. */
	@Override
	public void onStart() {
		
		super.onStart();
		
		//call superclass method
		initialize(this);
		
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		
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

		exp = new ExperimentBudgetLine(context, context.getSharedPreferences(Experiment.makeSPName(extras.getInt("expId")), Context.MODE_PRIVATE));
		probabilistic = exp.getProbabilistic();
		Log.d(TAG, "probabilistic = " + probabilistic);
		

		TextView title = (TextView) findViewById(R.id.title);

		title.setText(exp.getTitle());
		DrawView.setLabels(exp.getX_label(), exp.getY_label(),
				exp.getX_units(), exp.getY_units());

		saveStateBoolean = true;
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

		if (saveStateBoolean) {
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
		DrawView.setDotValue((int) (progress * x / exp.getX_max() * 4));
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
			winner = (probabilistic ? exp.getSession(currentSession).getLine(currentLine).getWinner() : '-');
			line_chosen_boolean = (exp.getSession(currentSession).getLine_chosen() == currentLine);
			
			String confirmationMessage = "You have chosen " 
					+ FORMATTER.format(getX()) + " " + exp.getX_units()
					+ (!exp.getX_label().equalsIgnoreCase("") ? " of "  + exp.getX_label(): "") + " and "
					+ FORMATTER.format(getY()) + " " + exp.getY_units()
					+ (!exp.getY_label().equalsIgnoreCase("") ? " of "  + exp.getY_label(): "") + ". Select Confirm to save the selection or Cancel to select another point on this line.";
			
			
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
							int lineChosen = exp.getSession(currentSession - 1).getLine_chosen();
							
							//make it so progress is not saved
							saveStateBoolean = false;
							
							//Winning line chosen for message that informs subjects of their rewards
							SharedPreferences sharedPreferences = context.getSharedPreferences(ResponseBL.getSPName(Configuration.XLAB_BL_EXP, expId, currentSession - 1, lineChosen), Context.MODE_PRIVATE);
							Log.d(TAG, "SharedPreferences name: " + ResponseBL.getSPName(Configuration.XLAB_BL_EXP, expId, currentSession - 1, lineChosen));
							Log.d(TAG, "SharedPreferences x_int: " + context.getSharedPreferences(ResponseBL.getSPName(Configuration.XLAB_BL_EXP, expId, currentSession - 1, lineChosen), Context.MODE_PRIVATE).getFloat("x_int",(float)-99));
							String message = "Thank you for completing a session.\n\n"
									+ "The " + (lineChosen + 1) + Utils.getOrdinalFor(lineChosen + 1) + " ";
							
							if (probabilistic) {
								
								message = message
										+ "line and "
										+ winner
										+ "-axis was chosen.\n\nYou have won "
										+ ((winner == 'X') ? 
												(FORMATTER.format((double)sharedPreferences.getFloat("x_chosen", 0)) + " " + exp.getX_units() + (!exp.getX_label().equalsIgnoreCase("") ? " of "  + exp.getX_label(): " on the x-axis")) : 
													(FORMATTER.format((double)sharedPreferences.getFloat("y_chosen", 0)) + " " + exp.getY_units() + (!exp.getX_label().equalsIgnoreCase("") ? " of " + exp.getY_label() : " on the y-axis")))
													+ ".";
								
							} else {
								
								message = message + "line was chosen.\n\nYou have won "
										+ FORMATTER.format((double)sharedPreferences.getFloat("x_chosen", 0)) + " " + exp.getX_units()
										+ " of " + exp.getX_label() + " and "
										+ FORMATTER.format((double)sharedPreferences.getFloat("y_chosen", 0)) + " " + exp.getY_units()
										+ " of " + exp.getY_label() + ".";
								
							}
							
							cleanUpExp(exp, message);
							
						} else {
							displayNewLine();
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
		
		String xFormatted = FORMATTER.format((float) progress * x / (float) seekBar.getMax());
		String yFormatted = FORMATTER.format(-slope * ((float) progress * x / (float) seekBar.getMax()) + y);
		String message;
		
		if (probabilistic) {
			
			//TODO: Hardcoded for dollars now. Make monetary units on server-side.
			message = "If this line is chosen, you will get $" + xFormatted + " if the x-axis is chosen and " 
					+ "$" + yFormatted + " if the y-axis is chosen.";
			
		} else {
			
			message = "If this line is chosen, you will get " + xFormatted + " " + exp.getX_units() + " of " + exp.getX_label() + " and " 
					+ yFormatted + " " + exp.getY_units() + " of " + exp.getY_label() + ".";
			
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
		return ((float) progress * x / (float) seekBar.getMax());
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
		intercepts[0] = (float) (x * seekBar.getMax() / exp.getX_max() * 4);
		intercepts[1] = (float) (y * seekBar.getMax() / exp.getY_max() * 4);
		slope = intercepts[1] / intercepts[0];
		System.out.println("x intercept: " + intercepts[0]);
		seekBar.setProgress(50);
		progress = 50;
		DrawView.loadLineValues(intercepts[0], intercepts[1]);
		DrawView.setDotValue((int) Math.round(intercepts[0] / 2));
        explanation.setText(getExplanation());
		System.out.println("progress: " + progress);
		layout.invalidate();
	}
	
}