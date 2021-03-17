package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


import androidx.annotation.Nullable;

import com.yunqipei.widgetlibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * edie create on 2020-05-19
 */
public class TouchGLCurtainView extends View {
    private Path mPath;

    private Paint mPaint;
    private Paint mAllPaint;
    private int mDim6;
    private int mSpan;

    private float mPreX, mPreY;
    private boolean mIsTouchEnd;
    private OnTouchUpListener mListener;
    private RectF mRectF = new RectF();
    private Rect mBounds = new Rect();
    private ArrayList<Point> mPoints = new ArrayList<>();
    private Region mRegion = new Region();

    private ScaleGestureDetector mScaleGestureDetector;
    private OnScaleListener mScaleListener;

    public TouchGLCurtainView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mPath = new Path();

        mDim6 = context.getResources().getDimensionPixelSize(R.dimen.dim_6) / 2;
        mSpan = context.getResources().getDimensionPixelSize(R.dimen.dim_10);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mDim6);
        mPaint.setAntiAlias(true);

        mAllPaint = new Paint();
        mAllPaint.setColor(Color.parseColor("#66FA3E3D"));
        mAllPaint.setStrokeCap(Paint.Cap.ROUND);
        mAllPaint.setAntiAlias(true);


        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            //手势

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }

                if (mScaleListener != null) {
                    mScaleListener.onScaleTouch(scaleFactor);
                }

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });

    }

    public TouchGLCurtainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public TouchGLCurtainView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private  boolean Pointer;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouchEnd = false;
                mPath.reset();
                invalidate();

                Pointer=false;
                    mPath.moveTo(event.getX(), event.getY());
                    mPreX = event.getX();
                    mPreY = event.getY();

                return true;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    float middleX = (mPreX + event.getX()) / 2;
                    float middleY = (mPreY + event.getY()) / 2;
                    mPreX = event.getX();
                    mPreY = event.getY();
                    mPath.quadTo(middleX, middleY, mPreX, mPreY);
                    invalidate();
                } else {
                    Pointer=true;
                }
                break;
            case MotionEvent.ACTION_UP:
                    mIsTouchEnd = true;
                    mPath.close();

                    mPoints.clear();

                    if (Pointer){
                        return true;
                    }
                    mPath.computeBounds(mRectF, true);
                    mBounds.set((int) mRectF.left, (int) mRectF.top, (int) mRectF.right, (int) mRectF.bottom);

                    mRegion.setPath(mPath, new Region(mBounds));
                    for (int i = mBounds.left; i < mBounds.right; i += mSpan) {
                        for (int j = mBounds.top; j < mBounds.bottom; j += mSpan) {
                            if (mRegion.contains(i, j)) {
                                mPoints.add(new Point(i, j));
                            }
                        }
                    }

                    PathMeasure pm = new PathMeasure(mPath, false);
                    //获取当前长度的坐标集合和正切值集合
                    int length = Math.round(pm.getLength());
                    float[] pos;
                    for (int i = 0; i < length; i += mSpan) {
                        pos = new float[2];
                        pm.getPosTan(i, pos, null);
                        mPoints.add(new Point(Math.round(pos[0]), Math.round(pos[1])));
                    }

                    invalidate();

                    if (mListener != null) {
                        mListener.onPickPoint(mPoints, mRegion);
                    }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
        if (mIsTouchEnd) {
            canvas.drawPath(mPath, mAllPaint);
        }
    }

    public void setActivate(boolean activate) {
        mPath.reset();
        invalidate();
        setVisibility(activate ? View.VISIBLE : View.GONE);
    }


    public void setOnTouchUpListener(OnTouchUpListener listener) {
        mListener = listener;
    }

    /**
     * 功能：判断点是否在多边形内 方法：求解通过该点的水平线与多边形各边的交点 结论：单边交点为奇数，成立!
     *
     * @param point   指定的某个点
     * @param APoints 多边形的各个顶点坐标（首末点可以不一致）
     * @return
     */
    public boolean ptInPolygon(Point point, List<Point> APoints) {
        int nCross = 0;
        for (int i = 0; i < APoints.size(); i++) {
            Point p1 = APoints.get(i);
            Point p2 = APoints.get((i + 1) % APoints.size());
            // 求解 y=p.y 与 p1p2 的交点
            if (p1.y == p2.y)
                // p1p2 与 y=p0.y平行
                continue;
            if (point.y < Math.min(p1.y, p2.y))
                // 交点在p1p2延长线上
                continue;
            if (point.y >= Math.max(p1.y, p2.y))
                // 交点在p1p2延长线上
                continue;
            // 求交点的 X 坐标
            // --------------------------------------------------------------
            double x = (double) (point.y - p1.y) * (double) (p2.x - p1.x) / (double) (p2.y - p1.y) + p1.x;
            if (x > point.x)
                nCross++;
            // 只统计单边交点
        }
        // 单边交点为偶数，点在多边形之外 ---
        return (nCross % 2 == 1);
    }

    public void setScaleListener(OnScaleListener listener) {
        mScaleListener = listener;
    }

    public interface OnTouchUpListener {
        void onPickPoint(List<Point> points, Region region);
    }

    public interface OnScaleListener {
        void onScaleTouch(float scaleFactor);
    }
}
