package com.ptithcm.finacemanager.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;

import java.util.List;

public class PotAdapter extends RecyclerView.Adapter<PotAdapter.PotViewHolder> {

    private List<Pot> potList;
    private final OnPotClickListener listener;

    public interface OnPotClickListener {
        void onPotClick(Pot pot);
        void onPotLongClick(Pot pot);
    }

    public PotAdapter(List<Pot> potList, OnPotClickListener listener) {
        this.potList = potList;
        this.listener = listener;
    }

    public void updateData(List<Pot> newList) {
        this.potList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pot, parent, false);
        return new PotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PotViewHolder holder, int position) {
        Pot pot = potList.get(position);
        holder.bind(pot, position);
    }

    @Override
    public int getItemCount() {
        return potList.size();
    }

    class PotViewHolder extends RecyclerView.ViewHolder {
        private final View viewPotColor, viewIconBg;
        private final TextView tvPotIcon, tvPotName, tvPotBudget, tvPotBalance;
        private final TextView tvPotPercentage, tvPotRemaining;
        private final ProgressBar pbBudget;

        PotViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPotColor = itemView.findViewById(R.id.view_pot_color);
            viewIconBg = itemView.findViewById(R.id.view_icon_bg);
            tvPotIcon = itemView.findViewById(R.id.tv_pot_icon);
            tvPotName = itemView.findViewById(R.id.tv_pot_name);
            tvPotBudget = itemView.findViewById(R.id.tv_pot_budget);
            tvPotBalance = itemView.findViewById(R.id.tv_pot_balance);
            tvPotPercentage = itemView.findViewById(R.id.tv_pot_percentage);
            tvPotRemaining = itemView.findViewById(R.id.tv_pot_remaining);
            pbBudget = itemView.findViewById(R.id.pb_budget);
        }

        void bind(Pot pot, int position) {
            // === 1. Tên & Ngân sách ===
            tvPotName.setText(pot.getName());
            tvPotBudget.setText(itemView.getContext().getString(R.string.label_budget)
                    + ": " + CurrencyFormatter.format(pot.getBudgetLimit()));

            // === 2. Số dư ===
            tvPotBalance.setText(CurrencyFormatter.format(pot.getBalance()));

            // === 3. Màu pot (color strip bên trái) ===
            int potColor;
            try {
                potColor = Color.parseColor(pot.getColor());
            } catch (Exception e) {
                potColor = Color.parseColor("#4CAF50");
            }
            viewPotColor.setBackgroundColor(potColor);

            // === 4. Icon emoji trong vòng tròn nhạt (từ DB) ===
            String emoji = pot.getIcon();
            if (emoji == null || emoji.isEmpty() || emoji.equals("ic_default")) {
                emoji = Constants.DEFAULT_POT_ICON;
            }
            tvPotIcon.setText(emoji);

            // Tạo background tròn với màu nhạt (alpha 20%)
            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setShape(GradientDrawable.OVAL);
            int alphaColor = Color.argb(30,
                    Color.red(potColor), Color.green(potColor), Color.blue(potColor));
            iconBg.setColor(alphaColor);
            viewIconBg.setBackground(iconBg);

            // === 5. Progress Bar ===
            int percentage = pot.getBudgetPercentage();
            pbBudget.setProgress(percentage);

            // Đổi màu progress theo mức ngân sách
            int progressColor;
            if (percentage < Constants.BUDGET_WARNING_THRESHOLD * 100) {
                progressColor = ContextCompat.getColor(itemView.getContext(), R.color.color_budget_safe);
            } else if (percentage < Constants.BUDGET_DANGER_THRESHOLD * 100) {
                progressColor = ContextCompat.getColor(itemView.getContext(), R.color.color_budget_warning);
            } else {
                progressColor = ContextCompat.getColor(itemView.getContext(), R.color.color_budget_danger);
            }
            pbBudget.getProgressDrawable().setColorFilter(
                    progressColor, android.graphics.PorterDuff.Mode.SRC_IN);

            // === 6. Percentage & Remaining text ===
            String statusLabel;
            if (percentage < Constants.BUDGET_WARNING_THRESHOLD * 100) {
                statusLabel = "🟢 " + percentage + "% " +
                        itemView.getContext().getString(R.string.label_spent);
                tvPotRemaining.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.color_budget_safe));
            } else if (percentage < Constants.BUDGET_DANGER_THRESHOLD * 100) {
                statusLabel = "🟡 " + percentage + "% " +
                        itemView.getContext().getString(R.string.label_spent);
                tvPotRemaining.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.color_budget_warning));
            } else {
                statusLabel = "🔴 " + percentage + "% " +
                        itemView.getContext().getString(R.string.label_spent);
                tvPotRemaining.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.color_budget_danger));
            }
            tvPotPercentage.setText(statusLabel);
            tvPotRemaining.setText(itemView.getContext().getString(R.string.label_remaining)
                    + ": " + CurrencyFormatter.format(pot.getRemaining()));

            // === 7. Đổi màu balance nếu hết tiền ===
            if (pot.getBalance() <= 0) {
                tvPotBalance.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.color_expense));
            } else {
                tvPotBalance.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.color_text_primary));
            }

            // === 8. Click listeners ===
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPotClick(pot);
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onPotLongClick(pot);
                return true;
            });
        }
    }
}
