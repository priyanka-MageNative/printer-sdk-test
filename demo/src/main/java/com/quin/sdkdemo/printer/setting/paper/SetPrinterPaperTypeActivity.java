package com.quin.sdkdemo.printer.setting.paper;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.module.mprinter.PrinterKit;
import com.module.mprinter.printer.constant.PaperType;
import com.module.mprinter.util.LibraryKit;
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.util.ToastUtil;

public class SetPrinterPaperTypeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 进入页面先查一次打印机的纸张类型
        queryPaperType();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_printer_paper_type);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.tv_set_continuous_paper).setOnClickListener(this);
        findViewById(R.id.tv_set_clearance_paper).setOnClickListener(this);
        findViewById(R.id.tv_set_black_label_paper).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_set_continuous_paper:
                PrinterKit.setPaperType(PaperType.CONTINUOUS);
                break;
            case R.id.tv_set_clearance_paper:
                PrinterKit.setPaperType(PaperType.CLEARANCE);
                break;
            case R.id.tv_set_black_label_paper:
                PrinterKit.setPaperType(PaperType.BLACK_LABEL);
                break;

        }
        ToastUtil.showToast("set successfully");
        LibraryKit.getMainHandler().postDelayed(() -> {
            // 延迟查询打印机当前的纸张类型
            queryPaperType();
        }, 1000);
        finish();
    }

    /**
     * 查询纸张类型
     */
    private void queryPaperType() {
        PrinterKit.getPaperType(type -> {
            String paperType = "unknown";
            switch (type) {
                case PaperType.CLEARANCE:
                    paperType = "间隙纸";
                    break;
                case PaperType.CONTINUOUS:
                    paperType = "连续纸";
                    break;
                case PaperType.BLACK_LABEL:
                    paperType = "黑标纸";
                    break;
            }
            ToastUtil.showToast("current paper type：" + paperType);
        });
    }
}