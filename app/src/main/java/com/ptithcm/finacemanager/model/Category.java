package com.ptithcm.finacemanager.model;

import android.content.Context;

public class Category {
    private int id;
    private String name; // Lưu resource key (cat_food) cho default, hoặc text tùy chỉnh
    private String icon;
    private String type; // "INCOME", "EXPENSE", or "BOTH"
    private boolean isDefault;

    public Category() {
    }

    public Category(String name, String icon, String type, boolean isDefault) {
        this.name = name;
        this.icon = icon;
        this.type = type;
        this.isDefault = isDefault;
    }

    public Category(int id, String name, String icon, String type, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.type = type;
        this.isDefault = isDefault;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /**
     * Trả về resource key (cat_food) hoặc tên tùy chỉnh.
     * Dùng getLocalizedName(context) để lấy tên hiển thị đã dịch.
     */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * Resolve resource key → localized string.
     * Nếu name là resource key (cat_food) → trả về "Ăn uống" (VN) / "Food & Drink" (EN)
     * Nếu name không phải resource key → trả về nguyên gốc (category tùy chỉnh)
     */
    public String getLocalizedName(Context context) {
        if (name == null || name.isEmpty()) return "";

        // Thử tìm resource ID từ key
        int resId = context.getResources().getIdentifier(name, "string", context.getPackageName());
        if (resId != 0) {
            return context.getString(resId);
        }

        // Không tìm thấy → trả về nguyên gốc (category do user tự tạo)
        return name;
    }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}
