package com.ptithcm.finacemanager.model;

import android.content.Context;

/**
 * Model POJO cho Giao dịch định kỳ.
 *
 * <p>Mỗi RecurringTransaction đại diện cho một lệnh giao dịch tự động lặp lại.
 * WorkManager sẽ kiểm tra bảng này hàng ngày, nếu {@code nextDate <= today} thì
 * tự động tạo một Transaction thực tế trong bảng TRANSACTIONS và tính toán
 * {@code nextDate} cho chu kỳ tiếp theo.
 *
 * <p>Map với bảng {@code RECURRING_TRANSACTIONS} trong SQLite.
 */
public class RecurringTransaction {

    private int id;
    private int potId;
    private int categoryId;
    private double amount;
    private String type;        // "INCOME" || "EXPENSE"
    private String note;
    private String frequency;   // "DAILY" || "WEEKLY" || "MONTHLY" || "YEARLY"
    private String nextDate;    // Format: yyyy-MM-dd – ngày lặp tiếp theo
    private boolean isActive;
    private String createdAt;

    // Transient fields (không lưu DB, dùng để hiển thị)
    private String potName;
    private String categoryName;
    private String categoryIcon;

    public RecurringTransaction() {
    }

    /**
     * Constructor đầy đủ dùng khi tạo giao dịch định kỳ mới.
     *
     * @param potId      ID hủ gắn kết
     * @param categoryId ID danh mục giao dịch
     * @param amount     Số tiền mỗi lần lặp
     * @param type       Loại giao dịch (INCOME / EXPENSE)
     * @param note       Ghi chú (có thể null)
     * @param frequency  Tần suất lặp (DAILY / WEEKLY / MONTHLY / YEARLY)
     * @param nextDate   Ngày lặp đầu tiên (yyyy-MM-dd)
     */
    public RecurringTransaction(int potId, int categoryId, double amount,
                                 String type, String note, String frequency, String nextDate) {
        this.potId = potId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.frequency = frequency;
        this.nextDate = nextDate;
        this.isActive = true;
    }

    // === Getters & Setters ===

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

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getNextDate() { return nextDate; }
    public void setNextDate(String nextDate) { this.nextDate = nextDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPotName() { return potName; }
    public void setPotName(String potName) { this.potName = potName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    /**
     * Resolve category name resource key → localized string.
     * Tương tự Transaction.getLocalizedCategoryName().
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
