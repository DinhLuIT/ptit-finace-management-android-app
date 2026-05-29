package com.ptithcm.finacemanager.model;

/**
 * Model POJO cho Mục tiêu tiết kiệm (Phương Án A – Độc lập).
 *
 * <p>Mỗi SavingsGoal là một mục tiêu tiết kiệm độc lập, KHÔNG gắn với Pot.
 * Người dùng "đóng góp" tiền vào goal thông qua GoalContribution.
 * Tiến độ = currentAmount / targetAmount.
 *
 * <p>Map với bảng {@code SAVINGS_GOALS} trong SQLite.
 */
public class SavingsGoal {

    private int id;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private String targetDate;   // Format: yyyy-MM-dd (nullable)
    private String icon;         // Emoji icon
    private String color;        // Hex color
    private String createdAt;

    public SavingsGoal() {
    }

    /**
     * Constructor dùng khi tạo mục tiêu mới.
     *
     * @param name         Tên mục tiêu (VD: "Mua xe máy", "Du lịch Đà Lạt")
     * @param targetAmount Số tiền mục tiêu
     * @param targetDate   Ngày hoàn thành dự kiến (yyyy-MM-dd), có thể null
     * @param icon         Emoji icon
     * @param color        Hex color
     */
    public SavingsGoal(String name, double targetAmount, String targetDate,
                       String icon, String color) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
        this.targetDate = targetDate;
        this.icon = icon;
        this.color = color;
    }

    /**
     * Tính phần trăm tiến độ, cap tại 100%.
     */
    public int getProgressPercentage() {
        if (targetAmount <= 0) return 0;
        int percentage = (int) ((currentAmount / targetAmount) * 100);
        return Math.min(percentage, 100);
    }

    /**
     * Kiểm tra mục tiêu đã hoàn thành chưa.
     */
    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }

    /**
     * Số tiền còn thiếu để hoàn thành mục tiêu.
     */
    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    // === Getters & Setters ===

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
