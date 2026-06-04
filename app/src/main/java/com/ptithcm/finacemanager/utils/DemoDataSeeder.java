package com.ptithcm.finacemanager.utils;

import android.database.sqlite.SQLiteDatabase;

/**
 * Tạo dữ liệu demo chi tiết mô phỏng một người dùng thực tế
 * đã sử dụng ứng dụng từ tháng 3/2026 đến 4/6/2026.
 *
 * Bao gồm: 5 Hũ, ~100 giao dịch, 3 giao dịch định kỳ,
 * 2 mục tiêu tiết kiệm với các lần đóng góp.
 */
public class DemoDataSeeder {

    // Category IDs (seeded by DBManager.seedCategories)
    private static final int CAT_FOOD = 1;
    private static final int CAT_HOUSING = 2;
    private static final int CAT_TRANSPORT = 3;
    private static final int CAT_ENTERTAINMENT = 4;
    private static final int CAT_EDUCATION = 5;
    private static final int CAT_HEALTH = 6;
    private static final int CAT_SHOPPING = 7;
    private static final int CAT_SAVINGS = 8;
    private static final int CAT_SALARY = 9;
    private static final int CAT_GIFT = 10;
    private static final int CAT_OTHER = 11;
    private static final int CAT_TRANSFER_OUT = 12;
    private static final int CAT_TRANSFER_IN = 13;

    public static void seedDemoData(SQLiteDatabase db) {
        // Xóa dữ liệu cũ (thứ tự quan trọng do foreign keys)
        db.execSQL("DELETE FROM GOAL_CONTRIBUTIONS");
        db.execSQL("DELETE FROM SAVINGS_GOALS");
        db.execSQL("DELETE FROM RECURRING_TRANSACTIONS");
        db.execSQL("DELETE FROM TRANSACTIONS");
        db.execSQL("DELETE FROM POTS");

        // ================================================================
        // ===== 5 HŨ TÀI CHÍNH =====
        // ================================================================
        pot(db, "Chi tiêu hàng ngày",    10000000, "#4CAF50", "ic_food",          "2026-02-15 10:00:00");
        pot(db, "Tiết kiệm",            20000000, "#2196F3", "ic_savings",        "2026-02-15 10:05:00");
        pot(db, "Học tập & Phát triển",   3000000, "#FF9800", "ic_education",      "2026-02-15 10:10:00");
        pot(db, "Giải trí",              2500000, "#9C27B0", "ic_entertainment",  "2026-02-20 14:00:00");
        pot(db, "Quỹ dự phòng",          5000000, "#F44336", "ic_health",         "2026-03-01 09:00:00");
        // Auto IDs: Pot 1, 2, 3, 4, 5

        // ================================================================
        // ===== THÁNG 3/2026 (~28 giao dịch) =====
        // ================================================================

        // -- Lương & Thu nhập --
        tx(db, 1, CAT_SALARY, 12000000, "INCOME",  "2026-03-01", "Nhận lương tháng 3");
        tx(db, 1, CAT_OTHER,   2500000, "INCOME",  "2026-03-03", "Freelance thiết kế logo");

        // -- Nhà ở --
        tx(db, 1, CAT_HOUSING, 3500000, "EXPENSE", "2026-03-05", "Tiền nhà tháng 3");
        tx(db, 1, CAT_HOUSING,  350000, "EXPENSE", "2026-03-29", "Tiền điện nước tháng 3");
        tx(db, 1, CAT_HOUSING,  220000, "EXPENSE", "2026-03-30", "Tiền internet tháng 3");

        // -- Chuyển khoản nội bộ --
        transfer(db, 1, 2, 3000000, "2026-03-05", "Trích tiết kiệm tháng 3");
        transfer(db, 1, 3,  500000, "2026-03-05", "Quỹ học tập tháng 3");
        transfer(db, 1, 5, 1000000, "2026-03-18", "Quỹ dự phòng tháng 3");

        // -- Ăn uống --
        tx(db, 1, CAT_FOOD,  350000, "EXPENSE", "2026-03-02", "Đi chợ đầu tuần");
        tx(db, 1, CAT_FOOD,   55000, "EXPENSE", "2026-03-04", "Cà phê sáng");
        tx(db, 1, CAT_FOOD,   80000, "EXPENSE", "2026-03-07", "Ăn trưa văn phòng");
        tx(db, 1, CAT_FOOD,  450000, "EXPENSE", "2026-03-10", "Siêu thị cuối tuần");
        tx(db, 1, CAT_FOOD,  300000, "EXPENSE", "2026-03-15", "Ăn tiệc sinh nhật bạn");
        tx(db, 1, CAT_FOOD,  400000, "EXPENSE", "2026-03-20", "Đi chợ cuối tuần");
        tx(db, 1, CAT_FOOD,  120000, "EXPENSE", "2026-03-24", "Ăn ngoài");
        tx(db, 1, CAT_FOOD,  280000, "EXPENSE", "2026-03-27", "Đi chợ");

        // -- Di chuyển --
        tx(db, 1, CAT_TRANSPORT,  45000, "EXPENSE", "2026-03-03", "Grab đi làm");
        tx(db, 1, CAT_TRANSPORT, 200000, "EXPENSE", "2026-03-08", "Đổ xăng xe máy");
        tx(db, 1, CAT_TRANSPORT,  65000, "EXPENSE", "2026-03-14", "Grab đi chơi");

        // -- Mua sắm --
        tx(db, 1, CAT_SHOPPING, 650000, "EXPENSE", "2026-03-22", "Mua quần áo mới");

        // -- Sức khỏe --
        tx(db, 1, CAT_HEALTH, 500000, "EXPENSE", "2026-03-17", "Khám bệnh định kỳ");

        // -- Quà tặng --
        tx(db, 1, CAT_GIFT, 200000, "EXPENSE", "2026-03-15", "Quà sinh nhật bạn Lan");

        // -- Giải trí (Pot 4) --
        tx(db, 4, CAT_ENTERTAINMENT, 150000, "EXPENSE", "2026-03-12", "Xem phim rạp");
        tx(db, 4, CAT_ENTERTAINMENT,  59000, "EXPENSE", "2026-03-25", "Spotify Premium");

        // -- Học tập (Pot 3) --
        tx(db, 3, CAT_EDUCATION, 250000, "EXPENSE", "2026-03-10", "Mua sách lập trình Android");
        tx(db, 3, CAT_EDUCATION, 300000, "EXPENSE", "2026-03-20", "Khóa học Udemy sale");

        // ================================================================
        // ===== THÁNG 4/2026 (~27 giao dịch) =====
        // ================================================================

        // -- Lương & Thu nhập --
        tx(db, 1, CAT_SALARY, 12000000, "INCOME",  "2026-04-01", "Nhận lương tháng 4");
        tx(db, 1, CAT_OTHER,   4000000, "INCOME",  "2026-04-02", "Freelance dự án website");
        tx(db, 1, CAT_GIFT,     500000, "INCOME",  "2026-04-10", "Tiền thưởng sinh nhật từ công ty");

        // -- Nhà ở --
        tx(db, 1, CAT_HOUSING, 3500000, "EXPENSE", "2026-04-03", "Tiền nhà tháng 4");
        tx(db, 1, CAT_HOUSING,  380000, "EXPENSE", "2026-04-27", "Tiền điện nước tháng 4");
        tx(db, 1, CAT_HOUSING,  220000, "EXPENSE", "2026-04-28", "Tiền internet tháng 4");

        // -- Chuyển khoản nội bộ --
        transfer(db, 1, 2, 4000000, "2026-04-03", "Trích tiết kiệm tháng 4");
        transfer(db, 1, 3,  500000, "2026-04-03", "Quỹ học tập tháng 4");
        transfer(db, 1, 5,  500000, "2026-04-23", "Quỹ dự phòng tháng 4");

        // -- Ăn uống --
        tx(db, 1, CAT_FOOD, 500000, "EXPENSE", "2026-04-05", "Đi chợ đầu tháng");
        tx(db, 1, CAT_FOOD,  45000, "EXPENSE", "2026-04-07", "Cà phê sáng với đồng nghiệp");
        tx(db, 1, CAT_FOOD, 320000, "EXPENSE", "2026-04-12", "Đi chợ giữa tháng");
        tx(db, 1, CAT_FOOD, 250000, "EXPENSE", "2026-04-13", "Ăn ngoài cuối tuần");
        tx(db, 1, CAT_FOOD, 380000, "EXPENSE", "2026-04-19", "Siêu thị");
        tx(db, 1, CAT_FOOD, 350000, "EXPENSE", "2026-04-25", "Đi chợ");
        tx(db, 1, CAT_FOOD,  95000, "EXPENSE", "2026-04-30", "Ăn tối");

        // -- Di chuyển --
        tx(db, 1, CAT_TRANSPORT,  38000, "EXPENSE", "2026-04-06", "Grab đi làm");
        tx(db, 1, CAT_TRANSPORT, 180000, "EXPENSE", "2026-04-08", "Đổ xăng xe máy");
        tx(db, 1, CAT_TRANSPORT,  52000, "EXPENSE", "2026-04-17", "Grab đi meeting");

        // -- Mua sắm --
        tx(db, 1, CAT_SHOPPING, 800000, "EXPENSE", "2026-04-15", "Mua giày thể thao");

        // -- Sức khỏe --
        tx(db, 1, CAT_HEALTH, 120000, "EXPENSE", "2026-04-20", "Mua thuốc cảm cúm");

        // -- Giải trí (Pot 4) --
        tx(db, 4, CAT_ENTERTAINMENT, 350000, "EXPENSE", "2026-04-10", "Mua game Steam sale");
        tx(db, 4, CAT_ENTERTAINMENT, 200000, "EXPENSE", "2026-04-22", "Xem phim rạp cuối tuần");

        // -- Học tập (Pot 3) --
        tx(db, 3, CAT_EDUCATION, 450000, "EXPENSE", "2026-04-16", "Khóa học Coursera");

        // ================================================================
        // ===== THÁNG 5/2026 (~31 giao dịch) =====
        // ================================================================

        // -- Lương & Thu nhập --
        tx(db, 1, CAT_SALARY, 12000000, "INCOME",  "2026-05-01", "Nhận lương tháng 5");
        tx(db, 1, CAT_SALARY,  2000000, "INCOME",  "2026-05-01", "Thưởng lễ 30/4 - 1/5");
        tx(db, 1, CAT_OTHER,   1500000, "INCOME",  "2026-05-15", "Freelance fix bug ứng dụng");

        // -- Nhà ở --
        tx(db, 1, CAT_HOUSING, 3500000, "EXPENSE", "2026-05-02", "Tiền nhà tháng 5");
        tx(db, 1, CAT_HOUSING,  400000, "EXPENSE", "2026-05-27", "Tiền điện nước tháng 5");
        tx(db, 1, CAT_HOUSING,  220000, "EXPENSE", "2026-05-28", "Tiền internet tháng 5");

        // -- Chuyển khoản nội bộ --
        transfer(db, 1, 2, 3500000, "2026-05-02", "Trích tiết kiệm tháng 5");
        transfer(db, 1, 3,  500000, "2026-05-02", "Quỹ học tập tháng 5");
        transfer(db, 1, 5,  500000, "2026-05-23", "Quỹ dự phòng tháng 5");

        // -- Ăn uống --
        tx(db, 1, CAT_FOOD, 800000, "EXPENSE", "2026-05-04", "Ăn uống du lịch 30/4");
        tx(db, 1, CAT_FOOD, 420000, "EXPENSE", "2026-05-05", "Đi chợ đầu tuần");
        tx(db, 1, CAT_FOOD,  75000, "EXPENSE", "2026-05-12", "Ăn trưa văn phòng");
        tx(db, 1, CAT_FOOD,  90000, "EXPENSE", "2026-05-13", "Cà phê với bạn bè");
        tx(db, 1, CAT_FOOD, 300000, "EXPENSE", "2026-05-16", "Đi chợ giữa tháng");
        tx(db, 1, CAT_FOOD, 180000, "EXPENSE", "2026-05-22", "Ăn ngoài cuối tuần");
        tx(db, 1, CAT_FOOD, 380000, "EXPENSE", "2026-05-25", "Đi chợ cuối tuần");
        tx(db, 1, CAT_FOOD, 350000, "EXPENSE", "2026-05-30", "Ăn tiệc công ty");

        // -- Di chuyển --
        tx(db, 1, CAT_TRANSPORT,  55000, "EXPENSE", "2026-05-07", "Grab đi làm");
        tx(db, 1, CAT_TRANSPORT, 210000, "EXPENSE", "2026-05-08", "Đổ xăng xe máy");
        tx(db, 1, CAT_TRANSPORT,  42000, "EXPENSE", "2026-05-20", "Grab đi meeting client");

        // -- Mua sắm --
        tx(db, 1, CAT_SHOPPING, 350000, "EXPENSE", "2026-05-14", "Mua đồ gia dụng");
        tx(db, 1, CAT_SHOPPING, 450000, "EXPENSE", "2026-05-21", "Mua áo sơ mi mới");

        // -- Sức khỏe --
        tx(db, 1, CAT_HEALTH, 800000, "EXPENSE", "2026-05-17", "Khám sức khỏe tổng quát");

        // -- Quà tặng --
        tx(db, 1, CAT_GIFT, 500000, "EXPENSE", "2026-05-29", "Mua quà tặng mẹ");

        // -- Giải trí (Pot 4) --
        tx(db, 4, CAT_ENTERTAINMENT, 2500000, "EXPENSE", "2026-05-03", "Du lịch lễ 30/4 - Vũng Tàu");
        tx(db, 4, CAT_ENTERTAINMENT,  180000, "EXPENSE", "2026-05-18", "Netflix tháng 5");

        // -- Học tập (Pot 3) --
        tx(db, 3, CAT_EDUCATION, 180000, "EXPENSE", "2026-05-10", "Mua sách Clean Code");

        // ================================================================
        // ===== THÁNG 6/2026 (1-4, ~9 giao dịch) =====
        // ================================================================

        // -- Lương --
        tx(db, 1, CAT_SALARY, 12000000, "INCOME",  "2026-06-01", "Nhận lương tháng 6");

        // -- Chuyển khoản --
        transfer(db, 1, 2, 3000000, "2026-06-01", "Trích tiết kiệm tháng 6");

        // -- Nhà ở --
        tx(db, 1, CAT_HOUSING, 3500000, "EXPENSE", "2026-06-02", "Tiền nhà tháng 6");

        // -- Ăn uống --
        tx(db, 1, CAT_FOOD, 450000, "EXPENSE", "2026-06-02", "Đi chợ đầu tháng");
        tx(db, 1, CAT_FOOD,  40000, "EXPENSE", "2026-06-03", "Cà phê sáng");
        tx(db, 1, CAT_FOOD,  85000, "EXPENSE", "2026-06-04", "Ăn trưa");

        // -- Di chuyển --
        tx(db, 1, CAT_TRANSPORT, 35000, "EXPENSE", "2026-06-03", "Grab đi làm");

        // -- Mua sắm --
        tx(db, 1, CAT_SHOPPING, 280000, "EXPENSE", "2026-06-04", "Mua ốp lưng điện thoại");

        // ================================================================
        // ===== GIAO DỊCH ĐỊNH KỲ =====
        // ================================================================
        recurring(db, 1, CAT_SALARY, 12000000, "INCOME",  "Nhận lương hàng tháng",      "MONTHLY", "2026-07-01");
        recurring(db, 1, CAT_HOUSING, 3500000, "EXPENSE", "Tiền nhà hàng tháng",        "MONTHLY", "2026-07-05");
        recurring(db, 1, CAT_SAVINGS, 3000000, "EXPENSE", "Trích tiết kiệm hàng tháng", "MONTHLY", "2026-07-01");

        // ================================================================
        // ===== MỤC TIÊU TIẾT KIỆM =====
        // ================================================================
        goal(db, "Mua MacBook Pro mới",  25000000, "2026-12-31", "💻", "#2196F3", "2026-02-20 15:00:00");
        goal(db, "Du lịch Đà Nẵng",      8000000, "2026-09-15", "✈️", "#FF9800", "2026-03-01 10:00:00");
        // Auto IDs: Goal 1, 2

        // ===== Đóng góp cho mục tiêu =====
        // Goal 1: MacBook Pro
        contribution(db, 1, 2000000, "2026-03-10", "Tiết kiệm tháng 3");
        contribution(db, 1, 2000000, "2026-04-05", "Tiết kiệm tháng 4");
        contribution(db, 1, 2500000, "2026-05-03", "Tiết kiệm tháng 5 + thưởng lễ");
        contribution(db, 1, 2000000, "2026-06-01", "Tiết kiệm tháng 6");

        // Goal 2: Du lịch Đà Nẵng
        contribution(db, 2, 1000000, "2026-03-15", "Gom quỹ du lịch tháng 3");
        contribution(db, 2, 1000000, "2026-04-10", "Gom quỹ du lịch tháng 4");
        contribution(db, 2, 1500000, "2026-05-05", "Gom quỹ du lịch tháng 5");

        // ================================================================
        // ===== CẬP NHẬT SỐ DƯ TỰ ĐỘNG =====
        // ================================================================
        db.execSQL("UPDATE POTS SET BALANCE = COALESCE(" +
                "(SELECT SUM(CASE WHEN t.TYPE='INCOME' THEN t.AMOUNT ELSE -t.AMOUNT END) " +
                "FROM TRANSACTIONS t WHERE t.POT_ID = POTS.ID), 0)");

        db.execSQL("UPDATE SAVINGS_GOALS SET CURRENT_AMOUNT = COALESCE(" +
                "(SELECT SUM(c.AMOUNT) FROM GOAL_CONTRIBUTIONS c WHERE c.GOAL_ID = SAVINGS_GOALS.ID), 0)");
    }

    // ===== HELPER METHODS =====

    private static void pot(SQLiteDatabase db, String name, double budget,
                            String color, String icon, String createdAt) {
        db.execSQL("INSERT INTO POTS(NAME,BALANCE,BUDGET_LIMIT,COLOR,ICON,CREATED_AT,IS_ACTIVE) " +
                        "VALUES(?,0,?,?,?,?,1)",
                new Object[]{name, budget, color, icon, createdAt});
    }

    private static void tx(SQLiteDatabase db, int potId, int catId,
                           double amount, String type, String date, String note) {
        db.execSQL("INSERT INTO TRANSACTIONS(POT_ID,CATEGORY_ID,AMOUNT,TYPE,DATE,NOTE,CREATED_AT) " +
                        "VALUES(?,?,?,?,?,?,?)",
                new Object[]{potId, catId, amount, type, date, note, date + " 09:00:00"});
    }

    /** Chuyển khoản nội bộ = 2 giao dịch (EXPENSE từ nguồn + INCOME vào đích) */
    private static void transfer(SQLiteDatabase db, int fromPot, int toPot,
                                 double amount, String date, String note) {
        tx(db, fromPot, CAT_TRANSFER_OUT, amount, "EXPENSE", date, note);
        tx(db, toPot,   CAT_TRANSFER_IN,  amount, "INCOME",  date, note);
    }

    private static void recurring(SQLiteDatabase db, int potId, int catId,
                                  double amount, String type, String note,
                                  String frequency, String nextDate) {
        db.execSQL("INSERT INTO RECURRING_TRANSACTIONS(POT_ID,CATEGORY_ID,AMOUNT,TYPE,NOTE," +
                        "FREQUENCY,NEXT_DATE,IS_ACTIVE,CREATED_AT) VALUES(?,?,?,?,?,?,?,1,?)",
                new Object[]{potId, catId, amount, type, note, frequency, nextDate,
                        "2026-03-01 09:00:00"});
    }

    private static void goal(SQLiteDatabase db, String name, double target,
                             String targetDate, String icon, String color, String createdAt) {
        db.execSQL("INSERT INTO SAVINGS_GOALS(NAME,TARGET_AMOUNT,CURRENT_AMOUNT,TARGET_DATE," +
                        "ICON,COLOR,CREATED_AT) VALUES(?,?,0,?,?,?,?)",
                new Object[]{name, target, targetDate, icon, color, createdAt});
    }

    private static void contribution(SQLiteDatabase db, int goalId, double amount,
                                     String date, String note) {
        db.execSQL("INSERT INTO GOAL_CONTRIBUTIONS(GOAL_ID,AMOUNT,NOTE,DATE,CREATED_AT) " +
                        "VALUES(?,?,?,?,?)",
                new Object[]{goalId, amount, note, date, date + " 10:00:00"});
    }

    private DemoDataSeeder() {} // Prevent instantiation
}
