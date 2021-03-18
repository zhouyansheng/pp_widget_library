package com.yunqipei.widgetlibrary.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.yunqipei.widgetlibrary.R;


/**
 * edie create on 2018/9/18
 */
public class LoadingNewDialog extends AlertDialog {

    public LoadingNewDialog(@NonNull Context context) {
        super(context);
    }

    public LoadingNewDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public LoadingNewDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading_new);
        getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        setCanceledOnTouchOutside(false);
    }
}
