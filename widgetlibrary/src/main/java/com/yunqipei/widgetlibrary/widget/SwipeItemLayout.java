package com.yunqipei.widgetlibrary.widget;


import android.content.Context;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeItemLayout extends ViewGroup {
    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    private Mode mTouchMode;

    private ViewGroup mMainView;
    private ViewGroup mSideView;

    private ScrollRunnable mScrollRunnable;
    private int mScrollOffset;
    private int mMaxScrollOffset;

    private boolean mInLayout;
    private boolean mIsLaidOut;

    public SwipeItemLayout(Context context) {
        this(context, null);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchMode = Mode.RESET;
        mScrollOffset = 0;
        mIsLaidOut = false;

        mScrollRunnable = new ScrollRunnable(context);
    }

    public static void closeAllItems(RecyclerView recyclerView) {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            if (child instanceof SwipeItemLayout) {
                SwipeItemLayout swipeItemLayout = (SwipeItemLayout) child;
                if (swipeItemLayout.isOpen())
                    swipeItemLayout.close();
            }
        }
    }

    public boolean isOpen() {
        return mScrollOffset != 0;
    }

    Mode getTouchMode() {
        return mTouchMode;
    }

    void setTouchMode(Mode mode) {
        switch (mTouchMode) {
            case FLING:
                mScrollRunnable.abort();
                break;
            case RESET:
                break;
        }

        mTouchMode = mode;
    }

    void fling(int xVel) {
        mScrollRunnable.startFling(mScrollOffset, xVel);
    }

    void revise() {
        if (mScrollOffset < -mMaxScrollOffset / 2)
            open();
        else
            close();
    }

    public void open() {
        if (mScrollOffset != -mMaxScrollOffset) {
            //??????open??????????????????
            if (mTouchMode == Mode.FLING && mScrollRunnable.isScrollToLeft())
                return;

            //????????????????????????abort
            if (mTouchMode == Mode.FLING /*&& !mScrollRunnable.mScrollToLeft*/)
                mScrollRunnable.abort();

            mScrollRunnable.startScroll(mScrollOffset, -mMaxScrollOffset);
        }
    }

    public void close() {
        if (mScrollOffset != 0) {
            //??????close??????????????????
            if (mTouchMode == Mode.FLING && !mScrollRunnable.isScrollToLeft())
                return;

            //?????????????????????abort
            if (mTouchMode == Mode.FLING /*&& mScrollRunnable.mScrollToLeft*/)
                mScrollRunnable.abort();

            mScrollRunnable.startScroll(mScrollOffset, 0);
        }
    }

    boolean trackMotionScroll(int deltaX) {
        if (deltaX == 0)
            return false;

        boolean over = false;
        int newLeft = mScrollOffset + deltaX;
        if ((deltaX > 0 && newLeft > 0) || (deltaX < 0 && newLeft < -mMaxScrollOffset)) {
            over = true;
            newLeft = Math.min(newLeft, 0);
            newLeft = Math.max(newLeft, -mMaxScrollOffset);
        }

        offsetChildrenLeftAndRight(newLeft - mScrollOffset);
        mScrollOffset = newLeft;
        return over;
    }

    void offsetChildrenLeftAndRight(int delta) {
        ViewCompat.offsetLeftAndRight(mMainView, delta);
        ViewCompat.offsetLeftAndRight(mSideView, delta);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        //click main view??????????????????open???????????????????????????????????????????????????????????????click listener
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                View pointView = findTopChildUnder(this, x, y);
                if (pointView != null && pointView == mMainView && mScrollOffset != 0)
                    return true;
                break;
            }

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_UP: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                View pointView = findTopChildUnder(this, x, y);
                if (pointView != null && pointView == mMainView && mTouchMode == Mode.TAP && mScrollOffset != 0)
                    return true;
            }
        }

        return false;
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams && super.checkLayoutParams(p);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mScrollOffset != 0 && mIsLaidOut) {
            offsetChildrenLeftAndRight(-mScrollOffset);
            mScrollOffset = 0;
        } else
            mScrollOffset = 0;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mScrollOffset != 0 && mIsLaidOut) {
            offsetChildrenLeftAndRight(-mScrollOffset);
            mScrollOffset = 0;
        } else
            mScrollOffset = 0;
        removeCallbacks(mScrollRunnable);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!ensureChildren())
            throw new RuntimeException("SwipeItemLayout???????????????????????????");

        mInLayout = true;

        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        int pr = getPaddingRight();
        int pb = getPaddingBottom();

        MarginLayoutParams mainLp = (MarginLayoutParams) mMainView.getLayoutParams();
        MarginLayoutParams sideParams = (MarginLayoutParams) mSideView.getLayoutParams();

        int childLeft = pl + mainLp.leftMargin;
        int childTop = pt + mainLp.topMargin;
        int childRight = getWidth() - (pr + mainLp.rightMargin);
        int childBottom = getHeight() - (mainLp.bottomMargin + pb);
        mMainView.layout(childLeft, childTop, childRight, childBottom);

        childLeft = childRight + sideParams.leftMargin;
        childTop = pt + sideParams.topMargin;
        childRight = childLeft + sideParams.leftMargin + sideParams.rightMargin + mSideView.getMeasuredWidth();
        childBottom = getHeight() - (sideParams.bottomMargin + pb);
        mSideView.layout(childLeft, childTop, childRight, childBottom);

        mMaxScrollOffset = mSideView.getWidth() + sideParams.leftMargin + sideParams.rightMargin;
        mScrollOffset = mScrollOffset < -mMaxScrollOffset / 2 ? -mMaxScrollOffset : 0;

        offsetChildrenLeftAndRight(mScrollOffset);
        mInLayout = false;
        mIsLaidOut = true;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams ? p : new MarginLayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    static View findTopChildUnder(ViewGroup parent, int x, int y) {
        final int childCount = parent.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = parent.getChildAt(i);
            if (x >= child.getLeft() && x < child.getRight()
                    && y >= child.getTop() && y < child.getBottom()) {
                return child;
            }
        }
        return null;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (getVisibility() != View.VISIBLE) {
            mScrollOffset = 0;
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        //click main view??????????????????open???????????????????????????????????????????????????????????????click listener
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                View pointView = findTopChildUnder(this, x, y);
                if (pointView != null && pointView == mMainView && mScrollOffset != 0)
                    return true;
                break;
            }

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_UP: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                View pointView = findTopChildUnder(this, x, y);
                if (pointView != null && pointView == mMainView && mTouchMode == Mode.TAP && mScrollOffset != 0) {
                    close();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!ensureChildren())
            throw new RuntimeException("SwipeItemLayout???????????????????????????");

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        MarginLayoutParams lp = null;
        int horizontalMargin, verticalMargin;
        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        int verticalPadding = getPaddingTop() + getPaddingBottom();

        lp = (MarginLayoutParams) mMainView.getLayoutParams();
        horizontalMargin = lp.leftMargin + lp.rightMargin;
        verticalMargin = lp.topMargin + lp.bottomMargin;
        measureChildWithMargins(mMainView,
                widthMeasureSpec, horizontalMargin + horizontalPadding,
                heightMeasureSpec, verticalMargin + verticalPadding);

        if (widthMode == MeasureSpec.AT_MOST)
            widthSize = Math.min(widthSize, mMainView.getMeasuredWidth() + horizontalMargin + horizontalPadding);
        else if (widthMode == MeasureSpec.UNSPECIFIED)
            widthSize = mMainView.getMeasuredWidth() + horizontalMargin + horizontalPadding;

        if (heightMode == MeasureSpec.AT_MOST)
            heightSize = Math.min(heightSize, mMainView.getMeasuredHeight() + verticalMargin + verticalPadding);
        else if (heightMode == MeasureSpec.UNSPECIFIED)
            heightSize = mMainView.getMeasuredHeight() + verticalMargin + verticalPadding;

        setMeasuredDimension(widthSize, heightSize);

        //side layout???????????????????????????
        lp = (MarginLayoutParams) mSideView.getLayoutParams();
        verticalMargin = lp.topMargin + lp.bottomMargin;
        mSideView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - verticalMargin - verticalPadding, MeasureSpec.EXACTLY));
    }

    private boolean ensureChildren() {
        int childCount = getChildCount();

        if (childCount != 2)
            return false;

        View childView = getChildAt(0);
        if (!(childView instanceof ViewGroup))
            return false;
        mMainView = (ViewGroup) childView;

        childView = getChildAt(1);
        if (!(childView instanceof ViewGroup))
            return false;
        mSideView = (ViewGroup) childView;
        return true;
    }

    enum Mode {
        RESET, DRAG, FLING, TAP
    }

    public static class OnSwipeItemTouchListener implements RecyclerView.OnItemTouchListener {
        private SwipeItemLayout mCaptureItem;
        private float mLastMotionX;
        private float mLastMotionY;
        private VelocityTracker mVelocityTracker;

        private int mActivePointerId;

        private int mTouchSlop;
        private int mMaximumVelocity;

        private boolean mDealByParent;
        private boolean mIsProbeParent;

        public OnSwipeItemTouchListener(Context context) {
            ViewConfiguration configuration = ViewConfiguration.get(context);
            mTouchSlop = configuration.getScaledTouchSlop();
            mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
            mActivePointerId = -1;
            mDealByParent = false;
            mIsProbeParent = false;
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev) {
            if (mIsProbeParent)
                return false;

            boolean intercept = false;
            final int action = ev.getActionMasked();

            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);

            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mActivePointerId = ev.getPointerId(0);
                    final float x = ev.getX();
                    final float y = ev.getY();
                    mLastMotionX = x;
                    mLastMotionY = y;

                    boolean pointOther = false;
                    SwipeItemLayout pointItem = null;
                    //????????????ev??????????????????item
                    View pointView = findTopChildUnder(rv, (int) x, (int) y);
                    if (pointView == null || !(pointView instanceof SwipeItemLayout)) {
                        //?????????head view???bottom view
                        pointOther = true;
                    } else
                        pointItem = (SwipeItemLayout) pointView;

                    //?????????pointOther=true?????????????????????view???????????????????????????item
                    //????????????????????????item????????????capture item???????????????
                    if (!pointOther && (mCaptureItem == null || mCaptureItem != pointItem))
                        pointOther = true;

                    //????????????capture item
                    if (!pointOther) {
                        Mode touchMode = mCaptureItem.getTouchMode();

                        //????????????fling????????????drag
                        //?????????????????????requestDisallowInterceptTouchEvent
                        boolean disallowIntercept = false;
                        if (touchMode == Mode.FLING) {
                            mCaptureItem.setTouchMode(Mode.DRAG);
                            disallowIntercept = true;
                            intercept = true;
                        } else {//?????????expand??????????????????parent??????
                            mCaptureItem.setTouchMode(Mode.TAP);
                            if (mCaptureItem.isOpen())
                                disallowIntercept = true;
                        }

                        if (disallowIntercept) {
                            final ViewParent parent = rv.getParent();
                            if (parent != null)
                                parent.requestDisallowInterceptTouchEvent(true);
                        }
                    } else {//capture item???null?????????point item?????????
                        //????????????close???
                        if (mCaptureItem != null && mCaptureItem.isOpen()) {
                            mCaptureItem.close();
                            mCaptureItem = null;
                            intercept = true;
                        }

                        if (pointItem != null) {
                            mCaptureItem = pointItem;
                            mCaptureItem.setTouchMode(Mode.TAP);
                        } else
                            mCaptureItem = null;
                    }

                    //??????parent??????fling??????????????????parent????????????drag???????????????????????????move?????????parent??????
                    mIsProbeParent = true;
                    mDealByParent = rv.onInterceptTouchEvent(ev);
                    mIsProbeParent = false;
                    if (mDealByParent)
                        intercept = false;
                    break;
                }

                case MotionEvent.ACTION_POINTER_DOWN: {
                    final int actionIndex = ev.getActionIndex();
                    mActivePointerId = ev.getPointerId(actionIndex);

                    mLastMotionX = ev.getX(actionIndex);
                    mLastMotionY = ev.getY(actionIndex);
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    final int actionIndex = ev.getActionIndex();
                    final int pointerId = ev.getPointerId(actionIndex);
                    if (pointerId == mActivePointerId) {
                        final int newIndex = actionIndex == 0 ? 1 : 0;
                        mActivePointerId = ev.getPointerId(newIndex);

                        mLastMotionX = ev.getX(newIndex);
                        mLastMotionY = ev.getY(newIndex);
                    }
                    break;
                }

                //down???????????????capture item????????????????????????????????????????????????event??????
                case MotionEvent.ACTION_MOVE: {
                    final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (activePointerIndex == -1)
                        break;

                    //???down?????????????????????parent???drag????????????????????????parent????????????
                    if (mDealByParent) {
                        if (mCaptureItem != null && mCaptureItem.isOpen())
                            mCaptureItem.close();
                        return false;
                    }

                    final int x = (int) (ev.getX(activePointerIndex) + .5f);
                    final int y = (int) ((int) ev.getY(activePointerIndex) + .5f);

                    int deltaX = (int) (x - mLastMotionX);
                    int deltaY = (int) (y - mLastMotionY);
                    final int xDiff = Math.abs(deltaX);
                    final int yDiff = Math.abs(deltaY);

                    if (mCaptureItem != null && !mDealByParent) {
                        Mode touchMode = mCaptureItem.getTouchMode();

                        if (touchMode == Mode.TAP) {
                            //??????capture item???open????????????????????????????????????
                            //  1?????????????????????close item
                            //  2????????????open??????????????????????????????????????????????????????open????????????????????????capture item
                            //??????????????????open???????????????????????????close
                            //QQ??????open???????????????????????????close??????????????????????????????????????????????????????
                            if (xDiff > mTouchSlop && xDiff > yDiff) {
                                mCaptureItem.setTouchMode(Mode.DRAG);
                                final ViewParent parent = rv.getParent();
                                parent.requestDisallowInterceptTouchEvent(true);

                                deltaX = deltaX > 0 ? deltaX - mTouchSlop : deltaX + mTouchSlop;
                            } else {// if(yDiff>mTouchSlop){
                                mIsProbeParent = true;
                                boolean isParentConsume = rv.onInterceptTouchEvent(ev);
                                mIsProbeParent = false;
                                if (isParentConsume) {
                                    //??????????????????????????????????????????SwipeItemLayout?????????
                                    //??????????????????????????????SwipeRefreshLayout??????RecyclerView?????????
                                    //??????????????????????????????yDiff>mTouchSlop??????????????????????????????????????????
                                    //?????????????????????????????????????????????????????????close
                                    mDealByParent = true;
                                    mCaptureItem.close();
                                }
                            }
                        }

                        touchMode = mCaptureItem.getTouchMode();
                        if (touchMode == Mode.DRAG) {
                            intercept = true;
                            mLastMotionX = x;
                            mLastMotionY = y;

                            //???capture item????????????
                            mCaptureItem.trackMotionScroll(deltaX);
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                    if (mCaptureItem != null) {
                        Mode touchMode = mCaptureItem.getTouchMode();
                        if (touchMode == Mode.DRAG) {
                            final VelocityTracker velocityTracker = mVelocityTracker;
                            velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                            int xVel = (int) velocityTracker.getXVelocity(mActivePointerId);
                            mCaptureItem.fling(xVel);

                            intercept = true;
                        }
                    }
                    cancel();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    if (mCaptureItem != null)
                        mCaptureItem.revise();
                    cancel();
                    break;
            }

            return intercept;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent ev) {
            final int action = ev.getActionMasked();
            final int actionIndex = ev.getActionIndex();

            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);

            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    mActivePointerId = ev.getPointerId(actionIndex);

                    mLastMotionX = ev.getX(actionIndex);
                    mLastMotionY = ev.getY(actionIndex);
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    final int pointerId = ev.getPointerId(actionIndex);
                    if (pointerId == mActivePointerId) {
                        final int newIndex = actionIndex == 0 ? 1 : 0;
                        mActivePointerId = ev.getPointerId(newIndex);

                        mLastMotionX = ev.getX(newIndex);
                        mLastMotionY = ev.getY(newIndex);
                    }
                    break;

                //down???????????????capture item????????????????????????????????????????????????event??????
                case MotionEvent.ACTION_MOVE: {
                    final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    if (activePointerIndex == -1)
                        break;

                    final float x = ev.getX(activePointerIndex);
                    final float y = (int) ev.getY(activePointerIndex);

                    int deltaX = (int) (x - mLastMotionX);

                    if (mCaptureItem != null && mCaptureItem.getTouchMode() == Mode.DRAG) {
                        mLastMotionX = x;
                        mLastMotionY = y;

                        //???capture item????????????
                        mCaptureItem.trackMotionScroll(deltaX);
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                    if (mCaptureItem != null) {
                        Mode touchMode = mCaptureItem.getTouchMode();
                        if (touchMode == Mode.DRAG) {
                            final VelocityTracker velocityTracker = mVelocityTracker;
                            velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                            int xVel = (int) velocityTracker.getXVelocity(mActivePointerId);
                            mCaptureItem.fling(xVel);
                        }
                    }
                    cancel();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    if (mCaptureItem != null)
                        mCaptureItem.revise();

                    cancel();
                    break;

            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }

        void cancel() {
            mDealByParent = false;
            mActivePointerId = -1;
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }

    }

    class ScrollRunnable implements Runnable {
        private static final int FLING_DURATION = 200;
        private Scroller mScroller;
        private boolean mAbort;
        private int mMinVelocity;
        private boolean mScrollToLeft;

        ScrollRunnable(Context context) {
            mScroller = new Scroller(context, sInterpolator);
            mAbort = false;
            mScrollToLeft = false;

            ViewConfiguration configuration = ViewConfiguration.get(context);
            mMinVelocity = configuration.getScaledMinimumFlingVelocity();
        }

        void startFling(int startX, int xVel) {
            Log.e("fling - startX", "" + startX);

            if (xVel > mMinVelocity && startX != 0) {
                startScroll(startX, 0);
                return;
            }

            if (xVel < -mMinVelocity && startX != -mMaxScrollOffset) {
                startScroll(startX, -mMaxScrollOffset);
                return;
            }

            startScroll(startX, startX > -mMaxScrollOffset / 2 ? 0 : -mMaxScrollOffset);
        }

        void startScroll(int startX, int endX) {
            if (startX != endX) {
                Log.e("scroll - startX - endX", "" + startX + " " + endX);
                setTouchMode(Mode.FLING);
                mAbort = false;
                mScrollToLeft = endX < startX;
                mScroller.startScroll(startX, 0, endX - startX, 0, 400);
                ViewCompat.postOnAnimation(SwipeItemLayout.this, this);
            }
        }

        void abort() {
            if (!mAbort) {
                mAbort = true;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    removeCallbacks(this);
                }
            }
        }

        //????????????????????????????????????
        boolean isScrollToLeft() {
            return mScrollToLeft;
        }

        @Override
        public void run() {
            Log.e("abort", Boolean.toString(mAbort));
            if (!mAbort) {
                boolean more = mScroller.computeScrollOffset();
                int curX = mScroller.getCurrX();
                Log.e("curX", "" + curX);

                boolean atEdge = trackMotionScroll(curX - mScrollOffset);
                if (more && !atEdge) {
                    ViewCompat.postOnAnimation(SwipeItemLayout.this, this);
                    return;
                }

                if (atEdge) {
                    removeCallbacks(this);
                    if (!mScroller.isFinished())
                        mScroller.abortAnimation();
                    setTouchMode(Mode.RESET);
                }

                if (!more) {
                    setTouchMode(Mode.RESET);
                    //?????????????????????????????????????????????????????????
                    if (mScrollOffset != 0) {
                        if (Math.abs(mScrollOffset) > mMaxScrollOffset / 2)
                            mScrollOffset = -mMaxScrollOffset;
                        else
                            mScrollOffset = 0;
                        ViewCompat.postOnAnimation(SwipeItemLayout.this, this);
                    }
                }
            }
        }
    }

}

