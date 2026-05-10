package com.ptithcm.finacemanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private final Context context;

    // Map icon names to emoji for simple display
    private static final java.util.Map<String, String> ICON_MAP = new java.util.HashMap<>();
    static {
        ICON_MAP.put("ic_food", "🍜");
        ICON_MAP.put("ic_housing", "🏠");
        ICON_MAP.put("ic_transport", "🚗");
        ICON_MAP.put("ic_entertainment", "🎮");
        ICON_MAP.put("ic_education", "📚");
        ICON_MAP.put("ic_health", "💊");
        ICON_MAP.put("ic_shopping", "🛍");
        ICON_MAP.put("ic_savings", "💰");
        ICON_MAP.put("ic_salary", "💵");
        ICON_MAP.put("ic_gift", "🎁");
        ICON_MAP.put("ic_other", "📦");
    }

    public TransactionAdapter(List<Transaction> transactionList, Context context) {
        this.transactionList = transactionList;
        this.context = context;
    }

    public void updateData(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction trans = transactionList.get(position);
        holder.bind(trans);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final View viewCategoryBg;
        private final TextView tvCategoryIcon, tvTransCategory, tvTransNote, tvTransAmount, tvTransDate;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryBg = itemView.findViewById(R.id.view_category_bg);
            tvCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            tvTransCategory = itemView.findViewById(R.id.tv_trans_category);
            tvTransNote = itemView.findViewById(R.id.tv_trans_note);
            tvTransAmount = itemView.findViewById(R.id.tv_trans_amount);
            tvTransDate = itemView.findViewById(R.id.tv_trans_date);
        }

        void bind(Transaction trans) {
            // Category icon (emoji)
            String iconName = trans.getCategoryIcon();
            String emoji = ICON_MAP.getOrDefault(iconName, "📦");
            tvCategoryIcon.setText(emoji);

            // Màu nền icon theo loại giao dịch
            int bgColor = trans.isIncome()
                    ? ContextCompat.getColor(context, R.color.color_income)
                    : ContextCompat.getColor(context, R.color.color_expense);
            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setShape(GradientDrawable.OVAL);
            bgDrawable.setColor(bgColor);
            viewCategoryBg.setBackground(bgDrawable);

            // Category name & note
            String catName = trans.getLocalizedCategoryName(context);
            tvTransCategory.setText(catName.isEmpty() ? "Other" : catName);

            String note = trans.getNote();
            if (note != null && !note.isEmpty()) {
                tvTransNote.setText(note);
                tvTransNote.setVisibility(View.VISIBLE);
            } else {
                tvTransNote.setText(trans.getPotName() != null ? trans.getPotName() : "");
                tvTransNote.setVisibility(View.VISIBLE);
            }

            // Amount với màu
            tvTransAmount.setText(CurrencyFormatter.formatWithSign(trans.getAmount(), trans.isIncome()));
            tvTransAmount.setTextColor(trans.isIncome()
                    ? ContextCompat.getColor(context, R.color.color_income)
                    : ContextCompat.getColor(context, R.color.color_expense));

            // Date
            tvTransDate.setText(DateUtils.formatForDisplay(trans.getDate()));
        }
    }
}
