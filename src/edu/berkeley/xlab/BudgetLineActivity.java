package edu.berkeley.xlab;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


public class BudgetLineActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    /** Called when the activity is first created. */
    public double progress;
    TextView xValue;
    TextView yValue;
    View layout;
    Integer X;
    Integer Y;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.main);
        X = (int) Math.random();
        Y = (int) Math.random();
        
        layout = findViewById(R.id.layout);
        layout.setBackgroundColor(Color.WHITE);
        
        SeekBar seekBar = (SeekBar)findViewById(R.id.slider);
        seekBar.setOnSeekBarChangeListener(this);
        xValue = (TextView) findViewById(R.id.x_value);
        yValue = (TextView) findViewById(R.id.y_value);
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        this.progress = progress;
        xValue.setText("X = " + progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        
    }

}