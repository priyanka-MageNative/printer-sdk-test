package com.quin.sdkdemo.detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.module.mprinter.PrinterInfo;
import com.module.mprinter.PrinterKit;
import com.module.mprinter.element.geometry.Orientation;
import com.module.mprinter.element.geometry.ScaleUnit;
import com.module.mprinter.element.geometry.Shape;
import com.module.mprinter.element.job.Job;
import com.module.mprinter.element.param.BarcodeParam;
import com.module.mprinter.element.param.BitmapParam;
import com.module.mprinter.element.param.LineParam;
import com.module.mprinter.element.param.QrcodeParam;
import com.module.mprinter.element.param.ShapeParam;
import com.module.mprinter.element.param.TextParam;
import com.module.mprinter.element.zxing.BarcodeType;
import com.module.mprinter.element.zxing.ErrorCorrectionLevel;
import com.module.mprinter.printer.listener.state.OnPrintingStateChangeListener;
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.common.BaseActivity;
import com.quin.sdkdemo.entity.MenuEntity;
import com.quin.sdkdemo.printer.setting.PrinterSettingActivity;
import com.quin.sdkdemo.util.ToastUtil;
import com.quin.sdkdemo.view.LoadingDialog;

import java.io.IOException;

public class PrintDetailActivity extends BaseActivity {

    private LoadingDialog mLoadingDialog;
    private ImageView mIvPreview;
    private EditText mEtCount;
    private Job mPrintJob;

    private PrinterHelper mPrinterHelper;
    //标签宽度(单位MM)
    private float LABEL_WIDTH = 40.00f;
    private float LABEL_HEIGHT = 30.00f;
//    private Orientation orientation = Orientation.Deg0;

    private static final String EXTRA_KEY_PRINT_TYPE = "print_type";

    public static void gotoPrintDetailActivity(Activity activity, int printType) {
        activity.startActivity(new Intent(activity, PrintDetailActivity.class).putExtra(PrintDetailActivity.EXTRA_KEY_PRINT_TYPE, printType));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (!PrinterInfo.isConnect()) {
            ToastUtil.showToast("Please connect a printer");
            finish();
            return;
        } else {
            // 根据系列来初始化标签纸宽高
            mPrinterHelper = new PrinterHelper();
            mPrinterHelper.setOnPrintingStateChangeListener(new OnPrintingStateChangeListener() {
                @Override
                public void onComplete() {
                    if (!PrinterInfo.isConnect()) {
                        return;
                    }
                    Log.i(TAG, "onComplete: ");
                    mLoadingDialog.dismiss();
                    showAlertDialog("finished printing");
                }

                @Override
                public void onError() {
                    if (!PrinterInfo.isConnect()) {
                        return;
                    }
                    Log.i(TAG, "onError: ");
                    mLoadingDialog.dismiss();
                    showAlertDialog("print failed");
                }

                @Override
                public void onCancel() {
                    if (!PrinterInfo.isConnect()) {
                        return;
                    }
                    Log.i(TAG, "onCancel: ");
                    mLoadingDialog.dismiss();
                    showAlertDialog("Cancel printing");
                }
            });
            mPrinterHelper.setupLabelSizeWithSeries();
            LABEL_WIDTH = mPrinterHelper.getLabelWidth();
            LABEL_HEIGHT = mPrinterHelper.getLabelHeight();
//            orientation = mPrinterHelper.getOrientation();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_detail);
        mLoadingDialog = new LoadingDialog(this);

        int mPrintType = getIntent().getIntExtra(EXTRA_KEY_PRINT_TYPE, 0);

        mIvPreview = findViewById(R.id.iv_preview);
        mEtCount = findViewById(R.id.et_count);
        setEditCursorPosition();
        mEtCount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                setEditCursorPosition();
            }
        });

        initToolbar("Fun Printing Technology");
        mToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.print) {
                if (mPrintJob != null) {
                    if (PrinterInfo.isCoverOpen()) {
                        showAlertDialog("Printer cover is open");
                        return true;
                    }

                    if (!PrinterInfo.isHasPaper()) {
                        showAlertDialog("Printer is missing supplies");
                        return true;
                    }

                    if (mPrinterHelper != null) {
                        int count = getPrintCount();
                        if (count > 0) {
                            mLoadingDialog.setCancelable(true);
                            mLoadingDialog.show();
                            mPrinterHelper.printJob(mPrintJob, count);
                        }
                    }
                }
            } else if (item.getItemId() == R.id.setting) {
                startActivity(new Intent(PrintDetailActivity.this, PrinterSettingActivity.class));
            }
            return true;
        });


        if (!PrinterInfo.isConnect()) {
            Toast.makeText(getApplicationContext(), "Please connect a printer", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            mPrintJob = PrinterKit.startJob(LABEL_WIDTH, LABEL_HEIGHT, ScaleUnit.mm, Orientation.Deg0);
//            mPrintJob = PrinterKit.startJob(LABEL_WIDTH, LABEL_HEIGHT, ScaleUnit.mm, orientation);

            /**
             * 以下所使用的位置信息，单位皆为MM,
             * 需要注意，具体的坐标位置（元素的左上右下的位置）是基于标签纸尺寸来的；
             */
            switch (mPrintType) {
                case MenuEntity.BARCODE:
                    createBarcode();
                    break;
                case MenuEntity.QR_CODE:
                    createQrCode();
                    break;
                case MenuEntity.LINE:
                    createLine();
                    break;
                case MenuEntity.LINE_FRAME:
                    createShape();
                    break;
                case MenuEntity.PICTURE:
                    createPicture();
                    break;
                case MenuEntity.SAMPLE:
                    try {
                        createPrintSample();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    createText();
                    break;
            }
            mIvPreview.setImageBitmap(mPrintJob.getPrintBitmap());
            mIvPreview.setScaleX(2.0f);
            mIvPreview.setScaleY(2.0f);
        }
    }

    private int getPrintCount() {
        String countStr = mEtCount.getText().toString();
        try {
            return Integer.parseInt(countStr);
        } catch (Exception e) {
            ToastUtil.showToast("Invalid value entered for number of copies：" + countStr);
            return -1;
        }
    }

    private void setEditCursorPosition() {
        Spannable text = mEtCount.getText();
        if (text != null) {
            Selection.setSelection(text, text.length());
        }
    }

    /**
     * 绘制文本
     * 传入左、上、右、下的坐标值，以及内容和字体大小
     * 不需要自动换行，可只传左以及下的位置。需自动换行需传所有位置值
     */
    private void createText() {
        // 绘制文本

        float margin = 3f;
        // 设置绘制的范围、内容、以及字体大小
        TextParam textParam = new TextParam(
                margin, margin,
                LABEL_WIDTH - margin, LABEL_HEIGHT - margin,
                "珠海趣印科技有限公司",
                4f);
        // 设置上面设置参数的对应单位， 可以设置毫米或者像素点
        textParam.scaleUnit = ScaleUnit.mm;
        // 是否自动换行
        textParam.isAutoLinefeed = true;
        // 水平间距
        textParam.horSpace = 1;
        // 上下边距（仅在设置了自动换行时生效）
        textParam.verSpace = 1;
        // 默认Left 字符边距( TextParam.TextAligment.Center,TextParam.TextAligment.Left,TextParam.TextAligment.Right)
        textParam.textAligment = TextParam.TextAligment.Center;
        // 字体
        textParam.typeface = Typeface.DEFAULT;
        // 字体样式, TextParam.STYLE_BOLD, TextParam.STYLE_ITALIC,TextParam.STYLE_UNDERLINE,TextParam.STYLE_STRIKETHRU
        textParam.textStyle = TextParam.STYLE_BOLD;
        mPrintJob.drawText(textParam);
    }

    /**
     * 绘制一维码
     * 传入左、上、右、下的坐标值，以及内容和编码格式
     * 调用job的drawBarcode方法，即将一维码添加进了打印内容中
     */
    private void createBarcode() {
        float margin = 3f;
        BarcodeType barcodeType = BarcodeType.Code39;
        String content = "123ABC";

        if (barcodeType == BarcodeType.Code39) {
            // 数字+符号（-,/,., ,+,$）,长度至少为1
            content = "1234567";
        } else if (barcodeType == BarcodeType.Code128) {
            // 字母+数字+特殊符号，长度至少为1， 最大长度纯字符32位，纯字符+特殊字符44位
            content = "223ABC";
        } else if (barcodeType == BarcodeType.Codabar) {
            // 数字+ 符号（$,/,:,-），长度至少为1
            content = "323789";
        } else if (barcodeType == BarcodeType.EAN8) {
            // 纯数字，长度至少为7
            content = "423456000";
        } else if (barcodeType == BarcodeType.EAN13) {
            // 纯数字，长度至少为12
            content = "5234560001234";
        } else if (barcodeType == BarcodeType.UPCA) {
            // 纯数字，长度至少为12
            content = "6231110001234";
        }

        int maxHeight = (int) Math.min((margin + 8), (LABEL_HEIGHT - margin));// 最高8毫米
        BarcodeParam barcodeParam = new BarcodeParam(
                margin, margin,
                (LABEL_WIDTH - margin), maxHeight,
                content, barcodeType);
        // 是否根据所给参数的width进行resize
        barcodeParam.isForceResize = true;
        // 是否显示*号，仅Code39格式生效
//        barcodeParam.showAsterisk = true;
        barcodeParam.showArrow = false;
        barcodeParam.showLongLine = true;
        mPrintJob.drawBarcode(barcodeParam);
    }

    /**
     * 绘制二维码
     * 传入左、上、右、下的坐标值，以及内容和二维码纠错级别
     */
    private void createQrCode() {
        float qrcodeWidth = Math.min(LABEL_WIDTH, LABEL_HEIGHT) - 2;
        QrcodeParam qrcodeParam = new QrcodeParam(
                LABEL_WIDTH / 2 - qrcodeWidth / 2,
                LABEL_HEIGHT / 2 - qrcodeWidth / 2,
                LABEL_WIDTH / 2 + qrcodeWidth / 2,
                LABEL_HEIGHT / 2 + qrcodeWidth / 2,
                "珠海趣印科技有限公司", ErrorCorrectionLevel.M);
        mPrintJob.drawQrcode(qrcodeParam);
    }


    /**
     * 绘制直线
     * 入开始的X值、开始的Y值、结束的X值、结束的Y值，以及线条宽度，如要打印间隔直线，则需传入pathEffect
     *
     * @see DashPathEffect
     */
    private void createLine() {
        float margin = 5f;
        LineParam lineParam = new LineParam(margin, margin,
                LABEL_WIDTH - margin,
                LABEL_HEIGHT - margin, 1f);
        lineParam.pathEffect = new float[]{4f, 1f};
        mPrintJob.drawLine(lineParam);
    }

    /**
     * 绘制形状
     * 传入左、上、右、下的坐标值，以及线条宽度，形状类型
     */
    private void createShape() {
        float paddingLeft = 3;
        float paddingTop = 3;

        ShapeParam shapeParam = new ShapeParam(paddingLeft, paddingTop,
                LABEL_WIDTH - paddingLeft, LABEL_HEIGHT - paddingTop,
                0.5f, Shape.Rectangle);
        shapeParam.radius = 1f;//圆角
//        shapeParam.pathEffect = new float[]{4f, 1f};//间隔线
//        shapeParam.isFill 是否实心

        mPrintJob.drawShape(shapeParam);
    }

    /**
     * 绘制图片
     * 传入左、上、右、下的坐标值，以及需要打印的bitmap
     * drawStyle为绘制图片方式：threshold根据阀值二值化/halfTone半色调
     * 当选择了threshold的绘制方式后，可传入threshold值进行自定义阀值的二值化,默认阀值为127
     */
    private void createPicture() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("quin_logo.jpg"));
            int bitmapWidthDot = bitmap.getWidth() / 8;
            int bitmapHeightDot = bitmap.getHeight() / 8;
            float horizontalPadding = LABEL_WIDTH / 2 - bitmapWidthDot / 2;
            float verticalPadding = LABEL_HEIGHT / 2 - bitmapHeightDot / 2;
            BitmapParam bitmapParam = new BitmapParam(horizontalPadding, verticalPadding, LABEL_WIDTH - horizontalPadding, verticalPadding + bitmapHeightDot, bitmap);
//            bitmapParam.threshold 阀值,当drawstyle设置成threshold时生效，默认127
//            bitmapParam.drawStyle 二值化图片类型(BitmapParam.DrawStyle.Halftone,BitmapParam.DrawStyle.Threshold)
            mPrintJob.drawBitmap(bitmapParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 样张示例，单位mm
     *
     * @throws IOException
     */
    private void createPrintSample() throws IOException {
        //边框
        float shapeWidth = LABEL_WIDTH;
        float shapeHeight = LABEL_HEIGHT;
        float shapePadding = 3f;
        float shapeLeft = shapePadding;
        float shapeTop = shapePadding;
        float shapeRight = shapeWidth - shapePadding;
        float shapeBottom = shapeHeight - shapePadding;
        ShapeParam shapeParam = new ShapeParam(shapeLeft, shapeTop, shapeRight, shapeBottom, 0.4f, Shape.Rectangle);
        shapeParam.radius = 1f;
        mPrintJob.drawShape(shapeParam);

        //二维码
        float qrcodeWidth = LABEL_HEIGHT / 2 - shapePadding;
        float qrcodePadding = 4f;
        float qrcodeLeft = qrcodePadding;
        float qrcodeTop = qrcodePadding;
        float qrcodeRight = qrcodeWidth + qrcodePadding;
        float qrcodeBottom = qrcodeWidth + qrcodePadding;
        QrcodeParam qrcodeParam = new QrcodeParam(qrcodeLeft, qrcodeTop, qrcodeRight, qrcodeBottom,
                "珠海趣印科技有限公司", ErrorCorrectionLevel.M);
        mPrintJob.drawQrcode(qrcodeParam);

        //一维码
        float barcodeMargin = 1f;
        float barcodeLeft = qrcodeRight + barcodeMargin;
        float barcodeTop = qrcodeTop;
        float barcodeRight = LABEL_WIDTH - 2 * barcodeMargin;
        float barcodeBottom = qrcodeBottom;
        BarcodeParam barcodeParam = new BarcodeParam(barcodeLeft, barcodeTop, barcodeRight, barcodeBottom, "123ABC", BarcodeType.Code128);
        mPrintJob.drawBarcode(barcodeParam);

        //线
        float lineMargin = 2f;
        float lineLeft = qrcodeLeft;
        float lineTop = qrcodeBottom + lineMargin;
        float lineRight = LABEL_WIDTH - lineLeft;
        float lineBottom = qrcodeBottom + lineMargin;
        LineParam lineParam = new LineParam(lineLeft, lineTop, lineRight, lineBottom, 0.5f);
        lineParam.pathEffect = new float[]{4f, 1f};
        mPrintJob.drawLine(lineParam);

        //图片
        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("quin_logo.jpg"));
        float bitmapWidth = bitmap.getWidth() / 8;
        float bitmapHeight = bitmap.getHeight() / 8;
        float horizontalPadding = LABEL_WIDTH / 2 - bitmapWidth / 2;
        float verticalPadding = 1f;
        float bitmapLeft = horizontalPadding;
        float bitmapRight = LABEL_WIDTH - horizontalPadding;
        float bitmapTop = lineTop + verticalPadding;
        float bitmapBottom = bitmapTop + bitmapHeight;
        BitmapParam bitmapParam = new BitmapParam(bitmapLeft, bitmapTop, bitmapRight, bitmapBottom, bitmap);
        bitmapParam.drawStyle = BitmapParam.DrawStyle.Halftone;
        mPrintJob.drawBitmap(bitmapParam);


        //文本
        float textMargin = 1f;
        float textBegin = 2f;
        float textTop = bitmapBottom + textMargin;
        TextParam textParam = new TextParam(textBegin, textTop, textBegin, textTop,
                "珠海趣印科技有限公司珠海趣印科技有限公司", 3.0f);

        textParam.isAutoLinefeed = false;

        mPrintJob.drawText(textParam);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterKit.setOnPrintCompleteListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.print_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}