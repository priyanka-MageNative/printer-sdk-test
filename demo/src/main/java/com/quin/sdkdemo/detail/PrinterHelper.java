package com.quin.sdkdemo.detail;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import com.module.mprinter.PrinterInfo;
import com.module.mprinter.PrinterKit;
import com.module.mprinter.element.geometry.Orientation;
import com.module.mprinter.element.job.Job;
import com.module.mprinter.printer.constant.PrinterConstants;
import com.module.mprinter.printer.listener.state.OnPrintingStateChangeListener;
import com.module.mprinter.util.BitmapUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrinterHelper {
    private static final String TAG = "PrinterHelper";

    // 标签纸信息
    //标签宽度(单位MM)
    private float mLabelWidth = 40.00f;
    private float mLabelHeight = 30.00f;
    private Orientation orientation = Orientation.Deg0;

    private OnPrintingStateChangeListener mOnPrintingStateChangeListener;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    public void setOnPrintingStateChangeListener(OnPrintingStateChangeListener onPrintingStateChangeListener) {
        mOnPrintingStateChangeListener = onPrintingStateChangeListener;
    }

    public float getLabelWidth() {
        return mLabelWidth;
    }

    public float getLabelHeight() {
        return mLabelHeight;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    // 行业标签
    public static List<String> MSerial = new ArrayList<>(Arrays.asList(
            PrinterConstants.Serial.M110, PrinterConstants.Serial.M120,
            PrinterConstants.Serial.M200, PrinterConstants.Serial.B246D,
            PrinterConstants.Serial.E6000, PrinterConstants.Serial.E600S,
            PrinterConstants.Serial.OEM_M3, PrinterConstants.Serial.OEM_M8,
            PrinterConstants.Serial.OEM_M210, PrinterConstants.Serial.OEM_PM201
    ));
    // 家用标签
    public static List<String> DSerial = new ArrayList<>(Arrays.asList(
            PrinterConstants.Serial.D30, PrinterConstants.Serial.Q30,
            PrinterConstants.Serial.D50, PrinterConstants.Serial.P5100
    ));
    // 也是家用标签吧,但是和DSerial尺寸有区别
    public static List<String> PSerial = new ArrayList<>(Arrays.asList(
            PrinterConstants.Serial.P1000, PrinterConstants.Serial.P12
    ));

    /**
     * 按大概的系列来设置测试的打印标签尺寸
     */
    public void setupLabelSizeWithSeries() {
        int maxPrintDot = PrinterInfo.getPrintMaxDot();
        if (maxPrintDot >= 384) {
            mLabelWidth = 40f;
            mLabelHeight = 30f;
            orientation = Orientation.Deg0;
        } else {
            mLabelWidth = 40f;
            mLabelHeight = 12f;
            orientation = Orientation.Deg90;
        }
    }

    public void printJob(Job printJob, int amount) {
        printBitmap(printJob.getPrintBitmap(), amount);
    }

    public void printBitmap(Bitmap bitmap, int amount) {
        mExecutorService.submit(() -> {
            if (!PrinterInfo.isConnect()) {
                mOnPrintingStateChangeListener.onError();
                return;
            }

            Bitmap oriBitmap = bitmap;

            // 先根据dpi缩放图像，因为默认标签尺寸的画布是根据203dpi来画的，对于不同dpi的打印机则需要进行缩放，调整打印图像的打印点数
            // 如果已经按打印机的打印点数来生成画布大小的,则不需要根据具体的dpi来进行缩放图像处理
            float dpiScale = PrinterKit.getBitmapScaleSize();
            Matrix dpiMatrix = new Matrix();
            dpiMatrix.postScale(dpiScale, dpiScale);
            oriBitmap = Bitmap.createBitmap(oriBitmap, 0, 0, oriBitmap.getWidth(), oriBitmap.getHeight(), dpiMatrix, false);

            // 特定型号的机器，需要将图像旋转90度
            // M系列的SDK内部会根据最大打印宽度进行处理，其他系列的则需要手动讲打印图像处理成打印机的打印宽度限制
            String series = PrinterInfo.getSeries();
            if (DSerial.contains(series) || PSerial.contains(series)) {
                // 宽度的打印点数不够时，让打印图像居中,需要主动留白处理, M系列不需要旋转的就不用添加留白来居中处理
                int heightDot = oriBitmap.getHeight();
                int printMaxWidthDot = PrinterInfo.getPrintMaxDot();
                if (heightDot < printMaxWidthDot) {
                    int diff = printMaxWidthDot - heightDot;
                    if (diff > 0) {
                        Bitmap targetBitmap = Bitmap.createBitmap(oriBitmap.getWidth(),
                                printMaxWidthDot, Bitmap.Config.ARGB_8888);
                        targetBitmap.eraseColor(Color.TRANSPARENT);
                        Canvas canvas = new Canvas(targetBitmap);
                        int padding = diff / 2;
                        Rect rect = new Rect(0, padding, oriBitmap.getWidth(), oriBitmap.getHeight() + padding);
                        canvas.drawBitmap(oriBitmap, null, rect, new Paint());
                        oriBitmap = targetBitmap;
                    }
                }

                String printerType = PrinterInfo.getType();
                if (heightDot > printMaxWidthDot
                        || printerType.equals(PrinterConstants.Type.P1000)) {
                    // 打印宽度小于12mm的， 按9mm来

                    float scale = printMaxWidthDot / (float) heightDot;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);
                    oriBitmap = Bitmap.createBitmap(oriBitmap, 0, 0, oriBitmap.getWidth(), oriBitmap.getHeight(), matrix, false);
                }

                oriBitmap = BitmapUtil.rotateBitmap(oriBitmap, 90);
            }

            // for debug bitmap modify.
//            if (mOnPrintingStateChangeListener != null) {
//                mOnPrintingStateChangeListener.onComplete();
//            }
            PrinterKit.setOnPrintCompleteListener(mOnPrintingStateChangeListener);
            PrinterKit.printBitmap(oriBitmap, amount);
        });
    }
}
