package com.ptithcm.finacemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.ExchangeRate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị bảng tỷ giá ngoại tệ so với VND.
 *
 * <p>Mỗi item hiện: cờ quốc gia, mã tiền, tên đầy đủ,
 * và tỷ giá quy đổi dạng "1 USD = 25,100 VNĐ".
 */
public class ExchangeRateAdapter extends RecyclerView.Adapter<ExchangeRateAdapter.ViewHolder> {

    private List<ExchangeRate> items;
    private OnRateClickListener listener;

    /** Callback khi user tap vào 1 loại tiền để quy đổi nhanh. */
    public interface OnRateClickListener {
        void onRateClick(ExchangeRate rate);
    }

    public ExchangeRateAdapter(List<ExchangeRate> items) {
        this.items = items;
    }

    public void setOnRateClickListener(OnRateClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<ExchangeRate> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exchange_rate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExchangeRate rate = items.get(position);

        holder.tvFlag.setText(rate.getFlag());
        holder.tvCurrencyCode.setText(rate.getCurrencyCode());
        holder.tvCurrencyName.setText(rate.getFullName());
        holder.tvRate.setText(formatRate(rate.getRateToVnd()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRateClick(rate);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Format tỷ giá: "1 = 25,100 VNĐ" (dùng dấu phẩy ngăn cách hàng nghìn).
     */
    private String formatRate(double rate) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        DecimalFormat formatter;
        if (rate >= 1000) {
            formatter = new DecimalFormat("#,##0", symbols);
        } else if (rate >= 1) {
            formatter = new DecimalFormat("#,##0.00", symbols);
        } else {
            formatter = new DecimalFormat("0.0000", symbols);
        }

        return formatter.format(rate) + " VNĐ";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlag, tvCurrencyCode, tvCurrencyName, tvRate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFlag = itemView.findViewById(R.id.tv_flag);
            tvCurrencyCode = itemView.findViewById(R.id.tv_currency_code);
            tvCurrencyName = itemView.findViewById(R.id.tv_currency_name);
            tvRate = itemView.findViewById(R.id.tv_rate);
        }
    }
}
