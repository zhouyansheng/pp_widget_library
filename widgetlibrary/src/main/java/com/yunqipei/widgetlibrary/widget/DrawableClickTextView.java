package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * DrawableClickTextView drawable可点击的TextView
 * Created by jiajie on 2017/9/25.
 */

public class DrawableClickTextView extends AppCompatTextView {

    private static final String TAG = "DrawableClickTextView";

    private DrawableClickListener mListener;

    final int DRAWABLE_RIGHT = 2;

    public DrawableClickTextView(Context context) {
        super(context);
    }

    public DrawableClickTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableClickTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mListener != null) {
                    Drawable drawableRight = getCompoundDrawables()[DRAWABLE_RIGHT];
                    if (drawableRight != null && event.getRawX() >= (getRight() - getPaddingEnd() - drawableRight.getBounds().width())) {
                        mListener.onDrawableRightClick(this);
                        return true;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setDrawableClickListener(DrawableClickListener listener) {
        this.mListener = listener;
    }

    public interface DrawableClickListener {
        void onDrawableRightClick(View view);
    }

}
