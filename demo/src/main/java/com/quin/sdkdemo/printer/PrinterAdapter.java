package com.quin.sdkdemo.printer;

import android.annotation.SuppressLint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.module.mprinter.bluetooth.Device;
import com.quin.sdkdemo.R;

import java.util.ArrayList;
import java.util.List;

public abstract class PrinterAdapter extends RecyclerView.Adapter<PrinterAdapter.DeviceHolder> {

    private final List<Printer> mListPrinter;
    private String mConnectMac;

    public PrinterAdapter() {
        mListPrinter = new ArrayList<>();
    }

    public void setConnectMac(String connectMac) {
        String last = mConnectMac;
        mConnectMac = connectMac;

        String mac;
        int selectedIndex = 0;
        for (int i = 0; i < mListPrinter.size(); i++) {
            mac = mListPrinter.get(i).mac;
            if (mac != null) {
                if (mac.equals(last)) {
                    notifyItemChanged(i);
                } else if (mac.equals(mConnectMac)) {
                    selectedIndex = i;
                }
            }
        }
        if (connectMac != null && mListPrinter.size() > 0) {
            mListPrinter.add(0, mListPrinter.remove(selectedIndex));
            notifyItemMoved(selectedIndex, 0);
            notifyItemChanged(0);
        }
    }

    public void onDeviceFound(Device device) {
        Printer printer = getPrinter(device.getMac());
        if (printer == null) {
            mListPrinter.add(new Printer(device.getMac(), device.getRssi(), device.getName()));
            notifyItemInserted(mListPrinter.size() - 1);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        mListPrinter.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_recycle_printer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        Printer printer = mListPrinter.get(position);
//        holder.ivPicture.setImageResource(PrinterUtil.getPrinterMipmap(mListPrinter.get(position).model));
        holder.tvModel.setText(printer.model);
        holder.tvMac.setText(printer.mac);
        holder.itemView.setTag(printer.model);
        if (printer.mac.equals(mConnectMac)) {
            holder.btnSetting.setEnabled(true);
            holder.tvState.setText(R.string.connected);
            holder.itemView.setOnClickListener(v -> toSetting());
        } else {
            holder.btnSetting.setEnabled(false);
            holder.btnSetting.setOnClickListener(null);
            holder.tvState.setText(Html.fromHtml(holder.tvState.getContext().getString(R.string.printer_not_connected_to_connect)));
            holder.itemView.setOnClickListener(v -> onItemClick(holder.tvMac.getText().toString(), (String) holder.itemView.getTag()));
        }
    }

    @Override
    public int getItemCount() {
        return mListPrinter.size();
    }

    private Printer getPrinter(String mac) {
        int count = mListPrinter.size();
        for (int i = 0; i < count; i++) {
            if (mListPrinter.get(i).mac.equals(mac)) {
                return mListPrinter.get(i);
            }
        }
        return null;
    }

    abstract void onItemClick(String mac, String model);

    abstract void toSetting();

    protected static class DeviceHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPicture;
        private final TextView tvModel;
        private final TextView tvState;
        private final TextView tvMac;
        private final ImageView btnSetting;

        public DeviceHolder(@NonNull View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.iv_picture);
            tvModel = itemView.findViewById(R.id.tv_model);
            tvState = itemView.findViewById(R.id.tv_state);
            tvMac = itemView.findViewById(R.id.tv_mac);
            btnSetting = itemView.findViewById(R.id.iv_setting);
        }
    }

    private static class Printer {
        private final String mac;
        private final int rssi;
        private final String model;

        public Printer(String mac, int rssi, String model) {
            this.mac = mac;
            this.rssi = rssi;
            this.model = model;
        }
    }
}
