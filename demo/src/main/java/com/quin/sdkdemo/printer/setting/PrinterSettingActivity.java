package com.quin.sdkdemo.printer.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.module.mprinter.PrinterInfo;
import com.module.mprinter.PrinterKit;
import com.module.mprinter.bluetooth.Bluetooth;
import com.module.mprinter.printer.constant.UpdateResultCode;
import com.quin.sdkdemo.common.BaseActivity;
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.printer.NewPrinterListActivity;
import com.quin.sdkdemo.printer.setting.concentration.PrinterConcentrationSettingActivity;
import com.quin.sdkdemo.printer.setting.paper.SetPrinterPaperTypeActivity;
import com.quin.sdkdemo.printer.setting.power.AutoPowerOffActivity;
import com.quin.sdkdemo.printer.setting.speed.PrinterSpeedSettingActivity;
import com.quin.sdkdemo.util.FileUtil;
import com.quin.sdkdemo.util.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrinterSettingActivity extends BaseActivity implements View.OnClickListener {

    private AlertDialog mAlertDialog;

    private final Bluetooth.BluetoothConnectStateListener mConnectStateListener = new Bluetooth.BluetoothConnectStateListener() {
        @Override
        public void onBluetoothConnected(String name, String mac) {
            ToastUtil.showToast("printer is connected");
        }

        @Override
        public void onBluetoothConnectionFailed() {
            ToastUtil.showToast("Printer connection failed");
        }

        @Override
        public void onBluetoothDisconnected(boolean isActive) {
            startActivity(new Intent(PrinterSettingActivity.this, NewPrinterListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            ToastUtil.showToast("Printer disconnected");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (PrinterInfo.isConnect()) {
            PrinterKit.subscribePrinterConnectStateListener(mConnectStateListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (PrinterInfo.isConnect()) {
            PrinterKit.unSubscribePrinterConnectStateListener(mConnectStateListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_setting);
        initToolbar("设置");
        findViewById(R.id.tv_set_paper_temporary).setOnClickListener(this);
        findViewById(R.id.tv_set_concentration_temporary).setOnClickListener(this);
        findViewById(R.id.tv_set_speed_temporary).setOnClickListener(this);
        findViewById(R.id.tv_set_concentration_permanent).setOnClickListener(this);
        findViewById(R.id.tv_set_speed_permanent).setOnClickListener(this);
        findViewById(R.id.tv_set_power_off_time).setOnClickListener(this);
        findViewById(R.id.tv_firmware_update).setOnClickListener(this);
    }

    private void showUpdatingDialog() {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog
                    .Builder(this)
                    .setTitle("更新提示")
                    .setMessage("机器更新中...")
                    .setCancelable(false)
                    .setNegativeButton("取消更新", (dialog, which) -> {
                        PrinterKit.cancelUpdate();
                        ToastUtil.showToast("Cancel update");
                        dialog.dismiss();
                    })
                    .create();
        }
        mAlertDialog.show();
    }

    private void hideUpdatingDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_set_paper_temporary:
                startActivity(new Intent(PrinterSettingActivity.this, SetPrinterPaperTypeActivity.class));
                break;
            case R.id.tv_set_concentration_temporary:
                PrinterConcentrationSettingActivity.gotoSettingPrinterConcentration(PrinterSettingActivity.this, false);
                break;
            case R.id.tv_set_speed_temporary:
                PrinterSpeedSettingActivity.gotoSettingPrinterSpeed(PrinterSettingActivity.this, false);
                break;
            case R.id.tv_set_concentration_permanent:
                PrinterConcentrationSettingActivity.gotoSettingPrinterConcentration(PrinterSettingActivity.this, true);
                break;
            case R.id.tv_set_speed_permanent:
                PrinterSpeedSettingActivity.gotoSettingPrinterSpeed(PrinterSettingActivity.this, true);
                break;
            case R.id.tv_set_power_off_time:
                startActivity(new Intent(PrinterSettingActivity.this, AutoPowerOffActivity.class));
                break;
            case R.id.tv_firmware_update:
                if (!PrinterInfo.isConnect()) {
                    ToastUtil.showToast("Printer not connected");
                    return;
                }
                showUpdatingDialog();
                PrinterKit.getSerial(s -> {
                    try {
                        String sn = s.substring(0, 4);
                        InputStream in;
                        File firmWareFile;
                        FileOutputStream out;
                        String printerName = "";
                        // 只需要验证D30-杰里蓝牙(Q109)和 M200-其他蓝牙(Q006)
                        switch (sn) {
                            case "Q109":
                                printerName = "Q109";
                                break;
                            case "Q006":
                                printerName = "Q006";
                                break;
                        }

                        if (TextUtils.isEmpty(printerName)) {
                            runOnUiThread(() -> {
                                ToastUtil.showToast("Firmware upgrade file not found");
                                hideUpdatingDialog();
                            });
                            return;
                        }

                        in = getAssets().open(printerName + ".zip");
                        FileUtil.clearFirmWareDir();//清空上一次的固件残留文件
                        firmWareFile = FileUtil.getFirmwareFile(printerName);
                        out = new FileOutputStream(firmWareFile);

                        if (FileUtil.writeFirmWareToLocal(out, in)) {
                            PrinterKit.update(firmWareFile.getPath(), i -> {
                                runOnUiThread(() -> {
                                    hideUpdatingDialog();
                                    switch (i) {
                                        case UpdateResultCode.COMPLETE:
                                            ToastUtil.showToast("update completed");
                                            break;
                                        case UpdateResultCode.DATA_ERROR:
                                            ToastUtil.showToast("update failed");
                                            break;
                                        case UpdateResultCode.MODEL_ERROR:
                                            ToastUtil.showToast("Firmware file error");
                                            break;
                                    }
                                });
                            });
                        } else {
                            ToastUtil.showToast("Firmware file error");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            ToastUtil.showToast("update failed\n");
                        });
                    }
                });
                break;
        }

    }
}