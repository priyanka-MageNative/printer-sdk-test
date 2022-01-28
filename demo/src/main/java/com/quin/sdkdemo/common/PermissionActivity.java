package com.quin.sdkdemo.common;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.quin.sdkdemo.R;
import com.quin.sdkdemo.view.ConfirmDialog;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public abstract class PermissionActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    private static final String SAVE_CODE_REQUEST_CODE = "mRequestCode";
    private static final String SAVE_CODE_PERMISSIONS = "mPermissions";

    protected int mRequestCode;
    private String[] mPermissions;
    protected ConfirmDialog mDialogSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mRequestCode = savedInstanceState.getInt(SAVE_CODE_REQUEST_CODE);
            mPermissions = savedInstanceState.getStringArray(SAVE_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDialogSetting != null && mPermissions != null) {
            if (EasyPermissions.hasPermissions(this, mPermissions)) {
                mDialogSetting.dismiss();
                onPermissionsGrantedAll(mRequestCode);
            }
        }
    }

    public void requestPermissions(int requestCode, String... permissions) {
        mRequestCode = requestCode;
        mPermissions = permissions;

        boolean ignore = false;
        for (String perm : permissions) {
            //1. 权限未授权
            //2. 不需要提醒（也就是判断勾选了不再询问）
            //3. 当前已经显示了拒绝后的提示对话框(如果该mDialogSetting对话框显示的话，就标识已经申请过一次权限，并拒绝了)
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
                    && !ActivityCompat.shouldShowRequestPermissionRationale(this, perm)
                    && mDialogSetting != null && mDialogSetting.isShowing()) {
                ignore = true;
            }
        }

        if (!ignore) {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, mRequestCode, permissions)
                            .setRationale(rationale())
                            .setPositiveButtonText(R.string.confirm)
                            .setNegativeButtonText(R.string.cancel)
                            .setTheme(R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .build());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_CODE_REQUEST_CODE, mRequestCode);
        outState.putStringArray(SAVE_CODE_PERMISSIONS, mPermissions);
    }

    /**
     * 权限被永久拒绝时显示的提示
     */
    protected @StringRes
    int permitPermanentDeny(int requestCode) {
        return R.string.printer_list_permission_set;
    }

    /**
     * 权限被拒绝过一次下次申请时会询问
     */
    protected @StringRes
    int rationale() {
        return R.string.printer_list_permission_deny;
    }

    /**
     * 所有权限通过时被调用
     */
    protected void onPermissionsGrantedAll(int requestCode) {
    }

    protected void onPermissionGrantedPart(int requestCode, List<String> perms) {
    }

    protected void showDenyDialog() {
        if (mDialogSetting == null) {
            mDialogSetting = new ConfirmDialog(this);
            mDialogSetting.setTitle(R.string.request_permission);
            mDialogSetting.setCanceledOnTouchOutside(false);
            mDialogSetting.setCancelable(false);
            mDialogSetting.setNegative(R.string.cancel);
            mDialogSetting.setPositive(R.string.go_setting);
            mDialogSetting.setMessage(permitPermanentDeny(mRequestCode));
            mDialogSetting.setOnBtnClickListener(new ConfirmDialog.OnBtnClickListener() {
                @Override
                public void onNegativeBtnClick(ConfirmDialog dialog) {
                    finish();
                }

                @Override
                public void onPositiveBtnClick(ConfirmDialog dialog) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                }
            });
        }
        if (!mDialogSetting.isShowing()) {
            mDialogSetting.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        showDenyDialog();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (perms.size() == mPermissions.length) {
            onPermissionsGrantedAll(requestCode);
        } else {
            onPermissionGrantedPart(requestCode, perms);
        }
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        showDenyDialog();
    }

    @Override
    public void onRationaleAccepted(int requestCode) {

    }
}
