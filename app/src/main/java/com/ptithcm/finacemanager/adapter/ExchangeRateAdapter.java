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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị bảng tỷ giá ngoại tệ.
 *
 * <p>Hỗ trợ dynamic base currency: tỷ giá hiển thị tùy thuộc vào
 * tiền tệ FROM mà user đang chọn trong converter.
 */
public class ExchangeRateAdapter extends RecyclerView.Adapter<ExchangeRateAdapter.ViewHolder> {

    private List<ExchangeRate> items;
    private String baseCurrencyCode = "VND";
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

    /**
     * Cập nhật dữ liệu bảng tỷ giá.
     *
     * @param newItems         Danh sách tỷ giá mới
     * @param baseCurrencyCode Mã tiền tệ base (FROM) để hiển thị đúng label
     */
    public void updateData(List<ExchangeRate> newItems, String baseCurrencyCode) {
        this.items = newItems;
        this.baseCurrencyCode = baseCurrencyCode;
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

        // Hiện: "25,100 VNĐ" hoặc "1.08 USD" tùy base currency
        holder.tvRate.setText(formatRate(rate.getRateToVnd(), baseCurrencyCode));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRateClick(rate);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Format tỷ giá thông minh dựa trên loại tiền tệ.
     */
    private String formatRate(double rate, String baseCurrency) {
        boolean isZeroDecimal = Arrays.asList("VND", "JPY", "KRW").contains(baseCurrency);
        
        if (isZeroDecimal) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
            symbols.setGroupingSeparator('.');
            DecimalFormat formatter = new DecimalFormat("#,###", symbols);
            return formatter.format(Math.round(rate)) + " " + baseCurrency;
        } else {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
    
            DecimalFormat formatter;
            if (rate >= 1000) {
                formatter = new DecimalFormat("#,##0.##", symbols);
            } else if (rate >= 1) {
                formatter = new DecimalFormat("#,##0.####", symbols);
            } else {
                formatter = new DecimalFormat("0.000000", symbols);
            }
            
            String result = formatter.format(rate);
            if (result.contains(".") && rate >= 1) {
                result = result.replaceAll("0*$", "").replaceAll("\\.$", "");
            }
            return result + " " + baseCurrency;
        }
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
