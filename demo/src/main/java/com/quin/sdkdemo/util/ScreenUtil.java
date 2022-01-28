package com.quin.sdkdemo.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.module.mprinter.util.LibraryKit;

public class ScreenUtil {

    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) LibraryKit.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        // 屏幕宽度（像素）
        return dm.widthPixels;
    }

    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) LibraryKit.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        // 屏幕高度（像素）
        return dm.heightPixels;
    }

    public static int getScreenRealHeight() {
        WindowManager wm = (WindowManager) LibraryKit.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        // 屏幕高度（像素）
        return dm.heightPixels;
    }

    /**
     * 判断是否显示虚拟导航栏
     *
     * @param activity activity
     * @return boolean
     */
    public static boolean isShowNavigationBar(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        return decorView.getChildAt(0).getHeight() + getNavigationBarHeight() <= decorView.getHeight();
    }

    public static void setNavigationBarColor(@NonNull Activity activity, int color) {
        setNavigationBarColor(activity.getWindow(), color);
    }

    public static void setNavigationBarColor(@NonNull Dialog dialog, int color) {
        setNavigationBarColor(dialog.getWindow(), color);
    }

    /**
     * 设置导航栏背景色
     *
     * @param color 背景颜色值
     */
    public static void setNavigationBarColor(Window window, int color) {
        if (window == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color);
        }
    }

    /**
     * @param window 显示的Window（Activity、Dialog）
     * @param isLightMode 导航栏（背景、虚拟按键颜色）显示样式控制， false--默认白色背景，黑色图标， true--默认黑色背景，灰白图标，
     * */
    public static void setNavigationBarLightMode(Window window, boolean isLightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = window.getDecorView();
            int vis = decorView.getSystemUiVisibility();
            if (isLightMode) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            decorView.setSystemUiVisibility(vis);
        }
    }

    /**
     * 获取导航栏高度
     * 个别手机，在没有显示导航栏时，返回值也不为0
     *
     * @return 导航栏高度
     */
    public static int getNavigationBarHeight() {
        Resources res = Resources.getSystem();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    /**
     * 获取状态栏高度
     *
     * @return 高度， 单位px
     */
    public static int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = Resources.getSystem().getDimensionPixelSize(resId);
        }
        return statusBarHeight;
    }

    public static int dp2px(float value) {
        return (int) dp2pxF(value);
    }

    public static float dp2pxF(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                Resources.getSystem().getDisplayMetrics());
    }

    public static float sp2px(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, Resources.getSystem().getDisplayMetrics());
    }

    public static int px2sp(float pxValue) {
        float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int mm2px(float mmValue) {
        return (int) mm2pxF(mmValue);
    }

    public static float mm2pxF(float mmValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mmValue, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 按 1pt = 0.3527mm 转换
     *
     * @param ptValue
     * @return
     */
    public static float pt2pxF(float ptValue) {
        float densityDpi = Resources.getSystem().getDisplayMetrics().densityDpi; // 每英寸（25.4mm）px数
        return ptValue * 0.3527f * densityDpi / 25.4f; // pt对应毫米数 * 每毫米px数
    }

    /**
     * 将打印机头的长度转换成实际手机上的长度（视觉上长度相等）
     *
     * @param printWidth 打印机头的长度，单位像素
     * @param printerDpi 打印机的Dpi
     */
    public static int changeWidthFromPrint2Phone(int printWidth, int printerDpi) {
        float xdpi = Resources.getSystem().getDisplayMetrics().xdpi;
        return (int) (printWidth * xdpi / printerDpi);
    }

    /**
     * 给窗口设置进制截屏标志
     */
    public static void enableWindowSecure(@NonNull Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }
}
