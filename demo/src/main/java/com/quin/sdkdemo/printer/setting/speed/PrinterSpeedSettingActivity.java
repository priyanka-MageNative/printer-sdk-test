package com.quin.sdkdemo.printer.setting.speed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.module.mprinter.PrinterKit;
import com.quin.sdkdemo.common.BaseActivity;
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.util.ToastUtil;

public class PrinterSpeedSettingActivity extends BaseActivity {

    private final static String EXTRA_IS_PERMANENT_SETTING = "is_permanent_setting";

    public static void gotoSettingPrinterSpeed(Activity activity, boolean isPermanent) {
        activity.startActivity(new Intent(activity, PrinterSpeedSettingActivity.class).putExtra(EXTRA_IS_PERMANENT_SETTING, isPermanent));
    }

    private EditText mEtPrintSpeed;
    private boolean mIsPermanent;//是否永久设置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_speed_setting);
        mIsPermanent = getIntent().getBooleanExtra(EXTRA_IS_PERMANENT_SETTING, false);
        initToolbar(mIsPermanent ? "Set print speed (permanent modification)" : "Set print speed (temporary modification)");
        mEtPrintSpeed = findViewById(R.id.et_speed);
        setEditCursor();
        mEtPrintSpeed.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                setEditCursor();
            }
        });
        mEtPrintSpeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String speed = mEtPrintSpeed.getText().toString().trim();
                if (TextUtils.isEmpty(speed)) return;
                if (Integer.parseInt(speed) > 5) {
                    mEtPrintSpeed.setText("5");
                }

                if (Integer.parseInt(speed) < 1) {
                    mEtPrintSpeed.setText("1");
                }
            }
        });


        findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            if (mIsPermanent) {
                PrinterKit.setDefaultRate(Integer.parseInt(mEtPrintSpeed.getText().toString()));
            } else {
                PrinterKit.setRate(Integer.parseInt(mEtPrintSpeed.getText().toString()));
            }
            ToastUtil.showToast("set successfully");
            finish();
        });
    }

    private void setEditCursor() {
        // 光标移动到最后
        mEtPrintSpeed.post(() -> {
            Spannable text = mEtPrintSpeed.getText();
            if (text != null) {
                Selection.setSelection(text, text.length());
            }
        });
    }
}