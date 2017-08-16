package com.example.testdraw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2016/4/29.
 */
public class DrawSwich extends View {
    private OnChangedListener listener;
    private Context mContext;
    private Paint mPaint;
    private Bitmap open, close, bar;
    private Rect orOpen, orClose, orbar;
    private Rect dstOpen, dstClose, dstbar;
    private float with;
    private boolean newStatus = false;

    public DrawSwich(Context context) {
        this(context, null);
    }

    public DrawSwich(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawSwich(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mPaint = new Paint();

        mPaint.setTextSize(18);
        mPaint.setStrokeWidth(Color.BLUE);
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);


        open = BitmapFactory.decodeResource(context.getResources(), R.drawable.kai);
        close = BitmapFactory.decodeResource(context.getResources(),R.drawable.guan);
        bar = BitmapFactory.decodeResource(context.getResources(),R.drawable.bar);


        with = context.getResources().getDisplayMetrics().widthPixels;

        int closex = close.getWidth();
        int closey = close.getHeight();
        orClose = new Rect(0,0,closex,closey);//关闭

        int orx = open.getWidth();
        int ory = open.getHeight();
        orOpen = new Rect(0,0, orx, ory);//打开

        int barx = bar.getWidth();
        int bary = bar.getHeight();
        orbar = new Rect(0,0,barx,bary);//长条

        int dstclosex = (int) (with* 54/1024);
        int dstclosey = dstclosex*closex/closey;
        dstClose = new Rect(0,0,dstclosex,dstclosey);//关闭

        int dstclix,dstcliy;
        dstclix = (int)(with *100/1024);
        dstcliy = dstclix*bary/barx;
        dstbar = new Rect(0,0,dstclix,dstcliy);//长条

        int dstx, dsty;
        dstx = (int) (with * 54/1024);
        dsty = dstx*ory/orx;
        dstOpen = new Rect(dstclix-dstx,0,dstclix,dsty);//打开

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bar,orbar,dstbar,mPaint);
        if(newStatus) {
            canvas.drawBitmap(open, orOpen, dstOpen, mPaint);
        }else {
            canvas.drawBitmap(close, orClose, dstClose, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = getPaddingLeft() + getPaddingRight() + dstbar.width();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getPaddingTop() + dstbar.height() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x,y;
        x = event.getX();
        y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(x < dstbar.left || x > dstbar.right || y < dstbar.top || y > dstbar.bottom){
                    return false;
                }else{

                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if(x < dstbar.left || x > dstbar.right || y < dstbar.top || y > dstbar.bottom){
                    return false;
                }else{
                    if(newStatus == false){
                        newStatus = true;
                    }else{
                        newStatus = false;
                    }
                }
                if(listener != null){
                    listener.OnChanged(DrawSwich.this,newStatus);
                }
                break;
        }
        invalidate();
        return true;
    }
    public void setChecked(boolean checked){
        if(checked){

        }else{

        }
        newStatus = checked;
    }
    public interface OnChangedListener{
        public void OnChanged(DrawSwich drawSwitch,boolean checkState);
    }
    public void setOnChangedListener(OnChangedListener list){
        this.listener = list;
    }

}
