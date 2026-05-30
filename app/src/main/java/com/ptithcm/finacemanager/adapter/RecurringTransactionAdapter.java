package com.ptithcm.finacemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.RecurringTransaction;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.DateUtils;
import com.ptithcm.finacemanager.utils.IconMapper;

import java.util.List;

/**
 * Adapter hiển thị danh sách giao dịch định kỳ.
 *
 * <p>Mỗi item hiện: icon danh mục, ghi chú, badge tần suất, tên hủ,
 * ngày lặp tiếp theo, và số tiền (màu xanh cho thu, đỏ cho chi).
 */
public class RecurringTransactionAdapter extends RecyclerView.Adapter<RecurringTransactionAdapter.ViewHolder> {

    private List<RecurringTransaction> items;
    private final Context context;
    private OnRecurringClickListener listener;

    public interface OnRecurringClickListener {
        void onItemClick(RecurringTransaction recurring);
        void onItemLongClick(RecurringTransaction recurring);
    }

    public RecurringTransactionAdapter(List<RecurringTransaction> items, Context context) {
        this.items = items;
        this.context = context;
    }

    public void setOnRecurringClickListener(OnRecurringClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<RecurringTransaction> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recurring_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringTransaction recurring = items.get(position);

        // Icon danh mục (map key DB → emoji)
        holder.tvCategoryIcon.setText(IconMapper.toEmoji(recurring.getCategoryIcon()));

        // Note hoặc tên danh mục
        String displayName = recurring.getNote() != null && !recurring.getNote().isEmpty()
                ? recurring.getNote()
                : recurring.getLocalizedCategoryName(context);
        holder.tvNote.setText(displayName);

        // Badge tần suất
        holder.tvFrequency.setText(getFrequencyLabel(recurring.getFrequency()));

        // Tên hủ
        holder.tvPotName.setText(recurring.getPotName() != null
                ? recurring.getPotName() : "");

        // Ngày lặp tiếp theo
        String nextDateDisplay = DateUtils.formatForDisplay(recurring.getNextDate());
        holder.tvNextDate.setText(context.getString(R.string.label_next_date) + ": " + nextDateDisplay);

        // Số tiền + màu theo loại
        holder.tvAmount.setText(CurrencyFormatter.format(recurring.getAmount()));
        if (Constants.TYPE_INCOME.equals(recurring.getType())) {
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.color_income));
        } else {
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.color_expense));
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(recurring);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(recurring);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Map frequency constant sang label hiển thị (localized).
     */
    private String getFrequencyLabel(String frequency) {
        if (frequency == null) return "";
        switch (frequency) {
            case Constants.FREQ_DAILY:
                return context.getString(R.string.freq_daily);
            case Constants.FREQ_WEEKLY:
                return context.getString(R.string.freq_weekly);
            case Constants.FREQ_MONTHLY:
                return context.getString(R.string.freq_monthly);
            case Constants.FREQ_YEARLY:
                return context.getString(R.string.freq_yearly);
            default:
                return frequency;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon, tvNote, tvFrequency, tvPotName, tvNextDate, tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvPotName = itemView.findViewById(R.id.tv_pot_name);
            tvNextDate = itemView.findViewById(R.id.tv_next_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
