package edu.berkeley.xlab;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import edu.berkeley.xlab.experiments.*;
import edu.berkeley.xlab.util.Utils;
//ADAM: Important too
/**
 * Draw is an activity that controls choosing a point on a line and recording
 * that choice.
 * 
 * @author John Gunnison
 */
public class BudgetLineActivity extends Activity implements
		SeekBar.OnSeekBarChangeListener, OnClickListener, OnTouchListener {

	/** TAG is an identifier for the log. */
	static final String TAG = "XLab-BL";

	/** exp is the XLabBudgetLineExp. */
	private XLabBudgetLineExp exp;

	/** status is the the flag for the unload. Equals 0 if failure, 1 otherwise. */
	private int status;

	/** _progress is the current value of the slider. */
	private static int _progress;

	/** xValue is the displayed value of the x coordinate of the dot. */
	private TextView xValue;

	/** yValue is the displayed value of the y coordinate of the dot. */
	private TextView yValue;

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

	/** unique identifier of XLabBudgetLineExp */
	private int bl_Id;

	/** for http post, defined in onCreate */
	private String username;

	/** record of lines and intercepts chosen for post */
	private ArrayList<Float> x_ints = new ArrayList<Float>();
	private ArrayList<Float> y_ints = new ArrayList<Float>();
	private ArrayList<Float> x_chosens = new ArrayList<Float>();
	private ArrayList<Float> y_chosens = new ArrayList<Float>();

	private DecimalFormat formatter = new DecimalFormat("#.##");

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.budget_line);
		Log.d(TAG, "In onCreate method");

		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();

		bl_Id = (int) extras.getInt("id");
		Log.d(TAG, "bl_Id = " + bl_Id);

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

		App app = ((App) getApplicationContext());
		exp = (XLabBudgetLineExp) app.getXLabExp(new Integer(bl_Id));

		TextView title = (TextView) findViewById(R.id.title);
		for (Map.Entry<Integer, Experiment> entry : app.getXLabExps()
				.entrySet()) {
			Log.d(TAG, entry.getValue().getId() + ": Key = " + entry.getKey()
					+ "Title = " + entry.getValue().getTitle() + ", Class = "
					+ entry.getValue().getClass());
		}
		title.setText(exp.getTitle());
		DrawView.setLabels(exp.getX_label(), exp.getY_label(),
				exp.getX_units(), exp.getY_units());

		this.username = Utils.getStringPreference(this, Configuration.USERNAME,
				"anonymous");

		displayNewLine();
	}

	@Override
	public void onProgressChanged(SeekBar seekbar, int progress,
			boolean fromUser) {
		_progress = progress;
		DrawView.setDotValue(progress);
		layout.invalidate();
		xValue.setText(getXLabel());
		yValue.setText(getYLabel());
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
			x_ints.add(x);
			y_ints.add(y);
			x_chosens.add(getX());
			y_chosens.add(getY());
			exp.nextLine();
			if (exp.getCurrLine() == 0) {
				int lineChosen = exp.getSession(exp.getCurrSession() - 1)
						.getLine_chosen();
				String message = "Thank you for completing a session.\n\n"
						+ "The " + lineChosen + Utils.getOrdinalFor(lineChosen) + " ";
				if (exp.getProbabilistic()) {
					char winner = exp.getSession(exp.getCurrSession() - 1).getLine(
							lineChosen).getWinner();
					message = message
							+ "line and "
							+ winner
							+ "-axis was chosen.\nYou have won "
							+ ((winner == 'X') ? (formatter.format(getX())
									+ " " + exp.getX_units() + " of " + exp
									.getX_label()) : (formatter.format(getY())
									+ " " + exp.getY_units() + " of " + exp
									.getY_label())) + ".";
				} else {
					message = message + "line was chosen.\nYou have won "
							+ formatter.format(getX()) + " " + exp.getX_units()
							+ " of " + exp.getX_label() + " and "
							+ formatter.format(getY()) + " " + exp.getY_units()
							+ " of " + exp.getY_label() + ".";
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(BudgetLineActivity.this);				
				builder.setMessage(message);
				builder.setNeutralButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
		                new PostBL().execute();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				
			} else {
				displayNewLine();
			}
		}
	}

	private boolean holdThreadRunning = false;
	private boolean cancelHoldThread = false;
	private Handler handler = new Handler();

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
									if (_progress > 0) {
										DrawView.addToX(-1);
										_progress -= 1;
										seekBar.setProgress(_progress);
										xValue.setText(getXLabel());
										yValue.setText(getYLabel());
									}
									break;
								case R.id.right_button:
									if (_progress < x) {
										DrawView.addToX(1);
										_progress += 1;
										seekBar.setProgress(_progress);
										xValue.setText(getXLabel());
										yValue.setText(getYLabel());
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
			if (_progress > 0) {
				DrawView.addToX(-1);
				_progress -= 1;
				seekBar.setProgress(_progress);
				xValue.setText(getXLabel());
				yValue.setText(getYLabel());
			}
			break;
		case R.id.right_button:
			if (_progress < x) {
				DrawView.addToX(1);
				_progress += 1;
				seekBar.setProgress(_progress);
				xValue.setText(getXLabel());
				yValue.setText(getYLabel());
			}
			break;
		}
		layout.invalidate();
	}

	private class PostBL extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog dialog = new ProgressDialog(BudgetLineActivity.this);

		@Override
		protected void onPreExecute() {
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage("Sending answer to server...");
			dialog.show();
		}

		@Override
		protected Integer doInBackground(Void... whatever) {
			
			status = 1;
			
			for (int i = 0; i < x_ints.size(); i++) {
				new Thread( new UploadSingleBL(x_ints.get(i),y_ints.get(i),x_chosens.get(i),y_chosens.get(i)) ).start();
			}
			
			return status;
			
	    }
		
		@Override
		protected void onPostExecute(Integer status) {
			Log.d(TAG, "In onPostExecute");
			dialog.dismiss();
			if (status == 1) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(BudgetLineActivity.this);
						builder.setMessage("Thank you. Your budget lines were received.");
						builder.setNeutralButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
				                BudgetLineActivity.this.finish();
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
					}
				});
			} else {
				handler.post(new Runnable() {
					AlertDialog.Builder builder = new AlertDialog.Builder(BudgetLineActivity.this);

					@Override
					public void run() {
						builder.setMessage("Sorry. Your message was not received.");
						builder.setNeutralButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
				                BudgetLineActivity.this.finish();
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
					}
				});
			}
		}	    
	}

	class UploadSingleBL implements Runnable {
		
		private String response;
		private float x_int;
		private float y_int;
		private float x_chosen;
		private float y_chosen;
		
		
		public UploadSingleBL(float x_int, float y_int, float x_chosen, float y_chosen) {
			super();
			Log.d(TAG,"In UploadSingleBL constructor");
			this.x_int = x_int;
			this.y_int = y_int;
			this.x_chosen = x_chosen;
			this.y_chosen = y_chosen;
		}

		@Override
		public void run() {
			
			try {
				Log.d(TAG,Configuration.XLAB_API_ENDPOINT_BL
						+ "?bl_id=" + bl_Id + "&bl_username=" + username
						+ "&bl_lat=" + BackgroundService.getLastLat()
						+ "&bl_lon=" + BackgroundService.getLastLon()
						+ "&bl_x_intercept=" + x_int + "&bl_y_intercept=" + y_int
						+ "&bl_x=" + x_chosen + "&bl_y=" + y_chosen);
				response = Utils.getData(Configuration.XLAB_API_ENDPOINT_BL
						+ "?bl_id=" + bl_Id + "&bl_username=" + username
						+ "&bl_lat=" + BackgroundService.getLastLat()
						+ "&bl_lon=" + BackgroundService.getLastLon()
						+ "&bl_x_intercept=" + x_int + "&bl_y_intercept=" + y_int
						+ "&bl_x=" + x_chosen + "&bl_y=" + y_chosen);

				if(null == response) {
					//TODO: Check for response and retry if it failed
					Log.e(TAG, "Received null response");
					status = status * 0;
				} else if(response.equalsIgnoreCase("1")) {
					Log.d(TAG, "Received response from server - " + response);
					status = status * 1;
				} else if(response.equalsIgnoreCase("0")) {
					Log.d(TAG, "Received response from server - " + response);
					status = status * 0;
				}
				
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
	        	    			
		}

	}

	
	/**
	 * Converts current SeekBar progress value to X-axis label
	 * 
	 * @param currProgress
	 *            Current progress value of SeekBar
	 * @return message The message to be displayed under the X axis
	 */
	private String getXLabel() {
		return ("X = " + formatter.format((float) _progress * x
				/ (float) seekBar.getMax()));
	}

	/**
	 * Converts current SeekBar progress value to X-axis label
	 * 
	 * @param currProgress
	 *            Current progress value of SeekBar
	 * @return message The message to be displayed under the X axis
	 */
	private String getYLabel() {
		return ("Y = " + formatter.format(-slope
				* ((float) _progress * x / (float) seekBar.getMax()) + y));
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
		return ((float) _progress * x / (float) seekBar.getMax());
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
		return (-slope * ((float) _progress * x / (float) seekBar.getMax()) + y);
	}

	/**
	 * Displays new budgetLine
	 */
	private void displayNewLine() {
		/** intercept is the x-y intercepts of the new line, in that order. */
		float[] intercepts = new float[2];

		Log.d(TAG, "Creating new line");
		x = exp.getSession(exp.getCurrSession()).getLine(exp.getCurrLine())
				.getX_int();
		y = exp.getSession(exp.getCurrSession()).getLine(exp.getCurrLine())
				.getY_int();
		intercepts[0] = x * (float) seekBar.getMax() / exp.getX_max();
		intercepts[1] = y * (float) seekBar.getMax() / exp.getY_max();
		slope = intercepts[1] / intercepts[0];
		_progress = (int) Math.round(intercepts[0] / 2);
		seekBar.setProgress(_progress);
		DrawView.loadLineValues(intercepts[0], intercepts[1]);
		DrawView.setDotValue(_progress);
		xValue.setText(getXLabel());
		yValue.setText(getYLabel());
		layout.invalidate();
	}

}