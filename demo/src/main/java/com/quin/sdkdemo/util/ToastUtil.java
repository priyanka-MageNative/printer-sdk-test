package com.quin.sdkdemo.util;

import android.widget.Toast;

import com.quin.sdkdemo.App;

public class ToastUtil {
    private static Toast mToast;

    public static void showToast(String msg) {
        if (mToast == null) {
            mToast = new Toast(App.getApp());
            mToast = Toast.makeText(App.getApp(), msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setText(msg);
        }
        mToast.show();
    }

    public static void showLongToast(String msg) {
        if (mToast == null) {
            mToast = new Toast(App.getApp());
            mToast = Toast.makeText(App.getApp(), msg, Toast.LENGTH_LONG);
        } else {
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.setText(msg);
        }
        mToast.show();
    }
}
