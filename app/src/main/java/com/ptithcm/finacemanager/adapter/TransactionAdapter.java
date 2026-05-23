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
    private OnTransactionClickListener clickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

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

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
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
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final View viewCategoryBackground;
        private final TextView textViewCategoryIcon, textViewTransactionCategory, textViewTransactionNote, textViewTransactionAmount, textViewTransactionDate;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryBackground = itemView.findViewById(R.id.view_category_bg);
            textViewCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            textViewTransactionCategory = itemView.findViewById(R.id.tv_trans_category);
            textViewTransactionNote = itemView.findViewById(R.id.tv_trans_note);
            textViewTransactionAmount = itemView.findViewById(R.id.tv_trans_amount);
            textViewTransactionDate = itemView.findViewById(R.id.tv_trans_date);
        }

        void bind(Transaction transaction) {
            // Category icon (emoji)
            String iconName = transaction.getCategoryIcon();
            String emoji = ICON_MAP.getOrDefault(iconName, "📦");
            textViewCategoryIcon.setText(emoji);

            // Màu nền icon theo loại giao dịch
            int backgroundColor = transaction.isIncome()
                    ? ContextCompat.getColor(context, R.color.color_income)
                    : ContextCompat.getColor(context, R.color.color_expense);
            GradientDrawable backgroundDrawable = new GradientDrawable();
            backgroundDrawable.setShape(GradientDrawable.OVAL);
            backgroundDrawable.setColor(backgroundColor);
            viewCategoryBackground.setBackground(backgroundDrawable);

            // Category name & note
            String categoryName = transaction.getLocalizedCategoryName(context);
            textViewTransactionCategory.setText(categoryName.isEmpty() ? "Other" : categoryName);

            String note = transaction.getNote();
            if (note != null && !note.isEmpty()) {
                textViewTransactionNote.setText(note);
                textViewTransactionNote.setVisibility(View.VISIBLE);
            } else {
                textViewTransactionNote.setText(transaction.getPotName() != null ? transaction.getPotName() : "");
                textViewTransactionNote.setVisibility(View.VISIBLE);
            }

            // Amount với màu
            textViewTransactionAmount.setText(CurrencyFormatter.formatWithSign(transaction.getAmount(), transaction.isIncome()));
            textViewTransactionAmount.setTextColor(transaction.isIncome()
                    ? ContextCompat.getColor(context, R.color.color_income)
                    : ContextCompat.getColor(context, R.color.color_expense));

            // Date
            textViewTransactionDate.setText(DateUtils.formatForDisplay(transaction.getDate()));

            // Click listener for showing options
            itemView.setOnClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onTransactionClick(transaction);
                }
            });
        }
    }
}
