package com.hqyj.dev.doctorforhealth.PaintTools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created by Administrator on 2016/3/29.
 */
public class ECGView extends View {
    private String TAG = "ECGView";
    private Context context;
    private Paint mPaint;
    public static ECGDataHolder mECGDataHolder;
    private boolean isMeasured = false;
    DrawThread thr;

    private int[] mBuffer;
    private int topBaseLine = 0;
    //private int btmBaseLine = 0;

    private int mWidth, mHeight;

    // X,Y方向的像素乘积系数
    private float mXCoefficient, mYCoefficient;


    private HorizontalScrollView mScrollView;

    // 总宽度
    private int totalWidth;
    public ECGView(Context context) {
        super(context);
        this.context = context;
        // TODO Auto-generated constructor stub
        initView();
    }


    public ECGView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        // TODO Auto-generated constructor stub
        initView();
    }

    public ECGView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        // TODO Auto-generated constructor stub
        initView();
    }

    public void setBatteryLevel(int bat){

    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(42);
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        //mHeartBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.heart3);


        // 获得屏幕实际大小
        DisplayMetrics dm;
        dm = getResources().getDisplayMetrics();
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;

        // 平板上800x480 1mv为两个大格，在平板高度上有12大格，即：1mv为80像素高
        if(mWidth / (mHeight+0.0) > 800 / 480.0){  // 当前屏幕更宽,例如小米2，1280x722
            mXCoefficient =  mHeight / 480.0f;
            mYCoefficient = 0.005f * 80 * mXCoefficient;
        }else{								// 当前屏幕正好合适或更窄
            mXCoefficient =  mWidth / 800.0f;
            mYCoefficient = 0.005f * 80 * mXCoefficient;
        }
        Log.d(TAG, " mXCoefficient = " + mXCoefficient + " \n mYCoefficient " + mYCoefficient);

        totalWidth = (int) (12 * 200);
        thr = new DrawThread();
        thr.start();
    }

    public void destroyView(){
        Log.d("ECGView", "destroyView");

        thr.cancel();
        try {
            thr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int drawIndex = 0;
    private class DrawThread extends Thread{
        private boolean isStart = true;
        private long startTime;

        public void cancel(){
            isStart = false;
        }
        public void run(){
            while(isStart){
                long sl = System.currentTimeMillis() - startTime;
                if (sl < getDrawDelay()) {
                    try {
                        Thread.sleep(getDrawDelay() - sl);
                    } catch (InterruptedException e) {
                    }
                }
                if(!isStart)
                    break;
                startTime = System.currentTimeMillis();
                if(mECGDataHolder == null)
                    continue;
                mBuffer = mECGDataHolder.getBuffer();
                if(drawIndex >= mECGDataHolder.getDataCount()){
                    break;
                }
                if (drawIndex >= mECGDataHolder.getIndex() - 1) {
                    continue;
                }
                postInvalidate();
                drawIndex++;
            }
        }
    }

    /**
     * 根据data值在View上描制心电数据。
     * @param data 心电数据
     */
    public void drawECG(int data){
        //Log.d("ECGView", "drawECG");

        // 保存新数据到Buffer中
        mECGDataHolder.feedData(data);

        if(this.getParent() instanceof HorizontalScrollView){
            mScrollView = (HorizontalScrollView) this.getParent();
            mScrollView.scrollTo((int)(mECGDataHolder.getIndex() * mXCoefficient) - mWidth + mWidth / 12, 0);
        }

        // 获得全部心电数据Buffer
        mBuffer = mECGDataHolder.getBuffer();
    }

    /**
     * 获得心电数据缓存
     * @return 心电数据缓存
     */
    public int[] getDataBuffer(){
        return mBuffer;
    }

    /**
     * 获得心电数据数量（每个心电数据绘制一个像素点）
     * @return 返回心电数据数量
     */
    public int getTotalDataCount(){
        return totalWidth;
    }

    /**
     * 计算每像素点绘制延时（ms）
     * @return 每像素点绘制延时（ms）
     */
    public int getDrawDelay(){
        return  5;
        // ----------------------------------------------------
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(this.getParent() instanceof HorizontalScrollView){
            mScrollView = (HorizontalScrollView) this.getParent();
            mScrollView.scrollTo((int)(drawIndex * mXCoefficient) - mWidth + mWidth / 12, 0);
        }

        if(mBuffer != null){
            for(int i = 0; i < drawIndex - 1; i++){
                canvas.drawLine(i * mXCoefficient + getPaddingLeft() , topBaseLine - mBuffer[i] * mYCoefficient  + getPaddingTop(),
                        (i + 1)*mXCoefficient + getPaddingLeft() , topBaseLine - mBuffer[i + 1] * mYCoefficient + getPaddingTop(), mPaint);
            }
        }
        super.onDraw(canvas);
    }

    /**
     * 当控件的父元素正要放置该控件时调用.父元素会问子控件一个问题，“你想要用多大地方啊？”
     * 然后传入两个参数——widthMeasureSpec和heightMeasureSpec.
     * 这两个参数指明控件可获得的空间以及关于这个空间描述的元数据.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        Log.d("ECGView","onMeasure:w=" + width +" h=" + height +" totalWidth=" + totalWidth);

        // 告诉父控件，需要多大地方放置子控件
        setMeasuredDimension((int)(totalWidth*mXCoefficient) , height);

        // 上半部分心电图基线高度
        topBaseLine = (int) (height *1.2) ;

        // 控件还没有测量完尺寸时
        if(!isMeasured)
            mECGDataHolder = new ECGDataHolder(totalWidth);
        isMeasured  = true;
    }

    /**
     * 依据specMode的值，（MeasureSpec有3种模式分别是UNSPECIFIED, EXACTLY和AT_MOST）
     * 如果是AT_MOST，specSize 代表的是最大可获得的空间；
     * 如果是EXACTLY，specSize 代表的是精确的尺寸；
     * 如果是UNSPECIFIED，对于控件尺寸来说，没有任何参考意义。
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = getWidth();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = getHeight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

}
