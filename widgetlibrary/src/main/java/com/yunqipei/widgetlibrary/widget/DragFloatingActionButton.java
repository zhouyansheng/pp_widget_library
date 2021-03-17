package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * edie create on 2018/8/27
 */
public class DragFloatingActionButton extends FloatingActionButton {
    private int mTouchSlop;
    private int parentHeight;
    private int parentWidth;
    private int lastX;
    private int lastY;
    private int downX;
    private int downY;
    private boolean isDrag;

    public DragFloatingActionButton(Context context) {
        this(context, null);
    }

    public DragFloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                downX = rawX;
                downY = rawY;
                lastX = rawX;
                lastY = rawY;
                ViewGroup parent;
                if (getParent() != null) {
                    parent = (ViewGroup) getParent();
                    parentHeight = parent.getHeight();
                    parentWidth = parent.getWidth();
                }
                break;
            case MotionEvent.ACTION_MOVE:

                int dx = rawX - lastX;
                int dy = rawY - lastY;

                float x = getX() + dx;
                float y = getY() + dy;
                //检测是否到达边缘 左上右下
                x = x < 0 ? 0 : x > parentWidth - getWidth() ? parentWidth - getWidth() : x;
                y = getY() < 0 ? 0 : getY() + getHeight() > parentHeight ? parentHeight - getHeight() : y;
                setX(x);
                setY(y);
                lastX = rawX;
                lastY = rawY;
                break;
            case MotionEvent.ACTION_UP:
                int offX = rawX - downX;
                int offY = rawY - downY;
                int distance = (int) Math.sqrt(offX * offX + offY * offY);
                if (distance < mTouchSlop) {
                    //不是有效滑动 点击
                    isDrag = false;
                } else {
                    isDrag = true;
                }
                break;
        }
        return true;

    }

    @Override
    public boolean performClick() {
        if (!isDrag) {
            super.performClick();
        }
        return false;
    }
}
