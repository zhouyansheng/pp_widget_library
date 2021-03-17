package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


import com.yunqipei.widgetlibrary.R;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * - @Description: 可伸缩的<br/>
 * - @Author: edie<br/>
 * - @Time: 2019/7/18 下午1:53
 */
public class FlexTagFlowLayout extends TagFlowLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    protected static final int LEFT = -1;
    protected static final int CENTER = 0;
    protected static final int RIGHT = 1;
    protected int mGravity;
    protected List<View> lineViews = new ArrayList<>();
    protected boolean mIsHintShowAll;
    protected boolean mLayoutFlag;

    public FlexTagFlowLayout(Context context) {
        this(context, null);
    }

    public FlexTagFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexTagFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        mGravity = ta.getInt(R.styleable.TagFlowLayout_tag_gravity, LEFT);
        ta.recycle();

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void setHintShowAll(boolean isHintShowAll) {
        mIsHintShowAll = isHintShowAll;
        //隐藏最后一个
        getChildAt(getChildCount() - 1).setVisibility(GONE);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mIsHintShowAll && mLineHeight.size() > 2) {
            int sizeWidth = View.MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(sizeWidth,
                    mLineHeight.get(0) + mLineHeight.get(1) + getPaddingTop() + getPaddingBottom()//
            );
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();
        mLineWidth.clear();
        lineViews.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        int cCount = getChildCount();

        //最后一个数据不拿(显示全部)
        for (int i = 0; i < cCount - 1; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child
                    .getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width - getPaddingLeft() - getPaddingRight()) {
                //换行 添加上一行的数据
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);
                mLineWidth.add(lineWidth);

                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                lineViews = new ArrayList<View>();
                if (!mIsHintShowAll && mAllViews.size() == 1) {
                    //第二行并且不隐藏
                    lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
                    View childAll = getChildAt(cCount - 1);
                    lineViews.add(childAll);
                }

            }
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin
                    + lp.bottomMargin);
            if (!mIsHintShowAll && mAllViews.size() == 1) {
                lineViews.add(lineViews.size() - 1, child);
            } else {
                lineViews.add(child);
            }

        }
        mLineHeight.add(lineHeight);
        mLineWidth.add(lineWidth);
        mAllViews.add(lineViews);


        int left = getPaddingLeft();
        int top = getPaddingTop();

        int lineNum = mAllViews.size();
        if (!mIsHintShowAll && lineNum > 2) {
            lineNum = 2;
        }

        for (int i = 0; i < lineNum; i++) {
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            // set gravity
            int currentLineWidth = this.mLineWidth.get(i);
            switch (this.mGravity) {
                case LEFT:
                    left = getPaddingLeft();
                    break;
                case CENTER:
                    left = (width - currentLineWidth) / 2 + getPaddingLeft();
                    break;
                case RIGHT:
                    left = width - currentLineWidth + getPaddingLeft();
                    break;
            }

            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }

                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child
                        .getLayoutParams();

                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.leftMargin
                        + lp.rightMargin;
            }
            top += lineHeight;
        }
    }

    @Override
    public void onGlobalLayout() {
        if (!mLayoutFlag && !mIsHintShowAll && mLineHeight.size() > 2) {
            mLayoutFlag = true;
            requestLayout();
        }
    }
}
