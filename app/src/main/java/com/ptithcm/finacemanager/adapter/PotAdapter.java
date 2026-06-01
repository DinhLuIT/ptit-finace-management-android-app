package com.ptithcm.finacemanager.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

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
        private final View viewIconBg;
        private final TextView tvPotLetter, tvPotName, tvPotBudget, tvPotBalance;
        private final ImageView ivPotIcon;
        private final TextView tvPotPercentage, tvPotRemaining;
        private final LinearProgressIndicator pbBudget;

        PotViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIconBg = itemView.findViewById(R.id.view_icon_bg);
            tvPotLetter = itemView.findViewById(R.id.tv_pot_letter);
            ivPotIcon = itemView.findViewById(R.id.iv_pot_icon);
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

            // === 3. Màu pot ===
            int potColor;
            try {
                potColor = Color.parseColor(pot.getColor());
            } catch (Exception e) {
                potColor = Color.parseColor("#4CAF50");
            }

            // === 4. Icon & Avatar (Tinted Background) ===
            // Background vòng tròn 15% opacity
            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setShape(GradientDrawable.OVAL);
            int alphaColor = Color.argb(38, // ~15% opacity
                    Color.red(potColor), Color.green(potColor), Color.blue(potColor));
            iconBg.setColor(alphaColor);
            viewIconBg.setBackground(iconBg);

            String rawIcon = pot.getIcon();
            boolean isVector = com.ptithcm.finacemanager.utils.IconMapper.getIconResource(itemView.getContext(), rawIcon) != com.ptithcm.finacemanager.R.drawable.ic_other;
            // Nếu rawIcon là cat_food, etc.. nó sẽ tìm thấy vector. Nếu không nó là emoji hoặc rỗng.

            if (isVector) {
                tvPotLetter.setVisibility(View.GONE);
                ivPotIcon.setVisibility(View.VISIBLE);
                ivPotIcon.setImageResource(com.ptithcm.finacemanager.utils.IconMapper.getIconResource(itemView.getContext(), rawIcon));
                ivPotIcon.setColorFilter(potColor); // Vector icon mang màu 100% của hũ
            } else {
                // Letter Avatar Fallback
                ivPotIcon.setVisibility(View.GONE);
                tvPotLetter.setVisibility(View.VISIBLE);
                String firstLetter = "P";
                if (pot.getName() != null && !pot.getName().trim().isEmpty()) {
                    firstLetter = pot.getName().trim().substring(0, 1).toUpperCase();
                }
                tvPotLetter.setText(firstLetter);
                tvPotLetter.setTextColor(potColor); // Chữ mang màu 100% của hũ
            }

            // === 5. Progress Bar ===
            int percentage = pot.getBudgetPercentage();
            pbBudget.setProgress(percentage);
            pbBudget.setIndicatorColor(potColor); // Thanh chạy màu của Hũ

            // === 6. Percentage & Remaining text ===
            String statusLabel;
            if (percentage < Constants.BUDGET_WARNING_THRESHOLD * 100) {
                statusLabel = percentage + "% " + itemView.getContext().getString(R.string.label_spent);
                tvPotRemaining.setTextColor(potColor); // Text an toàn có màu của hũ
            } else if (percentage < Constants.BUDGET_DANGER_THRESHOLD * 100) {
                statusLabel = percentage + "% " + itemView.getContext().getString(R.string.label_spent);
                tvPotRemaining.setTextColor(ContextCompat.getColor(
                        itemView.getContext(), R.color.color_budget_warning));
            } else {
                statusLabel = percentage + "% " + itemView.getContext().getString(R.string.label_spent);
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
