package com.quin.sdkdemo.view.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.quin.sdkdemo.R;

/**
 * Created by cwz
 * Created at 2020/5/13
 * Description 缺省页面Manager
 *
 * 1.关于使用：
 *   创建DefaultViewManager对象，构造方法传入将显示缺省视图的容器，
 *   调用对象的各个showXX()方法，显示各缺省页面
 *
 * 2.其他说明：
 *   1)缺省页面复用同一个ViewGroup；
 *   2)如果容器内有其他子View，在显示缺省页面时，这些子View的可见性均设置为GONE；
 *   3)关闭缺省页面，调用hideDefaultView()，缺省视图可见性均设置为GONE，而子View的可见性均设置为VISIBLE
 *   4)缺省视图拦截所有触碰事件，但其中提示信息中的点击事件依然可用
 */
public class DefaultViewManager {

    private ViewGroup mContainer; // 缺省视图的容器

    private ViewStub mViewStub; // 缺省视图ViewStub
    private int mBgColor; // ViewStub模式时，给缺省视图设置的背景颜色

    private View mVDefaultView;
    private TextView mTvMessage;
    private TextView mTvRefresh;
    private Button mButton;

    /**
     * 构造方法
     *
     * @param container 缺省视图的容器
     */
    public DefaultViewManager(ViewGroup container) {
        this.mContainer = container;
    }

    /**
     * 构造方法
     *
     * @param viewStub 缺省视图ViewStub
     * @param bgColor 给缺省视图设置的背景颜色 null时为透明
     */
    public DefaultViewManager(ViewStub viewStub, Integer bgColor) {
        this.mViewStub = viewStub;
        if (bgColor != null) {
            mBgColor = bgColor;
        }
    }

    public void setBackgroundColor(int color) {
        mBgColor = color;
        if (mVDefaultView != null) {
            mVDefaultView.setBackgroundColor(mBgColor);
        }
    }

    /**
     * 显示 网络丢失
     */
    public void showNetworkLoss() {
        showNetworkLoss(null);
    }

    /**
     * 显示 网络丢失，带刷新按钮
     */
    public void showNetworkLoss(View.OnClickListener onRefreshClickListener) {
        SpannableString msgSpanStr = getMsgSpanStr(getContext().getString(R.string.default_network_loss), null);

        if (mTvMessage == null) {
            getDefaultView();
        }

        mTvMessage.setText(msgSpanStr);
        mTvMessage.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.theme_default_icon_network_loss, 0, 0);

        // 按需显示“点击刷新”
        if (onRefreshClickListener != null) {
            if (mTvRefresh == null) {
                mTvRefresh = getDefaultView().findViewById(R.id.tv_refresh);
            }
            mTvRefresh.setOnClickListener(onRefreshClickListener);
            mTvRefresh.setVisibility(View.VISIBLE);
        }

        // 不显示按钮
        if (mButton != null) {
            mButton.setVisibility(View.GONE);
        }

        show();
    }

    /**
     * 显示 即将上线
     */
    public void showComingSoon() {
        SpannableString msgSpanStr = getMsgSpanStr(getContext().getString(R.string.default_coming_soon), null);
        setContent(msgSpanStr, R.mipmap.theme_default_icon_coming_soon);
        show();
    }

    /**
     * 显示 使用次数达上限
     */
    public void showUsageLimit() {
        SpannableString msgSpanStr = getMsgSpanStr(getContext().getString(R.string.default_usage_limit), null);
        setContent(msgSpanStr, R.mipmap.theme_default_icon_usage_limit);
        show();
    }

    /**
     * 显示 自定义
     *
     * @param spanStr 提示文本
     * @param iconRid 图标资源id
     */
    public void showCustom(SpannableString spanStr, @DrawableRes int iconRid) {
        setContent(spanStr, iconRid);
        show();
    }

    /**
     * 显示 加载结果为空
     */
    public void showLoadEmpty() {
        SpannableString msgSpanStr = getMsgSpanStr(getContext().getString(R.string.default_load_empty), null);
        setContent(msgSpanStr, R.mipmap.theme_default_icon_load_empty);
        show();
    }

    /**
     * 显示 加载结果为空
     *
     * 支持设值文本信息
     */
    public void showLoadEmpty(String msg) {
        SpannableString msgSpanStr = getMsgSpanStr(msg, null);
        setContent(msgSpanStr, R.mipmap.theme_default_icon_load_empty);
        show();
    }

    /**
     * 显示 查找结果为空
     */
    public void showFindEmpty(String searchThing) {
        String msg = getContext().getString(R.string.default_search_empty, searchThing);
        SpannableString msgSpanStr = getMsgSpanStr(msg, null);
        setContent(msgSpanStr, R.mipmap.theme_default_icon_find_empty);
        show();
    }

    /**
     * 显示 空样式-搜索icon
     */
    public void showSearchEmpty(String msg, View.OnClickListener onClickListener) {
        SpannableString msgSpanStr = getMsgSpanStr(msg, onClickListener);
        setContent(msgSpanStr, R.mipmap.theme_default_icon_search_empty);
        show();
    }

    /**
     * 显示按钮（在已经显示的缺失页面上）
     *
     * @param btnText 按钮文案
     * @param btnClickListener 按钮监听
     */
    public void showButton(String btnText, View.OnClickListener btnClickListener) {
        if (!TextUtils.isEmpty(btnText) && btnClickListener != null) {
            if (mButton == null) {
                mButton = getDefaultView().findViewById(R.id.button);
            }
            mButton.setVisibility(View.VISIBLE);
            mButton.setText(btnText);
            mButton.setOnClickListener(btnClickListener);
        }
    }

    /**
     * 设置按钮大小
     *
     * @param btnWidth  按钮宽带
     * @param btnHeight 按钮高度
     */
    public void setButtonSize(int btnWidth, int btnHeight) {
        if (mButton == null) {
            mButton = getDefaultView().findViewById(R.id.button);
        }
        ViewGroup.LayoutParams params = mButton.getLayoutParams();
        params.width = btnWidth;
        params.height = btnHeight;
        mButton.setLayoutParams(params);
    }

    /**
     * 隐藏缺省视图
     */
    public void hideDefaultView() {
        if (mContainer != null && mVDefaultView != null) {
            if (mContainer.getChildCount() == 1) {
                mContainer.setVisibility(View.GONE);
            } else {
                for (int i = 0; i < mContainer.getChildCount(); i++) {
                    View child = mContainer.getChildAt(i);
                    if (child == getDefaultView()) {
                        child.setVisibility(View.GONE);
                    } else {
                        child.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            if (mVDefaultView != null) {
                getDefaultView().setVisibility(View.GONE);
            }
        }
    }

    private SpannableString getMsgSpanStr(String message, View.OnClickListener onClickListener) {
        SpannableString spanStr = new SpannableString(message);
        if (onClickListener != null) {
            int start = message.indexOf(" ");
            int end = message.lastIndexOf(" ") + 1;
            // 颜色
            spanStr.setSpan(new ForegroundColorSpan(0xFF3D82F3), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
            // 点击
            spanStr.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    onClickListener.onClick(widget);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
//                    super.updateDrawState(ds); // 避免下划线
                }
            }, start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spanStr;
    }

    /**
     * 设置缺省视图内容
     */
    private void setContent(SpannableString msgSpanStr, @DrawableRes int iconRid) {
        if (mTvMessage == null) {
            getDefaultView();
        }

        mTvMessage.setText(msgSpanStr);
        mTvMessage.setCompoundDrawablesWithIntrinsicBounds(0, iconRid, 0, 0);

        // 不显示无网络的“点击刷新”
        if (mTvRefresh != null) {
            mTvRefresh.setVisibility(View.GONE);
        }

        // 默认不显示按钮
        if (mButton != null) {
            mButton.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏容器内其他子View
     */
    private void show() {
        if (mContainer != null) {
            if (mContainer.getVisibility() != View.VISIBLE) {
                mContainer.setVisibility(View.VISIBLE);
            }
            if (mContainer.getChildCount() > 1) {
                for (int i = 0; i < mContainer.getChildCount(); i++) {
                    View child = mContainer.getChildAt(i);
                    if (child != getDefaultView()) {
                        child.setVisibility(View.GONE);
                    } else {
                        child.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            getDefaultView().setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取缺省视图
     * @return View
     */
    @SuppressLint("ClickableViewAccessibility")
    private View getDefaultView() {
        if (mVDefaultView == null) {
            if (mContainer != null) {
                mVDefaultView = LayoutInflater.from(getContext())
                        .inflate(R.layout.layout_default_view, mContainer, false);
                mContainer.addView(mVDefaultView);
            } else {
                mViewStub.setLayoutResource(R.layout.layout_default_view);
                mVDefaultView = mViewStub.inflate();
                mVDefaultView.setBackgroundColor(mBgColor);
            }
            mTvMessage = mVDefaultView.findViewById(R.id.tv_message);
            mTvMessage.setMovementMethod(LinkMovementMethod.getInstance()); // 支持局部点击事件
            mTvMessage.setHighlightColor(Color.TRANSPARENT);

            mVDefaultView.setOnTouchListener((v, event) -> true); // 拦截所有触碰事件
        }
        return mVDefaultView;
    }

    private Context getContext() {
        if (mContainer != null) {
            return mContainer.getContext();
        } else {
            return mViewStub.getRootView().getContext();
        }
    }
}
