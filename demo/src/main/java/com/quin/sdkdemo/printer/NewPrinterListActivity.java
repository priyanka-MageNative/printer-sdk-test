package com.quin.sdkdemo.printer;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.module.mprinter.PrinterInfo;
import com.module.mprinter.PrinterKit;
import com.module.mprinter.bluetooth.Bluetooth;
import com.module.mprinter.bluetooth.Device;
import com.quin.sdkdemo.ARouterPath;
import com.quin.sdkdemo.App;
import com.quin.sdkdemo.Constant;
import com.quin.sdkdemo.R;
import com.quin.sdkdemo.common.PermissionActivity;
import com.quin.sdkdemo.printer.detail.PrinterDetailActivity;
import com.quin.sdkdemo.util.ScreenUtil;
import com.quin.sdkdemo.util.ToastUtil;
import com.quin.sdkdemo.view.ConfirmDialog;
import com.quin.sdkdemo.view.LoadingDialog;
import com.quin.sdkdemo.view.manager.DefaultViewManager;

import java.util.List;

/**
 * 打印机列表Activity
 * 1.检查蓝牙是否开启
 * 2.检查定位权限
 * 3.Android 10以上检查GPS是否打开
 */

public class NewPrinterListActivity extends PermissionActivity {

    private static final int REQUEST_CODE_LOCATION = 0x0001;

    private ImageButton mBtnScan;
    private RecyclerView mRvPrinter;
    private RotateAnimation mRotateAnimation;

    private DefaultViewManager mDefaultViewManager;
    private LoadingDialog mDialogLoading;

    //连接打印机中...
    private boolean mIsConnecting;
    //连接打印机后获取打印机信息中...
    private boolean mInInitializing;

    //是否需要返回到主页
    private boolean mIsNeedBackHome;
    //是否需要回调OnActivityResult
    private static boolean isForResult;

    private final PrinterAdapter mAdapterDevice = new PrinterAdapter() {
        @Override
        void onItemClick(String mac, String model) {
            connectTo(mac, model);
        }

        @Override
        void toSetting() {
            Intent intent = new Intent(NewPrinterListActivity.this, PrinterDetailActivity.class);
            startActivity(intent);
        }
    };

    // -------------------
    //  启动页面的静态方法
    // -------------------

    public static void startFromHome(Activity activity) {
        // 为了连接完成后finish
        isForResult = true;
        activity.startActivity(new Intent(activity, NewPrinterListActivity.class)
                .putExtra(Constant.BUNDLE_KEY_IS_FROM_HOME, true));
    }

    public static void start(Activity activity, boolean forResult) {
        Intent intent = new Intent(activity, NewPrinterListActivity.class);
        isForResult = forResult;
        if (isForResult) {
            activity.startActivityForResult(intent, Constant.REQUEST_CODE_CONNECT_PRINTER);
        } else {
            activity.startActivity(intent);
        }
    }

    // -------------------
    //  生命周期
    // -------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_printer_list);
        initToolBar();
        mDefaultViewManager = new DefaultViewManager(findViewById(R.id.vs_default), Color.WHITE);

        mRvPrinter = findViewById(R.id.rv_printer);
        mRvPrinter.setAdapter(mAdapterDevice);
        if (PrinterKit.isConnect()) {
            mAdapterDevice.onDeviceFound(
                    new Device(PrinterInfo.getMac(),
                            PrinterInfo.getName(),
                            0)
            );
        }

        if (!PrinterInfo.isConnect()) {
            ToastUtil.showToast("Connect the printer first, then operate");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PrinterKit.isConnect()) {
            mAdapterDevice.setConnectMac(PrinterInfo.getMac());
        } else {
            mAdapterDevice.setConnectMac(null);
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //小米 NOTE 3 Android 6.0.1, level 28出现获取BluetoothAdapter为null的情况
        if (bluetoothAdapter == null) {
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            //没有打开蓝牙
            ConfirmDialog dialog = new ConfirmDialog(this);
            dialog.setTitle(R.string.tip_turn_on_bluetooth);
            dialog.setMessage(R.string.content_turn_on_bluetooth);
            dialog.setNegative(R.string.cancel);
            dialog.setPositive(R.string.turn_on_bluetooth);
            dialog.setOnBtnClickListener(dialog1 -> {
                BluetoothAdapter.getDefaultAdapter().enable();
                requestPermissions(REQUEST_CODE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
            });
            dialog.show();
        } else {
            requestPermissions(REQUEST_CODE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PrinterKit.stopScan();
        if (mDialogLoading != null) {
            mDialogLoading.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (mDialogLoading != null) {
            mDialogLoading.dismiss();
            mDialogLoading = null;
        }
        super.onDestroy();
        PrinterKit.stopScan();
        mFoundListener = null;
        if (mRotateAnimation != null) {
            mRotateAnimation.cancel();
        }
    }

    private void initAnimation() {
        mRotateAnimation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        mRotateAnimation.setInterpolator(lin);
        mRotateAnimation.setDuration(1500);//设置动画持续周期
        mRotateAnimation.setRepeatCount(-1);//设置重复次数
        mRotateAnimation.setFillAfter(false);//动画执行完后是否停留在执行完的状态
    }

    private void initToolBar() {
        super.initToolbar("printer");
        ImageButton mBtnBack = findViewById(R.id.iv_back);
        setTitle(R.string.device);
        mBtnScan = new ImageButton(this);
        mBtnScan.setBackground(ContextCompat.getDrawable(this, android.R.color.transparent));
        mBtnScan.setImageResource(R.mipmap.device_icon_refresh);
        mBtnScan.setColorFilter(R.color.colorAccent);
        mBtnScan.setOnClickListener(v -> {
            if (PrinterKit.isDiscovering()) return;
            mAdapterDevice.clear();
            PrinterKit.scan(mFoundListener);
            mBtnScan.setEnabled(false);
        });
        mBtnScan.setEnabled(false);
        addRightIcon(mBtnScan);
        mBtnBack.setOnClickListener(v -> finish());
    }

    private void addRightIcon(View... icons) {
        LinearLayout llRight = mToolbar.findViewById(R.id.ll_right);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = ScreenUtil.dp2px(3);
        for (View icon : icons) {
            llRight.addView(icon, layoutParams);
        }
    }

    // -------------------
    //  Bluetooth
    // -------------------

    private void scan() {
        initAnimation();
        PrinterKit.scan(mFoundListener);
        mBtnScan.setEnabled(true);
    }

    /**
     * 连接打印前判断处理
     * 1.当前要连接的打印机是否不会丢失数据
     * 2.是否连接打印机直接回主页
     */
    private void connectTo(String mac, String model) {
        mIsNeedBackHome = isDiffPrintModel(model);
        //1.当前连接的机器是包含在当前编辑主页所支持的机器
        //2.是否是要回到主页 [非编辑界面]
        if (!mIsNeedBackHome || getIntent().getBooleanExtra(Constant.BUNDLE_KEY_IS_FROM_HOME, false)) {
            connect(model, mac);
            return;
        }

        if (mIsNeedBackHome) {
            //在编辑界面,连接不同设备会丢失数据
            ConfirmDialog confirmDialog = new ConfirmDialog(this);
            confirmDialog.setMessage(getString(R.string.tip_content_lose, model));
            confirmDialog.setNegative(R.string.cancel);
            confirmDialog.setPositive(R.string.connect_confirm);
            confirmDialog.setOnBtnClickListener(dialog -> connect(model, mac));
            confirmDialog.show();
        }
    }

    /**
     * 真正发起连接
     */
    private void connect(String name, String mac) {
        // 如果需要多出地方监听蓝牙连接变化, 用于不同页面执行不同操作, 则需要注册多个监听
//        Bluetooth.BluetoothConnectStateListener connectStateListener = App.getConnectStateListener();
//        if (connectStateListener != null) {
//            runOnUiThread(() -> {
//                if (mDialogLoading == null) {
//                    mDialogLoading = new LoadingDialog(NewPrinterListActivity.this);
//                    mDialogLoading.show();
//                }
//            });
//            mIsConnecting = true;
//            // 需要通过PrinterKit.subscribePrinterConnectStateListener()来注册蓝牙连接状态变更的监听
//            PrinterKit.subscribePrinterConnectStateListener(connectStateListener);
//            PrinterKit.connect(name, mac);
//        }

        // demo这里，蓝牙连接状态变更统一在App中注册，通过Toast提示,全局使用同一个
        runOnUiThread(() -> {
            if (mDialogLoading == null) {
                mDialogLoading = new LoadingDialog(NewPrinterListActivity.this);
                mDialogLoading.show();
            }
        });
        PrinterKit.connect(name, mac);
    }

    // -------------------
    //  Bluetooth.PrinterDiscoveryListener
    // -------------------

    private Bluetooth.PrinterDiscoveryListener mFoundListener = new Bluetooth.PrinterDiscoveryListener() {
        @Override
        public void onStart() {
            if (mBtnScan != null) {
                mBtnScan.startAnimation(mRotateAnimation);
            }
            //开始搜索设备，先隐藏缺省样式
            mRvPrinter.setVisibility(View.VISIBLE);
            mDefaultViewManager.hideDefaultView();
        }

        @Override
        public void onFound(Device device) {
            mAdapterDevice.onDeviceFound(device);
        }

        @Override
        public void onFinished() {
            if (mRotateAnimation != null) {
                mRotateAnimation.cancel();
            }
            if (mBtnScan != null) mBtnScan.setEnabled(true);
            if (mAdapterDevice.getItemCount() == 0) {
                //最终列表都没有搜索到设备，则显示缺省样式
                mRvPrinter.setVisibility(View.GONE);
                SpannableString spannedString = new SpannableString(getString(R.string.printer_list_search_empty_tips));
                mDefaultViewManager.showCustom(spannedString, R.drawable.theme_default_icon_search_device_empty);
            }
        }
    };

    // -------------------
    //  Connect State
    // -------------------

    @Override
    public void onBluetoothConnected(String name, String mac) {
        // 连接成功
        mIsConnecting = false;
        mInInitializing = false;

        // 避免在极少数情况下，由于连接打印机后，获取打印机sn号等信息失败引起的，不触发调用onInitSuccess()的问题
        new Handler().post(() -> {
            if (!mInInitializing) {
                mInInitializing = true;
                onInitSuccess();
            }
        });
    }

    @Override
    public void onBluetoothDisconnected(boolean isActive) {
        if (!mIsConnecting) {
            super.onBluetoothDisconnected(isActive);
        }
        mAdapterDevice.setConnectMac(null);
    }

    @Override
    public void onBluetoothConnectionFailed() {
        mIsConnecting = false;
        if (mDialogLoading != null) {
            // 空判断：在不明情况下mDialogLoading会为null，然后导致空指针崩溃
            mDialogLoading.dismiss();
        }
        super.onBluetoothConnectionFailed();
    }

    @Override
    public void onBluetoothConnecting() {
        super.onBluetoothConnecting();
    }

    private void onInitSuccess() {
        if (mDialogLoading != null && mDialogLoading.isShowing()) {
            // 空判断：在不明情况下mDialogLoading会为null，然后导致空指针崩溃
            mDialogLoading.dismiss();
        }
        if (mIsNeedBackHome) {
            ARouter.getInstance().build(ARouterPath.APP_HOME)
                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .navigation(this);
        } else {
            if (isForResult) {
                setResult(RESULT_OK);
                finish();
            } else {
                runOnUiThread(() -> mAdapterDevice.setConnectMac(PrinterInfo.getMac()));
            }
        }
    }

    // -------------------
    //  Permissions
    // -------------------

    @Override
    protected int permitPermanentDeny(int requestCode) {
        return R.string.printer_list_permission_set;
    }

    @Override
    protected int rationale() {
        return R.string.printer_list_permission_deny;
    }

    @Override
    protected void onPermissionsGrantedAll(int requestCode) {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (!hasGPSDevice() || checkGPS()) {//没有GPS设备或者已经获取GPS权限
                scan();
            } else {
                showGpsDialog();
            }
        } else {
            scan();
        }
    }

    //设备是否有GPS设备
    private boolean hasGPSDevice() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (lm == null) {
            return false;
        }
        List<String> providers = lm.getAllProviders();
        try {
            return providers.contains(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    //是否有GPS权限
    private boolean checkGPS() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showGpsDialog() {
        ConfirmDialog dialog = new ConfirmDialog(this);
        dialog.setTitle(R.string.tip_gps);
        dialog.setMessage(R.string.content_gps);
        dialog.setNegative(R.string.cancel);
        dialog.setPositive(R.string.gps_positive);
        dialog.setOnBtnClickListener(dialog1
                -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        dialog.show();
    }

    // -------------------
    //
    // -------------------

    /**
     * 判断当前要连接的机器是否包含在当前编辑主页所支持的机器
     *
     * @param connectModel 要连接的打印机的型号
     */
    private boolean isDiffPrintModel(String connectModel) {
        return !PrinterInfo.getType().equals(connectModel);
    }

    /**
     * 扫描连接
     */
    public void scanConnect(View view) {
    }
}