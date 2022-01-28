package com.quin.sdkdemo.printer.setting.power;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.module.mprinter.PrinterKit;
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.common.BaseActivity;
import com.quin.sdkdemo.util.TimeUtil;
import com.quin.sdkdemo.util.ToastUtil;

public class AutoPowerOffActivity extends BaseActivity {

    private AlertDialog mTimeSelectedDialog;
    private TextView mPowerOffTime;

    private String[] mPowerOffTimeArray;

    @Override
    protected void onResume() {
        super.onResume();
        PrinterKit.getPowerOffTime(i -> runOnUiThread(() -> mPowerOffTime.setText(TimeUtil.getTimeStringByMinute(i))));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_power_off);
        initToolbar("自动关机时间");
        findViewById(R.id.ll_power_off_click_area).setOnClickListener(v -> {
            showTimeSelectDialog();
        });
        mPowerOffTime = findViewById(R.id.tv_power_off_time);
        mPowerOffTimeArray = getResources().getStringArray(R.array.power_off_time);
    }


    private void showTimeSelectDialog() {
        if (mTimeSelectedDialog == null) {
            mTimeSelectedDialog = new AlertDialog.Builder(this)
                    .setTitle("设置关机时间")
                    .setItems(R.array.power_off_time, (dialog, which) -> {
                        int powerOffTime = getSelectedPowerOffTimeMinute(which);
                        if (powerOffTime != -1) {
                            PrinterKit.setPowerOffTime(powerOffTime);
                            mPowerOffTime.setText(mPowerOffTimeArray[which]);
                        } else {
                            ToastUtil.showToast("invalid value setting");
                        }
                    }).create();
        }
        if (mTimeSelectedDialog.isShowing()) return;
        mTimeSelectedDialog.show();
    }

    /**
     * 选中项转换成具体的时间（单位分钟）
     */
    private int getSelectedPowerOffTimeMinute(int index) {
        switch (index) {
            case 0:
                return 0;
            case 1:
                return 5;
            case 2:
                return 15;
            case 3:
                return 30;
            case 4:
                return 60;
            case 5:
                return 60 * 2;
            case 6:
                return 60 * 4;
            case 7:
                return 60 * 8;
            case 8:
                return 60 * 24;
        }
        return -1;
    }
}

