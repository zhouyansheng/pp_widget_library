package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;

import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.yunqipei.widgetlibrary.R;


/**
 * 有头部间隙的LinearLayout
 * edie create on 2018/11/22
 */
public class GapLinearLayout extends LinearLayout {

    private int mGapHeight;

    public GapLinearLayout(Context context) {
        this(context, null);
    }

    public GapLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GapLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GapLinearLayout);
        mGapHeight = typedArray.getDimensionPixelSize(R.styleable.GapLinearLayout_gap_height, 96);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getOrientation() == VERTICAL) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - mGapHeight, MeasureSpec.getMode(heightMeasureSpec));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }
}
