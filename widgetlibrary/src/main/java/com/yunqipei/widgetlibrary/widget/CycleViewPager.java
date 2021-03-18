package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.yunqipei.baselibrary.picloader.GlideEngine;

/**
 * Created by wangx on 2016/9/13 0013.
 */
public class CycleViewPager extends ViewPager {
    public CycleViewPager(Context context) {
        super(context);
    }

    public CycleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        InnerPagerChangeLisener innerPagerChangeLisener = new InnerPagerChangeLisener(listener);
        super.addOnPageChangeListener(innerPagerChangeLisener);
    }


    class InnerPagerChangeLisener implements OnPageChangeListener {

        private OnPageChangeListener listener;
        private int position;

        public InnerPagerChangeLisener(OnPageChangeListener listener) {

            this.listener = listener;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (listener != null) {
                listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {

            this.position = position;
            if (listener != null) {
                listener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                //完成页面切换的时候  自动悄悄切换
                if (position == 0) {
                    //滑动到了最前边的D元素
                    CycleViewPager.this.setCurrentItem(getAdapter().getCount() - 2, false);
                } else if (position == getAdapter().getCount() - 1) {
                    //滑动到了 最后边的A元素
                    setCurrentItem(1, false);
                }
            }
            if (listener != null) {
                listener.onPageScrollStateChanged(state);
            }
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {

        InnerPageerAdapter pageerAdapter = new InnerPageerAdapter(adapter);// [DABCDA]
        super.setAdapter(pageerAdapter);
        addOnPageChangeListener(null);
        setCurrentItem(1);
        startScroll();//自动轮播
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentItem = getCurrentItem();
            currentItem++;
            setCurrentItem(currentItem);
            handler.sendEmptyMessageDelayed(1, 4000);
        }
    };

    private void startScroll() {
        handler.sendEmptyMessageDelayed(1, 4000);
    }
    public void stopScroll(){
        handler.removeMessages(1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
               stopScroll();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL://手指滑动到外边
            case MotionEvent.ACTION_UP:
                startScroll();
                break;
        }
        return super.onTouchEvent(ev);
    }

    class InnerPageerAdapter extends PagerAdapter {

        private PagerAdapter adapter;

        public InnerPageerAdapter(PagerAdapter adapter) {

            this.adapter = adapter;
        }

        @Override
        public int getCount() {
            return adapter.getCount() + 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return adapter.isViewFromObject(view, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //position 修正
            if (position == 0) {
                //D
                position = adapter.getCount() - 1;
            } else if (position == getCount() - 1) {
                //A
                position = 0;
            } else {
                position -= 1;
            }
            return adapter.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            adapter.destroyItem(container, position, object);
        }
    }
}
