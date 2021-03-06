/**
 * Copyright 2017 ChenHao Dendi
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.yunqipei.widgetlibrary.R;

import java.util.ArrayList;
import java.util.List;

public class IndexBar extends View {
    private float mLetterSpacingExtra;

    private OnTouchingLetterChangeListener mOnTouchingLetterChangeListener;
    private List<String> mNavigators;

    private int mFocusIndex;
    private Paint mPaint;
    private Paint mFocusPaint;
    private float mBaseLineHeight;
    private Bitmap mBitmap;

    public IndexBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initSetting(context, attrs);
    }

    /******************
     * common
     ******************/

    private void initSetting(Context context, AttributeSet attrs) {
        mOnTouchingLetterChangeListener = getDummyListener();
        mNavigators = new ArrayList<>(0);
        mFocusIndex = -1;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndexBar);
        float textSize = typedArray.getDimension(R.styleable.IndexBar_letterSize, 8);
        int letterColor = typedArray.getColor(R.styleable.IndexBar_letterColor,
                ContextCompat.getColor(getContext(), android.R.color.black));
        mLetterSpacingExtra = typedArray.getFloat(R.styleable.IndexBar_letterSpacingExtra, 1.4f);
        int focusLetterColor = typedArray.getColor(R.styleable.IndexBar_focusLetterColor,
                ContextCompat.getColor(getContext(), android.R.color.black));
        typedArray.recycle();

        mPaint = new Paint();
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setAntiAlias(true);
        mPaint.setColor(letterColor);
        mPaint.setTextSize(textSize);

        mFocusPaint = new Paint();
        mFocusPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mFocusPaint.setAntiAlias(true);
        mFocusPaint.setFakeBoldText(true);
        mFocusPaint.setTextSize(textSize);
        mFocusPaint.setColor(focusLetterColor);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_hot_brand);

    }

    private OnTouchingLetterChangeListener getDummyListener() {
        return new OnTouchingLetterChangeListener() {
            @Override
            public void onTouchingLetterChanged(String s) {

            }

            @Override
            public void onTouchingStart(String s) {

            }

            @Override
            public void onTouchingEnd(String s) {

            }
        };
    }

    /******************
     * external interfaces
     ******************/

    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSetting(context, attrs);
    }

    public IndexBar(Context context) {
        super(context);
    }

    /**
     * set Listener
     *
     * @param onTouchingLetterChangeListener
     */
    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangeListener onTouchingLetterChangeListener) {
        this.mOnTouchingLetterChangeListener = onTouchingLetterChangeListener;
    }

    /**
     * set the letters , will be displayed aside
     * and also related with the OnTouchingLetterChangeListener#onTouchingLetterChanged
     *
     * @param navigators
     */
    public void setNavigators(@NonNull List<String> navigators) {
        mNavigators = navigators;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float y = event.getY();
        final int formerFocusIndex = mFocusIndex;
        final OnTouchingLetterChangeListener listener = mOnTouchingLetterChangeListener;
        final int c = calculateOnClickItemNum(y);
        if (mNavigators == null || mNavigators.isEmpty()) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mFocusIndex = -1;
                invalidate();
                listener.onTouchingEnd(mNavigators.get(c));
                break;
            case MotionEvent.ACTION_DOWN:
                listener.onTouchingStart(mNavigators.get(c));
            default:
                if (formerFocusIndex != c) {
                    if (c >= 0 && c < mNavigators.size()) {
                        listener.onTouchingLetterChanged(mNavigators.get(c));
                        mFocusIndex = c;
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        if (height == 0) {
            return;
        }
//        int singleHeight = height / mNavigators.size();
        int singleHeight = height / 27;

        int voffset = (height - singleHeight * mNavigators.size()) / 2;
        canvas.translate(0, voffset);

        for (int i = 0; i < mNavigators.size(); i++) {
            float xPos = width / 2 - mPaint.measureText(mNavigators.get(i)) / 2;
            float yPos = singleHeight * (i + 1);
            if ("#".equals(mNavigators.get(i))) {
                canvas.drawBitmap(mBitmap, (width - mBitmap.getWidth()) / 2, singleHeight * i + (singleHeight - mBitmap.getHeight()) / 2, mPaint);
            } else {
                if (i == mFocusIndex) {
                    canvas.drawText(mNavigators.get(i), xPos, yPos - mBaseLineHeight, mFocusPaint);
                } else {
                    canvas.drawText(mNavigators.get(i), xPos, yPos - mBaseLineHeight, mPaint);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = getSuggestedMinWidth();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            Paint.FontMetrics fm = mPaint.getFontMetrics();
            float singleHeight = fm.bottom - fm.top;
            mBaseLineHeight = fm.bottom * mLetterSpacingExtra;
            result = (int) (mNavigators.size() * singleHeight * mLetterSpacingExtra);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int getSuggestedMinWidth() {
        String maxLengthTag = "";
        for (String tag : mNavigators) {
            if (maxLengthTag.length() < tag.length()) {
                maxLengthTag = tag;
            }
        }
        return (int) (mPaint.measureText(maxLengthTag) + 0.5);
    }

    /**
     * @param yPos
     * @return the corresponding position in list
     */
    private int calculateOnClickItemNum(float yPos) {
        int result;
        result = (int) (yPos / getHeight() * mNavigators.size());
        if (result >= mNavigators.size()) {
            result = mNavigators.size() - 1;
        } else if (result < 0) {
            result = 0;
        }
        return result;
    }

    public interface OnTouchingLetterChangeListener {
        void onTouchingLetterChanged(String s);

        void onTouchingStart(String s);

        void onTouchingEnd(String s);
    }

}
