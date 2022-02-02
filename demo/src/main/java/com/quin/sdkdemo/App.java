package com.quin.sdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.alibaba.android.arouter.launcher.ARouter;
import com.module.mprinter.PrinterKit;
import com.module.mprinter.bluetooth.Bluetooth;
import com.module.mprinter.printer.constant.BatteryFlag;
import com.module.mprinter.printer.constant.PrinterConstants;
import com.module.mprinter.printer.listener.state.OnPrinterStateChangeListener;
import com.module.mprinter.printer.support.finder.XPrinterFinder;
import com.module.mprinter.util.LibraryKit;
import com.quin.sdkdemo.common.BaseActivity;
import com.quin.sdkdemo.util.ToastUtil;

public class App extends MultiDexApplication {
    private static final String TAG = "App";

    private static App app;

    private BaseActivity mTopActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        // 初始化SDK
        PrinterKit.init(this);
        // 设置默认打印机型号信息(用于未连接时的一些预置处理,如果确定连接机器后才调用SDK相关方法，就不需要预先设置机型信息)
        // 主要是为了设置一些打印机默认信息, 如打印机名称,dpi,打印宽度等
        PrinterKit.setDefaultPrinterConfig(PrinterConstants.Type.M110);
        // 所有接入的机器, 默认可以不使用
        PrinterKit.setPrinterFinder(new XPrinterFinder());

        ARouter.init(this);
        LibraryKit.init(this);
        registerListener();
    }

    public static App getApp() {
        return app;
    }

    private void registerListener() {
        registerActivityLifecycle();
        registerBluetoothConnect();
        // 注册监听蓝牙状态, 大部分情况全局用同一个即可, 也可以注册多个用于不同情况的处理
        PrinterKit.subscribePrinterConnectStateListener(mConnectStateListener);
        // 注册打印机状态监听, 全局用同一个(需要注意的是，该注册，之后最后一次调用设置的监听才会生效)
        PrinterKit.setOnPrinterStateChangeListener(mOnPrinterStateChangeListener);
    }

    /**
     * 注册监听Activity生命周期
     */
    private void registerActivityLifecycle() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (activity instanceof BaseActivity) {
                    mTopActivity = (BaseActivity) activity;
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (activity.equals(mTopActivity)) {
                    mTopActivity = null;
                }
            }
        });
    }

    /**
     *
     */
    private void registerBluetoothConnect() {
        // 全局使用同一个连接回调即可

    }

    protected OnPrinterStateChangeListener mOnPrinterStateChangeListener = new OnPrinterStateChangeListener() {

        @Override
        public void onCarbonBeltRemainCount(int remainAmount) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onCarbonBeltRemainCount(remainAmount);
                });
            }
        }

        @Override
        public void onPaperRemainCount(int remainAmount) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onPaperRemainCount(remainAmount);
                });
            }
        }

        @Override
        public void onRibbonRemainCount(int remainAmount) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onRibbonRemainCount(remainAmount);
                });
            }
        }

        @Override
        public void onConsumableStateChange(int type, boolean hasConsumable) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onConsumableStateChange(type, hasConsumable);
                });
            }
        }

        @Override
        public void onCoverStateChanged(boolean isOpen) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onCoverStateChanged(isOpen);
                });
            }
        }

        @Override
        public void onCutterStateChanged(boolean isPress) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onCutterStateChanged(isPress);
                });
            }
        }

        @Override
        public void onHighTempStateChanged(boolean highTemp) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onHighTempStateChanged(highTemp);
                });
            }
        }

        @Override
        public void onConsumableError(int consumableType) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onConsumableError(consumableType);
                });
            }
        }

        @Override
        public void onLowBattery(@BatteryFlag int level) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onLowBattery(level);
                });
            }
        }

        @Override
        public void onPaperStateChanged(boolean hasPaper) {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onPaperStateChanged(hasPaper);
                });
            }
        }

        @Override
        public void onCancel() {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onCancel();
                });
            }
        }

        @Override
        public void onComplete() {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onComplete();
                });
            }
        }

        @Override
        public void onError() {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onError();
                });
            }
        }
    };

    private void showToast(String msg) {
        if (mTopActivity != null) {
            mTopActivity.runOnUiThread(() -> ToastUtil.showToast(msg));
        }
    }

    public final Bluetooth.BluetoothConnectStateListener mConnectStateListener = new Bluetooth.BluetoothConnectStateListener() {
        @Override
        public void onBluetoothConnected(String name, String mac) {
            String msg = "printer is connected";
            showToast(msg);
            Log.i(TAG, msg);
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onBluetoothConnected(name, mac);
                });
            }
        }

        @Override
        public void onBluetoothConnectionFailed() {
            String msg = "Printer connection failed";
            showToast(msg);
            Log.i(TAG, msg);
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onBluetoothConnectionFailed();
                });
            }
        }

        @Override
        public void onBluetoothDisconnected(boolean isActive) {
            String msg = "The printer is disconnected, manually disconnect?" + isActive;
            showToast(msg);
            Log.i(TAG, msg);
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onBluetoothDisconnected(isActive);
                });
            }
        }

        @Override
        public void onBluetoothConnecting() {
            if (mTopActivity != null) {
                mTopActivity.runOnUiThread(() -> {
                    mTopActivity.onBluetoothConnecting();
                });
            }
        }
    };
}
