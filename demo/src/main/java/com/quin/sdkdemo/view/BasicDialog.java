package com.quin.sdkdemo.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import androidx.annotation.NonNull;

//todo:增加Dialog动画出场效果
public class BasicDialog extends Dialog {

    public BasicDialog(@NonNull Context context) {
        super(context);
        //设置透明背景
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

}
