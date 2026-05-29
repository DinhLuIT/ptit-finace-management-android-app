package com.ptithcm.finacemanager.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.GoalContribution;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.util.List;

/**
 * Adapter hiển thị danh sách lịch sử đóng góp vào mục tiêu tiết kiệm.
 *
 * <p>Mỗi item hiển thị: icon tròn xanh lá, số tiền (formatted),
 * ghi chú (nếu có), và ngày đóng góp.
 * Hỗ trợ click listener để xóa đóng góp.
 */
public class GoalContributionAdapter extends RecyclerView.Adapter<GoalContributionAdapter.ContributionViewHolder> {

    private List<GoalContribution> contributionList;
    private final Context context;
    private OnItemClickListener clickListener;

    /**
     * Interface callback khi người dùng click vào item đóng góp.
     */
    public interface OnItemClickListener {
        void onItemClick(GoalContribution contribution);
    }

    public GoalContributionAdapter(List<GoalContribution> contributionList, Context context) {
        this.contributionList = contributionList;
        this.context = context;
    }

    /**
     * Đặt listener cho sự kiện click item (dùng để xóa đóng góp).
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Cập nhật dữ liệu mới và refresh RecyclerView.
     *
     * @param newList Danh sách GoalContribution mới
     */
    public void updateData(List<GoalContribution> newList) {
        this.contributionList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_contribution, parent, false);
        return new ContributionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContributionViewHolder holder, int position) {
        GoalContribution contribution = contributionList.get(position);
        holder.bind(contribution);
    }

    @Override
    public int getItemCount() {
        return contributionList.size();
    }

    /**
     * ViewHolder cho mỗi item đóng góp.
     */
    class ContributionViewHolder extends RecyclerView.ViewHolder {

        private final View viewContributionBackground;
        private final TextView textViewContributionIcon;
        private final TextView textViewContributionAmount;
        private final TextView textViewContributionNote;
        private final TextView textViewContributionDate;

        ContributionViewHolder(@NonNull View itemView) {
            super(itemView);
            viewContributionBackground = itemView.findViewById(R.id.view_contribution_bg);
            textViewContributionIcon = itemView.findViewById(R.id.tv_contribution_icon);
            textViewContributionAmount = itemView.findViewById(R.id.tv_contribution_amount);
            textViewContributionNote = itemView.findViewById(R.id.tv_contribution_note);
            textViewContributionDate = itemView.findViewById(R.id.tv_contribution_date);
        }

        /**
         * Bind dữ liệu GoalContribution vào view.
         */
        void bind(GoalContribution contribution) {
            // Icon tròn xanh lá với dấu "+"
            textViewContributionIcon.setText("+");
            GradientDrawable backgroundDrawable = new GradientDrawable();
            backgroundDrawable.setShape(GradientDrawable.OVAL);
            backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.color_income));
            viewContributionBackground.setBackground(backgroundDrawable);

            // Số tiền đóng góp (format với dấu +)
            textViewContributionAmount.setText("+" + CurrencyFormatter.format(contribution.getAmount()));

            // Ghi chú (ẩn nếu không có)
            String note = contribution.getNote();
            if (note != null && !note.isEmpty()) {
                textViewContributionNote.setText(note);
                textViewContributionNote.setVisibility(View.VISIBLE);
            } else {
                textViewContributionNote.setVisibility(View.GONE);
            }

            // Ngày đóng góp
            textViewContributionDate.setText(DateUtils.formatForDisplay(contribution.getDate()));

            // Click listener (long press để xóa)
            itemView.setOnLongClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onItemClick(contribution);
                }
                return true;
            });

            // Click thường cũng trigger (tùy chọn)
            itemView.setOnClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onItemClick(contribution);
                }
            });
        }
    }
}
