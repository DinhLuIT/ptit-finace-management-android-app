package com.ptithcm.finacemanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.DateUtils;
import com.ptithcm.finacemanager.utils.IconMapper;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private final Context context;
    private OnTransactionClickListener clickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
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
        private final TextView textViewTransactionCategory, textViewTransactionNote, textViewTransactionAmount, textViewTransactionDate;
        private final ImageView textViewCategoryIcon;

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
            // Category icon (emoji) – dùng IconMapper utility
            textViewCategoryIcon.setImageResource(IconMapper.getIconResource(context, transaction.getCategoryIcon()));

            // Màu nền icon đa dạng dựa theo Category ID để giao diện không bị đơn điệu
            int colorIndex = Math.abs(transaction.getCategoryId()) % Constants.POT_COLORS.length;
            int backgroundColor = Color.parseColor(Constants.POT_COLORS[colorIndex]);
            
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
