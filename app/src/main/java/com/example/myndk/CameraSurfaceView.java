package com.example.myndk;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/6/7.
 */

public class CameraSurfaceView extends android.support.v7.widget.AppCompatImageView {


    public CameraSurfaceView(Context context) {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    Paint paint = new Paint();
    {
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);//设置线宽
        paint.setAlpha(100);
    };

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        int miHeight = this.getMeasuredHeight()/2;
        canvas.drawLine(0,miHeight,this.getMeasuredWidth(),miHeight,paint);
        canvas.drawCircle(this.getWidth()/2,miHeight/2,60,paint);
        canvas.drawCircle(this.getWidth()/2,3*miHeight/2,60,paint);

    }
}
