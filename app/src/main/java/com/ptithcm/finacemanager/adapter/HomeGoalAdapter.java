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

import java.util.List;

/**
 * Adapter cho carousel mục tiêu tiết kiệm trên HomeFragment.
 *
 * <p>Hiển thị danh sách mục tiêu dạng card nhỏ cuộn ngang.
 * Card cuối cùng luôn là nút "＋ Thêm mục tiêu" (viewType ADD).
 * Tap card goal → mở GoalDetailActivity.
 * Tap card add → mở AddGoalDialog.
 */
public class HomeGoalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_GOAL = 0;
    private static final int TYPE_ADD = 1;

    private List<SavingsGoal> goals;
    private final Context context;
    private OnHomeGoalClickListener listener;

    public interface OnHomeGoalClickListener {
        void onGoalClick(SavingsGoal goal);
        void onAddGoalClick();
    }

    public HomeGoalAdapter(List<SavingsGoal> goals, Context context) {
        this.goals = goals;
        this.context = context;
    }

    public void setOnHomeGoalClickListener(OnHomeGoalClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<SavingsGoal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // Card cuối cùng luôn là nút "Thêm mục tiêu"
        return position < goals.size() ? TYPE_GOAL : TYPE_ADD;
    }

    @Override
    public int getItemCount() {
        // goals + 1 card "Thêm mục tiêu"
        return goals.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_home_goal_add, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_home_goal_mini, parent, false);
            return new GoalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GoalViewHolder) {
            bindGoal((GoalViewHolder) holder, goals.get(position));
        } else if (holder instanceof AddViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAddGoalClick();
            });
        }
    }

    private void bindGoal(GoalViewHolder holder, SavingsGoal goal) {
        // Icon + Background tròn
        holder.tvIcon.setText(goal.getIcon() != null ? goal.getIcon() : Constants.DEFAULT_GOAL_ICON);
        String color = goal.getColor() != null ? goal.getColor() : Constants.DEFAULT_GOAL_COLOR;

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        try {
            int parsedColor = Color.parseColor(color);
            iconBg.setColor(Color.argb(40, Color.red(parsedColor),
                    Color.green(parsedColor), Color.blue(parsedColor)));
        } catch (Exception e) {
            iconBg.setColor(Color.parseColor("#E8F5E9"));
        }
        holder.viewIconBg.setBackground(iconBg);

        // Percentage
        int percentage = goal.getProgressPercentage();
        holder.tvPercentage.setText(percentage + "%");
        try {
            holder.tvPercentage.setTextColor(Color.parseColor(color));
        } catch (Exception e) {
            holder.tvPercentage.setTextColor(Color.parseColor("#4CAF50"));
        }

        // Name
        holder.tvGoalName.setText(goal.getName());

        // Progress bar
        holder.pbGoal.setProgress(percentage);

        // Amount info
        holder.tvAmountInfo.setText(CurrencyFormatter.formatCompact(goal.getCurrentAmount())
                + " / " + CurrencyFormatter.formatCompact(goal.getTargetAmount()));

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGoalClick(goal);
        });
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        View viewIconBg;
        TextView tvIcon, tvPercentage, tvGoalName, tvAmountInfo;
        ProgressBar pbGoal;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIconBg = itemView.findViewById(R.id.view_icon_bg);
            tvIcon = itemView.findViewById(R.id.tv_icon);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
            tvGoalName = itemView.findViewById(R.id.tv_goal_name);
            pbGoal = itemView.findViewById(R.id.pb_goal);
            tvAmountInfo = itemView.findViewById(R.id.tv_amount_info);
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
