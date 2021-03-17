package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.yunqipei.widgetlibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 关键词流式布局
 * Created by yangle on 2016/10/10.
 */
public class FlowLayout extends RelativeLayout {

    // 水平间距，单位为dp
    private int horizontalSpacing = dp2px(10);
    // 竖直间距，单位为dp
    private int verticalSpacing = dp2px(10);
    // 行的集合
    private List<Line> lines = new ArrayList<>();
    // 当前的行
    private Line line;
    // 当前行使用的空间
    private int lineSize = 0;
    // 关键字大小，单位为sp
    private int textSize;
    // 关键字颜色
    private ColorStateList textColor;
    // 关键字背景框
    private int itemBackground;
    // 关键字水平padding，单位为dp
    private int textPaddingH;
    // 关键字竖直padding，单位为dp
    private int textPaddingV;
    // 每行的child个数
    private int columnCount = 3;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.FlowLayout, defStyleAttr, 0);

        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.FlowLayout_horizontalSpacing) {
                horizontalSpacing = typedArray.getDimensionPixelSize(attr, dp2px(10));
            } else if (attr == R.styleable.FlowLayout_verticalSpacing) {
                verticalSpacing = typedArray.getDimensionPixelSize(attr, dp2px(10));
            } else if (attr == R.styleable.FlowLayout_textSize) {
                textSize = typedArray.getDimensionPixelSize(attr, sp2px(15));
            } else if (attr == R.styleable.FlowLayout_textColor) {
                textColor = typedArray.getColorStateList(attr);
            } else if (attr == R.styleable.FlowLayout_itemBackground) {
                itemBackground = typedArray.getResourceId(attr, 0);
            } else if (attr == R.styleable.FlowLayout_textPaddingH) {
                textPaddingH = typedArray.getDimensionPixelSize(attr, dp2px(7));
            } else if (attr == R.styleable.FlowLayout_textPaddingV) {
                textPaddingV = typedArray.getDimensionPixelSize(attr, dp2px(4));
            } else if (attr == R.styleable.FlowLayout_columnCount) {
                columnCount = typedArray.getInt(attr, 3);
            }
        }
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 实际可以用的宽和高
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // Line初始化
        restoreLine();

        int measuredWidth = (width - horizontalSpacing * (columnCount - 1)) / columnCount;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            // 测量所有的childView
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                    widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthMode);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                    heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : heightMode);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            if (line == null) {
                // 创建新一行
                line = new Line();
            }

            // 计算当前行已使用的宽度
            lineSize += measuredWidth;

            // 如果使用的宽度小于可用的宽度，这时候childView能够添加到当前的行上
            if (lineSize <= width) {
                line.addChild(child);
                lineSize += horizontalSpacing;
            } else {
                // 换行
                newLine();
                line.addChild(child);
                lineSize += measuredWidth;
                lineSize += horizontalSpacing;
            }
        }

        // 把最后一行记录到集合中
        if (line != null && !lines.contains(line)) {
            lines.add(line);
        }

        int totalHeight = 0;
        // 把所有行的高度加上
        for (int i = 0; i < lines.size(); i++) {
            totalHeight += lines.get(i).getHeight();
        }
        // 加上行的竖直间距
        totalHeight += verticalSpacing * (lines.size() - 1);
        // 加上上下padding
        totalHeight += getPaddingBottom();
        totalHeight += getPaddingTop();

        // 设置自身尺寸
        // 设置布局的宽高，宽度直接采用父view传递过来的最大宽度，而不用考虑子view是否填满宽度
        // 因为该布局的特性就是填满一行后，再换行
        // 高度根据设置的模式来决定采用所有子View的高度之和还是采用父view传递过来的高度
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                resolveSize(totalHeight, heightMeasureSpec));
    }

    private void restoreLine() {
        lines.clear();
        line = new Line();
        lineSize = 0;
    }

    private void newLine() {
        // 把之前的行记录下来
        if (line != null) {
            lines.add(line);
        }
        // 创建新的一行
        line = new Line();
        lineSize = 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            line.layout(left, top);
            // 计算下一行的起点y轴坐标
            top = top + line.getHeight() + verticalSpacing;
        }
    }

    /**
     * 管理每行上的View对象
     */
    private class Line {
        // 子控件集合
        private List<View> children = new ArrayList<>();
        // 行高
        int height;

        /**
         * 添加childView
         *
         * @param childView 子控件
         */
        void addChild(View childView) {
            children.add(childView);

            // 让当前的行高是最高的一个childView的高度
            if (height < childView.getMeasuredHeight()) {
                height = childView.getMeasuredHeight();
            }
        }

        /**
         * 指定childView的绘制区域
         *
         * @param left 左上角x轴坐标
         * @param top  左上角y轴坐标
         */
        public void layout(int left, int top) {
            int totalWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            // 当前childView的左上角x轴坐标
            int currentLeft = left;

            for (int i = 0; i < children.size(); i++) {
                View view = children.get(i);
                // 指定childView的绘制区域
                int viewWidth = (totalWidth - horizontalSpacing * (columnCount - 1)) / columnCount;
                view.layout(currentLeft, top, currentLeft + viewWidth, top + view.getMeasuredHeight());
                // 计算下一个childView的位置
                currentLeft = currentLeft + viewWidth + horizontalSpacing;
            }
        }

        public int getHeight() {
            return height;
        }

        public int getChildCount() {
            return children.size();
        }
    }

    public void setFlowItems(List<String> items, final OnFlowItemClickListener onItemClickListener) {
        if (items == null) return;
        removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            addItem(items.get(i), onItemClickListener);
        }
    }

    public void addItem(String item, final OnFlowItemClickListener onItemClickListener) {
        addView(createFlowItem(item, onItemClickListener),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private TextView createFlowItem(String item, final OnFlowItemClickListener onItemClickListener) {
        final TextView tv = new TextView(getContext());
        tv.setText(item);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        tv.setTextColor(textColor);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(textPaddingH, textPaddingV, textPaddingH, textPaddingV);
        tv.setBackgroundResource(itemBackground);
        tv.setSelected(false);

        tv.setClickable(true);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, tv.getText().toString());
            }
        });
        return tv;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = dp2px(horizontalSpacing);
    }

    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = dp2px(verticalSpacing);
    }

    public void setTextSize(int textSize) {
        this.textSize = sp2px(textSize);
    }

    public void setTextColor(ColorStateList textColor) {
        this.textColor = textColor;
    }

    public void setItemBackground(int itemBackground) {
        this.itemBackground = itemBackground;
    }

    public void setTextPaddingH(int textPaddingH) {
        this.textPaddingH = dp2px(textPaddingH);
    }

    public void setTextPaddingV(int textPaddingV) {
        this.textPaddingV = dp2px(textPaddingV);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }

    public interface OnFlowItemClickListener {
        void onItemClick(View view, String content);
    }

}