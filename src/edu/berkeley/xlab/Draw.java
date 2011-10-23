package edu.berkeley.xlab;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;


public class Draw extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.main);
        View layout = findViewById(R.id.layout);
        layout.setBackgroundColor(Color.WHITE);

    }

}