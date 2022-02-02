package com.quin.sdkdemo.printer.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.module.mprinter.PrinterInfo;
import com.module.mprinter.PrinterKit;
import com.module.mprinter.printer.listener.callback.OnConsumableInfoCallback;
import com.module.mprinter.printer.listener.callback.OnPaperTypeCallback;
import com.module.mprinter.printer.listener.state.OnConsumableRemainStateListener;
import com.module.mprinter.printer.listener.state.OnPaperStateChangeListener;
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.common.BaseActivity;
import com.quin.sdkdemo.printer.setting.PrinterSettingActivity;
import com.quin.sdkdemo.util.TimeUtil;

public class PrinterDetailActivity extends BaseActivity {
    public static final String TAG = "PrinterDetailActivity";

    private TextView tvPrinterName;
    private TextView tvMac;
    private TextView tvFirmWareVersion;
    private TextView tvSn;
    private TextView tvPower;
    private TextView tvPowerOffTime;
    private TextView tvDpi;
    private TextView tvMaxPrintWidth;
    private TextView tvCarbonBeltSerial;
    private TextView tvPaperSerial;
    private TextView tvRibbonSerial;

    private TextView tvCarbonBeltRemainAmount;
    private TextView tvPaperRemainAmount;
    private TextView tvRibbonRemainAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_detail);
        initViews();
        setData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @SuppressLint("DefaultLocale")
    private void setData() {
        tvPrinterName.setText(String.format("设备名称: %s", PrinterInfo.getName()));
        tvMac.setText(String.format("MAC: %s", PrinterInfo.getMac()));
        tvSn.setText(String.format("SN:%s", PrinterInfo.getSn()));
        tvDpi.setText(String.format("DPI: %dDPI", PrinterInfo.getDpi()));
        tvMaxPrintWidth.setText(String.format("maximum print width: %dmm", PrinterInfo.getPrintMaxWidth()));
    }

    @SuppressLint("DefaultLocale")
    private void loadData() {
        PrinterKit.getFirmwareVersion(s -> runOnUiThread(() -> {
            Log.i(TAG, "setData: " + PrinterInfo.getFwVersion());
            tvFirmWareVersion.setText(String.format("Firmware version number: %s", PrinterInfo.getFwVersion()));
        }));
        PrinterKit.getBattery(i -> runOnUiThread(() -> tvPower.setText(String.format("剩余电量: %d%%", i))));
        PrinterKit.getPowerOffTime(i -> runOnUiThread(() -> tvPowerOffTime.setText(String.format("Auto shutdown time: %s", TimeUtil.getTimeStringByMinute(i)))));

        getEncryptData();
    }

    private void initViews() {
        initToolbar("Device management");
        mToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.setting) {
                startActivity(new Intent(PrinterDetailActivity.this, PrinterSettingActivity.class));
            }
            return true;
        });

        tvPrinterName = findViewById(R.id.tv_printer_name);
        tvMac = findViewById(R.id.tv_mac);
        tvFirmWareVersion = findViewById(R.id.tv_firmware_version);
        tvSn = findViewById(R.id.tv_sn);
        tvPower = findViewById(R.id.tv_power);
        tvPowerOffTime = findViewById(R.id.tv_auto_power_off);
        tvDpi = findViewById(R.id.tv_dpi);
        tvMaxPrintWidth = findViewById(R.id.tv_max_print_width);
        tvCarbonBeltSerial = findViewById(R.id.tv_rfid_carbon_serial);
        tvPaperSerial = findViewById(R.id.tv_rfid_paper_serial);
        tvRibbonSerial = findViewById(R.id.tv_rfid_ribbon_serial);

        tvCarbonBeltRemainAmount = findViewById(R.id.tv_rfid_carbon_remain);
        tvPaperRemainAmount = findViewById(R.id.tv_rfid_paper_remain);
        tvRibbonRemainAmount = findViewById(R.id.tv_rfid_ribbon_remain);

        findViewById(R.id.btn_disconnect).setOnClickListener(v -> {
            PrinterKit.disconnect();
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 查询加密好像信息
     */
    private void getEncryptData() {
        // 查询顺序： 开盖状态-->（关盖）是否有纸--> （有纸）查询纸张类型--> （知道纸张类型）查RFID --> （有RFID）查余量
        if (PrinterInfo.isCoverOpen()) {
            // 开盖啥都不用查
            return;
        }

        PrinterKit.getPaperStatus(new OnPaperStateChangeListener() {
            @Override
            public void onPaperStateChanged(boolean hasPaper) {
                if (hasPaper) {
                    PrinterKit.getPaperType(new OnPaperTypeCallback() {
                        @Override
                        public void onPaperType(int type) {
                            if (type != 0) {
                                PrinterKit.getRFIDLabelInformation(new OnConsumableInfoCallback() {
                                    @Override
                                    public void onCarbonBeltInformationResult(String s) {
                                        Log.i(TAG, "onCarbonBeltInformationResult: " + s);
                                        runOnUiThread(() -> tvCarbonBeltSerial.setText(String.format("RFID Ribbon serial number: %s", s)));
                                        queryEncryptCount();
                                    }

                                    @Override
                                    public void onPaperBeltInformationResult(String s) {
                                        Log.i(TAG, "onPaperBeltInformationResult: " + s);
                                        runOnUiThread(() -> tvPaperSerial.setText(String.format("RFID paper serial number: %s", s)));
                                        queryEncryptCount();
                                    }

                                    @Override
                                    public void onRibbonInformationResult(String s) {
                                        Log.i(TAG, "onRibbonInformationResult: " + s);
                                        runOnUiThread(() -> tvRibbonSerial.setText(String.format("RFID Ribbon Serial Number: %s", s)));
                                        queryEncryptCount();
                                    }

                                    @Override
                                    public void onConsumableError(int consumableType) {
                                        runOnUiThread(() -> {
                                            PrinterDetailActivity.this.onConsumableError(consumableType);
                                        });
                                        Log.i(TAG, "onConsumableError: " + consumableType);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 查询加密耗材余量
     */
    private void queryEncryptCount() {
        PrinterKit.getRFIDConsumableRemainAmountStatus(new OnConsumableRemainStateListener() {
            @Override
            public void onCarbonBeltRemainCount(int i) {
                Log.i(TAG, "onCarbonBeltRemainCount: " + i);
                runOnUiThread(() -> tvCarbonBeltRemainAmount.setText(String.format("Ribbon allowance: %dmm", i)));
            }

            @Override
            public void onPaperRemainCount(int i) {
                Log.i(TAG, "onPaperRemainCount: " + i);
                runOnUiThread(() -> tvPaperRemainAmount.setText(String.format("Paper allowance: %d(sheets/mm)", i)));
            }

            @Override
            public void onRibbonRemainCount(int i) {
                Log.i(TAG, "onRibbonRemainCount: " + i);
                runOnUiThread(() -> tvRibbonRemainAmount.setText(String.format("Ribbon allowance: %dmm", i)));
            }
        });
    }
}