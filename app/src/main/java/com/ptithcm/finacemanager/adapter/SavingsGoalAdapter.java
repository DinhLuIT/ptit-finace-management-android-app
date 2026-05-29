package com.ptithcm.finacemanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.SavingsGoal;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter hiển thị danh sách mục tiêu tiết kiệm.
 *
 * <p>Mỗi item hiển thị: icon (emoji trên nền tròn), tên, tiến độ (%), progress bar,
 * số tiền còn thiếu, và đếm ngược ngày.
 */
public class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.GoalViewHolder> {

    private List<SavingsGoal> goals;
    private final Context context;
    private OnGoalClickListener listener;

    public interface OnGoalClickListener {
        void onGoalClick(SavingsGoal goal);
    }

    public SavingsGoalAdapter(List<SavingsGoal> goals, Context context) {
        this.goals = goals;
        this.context = context;
    }

    public void setOnGoalClickListener(OnGoalClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<SavingsGoal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        SavingsGoal goal = goals.get(position);

        // Icon + Background
        holder.tvIcon.setText(goal.getIcon() != null ? goal.getIcon() : Constants.DEFAULT_GOAL_ICON);
        String color = goal.getColor() != null ? goal.getColor() : Constants.DEFAULT_GOAL_COLOR;

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        try {
            // Tạo màu nhạt hơn cho background (alpha 30%)
            int parsedColor = Color.parseColor(color);
            iconBg.setColor(Color.argb(50, Color.red(parsedColor),
                    Color.green(parsedColor), Color.blue(parsedColor)));
        } catch (Exception e) {
            iconBg.setColor(Color.parseColor("#E8F5E9"));
        }
        holder.viewIconBg.setBackground(iconBg);

        // Name + Amount info
        holder.tvGoalName.setText(goal.getName());
        holder.tvAmountInfo.setText(
                CurrencyFormatter.format(goal.getCurrentAmount()) + " / " +
                        CurrencyFormatter.format(goal.getTargetAmount()));

        // Percentage
        int percentage = goal.getProgressPercentage();
        holder.tvPercentage.setText(percentage + "%");
        try {
            holder.tvPercentage.setTextColor(Color.parseColor(color));
        } catch (Exception e) {
            holder.tvPercentage.setTextColor(Color.parseColor("#4CAF50"));
        }

        // Progress bar
        holder.pbGoal.setProgress(percentage);

        // Remaining
        if (goal.isCompleted()) {
            holder.tvRemaining.setText("✅ " + context.getString(R.string.label_goal_completed));
        } else {
            holder.tvRemaining.setText(context.getString(R.string.label_remaining) + ": " +
                    CurrencyFormatter.format(goal.getRemainingAmount()));
        }

        // Days remaining
        holder.tvDays.setText(calculateDaysText(goal.getTargetDate()));

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalClick(goal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return goals != null ? goals.size() : 0;
    }

    /**
     * Tính text đếm ngược ngày.
     */
    private String calculateDaysText(String targetDate) {
        if (targetDate == null || targetDate.isEmpty()) {
            return context.getString(R.string.label_no_deadline);
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
            Date target = sdf.parse(targetDate);
            if (target == null) return "";
            long diffMs = target.getTime() - System.currentTimeMillis();
            long days = TimeUnit.MILLISECONDS.toDays(diffMs);
            if (days > 0) {
                return "📅 " + context.getString(R.string.label_days_remaining, days);
            } else if (days == 0) {
                return "⏰ " + context.getString(R.string.label_due_today);
            } else {
                return "⚠️ " + context.getString(R.string.label_overdue);
            }
        } catch (Exception e) {
            return "";
        }
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        View viewIconBg;
        TextView tvIcon, tvGoalName, tvAmountInfo, tvPercentage, tvRemaining, tvDays;
        ProgressBar pbGoal;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIconBg = itemView.findViewById(R.id.view_icon_bg);
            tvIcon = itemView.findViewById(R.id.tv_icon);
            tvGoalName = itemView.findViewById(R.id.tv_goal_name);
            tvAmountInfo = itemView.findViewById(R.id.tv_goal_amount_info);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
            pbGoal = itemView.findViewById(R.id.pb_goal);
            tvRemaining = itemView.findViewById(R.id.tv_remaining);
            tvDays = itemView.findViewById(R.id.tv_days);
        }
    }
}
