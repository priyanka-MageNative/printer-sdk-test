package com.quin.sdkdemo;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.module.mprinter.PrinterInfo;
import com.module.mprinter.bluetooth.BluetoothKit;
import com.quin.sdkdemo.common.PermissionActivity;
import com.quin.sdkdemo.detail.PrintDetailActivity;
import com.quin.sdkdemo.entity.MenuEntity;
import com.quin.sdkdemo.printer.NewPrinterListActivity;
import com.quin.sdkdemo.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

@Route(path = ARouterPath.APP_HOME)
public class MainActivity extends PermissionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar("Fun Printing Technology");
        mToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.printer) {
                //检查打开蓝牙
                if (!BluetoothKit.isBluetoothEnable()) {
                    //未打开蓝牙去申请打开蓝牙
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0x11);
                } else {
                    checkPermissionAndGotoPrinterList();
                }
            }
            return true;
        });

        RecyclerView rvMenu = findViewById(R.id.rv_items);
        rvMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        MenuAdapter menuAdapter = new MenuAdapter(createMenu(), this);
        menuAdapter.setOnMenuClickListener(menu -> {
            PrintDetailActivity.gotoPrintDetailActivity(MainActivity.this, menu.getId());
        });
        rvMenu.setAdapter(menuAdapter);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemPrint = menu.findItem(R.id.printer);
        if (PrinterInfo.isConnect()) {
            itemPrint.setIcon(R.mipmap.icon_printer_connected);
        } else {
            itemPrint.setIcon(R.mipmap.icon_printer_disconnected);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();
        if (!PrinterInfo.isConnect()) {
            NewPrinterListActivity.startFromHome(this);
        }
    }

    private List<MenuEntity> createMenu() {
        List<MenuEntity> list = new ArrayList<>();
        list.add(new MenuEntity("print text", MenuEntity.TEXT));
        list.add(new MenuEntity("print barcode", MenuEntity.BARCODE));
        list.add(new MenuEntity("print QR code", MenuEntity.QR_CODE));
        list.add(new MenuEntity("print lines", MenuEntity.LINE));
        list.add(new MenuEntity("print wireframe", MenuEntity.LINE_FRAME));
        list.add(new MenuEntity("print pictures", MenuEntity.PICTURE));
        list.add(new MenuEntity("print proof", MenuEntity.SAMPLE));
        return list;
    }

    private void checkPermissionAndGotoPrinterList() {
        //请求位置权限（搜索蓝牙设备，需使用到定位权限）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(0x12, Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                gotoPrinterList();
            }
        } else {
            gotoPrinterList();
        }
    }

    private boolean isGpsOpen() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void gotoPrinterList() {
        //Android10.0系列机器需要开启定位权限才能搜索到蓝牙设备
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (isGpsOpen()) {
//                startActivity(new Intent(MainActivity.this, PrinterListActivity.class));
                NewPrinterListActivity.startFromHome(MainActivity.this);
            } else {
                new AlertDialog
                        .Builder(this)
                        .setTitle("Turn on targeting")
                        .setMessage("Android 10.0 search for Bluetooth devices requires the location permission to be turned on")
                        .setPositiveButton("去开启", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0x12);
                            dialog.dismiss();
                        }).setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        } else {
//            startActivity(new Intent(MainActivity.this, PrinterListActivity.class));
            NewPrinterListActivity.startFromHome(MainActivity.this);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x11 && resultCode == RESULT_OK) {
            checkPermissionAndGotoPrinterList();
        }

        if (requestCode == 0x12) {
            gotoPrinterList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x12 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gotoPrinterList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}