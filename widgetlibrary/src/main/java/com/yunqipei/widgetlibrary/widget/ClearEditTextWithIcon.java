package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.yunqipei.widgetlibrary.R;


/**
 * 带有图标和删除符号的可编辑输入框，用户可以自定义传入的显示图标
 * <p>
 * Created by jiajie on 2016/10/26.
 */
public class ClearEditTextWithIcon extends TextInputEditText implements View.OnTouchListener, TextWatcher {

    private Drawable mClearTextIcon;
    private Drawable mLeftIcon;

    public ClearEditTextWithIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClearEditTextWithIcon);
        float spacing = typedArray.getFloat(R.styleable.ClearEditTextWithIcon_letterSpacing, 0);
        mClearTextIcon = getResources().getDrawable(R.drawable.ic_close);
        mClearTextIcon.setBounds(0, 0, mClearTextIcon.getIntrinsicWidth(), mClearTextIcon.getIntrinsicHeight());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setLetterSpacing(spacing);
        }
        setEnabled(true);
        setOnTouchListener(this);
        addTextChangedListener(this);
        refreshClearButton();
    }

    void refreshClearButton() {
        boolean isEmpty = TextUtils.isEmpty(getText().toString());
        setCompoundDrawables(mLeftIcon == null ? getCompoundDrawables()[0] : mLeftIcon, getCompoundDrawables()[1],
                isEmpty ? null : mClearTextIcon, getCompoundDrawables()[3]);
    }

    public ClearEditTextWithIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ClearEditTextWithIcon(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * 传入显示的图标资源id
     *
     * @param id id
     */
    public void setIconResource(int id) {
        mLeftIcon = getResources().getDrawable(id);
        mLeftIcon.setBounds(0, 0, mLeftIcon.getIntrinsicWidth(), mLeftIcon.getIntrinsicHeight());
        refreshClearButton();
    }

    /**
     * 传入删除图标资源id
     *
     * @param resId resId
     */
    public void setClearTextIcon(int resId) {
        mClearTextIcon = getResources().getDrawable(resId);
        mClearTextIcon.setBounds(0, 0, mClearTextIcon.getIntrinsicWidth(), mClearTextIcon.getIntrinsicHeight());
        refreshClearButton();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getX() > getWidth() - getPaddingRight() - mClearTextIcon.getIntrinsicWidth()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // 手指离开了删除图标
                setText("");
            }
        }
        return false;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        refreshClearButton();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}