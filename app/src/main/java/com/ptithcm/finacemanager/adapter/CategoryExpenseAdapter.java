package com.ptithcm.finacemanager.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.CategoryExpense;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter hiển thị danh sách chi tiết chi tiêu theo danh mục
 * trong màn hình Thống kê (StatisticsActivity).
 */
public class CategoryExpenseAdapter
        extends RecyclerView.Adapter<CategoryExpenseAdapter.CategoryExpenseViewHolder> {

    private List<CategoryExpense> categoryExpenseList;
    private final Context context;
    private final int[] chartColors;

    // Map icon name (từ DB) sang emoji để hiển thị
    private static final Map<String, String> ICON_MAP = new HashMap<>();
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
        ICON_MAP.put("ic_transfer", "🔄");
    }

    public CategoryExpenseAdapter(List<CategoryExpense> categoryExpenseList,
                                  Context context, int[] chartColors) {
        this.categoryExpenseList = categoryExpenseList;
        this.context = context;
        this.chartColors = chartColors;
    }

    @NonNull
    @Override
    public CategoryExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_expense, parent, false);
        return new CategoryExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryExpenseViewHolder holder, int position) {
        CategoryExpense categoryExpense = categoryExpenseList.get(position);

        // Thanh màu dọc đại diện (đồng bộ với màu slice trên PieChart)
        int colorIndex = position % chartColors.length;
        GradientDrawable colorBarBackground = (GradientDrawable) holder.viewColorDot.getBackground();
        colorBarBackground.setColor(chartColors[colorIndex]);

        // Icon danh mục (chuyển từ icon name sang emoji)
        String iconName = categoryExpense.getCategoryIcon();
        String emoji = ICON_MAP.getOrDefault(iconName, "📦");
        holder.textViewCategoryIcon.setText(emoji);

        // Tên danh mục (resolve từ resource key sang tên hiển thị theo ngôn ngữ)
        String categoryName = categoryExpense.getCategoryName();
        int stringResourceId = context.getResources().getIdentifier(
                categoryName, "string", context.getPackageName());
        if (stringResourceId != 0) {
            holder.textViewCategoryName.setText(context.getString(stringResourceId));
        } else {
            holder.textViewCategoryName.setText(categoryName);
        }

        // Phần trăm
        holder.textViewCategoryPercentage.setText(
                String.format(Locale.getDefault(), "%.1f%%", categoryExpense.getPercentage()));

        // Số tiền (hiển thị giá trị tuyệt đối, không có dấu +/-)
        holder.textViewCategoryAmount.setText(
                CurrencyFormatter.format(categoryExpense.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return categoryExpenseList != null ? categoryExpenseList.size() : 0;
    }

    /**
     * Cập nhật dữ liệu cho adapter khi người dùng chuyển tháng.
     */
    public void updateData(List<CategoryExpense> newList) {
        this.categoryExpenseList = newList;
        notifyDataSetChanged();
    }

    static class CategoryExpenseViewHolder extends RecyclerView.ViewHolder {
        View viewColorDot;
        TextView textViewCategoryIcon;
        TextView textViewCategoryName;
        TextView textViewCategoryPercentage;
        TextView textViewCategoryAmount;

        CategoryExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorDot = itemView.findViewById(R.id.view_color_dot);
            textViewCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            textViewCategoryName = itemView.findViewById(R.id.tv_category_name);
            textViewCategoryPercentage = itemView.findViewById(R.id.tv_category_percentage);
            textViewCategoryAmount = itemView.findViewById(R.id.tv_category_amount);
        }
    }
}
