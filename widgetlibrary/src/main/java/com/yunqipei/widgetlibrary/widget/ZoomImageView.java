package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.ViewPager;

import java.lang.ref.WeakReference;

/**
 * ZoomImageView 放大后可以自由移动，多指缩放
 * ScaleGestureDetector 捕获缩放事件
 * onTouch
 * onScale
 * Matrix
 * <p>
 * 双击放大、缩小
 * GestureDetector 捕获双击事件
 * postDelay + Runnable
 * <p>
 * Created by jiajie on 16/9/17.
 */
public class ZoomImageView extends AppCompatImageView implements
        OnGlobalLayoutListener,
        OnScaleGestureListener,
        View.OnTouchListener {

    private boolean mOnce = false;

    private float mInitScale;
    private float mMidScale;
    private float mMaxScale;
    private Matrix mScaleMatrix;

    //捕获用户多指触控时缩放的比例
    private ScaleGestureDetector mScaleGestureDetector;

    //--------自由移动
    private int mLastPointerCount;
    private float mLastX;
    private float mLastY;
    private int mTouchSlop;
    private boolean isCanDrag;
    private boolean isCheckLeftAndRight;
    private boolean isCheckTopAndBottom;

    //--------双击放大、缩小
    private GestureDetector mGestureDetector;
    private boolean isAutoScale;
    private static final long AUTO_SCALE_DELAY_MILLS = 16L;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScaleMatrix = new Matrix();
        setScaleType(ImageView.ScaleType.MATRIX);

        mScaleGestureDetector = new ScaleGestureDetector(context, this);

        setOnTouchListener(this);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                //如果正在缩放过程中，直接return
                if (isAutoScale) return true;

                float x = e.getX();
                float y = e.getY();

                if (getCurrentScale() < mMidScale) {
                    postDelayed(new AutoScaleRunnable(ZoomImageView.this, mMidScale, x, y), AUTO_SCALE_DELAY_MILLS);
                    isAutoScale = true;
                } else {
                    postDelayed(new AutoScaleRunnable(ZoomImageView.this, mInitScale, x, y), AUTO_SCALE_DELAY_MILLS);
                    isAutoScale = true;
                }

                return true;
            }
        });
    }

    //缩放区间 initScale ~ maxScale
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (getDrawable() == null) return true;

        float scale = getCurrentScale();
        float scaleFactor = detector.getScaleFactor();

        //缩放范围的控制
        if ((scale < mMaxScale && scaleFactor > 1.0f) || //放大动作、并 小于 最大范围值
                (scale > mInitScale && scaleFactor < 1.0f)) { //缩小动作、并 大于 最小范围值
            if (scale * scaleFactor < mInitScale) {
                scaleFactor = mInitScale / scale;
            }

            if (scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / scale;
            }

            //缩放
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;// 处理所有scale事件
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) return true; //双击时不能进行下面的移动操作

        mScaleGestureDetector.onTouchEvent(event);

        //图片自由移动
        float x = 0;
        float y = 0;

        int pointerCount = event.getPointerCount();

        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }

        x /= pointerCount;
        y /= pointerCount;

        if (mLastPointerCount != pointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }

        mLastPointerCount = pointerCount;

        RectF rectF = getMatrixRectF();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //如果图片被放大了，就不让 父布局 拦截事件
                if (rectF.width() > getWidth() + 0.01f || rectF.height() > getHeight() + 0.01f) {
                    if (getParent() instanceof ViewPager) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (rectF.width() > getWidth() + 0.01f || rectF.height() > getHeight() + 0.01f) {
                    if (getParent() instanceof ViewPager) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!isCanDrag) {
                    isCanDrag = isMoveAction(dx, dy);
                }

                if (isCanDrag) {
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        //如果宽小于控件宽，不允许横向移动
                        if (rectF.width() < getWidth()) {
                            isCheckLeftAndRight = false;
                            dx = 0;
                        }
                        //如果高小于控件高，不允许纵向移动
                        if (rectF.height() < getHeight()) {
                            isCheckTopAndBottom = false;
                            dy = 0;
                        }
                        mScaleMatrix.postTranslate(dx, dy);
                        checkBorderWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    public void onGlobalLayout() {
        //在ImageView加载完成后，计算drawable应该缩放的大小，并显示在控件中心，只执行一次
        if (!mOnce) {
            Drawable d = getDrawable();
            if (d == null) return;

            //控件宽高
            int width = getWidth();
            int height = getHeight();
            //得到图片，以及宽高
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();

            float scale = 1.0f;
            /**
             * 如果图片的宽度大于控件宽度，高度小于控件高度，将其缩小
             */
            if (dw > width && dh < height) {
                scale = width * 1.0f / dw;
            }

            /**
             * 如果图片高度大于控件高度，宽度小于控件宽度，将其缩小
             */
            if (dh > height && dw < width) {
                scale = height * 1.0f / dh;
            }

            /**
             * 如果图片宽度大于控件宽度，图片高度大于控件高度
             * or
             * 如果图片宽度小于控件宽度，图片高度小于控件高度
             */
            if ((dw > width && dh > height) || (dw < width && dh < height)) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            //得到初始化时缩放的比例
            mInitScale = scale;
            mMidScale = mInitScale * 2;
            mMaxScale = mInitScale * 4;

            //将图片移动至控件中心
            int dx = width / 2 - dw / 2;
            int dy = height / 2 - dh / 2;

            mScaleMatrix.postTranslate(dx, dy);
            mScaleMatrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
            setImageMatrix(mScaleMatrix);

            mOnce = true;
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 获取当前图片的缩放值
     *
     * @return scale
     */
    public float getCurrentScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    /**
     * 获得图片放大缩小以后的宽高，以及left,top,right,bottom
     *
     * @return rectF
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable d = getDrawable();
        if (d != null) {
            rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 在缩放的时候进行边界控制，以及位置控制
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        //图片宽 大于 控件宽
        if (rectF.width() >= width) {
            if (rectF.left > 0) {
                //屏幕左边有空隙
                deltaX = -rectF.left;
            }
            if (rectF.right < width) {
                //屏幕右边有空隙
                deltaX = width - rectF.right;
            }
        }

        //图片高 大于 控件高
        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                //屏幕上边有空隙
                deltaY = -rectF.top;
            }
            if (rectF.bottom < height) {
                //屏幕下边有空隙
                deltaY = height - rectF.bottom;
            }
        }

        //图片宽或高 小于 控件宽或高 ，则让其居中
        if (rectF.width() < width) {
            deltaX = width / 2f - rectF.right + rectF.width() / 2f;
        }
        if (rectF.height() < height) {
            deltaY = height / 2f - rectF.bottom + rectF.height() / 2f;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 在位移的时候进行边界控制，以及位置控制
     */
    private void checkBorderWhenTranslate() {
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        if (rectF.top > 0 && isCheckTopAndBottom) {
            deltaY = -rectF.top;
        }

        if (rectF.bottom < height && isCheckTopAndBottom) {
            deltaY = height - rectF.bottom;
        }

        if (rectF.left > 0 && isCheckLeftAndRight) {
            deltaX = -rectF.left;
        }

        if (rectF.right < width && isCheckLeftAndRight) {
            deltaX = width - rectF.right;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 自动放大、缩小 任务线程
     */
    private static class AutoScaleRunnable implements Runnable {

        private WeakReference<ZoomImageView> weakReference;

        //缩放目标值
        private float mTargetScale;
        //缩放的中心点
        private float x;
        private float y;

        private final float BIGGER = 1.07f;
        private final float SMALLER = 0.93f;

        private float tmpScale;

        AutoScaleRunnable(ZoomImageView zoomImageView, float mTargetScale, float x, float y) {
            this.weakReference = new WeakReference<>(zoomImageView);

            this.mTargetScale = mTargetScale;
            this.x = x;
            this.y = y;

            float currentScale = weakReference.get().getCurrentScale();

            if (currentScale < mTargetScale) {
                tmpScale = BIGGER;
            }
            if (currentScale > mTargetScale) {
                tmpScale = SMALLER;
            }
        }

        @Override
        public void run() {
            ZoomImageView zoomImageView = weakReference.get();
            if (zoomImageView != null) {
                //每次缩放 tmpScale(BIGGER or SMALLER)
                zoomImageView.mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
                zoomImageView.checkBorderAndCenterWhenScale();
                zoomImageView.setImageMatrix(zoomImageView.mScaleMatrix);

                float currentScale = zoomImageView.getCurrentScale();
                if ((tmpScale > 1.0f) && currentScale < mTargetScale ||
                        (tmpScale < 1.0f && currentScale > mTargetScale)) {
                    // 放大or缩小 过程
                    zoomImageView.postDelayed(this, AUTO_SCALE_DELAY_MILLS);
                } else {
                    // 缩放完成，设置为目标值
                    float scale = mTargetScale / currentScale;
                    zoomImageView.mScaleMatrix.postScale(scale, scale, x, y);
                    zoomImageView.checkBorderAndCenterWhenScale();
                    zoomImageView.setImageMatrix(zoomImageView.mScaleMatrix);
                    //取消缩放状态
                    zoomImageView.isAutoScale = false;
                }
            }
        }

    }

    /**
     * 判断是不是move事件
     *
     * @param dx x轴移动的距离
     * @param dy y轴移动的距离
     *
     * @return is move or not
     */
    private boolean isMoveAction(float dx, float dy) {
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }

}
