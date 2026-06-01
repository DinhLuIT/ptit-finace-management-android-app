package com.ptithcm.finacemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.model.Category;
import com.ptithcm.finacemanager.utils.IconMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter cho dropdown chọn danh mục.
 * Override Filter để khi chọn item, AutoCompleteTextView hiển thị
 * tên đã dịch (ví dụ "Ăn uống") thay vì resource key ("cat_food").
 */
public class CategoryDropdownAdapter extends ArrayAdapter<Category> {

    private final LayoutInflater inflater;
    private final List<Category> allCategories;

    public CategoryDropdownAdapter(@NonNull Context context, @NonNull List<Category> categories) {
        super(context, 0, categories);
        this.inflater = LayoutInflater.from(context);
        this.allCategories = new ArrayList<>(categories);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_dropdown_category, parent, false);
        }

        Category category = getItem(position);
        if (category != null) {
            ImageView ivIcon = convertView.findViewById(R.id.iv_category_icon);
            TextView tvName = convertView.findViewById(R.id.tv_category_name);

            tvName.setText(category.getLocalizedName(getContext()));

            String iconKey = category.getIcon();
            if (iconKey != null && !iconKey.isEmpty()) {
                ivIcon.setImageResource(IconMapper.getIconResource(getContext(), iconKey));
            } else {
                ivIcon.setImageResource(R.drawable.ic_other);
            }
        }
        return convertView;
    }

    /**
     * Override Filter để:
     * 1. convertResultToString() trả về tên đã dịch → AutoCompleteTextView hiển thị đúng.
     * 2. performFiltering() luôn trả về toàn bộ danh sách (không lọc) vì dropdown này chỉ dùng để chọn.
     */
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.values = allCategories;
                results.count = allCategories.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof Category) {
                    return ((Category) resultValue).getLocalizedName(getContext());
                }
                return super.convertResultToString(resultValue);
            }
        };
    }
}
