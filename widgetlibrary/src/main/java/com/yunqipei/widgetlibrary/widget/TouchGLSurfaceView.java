package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * edie create on 2019-12-06
 */
public class TouchGLSurfaceView extends GLSurfaceView {
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private OnTouchGLESListener mListener;
    private float mCurrX1;
    private float mCurrY1;
    private float mPrevX1;
    private float mPrevY1;
    private float mCurrX2;
    private float mCurrY2;
    private float mPrevX2;
    private float mPrevY2;

    public TouchGLSurfaceView(Context context) {
        this(context, null);
    }

    public TouchGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {

        setFocusable(true);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {


            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                int x = (int) e.getX();
                int y = (int) e.getY();

                if (mListener != null) {
                    mListener.onPicked(x, y);
                }

                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {


                float x = (float) (Math.PI * distanceX / getWidth());
                float y = (float) (Math.PI * distanceY / getWidth());


                if (mListener != null) {
                    if (e2.getPointerCount() == 1) {
                        //旋转
                        mListener.onRotate(y, x);
                    } else {
                        mCurrX1 = e2.getX();
                        mCurrY1 = e2.getY();
                        mCurrX2 = e2.getX(1);
                        mCurrY2 = e2.getY(1);


                        double angl1 = Math.atan2(mPrevY1 - mCurrY1, mPrevX1 - mCurrX1);
                        double angl2 = Math.atan2(mPrevY2 - mCurrY2, mPrevX2 - mCurrX2);

                        if (Math.abs(angl1 - angl2) * 180 / Math.PI < 15) {
                            //偏移15度之内认为是平移,偏移0.6以下认为是手指颤动
                            if (Math.abs(distanceX) < 0.6f) {
                                distanceX = 0;
                            }
                            if (Math.abs(distanceY) < 0.6f) {
                                distanceY = 0;
                            }
                            mListener.onTranslation(-distanceX / 20f, -distanceY / 20f, 0);
                        }


                    }

                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }

        });


        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            //手势

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }

                if (mListener != null) {
                    mListener.onScale(scaleFactor);
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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        super.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mPrevX1 = event.getX();
                mPrevY1 = event.getY();
                mPrevX2 = event.getX(1);
                mPrevY2 = event.getY(1);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        return true;
    }

    public void setOnTouchGLESListener(OnTouchGLESListener listener) {
        mListener = listener;
    }

    public interface OnTouchGLESListener {
        /**
         * 缩放
         *
         * @param scale
         */
        void onScale(float scale);

        /**
         * 移动
         *
         * @param x
         * @param y
         * @param z
         */
        void onTranslation(float x, float y, float z);

        /**
         * 旋转
         *
         * @param axleX 绕X轴的旋转弧度
         * @param axleY 绕Y轴的旋转弧度
         */
        void onRotate(float axleX, float axleY);

        /**
         * 拾取
         *
         * @param x
         * @param y
         */
        void onPicked(int x, int y);

    }
}
