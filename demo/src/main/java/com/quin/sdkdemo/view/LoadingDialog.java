package com.quin.sdkdemo.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.quin.sdkdemo.R;
import com.quin.sdkdemo.util.ScreenUtil;

public class LoadingDialog extends BasicDialog {

    private TextView tvLoading;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_loading);
        tvLoading = findViewById(R.id.tv_loading);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
    }

    public void setText(@StringRes int textRid) {
        setText(getContext().getString(textRid));
    }

    public void setText(String text) {
        if (!TextUtils.isEmpty(text)) {
            // 显示文字时，原本集中的动画view要往上摞一点
            View animView = findViewById(R.id.animation_view);
            ViewGroup.MarginLayoutParams animViewLp = ((ViewGroup.MarginLayoutParams) animView.getLayoutParams());
            animViewLp.topMargin = ScreenUtil.dp2px(-8);
            animView.setLayoutParams(animViewLp);

            // 设置文字
            tvLoading.setText(text);
            tvLoading.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏文本显示
     */
    public void clearText() {
        //根据setText()方法，还原topMargin，并隐藏文字的TextView
        View animView = findViewById(R.id.animation_view);
        ViewGroup.MarginLayoutParams animViewLp = ((ViewGroup.MarginLayoutParams) animView.getLayoutParams());
        animViewLp.topMargin = ScreenUtil.dp2px(0);
        animView.setLayoutParams(animViewLp);

        tvLoading.setText("");
        tvLoading.setVisibility(View.GONE);
    }
}
