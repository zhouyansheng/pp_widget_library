package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;

import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yunqipei.widgetlibrary.R;


/**
 * edie create on 2018/11/25
 */
public class GapFrameLayout extends FrameLayout {
    private int mGapHeight;

    public GapFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public GapFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GapFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GapFrameLayout);
        mGapHeight = typedArray.getDimensionPixelSize(R.styleable.GapFrameLayout_gap_height_frame, 0);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - mGapHeight, MeasureSpec.getMode(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
