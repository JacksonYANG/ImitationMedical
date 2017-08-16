package com.hqyj.dev.doctorforhealth.PaintTools;

/**
 * Created by Administrator on 2016/3/29.
 */
public class ECGDataHolder {

    private int[] mBuffer;
    private int index;
    private int maxLen;

    public ECGDataHolder(int size){
        if(size < 0)
            return ;
        maxLen = size;
        mBuffer = new int[maxLen];
        index = 0;
    }
    public ECGDataHolder(){
        this(12);
    }

    public int getDataCount(){
        return maxLen;
    }
    /**
     * 获得心电数据Buffer
     * @return
     */
    public int[] getBuffer(){
        return this.mBuffer;
    }
    /**
     * 向Buffer中添加新数据
     */
    public void feedData(int data){
        if(data < 0){
            return;
        }
        // Cache the data lenght
        ;		// 防止越界
        if(index + 1 > maxLen)
            return ;

        synchronized (mBuffer) {
            mBuffer[index] = data;
        }
        index ++;

    }
    /**
     * 返回当前Buffer的数据下标
     * @return
     */
    public int getIndex(){
        return this.index;
    }

}
