package edu.berkeley.xlab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

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

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        canvas.drawLine(10, 290, 10, 10, paint);
        canvas.drawLine(10, 290, 280, 290, paint);
        paint.setColor(Color.RED);
        canvas.drawLine(10, 190, 110, 290, paint);
    }

}

