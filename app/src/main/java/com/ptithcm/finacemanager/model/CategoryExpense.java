package com.ptithcm.finacemanager.model;

/**
 * Model chứa dữ liệu thống kê chi tiêu theo danh mục.
 * Dùng để hứng kết quả từ câu truy vấn GROUP BY trong DBManager.
 */
public class CategoryExpense {

    private int categoryId;
    private String categoryName;
    private String categoryIcon;
    private double totalAmount;
    private float percentage;

    public CategoryExpense() {
    }

    public CategoryExpense(int categoryId, String categoryName, String categoryIcon,
                           double totalAmount, float percentage) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    // === Getter & Setter ===

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
