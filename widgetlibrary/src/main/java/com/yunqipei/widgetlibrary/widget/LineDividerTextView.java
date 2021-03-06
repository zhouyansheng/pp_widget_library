package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.yunqipei.widgetlibrary.R;


/**
 * - @Description: 带分割线的textview<br/>
 * - @Author: edie<br/>
 * - @Time: 2018/12/4 下午6:28
 */
public class LineDividerTextView extends AppCompatTextView {

    public static final String TAG = LineDividerTextView.class.getSimpleName();
    ;
    private Rect mRect;

    private Drawable lineDivider;
    private int lineDividerHeight;

    public LineDividerTextView(Context context) {
        this(context, null);
    }

    public LineDividerTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineDividerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LineDividerTextView, defStyleAttr, 0);
        lineDivider = array.getDrawable(R.styleable.LineDividerTextView_line_divider);
        lineDividerHeight = array.getDimensionPixelSize(R.styleable.LineDividerTextView_line_divider_height, 0);
        array.recycle();

        init();
    }

    private void init() {
        mRect = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = getLineCount();

        for (int i = 0; i < count; i++) {
            // last line not draw
            if (i == count - 1) {
                break;
            }
            getLineBounds(i, mRect);

            lineDivider.setBounds(
                    mRect.left,
                    (int) (mRect.bottom - getLineSpacingExtra() / 2 - lineDividerHeight / 2),
                    mRect.right,
                    (int) (mRect.bottom - getLineSpacingExtra() / 2 + lineDividerHeight / 2));
            lineDivider.draw(canvas);
        }

    }

}
