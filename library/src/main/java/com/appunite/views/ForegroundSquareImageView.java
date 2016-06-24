package com.appunite.views;


import android.content.Context;
import android.util.AttributeSet;

public class ForegroundSquareImageView extends ForegroundImageView {

    public ForegroundSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
