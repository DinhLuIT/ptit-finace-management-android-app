package com.ptithcm.finacemanager.model;

/**
 * Model POJO cho lịch sử đóng góp vào mục tiêu tiết kiệm.
 *
 * <p>Mỗi GoalContribution ghi nhận một lần đóng góp tiền vào SavingsGoal.
 * Lịch sử đóng góp giúp người dùng theo dõi nỗ lực tiết kiệm theo thời gian.
 *
 * <p>Map với bảng {@code GOAL_CONTRIBUTIONS} trong SQLite.
 */
public class GoalContribution {

    private int id;
    private int goalId;
    private double amount;
    private String note;
    private String date;       // Format: yyyy-MM-dd
    private String createdAt;

    public GoalContribution() {
    }

    /**
     * Constructor dùng khi tạo bản ghi đóng góp mới.
     *
     * @param goalId ID mục tiêu
     * @param amount Số tiền đóng góp
     * @param note   Ghi chú (nullable)
     * @param date   Ngày đóng góp (yyyy-MM-dd)
     */
    public GoalContribution(int goalId, double amount, String note, String date) {
        this.goalId = goalId;
        this.amount = amount;
        this.note = note;
        this.date = date;
    }

    // === Getters & Setters ===

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
