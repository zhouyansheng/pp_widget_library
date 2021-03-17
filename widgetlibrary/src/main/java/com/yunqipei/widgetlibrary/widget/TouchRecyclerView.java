package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * edie create on 2018/10/24
 */
public class TouchRecyclerView extends RecyclerView {
    int mDownX;
    int mDownY;
    int mMoveX;
    int mMoveY;
    private OnUserTouchListener mListener;
    public TouchRecyclerView(Context context) {
        super(context);
    }
    public TouchRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public TouchRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean b = super.dispatchTouchEvent(ev);
        if (getLayoutManager().canScrollVertically()) {
            return b;
        }
        int currentX = (int) ev.getX();
        int currentY = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = currentX;
                mDownY = currentY;
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = currentX;
                mMoveY = currentY;

                int distanceX = Math.abs(mMoveX - mDownX);
                int distanceY = mMoveY - mDownY;
                if (distanceX < distanceY && distanceX < 20 && distanceY > 20) {
                    //重新布局
                    if (mListener != null) {
                        mListener.onUserTouch();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;

        }
        return b;
    }

    public void setOnUserTouchListener(OnUserTouchListener listener) {
        mListener = listener;
    }

    public interface OnUserTouchListener {
        void onUserTouch();
    }
}
