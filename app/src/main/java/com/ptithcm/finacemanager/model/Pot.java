package com.ptithcm.finacemanager.model;

public class Pot {
    private int id;
    private String name;
    private double balance;
    private double budgetLimit;
    private String color;
    private String icon;
    private String createdAt;
    private boolean isActive;

    public Pot() {
        this.color = "#4CAF50";
        this.icon = "ic_default";
        this.isActive = true;
    }

    public Pot(String name, double budgetLimit, String color) {
        this.name = name;
        this.balance = 0;
        this.budgetLimit = budgetLimit;
        this.color = color;
        this.icon = "ic_default";
        this.isActive = true;
    }

    public Pot(int id, String name, double balance, double budgetLimit, String color, String icon, String createdAt, boolean isActive) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.budgetLimit = budgetLimit;
        this.color = color;
        this.icon = icon;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Tính phần trăm đã chi tiêu so với ngân sách
    public int getBudgetPercentage() {
        if (budgetLimit <= 0) return 0;
        // Chi tiêu = budgetLimit - balance (nếu chỉ chi, balance giảm)
        // Nhưng vì balance có thể tăng (income), ta tính: spent = budgetLimit - balance
        // Cách tốt hơn: dùng tổng expense transactions, nhưng ở đây dùng đơn giản
        double spent = budgetLimit - balance;
        if (spent < 0) spent = 0;
        return (int) Math.min((spent / budgetLimit) * 100, 100);
    }

    // Số tiền còn lại so với ngân sách
    public double getRemaining() {
        return Math.max(balance, 0);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
