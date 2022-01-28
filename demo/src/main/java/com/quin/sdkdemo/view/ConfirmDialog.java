package com.quin.sdkdemo.view;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.quin.sdkdemo.R;
import com.quin.sdkdemo.util.ScreenUtil;

/**
 * Created by cwz
 * Created at 2020/3/30
 * Description 确认对话框
 * <p>
 * 注意：
 * 1.不可取消
 * 2.确认信息以及两个按钮的文案，必须手动设置
 */
public class ConfirmDialog extends BasicDialog {

    private TextView mTvTitle;
    private TextView mTvMessage;
    private Button mBtnNegative;
    private Button mBtnPositive;
    private CheckBox mCbNoTipsAgain;

    private OnBtnClickListener mOnBtnClickListener;

    @LayoutRes
    protected int getLayoutId() {
        return R.layout.dialog_confirm;
    }

    public ConfirmDialog(@NonNull Context context) {
        super(context);
        setContentView(getLayoutId());

        setCancelable(false);

        initView();
        initEvent();
    }

    protected void initView() {
        mTvTitle = findViewById(R.id.tv_title);
        mTvMessage = findViewById(R.id.tv_msg);
        mBtnNegative = findViewById(R.id.btn_negative);
        mBtnPositive = findViewById(R.id.btn_positive);
        mCbNoTipsAgain = findViewById(R.id.ch_no_tips_again);
    }

    protected void initEvent() {
        mBtnNegative.setOnClickListener(v -> {
            dismiss();
            if (mOnBtnClickListener != null) {
                mOnBtnClickListener.onNegativeBtnClick(this);
            }
        });
        mBtnPositive.setOnClickListener(v -> {
            dismiss();
            if (mOnBtnClickListener != null) {
                mOnBtnClickListener.onPositiveBtnClick(this);
            }
        });
        mCbNoTipsAgain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mOnBtnClickListener != null) {
                mOnBtnClickListener.onNoTipAgainCheckedChange(this, isChecked);
            }
        });
    }
    @Override
    public void setTitle(@StringRes int titleId) {
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setText(titleId);
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            mTvTitle.setVisibility(View.GONE);
        } else {
            mTvTitle.setVisibility(View.VISIBLE);
        }
        mTvTitle.setText(title);
    }

    public ConfirmDialog setMessage(@StringRes int msgRid) {
        mTvMessage.setText(msgRid);
        return this;
    }

    public ConfirmDialog setMessage(String message) {
        mTvMessage.setText(message);
        return this;
    }

    public ConfirmDialog setMessage(CharSequence message) {
        mTvMessage.setText(message);
        mTvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        return this;
    }

    public ConfirmDialog setMessageGravity(int gravity) {
        mTvMessage.setGravity(gravity);
        return this;
    }

    /**
     * 设置message的左右margin，单位dp。
     */
    public ConfirmDialog setMessageMargin(int start, int end) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mTvMessage.getLayoutParams();
        params.setMarginStart(ScreenUtil.dp2px(start));
        params.setMarginEnd(ScreenUtil.dp2px(end));
        return this;
    }

    /**
     * 设置message的margin，单位dp。
     */
    public ConfirmDialog setMessageMargin(int start, int top,  int end, int bottom) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mTvMessage.getLayoutParams();
        params.setMargins(ScreenUtil.dp2px(start), ScreenUtil.dp2px(top), ScreenUtil.dp2px(end), ScreenUtil.dp2px(bottom));
        return this;
    }

    public ConfirmDialog setSingleButton() {
        mBtnNegative.setVisibility(View.GONE);
        findViewById(R.id.v_button_centre_line).setVisibility(View.GONE);
        return this;
    }

    public ConfirmDialog setNoButton() {
        mBtnPositive.setVisibility(View.GONE);
        mBtnNegative.setVisibility(View.GONE);
        findViewById(R.id.v_button_centre_line).setVisibility(View.GONE);
        return this;
    }


    /**
     * 是否显示不在提醒
     * */
    public ConfirmDialog setNoTipAgain(boolean show) {
        mCbNoTipsAgain.setVisibility(show? View.VISIBLE: View.GONE);
        return this;
    }

    public ConfirmDialog setNegative(int negativeStringRid) {
        mBtnNegative.setVisibility(View.VISIBLE);
        findViewById(R.id.v_button_centre_line).setVisibility(View.VISIBLE);
        mBtnNegative.setText(negativeStringRid);
        return this;
    }

    public ConfirmDialog setNegative(CharSequence negativeString) {
        mBtnNegative.setText(negativeString);
        return this;
    }

    public ConfirmDialog setPositive(@StringRes int positiveStringRid) {
        mBtnPositive.setText(positiveStringRid);
        if (getContext().getString(R.string.delete).equals(mBtnPositive.getText().toString())) {
            activatedPositive();
        }
        return this;
    }

    public ConfirmDialog setPositive(CharSequence positiveString) {
        mBtnPositive.setText(positiveString);
        if (getContext().getString(R.string.delete).equals(positiveString.toString())) {
            activatedPositive();
        }
        return this;
    }

    public ConfirmDialog activatedPositive() {
        mBtnPositive.setActivated(true);
        return this;
    }

    public ConfirmDialog positiveTextColor(@ColorInt int color) {
        mBtnPositive.setTextColor(color);
        return this;
    }

    public ConfirmDialog setOnBtnClickListener(OnBtnClickListener onBtnClickListener) {
        this.mOnBtnClickListener = onBtnClickListener;
        return this;
    }

    public boolean getNoTipAgainCheckStatus() {
        return mCbNoTipsAgain.isChecked();
    }

    public interface OnBtnClickListener {
        default void onNegativeBtnClick(ConfirmDialog dialog) {
        }

        void onPositiveBtnClick(ConfirmDialog dialog);

        default void onNoTipAgainCheckedChange(ConfirmDialog dialog, boolean isChecked){

        }
    }
}
