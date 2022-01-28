package com.quin.sdkdemo.printer.setting.concentration;

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
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.common.BaseActivity;
import com.quin.sdkdemo.util.ToastUtil;

public class PrinterConcentrationSettingActivity extends BaseActivity {

    private final static String EXTRA_IS_PERMANENT_SETTING = "is_permanent_setting";

    public static void gotoSettingPrinterConcentration(Activity activity, boolean isPermanent) {
        activity.startActivity(new Intent(activity, PrinterConcentrationSettingActivity.class).putExtra(EXTRA_IS_PERMANENT_SETTING, isPermanent));
    }

    private EditText mEtConcentration;
    private boolean mIsPermanent;//是否永久设置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_concentration_setting);
        mIsPermanent = getIntent().getBooleanExtra(EXTRA_IS_PERMANENT_SETTING, false);
        initToolbar(mIsPermanent ? "设置打印浓度(永久修改)" : "设置打印浓度(临时修改)");
        mEtConcentration = findViewById(R.id.et_concentration);
        setEditCursor();
        mEtConcentration.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                setEditCursor();
            }
        });
        mEtConcentration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String speed = mEtConcentration.getText().toString().trim();
                if (TextUtils.isEmpty(speed)) return;
                if (Integer.parseInt(speed) > 15) {
                    mEtConcentration.setText("15");
                }

                if (Integer.parseInt(speed) < 1) {
                    mEtConcentration.setText("1");
                }
            }
        });


        findViewById(R.id.btn_confirm).setOnClickListener(v -> {

            int density;
            try {
                density = Integer.parseInt(mEtConcentration.getText().toString());
            } catch (NumberFormatException e) {
                ToastUtil.showToast("invalid input");
                return;
            }

            if (mIsPermanent) {
                PrinterKit.setDefaultPrintDensity(density);
            } else {
                PrinterKit.setPrintDensity(density);
            }
            ToastUtil.showToast("set successfully");
            finish();
        });
    }

    private void setEditCursor() {
        // 光标移动到最后
        mEtConcentration.post(() -> {
            Spannable text = mEtConcentration.getText();
            if (text != null) {
                Selection.setSelection(text, text.length());
            }
        });
    }
}