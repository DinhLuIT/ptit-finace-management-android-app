package com.ptithcm.finacemanager.model;

import android.content.Context;

public class Transaction {
    private int id;
    private int potId;
    private int categoryId;
    private double amount;
    private String type; // "INCOME" || "EXPENSE"
    private String date;
    private String note;
    private String createdAt;

    // Transient fields (không lưu DB, dùng để hiển thị)
    private String potName;
    private String categoryName;
    private String categoryIcon;

    public Transaction() {
    }

    public Transaction(int potId, int categoryId, double amount, String type, String date, String note) {
        this.potId = potId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.note = note;
    }

    public boolean isIncome() {
        return "INCOME".equals(type);
    }

    public boolean isExpense() {
        return "EXPENSE".equals(type);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPotId() { return potId; }
    public void setPotId(int potId) { this.potId = potId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPotName() { return potName; }
    public void setPotName(String potName) { this.potName = potName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    /**
     * Resolve category name resource key → localized string.
     * Nếu là key (cat_food) → trả về "Ăn uống" / "Food & Drink"
     * Nếu không phải key → trả về nguyên gốc
     */
    public String getLocalizedCategoryName(Context context) {
        if (categoryName == null || categoryName.isEmpty()) return "";

        int resId = context.getResources().getIdentifier(categoryName, "string", context.getPackageName());
        if (resId != 0) {
            return context.getString(resId);
        }
        return categoryName;
    }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
}
