package com.quin.sdkdemo.common;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.module.mprinter.PrinterInfo;
import com.module.mprinter.PrinterKit;
import com.module.mprinter.bluetooth.Bluetooth;
import com.module.mprinter.printer.constant.BatteryFlag;
import com.module.mprinter.printer.constant.EncryptType;
import com.module.mprinter.printer.listener.state.OnPrinterStateChangeListener;
import com.module.mprinter.printer.listener.state.OnPrintingStateChangeListener;
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.util.ToastUtil;

public class BaseActivity extends AppCompatActivity
        // 打印机状态回调
        implements OnPrinterStateChangeListener,
        // 蓝牙连接结果回调
        Bluetooth.BluetoothConnectStateListener,
        // OnPrinterStateChangeListener已经继承了OnPrintingStateChangeListener,所以也可以不在这里显式声明
        OnPrintingStateChangeListener {
    public static String TAG = "BaseActivity";

    protected Toolbar mToolbar;

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
    }

    public void showAlertDialog(String msg) {

        if (mAlertDialog == null && !isDestroyed()) {
            mAlertDialog = new AlertDialog
                    .Builder(this)
                    .setTitle("打印机提示")
                    .setMessage(msg)
                    .create();
        } else {
            mAlertDialog.setMessage(msg);
        }

        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }

        mAlertDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (PrinterInfo.isConnect()) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    protected void initToolbar(String titleName) {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        TextView tvTitle = findViewById(R.id.title);
        tvTitle.setText(titleName);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    // ================= 打印机状态回调 =================

    // =================加密耗材剩余数量，在主动查询的时候重写逻辑即可，监听状态暂时不需要用

    @Override
    public void onCarbonBeltRemainCount(int remainAmount) {
//        String msg = "碳带余量：" + remainAmount;
//        showAlertDialog(msg);
//        Log.i(TAG, msg);
    }

    @Override
    public void onPaperRemainCount(int remainAmount) {
//        String msg = "纸张余量：" + remainAmount;
//        showAlertDialog(msg);
//        Log.i(TAG, msg);
    }

    @Override
    public void onRibbonRemainCount(int remainAmount) {
//        String msg = "色带余量：" + remainAmount;
//        showAlertDialog(msg);
//        Log.i(TAG, msg);
    }

    @Override
    public void onConsumableStateChange(int type, boolean hasConsumable) {

        if (PrinterInfo.isPrinting()) {
            // 有的机器返回余量指令太多了， 会拖慢UI线程， 打印期间不提示
            return;
        }

        String msg = "";
        String pattern = "打印机%s%s";
        String status = hasConsumable ? "识别正常" : "耗尽";
        switch (type) {
            case EncryptType.CARBON:
                // 因为可能短时间内返回加密耗材信息，所以先分开碳带和纸张的提示方式，避免覆盖之后看不到上一条数据的提示
                msg = String.format(pattern, "碳带", status);
                ToastUtil.showToast(msg);
                break;
            case EncryptType.PAPER:
                msg = String.format(pattern, "纸张", status);
                showAlertDialog(msg);
                break;
            case EncryptType.RIBBON:
                msg = String.format(pattern, "色带", status);
                break;
        }

//        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onCoverStateChanged(boolean isOpen) {

        if (!isOpen) {
            // 关盖查询rfid信息（可能换了纸张类型）
            PrinterKit.getRFIDLabelInformation(null);
        }

//        String msg = "盖子状态：" + (isOpen ? "打开" : "关闭");
        String msg;
        if (isOpen) {
            msg = "打印机开盖";
        } else {
            msg = "打印机关盖";
        }
        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onCutterStateChanged(boolean isPress) {
//        String msg = "切刀状态：" + (isPress ? "按下" : "松开");
        String msg;
        if (isPress) {
            msg = "打印机切刀被触碰";
        } else {
            msg = "打印机切刀松开";
        }
        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onHighTempStateChanged(boolean highTemp) {
//        String msg = "高温状态：" + (highTemp ? "高温报警" : "高温解除");
        String msg;
        if (highTemp) {
            msg = "打印机温度过高";
        } else {
            msg = "打印机高温解除";
        }
        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onConsumableError(int consumableType) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("加密耗材异常：");
        switch (consumableType) {
            case EncryptType.CARBON:
                stringBuilder.append("加密碳带");
                break;
            case EncryptType.PAPER:
                stringBuilder.append("加密纸张");
                break;
            case EncryptType.RIBBON:
                stringBuilder.append("加密色带");
                break;
        }

        String msg = stringBuilder.toString();
        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onLowBattery(@BatteryFlag int level) {
        String msg = "unknown";
        switch (level) {
            case BatteryFlag.DRY_CELL:
                msg = "干电池电量不足";
                break;
            case BatteryFlag.LOW_POWER_3:
                msg = "打印机即将关机";
                break;
            case BatteryFlag.LOW_POWER_5:
                msg = "打印机电量低于5%";
                break;
            case BatteryFlag.LOW_POWER_10:
                msg = "打印机电量低于10%";
                break;
        }

        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onPaperStateChanged(boolean hasPaper) {
        // 关盖才提示纸张状态
        if (PrinterInfo.isCoverOpen()) {
            // 开盖时拿到的纸张状态没用， 也可能会不准
            return;
        }
//        String msg = "纸张状态：" + (hasPaper ? "上纸" : "缺纸");
        String msg;
        if (hasPaper) {
            msg = "打印机上纸";
            ToastUtil.showToast(msg);
        } else {
            msg = "打印机缺纸";
            showAlertDialog(msg);
        }
        Log.i(TAG, msg);
    }

    @Override
    public void onCancel() {
        String msg = "打印机打印取消";
        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onComplete() {
        String msg = "打印完成";
        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onError() {
        String msg = "打印异常";
        showAlertDialog(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onBluetoothConnected(String name, String mac) {
        String msg = "打印机已连接" + ", name:" + name + ", mac:" + mac;
//        showAlertDialog(msg);
        ToastUtil.showToast(msg);
        Log.i(TAG, msg);
    }

    @Override
    public void onBluetoothDisconnected(boolean isActive) {
//        String msg = "打印机断开连接";
////        showAlertDialog(msg);
//        ToastUtil.showToast(msg);
//        Log.i(TAG, msg);
    }

    @Override
    public void onBluetoothConnectionFailed() {
//        String msg = "打印机连接失败";
////        showAlertDialog(msg);
//        ToastUtil.showToast(msg);
//        Log.i(TAG, msg);
    }

    @Override
    public void onBluetoothConnecting() {
//        String msg = "正在连接打印机";
//        showAlertDialog(msg);
//        ToastUtil.showToast(msg);
//        Log.i(TAG, msg);
    }
}
