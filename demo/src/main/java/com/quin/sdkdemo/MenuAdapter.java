package com.quin.sdkdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.module.mprinter.PrinterInfo;
import com.quin.sdkdemo.entity.MenuEntity;
import com.quin.sdkdemo.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private List<MenuEntity> mMenuData = new ArrayList();
    private LayoutInflater mLayoutInflater;
    private OnItemClickListener mListener;

    public MenuAdapter(List<MenuEntity> menu, Context context) {
        this.mMenuData = menu;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.item_menu, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuEntity menu = mMenuData.get(position);
        holder.tvTitle.setText(menu.getName());
        holder.itemView.setOnClickListener(v -> {
            if (!PrinterInfo.isConnect()) {
                ToastUtil.showToast("还没有连接打印机");
                return;
            }
            if (mListener != null) {
                mListener.onClick(menu);
            }
        });
    }

    public void setOnMenuClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mMenuData.size();
    }

    public interface OnItemClickListener {
        void onClick(MenuEntity menu);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}
