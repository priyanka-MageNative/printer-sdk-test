package com.quin.sdkdemo.printer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.module.mprinter.bluetooth.Device;
import com.quin.sdkdemo.R;

import java.util.List;

/**
 * Created by chenk on 2019/6/3.
 */

public class PrinterListAdapter extends RecyclerView.Adapter<PrinterListAdapter.ViewHolder> {

    private List<Device> mData;
    private LayoutInflater mInflater;
    private OnItemClickListener mListener;

    public PrinterListAdapter(Context context, List<Device> data) {
        mInflater = LayoutInflater.from(context);
        mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_printer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = mData.get(position);
        holder.tvName.setText("printer name：" + device.getName());
        holder.tvMac.setText("Mac：" + device.getMac());
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick(position, device);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public interface OnItemClickListener {
        void onClick(int position, Device device);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvMac;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMac = itemView.findViewById(R.id.tv_mac);
        }
    }
}
