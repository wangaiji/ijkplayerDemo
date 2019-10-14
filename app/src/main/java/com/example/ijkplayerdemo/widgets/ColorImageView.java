package com.example.ijkplayerdemo.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

public class ColorImageView extends android.support.v7.widget.AppCompatImageView{
    private Paint paint;
    private boolean isCheck = false;
    private ColorImageView imageView;

    public ColorImageView(Context context) {
        this(context, null);
    }

    public ColorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    public void setFlag(boolean flag) {
        isCheck = flag;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("265434","1");
        if (isCheck) {
            Rect rect = canvas.getClipBounds();
            RectF rectF = new RectF((float) rect.left+10, (float) rect.top+10, (float) rect.right-10, (float) rect.bottom-10);

            if (paint == null) {
                paint = new Paint();
            }
            paint.setColor(Color.parseColor("#448AFF"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawRoundRect(rectF, 10, 10, paint);
        }

    }



}
