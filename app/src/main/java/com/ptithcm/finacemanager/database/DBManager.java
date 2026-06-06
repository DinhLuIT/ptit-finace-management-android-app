package com.ptithcm.finacemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ptithcm.finacemanager.model.Category;
import com.ptithcm.finacemanager.model.CategoryExpense;
import com.ptithcm.finacemanager.model.GoalContribution;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.model.RecurringTransaction;
import com.ptithcm.finacemanager.model.SavingsGoal;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.BuildConfig;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.DateUtils;
import com.ptithcm.finacemanager.utils.DemoDataSeeder;

import java.util.ArrayList;
import java.util.List;

public class DBManager extends SQLiteOpenHelper {

    private static DBManager instance;

    // === CREATE TABLE statements ===
    private static final String CREATE_TABLE_POTS =
            "CREATE TABLE " + Constants.TABLE_POTS + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "NAME TEXT NOT NULL, " +
                    "BALANCE REAL DEFAULT 0, " +
                    "BUDGET_LIMIT REAL NOT NULL, " +
                    "COLOR TEXT DEFAULT '#4CAF50', " +
                    "ICON TEXT DEFAULT 'ic_default', " +
                    "CREATED_AT TEXT NOT NULL, " +
                    "IS_ACTIVE INTEGER DEFAULT 1)";

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + Constants.TABLE_CATEGORIES + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "NAME TEXT NOT NULL, " +
                    "ICON TEXT NOT NULL, " +
                    "TYPE TEXT NOT NULL, " +
                    "IS_DEFAULT INTEGER DEFAULT 0)";

    private static final String CREATE_TABLE_TRANSACTIONS =
            "CREATE TABLE " + Constants.TABLE_TRANSACTIONS + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "POT_ID INTEGER NOT NULL, " +
                    "CATEGORY_ID INTEGER, " +
                    "AMOUNT REAL NOT NULL, " +
                    "TYPE TEXT NOT NULL, " +
                    "DATE TEXT NOT NULL, " +
                    "NOTE TEXT, " +
                    "CREATED_AT TEXT NOT NULL, " +
                    "FOREIGN KEY(POT_ID) REFERENCES POTS(ID), " +
                    "FOREIGN KEY(CATEGORY_ID) REFERENCES CATEGORIES(ID))";

    private static final String CREATE_TABLE_USER_SETTINGS =
            "CREATE TABLE " + Constants.TABLE_USER_SETTINGS + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "PIN_HASH TEXT, " +
                    "CURRENCY TEXT DEFAULT 'VND', " +
                    "DARK_MODE INTEGER DEFAULT 0, " +
                    "NOTIFICATION_ENABLED INTEGER DEFAULT 1)";

    // === Phase 3: Bảng mục tiêu tiết kiệm (Phương Án A – Độc lập) ===
    private static final String CREATE_TABLE_SAVINGS_GOALS =
            "CREATE TABLE " + Constants.TABLE_SAVINGS_GOALS + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "NAME TEXT NOT NULL, " +
                    "TARGET_AMOUNT REAL NOT NULL, " +
                    "CURRENT_AMOUNT REAL DEFAULT 0, " +
                    "TARGET_DATE TEXT, " +
                    "ICON TEXT DEFAULT '🎯', " +
                    "COLOR TEXT DEFAULT '#4CAF50', " +
                    "CREATED_AT TEXT NOT NULL)";

    // === Phase 3: Bảng lịch sử đóng góp vào mục tiêu ===
    private static final String CREATE_TABLE_GOAL_CONTRIBUTIONS =
            "CREATE TABLE " + Constants.TABLE_GOAL_CONTRIBUTIONS + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "GOAL_ID INTEGER NOT NULL, " +
                    "AMOUNT REAL NOT NULL, " +
                    "NOTE TEXT, " +
                    "DATE TEXT NOT NULL, " +
                    "CREATED_AT TEXT NOT NULL, " +
                    "FOREIGN KEY(GOAL_ID) REFERENCES SAVINGS_GOALS(ID))";

    // === Phase 3: Bảng giao dịch định kỳ ===
    private static final String CREATE_TABLE_RECURRING_TRANSACTIONS =
            "CREATE TABLE " + Constants.TABLE_RECURRING_TRANSACTIONS + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "POT_ID INTEGER NOT NULL, " +
                    "CATEGORY_ID INTEGER, " +
                    "AMOUNT REAL NOT NULL, " +
                    "TYPE TEXT NOT NULL, " +
                    "NOTE TEXT, " +
                    "FREQUENCY TEXT NOT NULL, " +
                    "NEXT_DATE TEXT NOT NULL, " +
                    "IS_ACTIVE INTEGER DEFAULT 1, " +
                    "CREATED_AT TEXT NOT NULL, " +
                    "FOREIGN KEY(POT_ID) REFERENCES POTS(ID), " +
                    "FOREIGN KEY(CATEGORY_ID) REFERENCES CATEGORIES(ID))";

    // Singleton pattern
    public static synchronized DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context.getApplicationContext());
        }
        return instance;
    }

    private DBManager(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_POTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_USER_SETTINGS);
        db.execSQL(CREATE_TABLE_SAVINGS_GOALS);
        db.execSQL(CREATE_TABLE_GOAL_CONTRIBUTIONS);
        db.execSQL(CREATE_TABLE_RECURRING_TRANSACTIONS);
        seedCategories(db);
        if (BuildConfig.DEBUG) {
            seedPotsAndTransactions(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migration an toàn: chỉ thêm bảng mới, không xóa dữ liệu cũ
        if (oldVersion < 5) {
            // Phase 3 v1: Tạo bảng RECURRING_TRANSACTIONS
            db.execSQL("CREATE TABLE IF NOT EXISTS " + Constants.TABLE_RECURRING_TRANSACTIONS + " (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "POT_ID INTEGER NOT NULL, " +
                    "CATEGORY_ID INTEGER, " +
                    "AMOUNT REAL NOT NULL, " +
                    "TYPE TEXT NOT NULL, " +
                    "NOTE TEXT, " +
                    "FREQUENCY TEXT NOT NULL, " +
                    "NEXT_DATE TEXT NOT NULL, " +
                    "IS_ACTIVE INTEGER DEFAULT 1, " +
                    "CREATED_AT TEXT NOT NULL, " +
                    "FOREIGN KEY(POT_ID) REFERENCES POTS(ID), " +
                    "FOREIGN KEY(CATEGORY_ID) REFERENCES CATEGORIES(ID))");
        }

        if (oldVersion < 6) {
            // Phase 3 v2 (Plan A): Redesign SAVINGS_GOALS – độc lập, không gắn Pot
            // Drop bảng cũ (nếu có – schema cũ có POT_ID, thiếu CURRENT_AMOUNT/ICON/COLOR)
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_SAVINGS_GOALS);

            // Tạo bảng SAVINGS_GOALS mới (Plan A – không POT_ID)
            db.execSQL(CREATE_TABLE_SAVINGS_GOALS);

            // Tạo bảng GOAL_CONTRIBUTIONS
            db.execSQL(CREATE_TABLE_GOAL_CONTRIBUTIONS);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Seed danh mục mặc định – lưu resource key để hỗ trợ i18n
    private void seedCategories(SQLiteDatabase db) {
        // NAME lưu resource key (cat_food, cat_housing...) → resolve lúc hiển thị
        String[][] defaultCategories = {
                {"cat_food", "ic_food", "EXPENSE"},
                {"cat_housing", "ic_housing", "EXPENSE"},
                {"cat_transport", "ic_transport", "EXPENSE"},
                {"cat_entertainment", "ic_entertainment", "EXPENSE"},
                {"cat_education", "ic_education", "EXPENSE"},
                {"cat_health", "ic_health", "EXPENSE"},
                {"cat_shopping", "ic_shopping", "EXPENSE"},
                {"cat_savings", "ic_savings", "BOTH"},
                {"cat_salary", "ic_salary", "INCOME"},
                {"cat_gift", "ic_gift", "BOTH"},
                {"cat_other", "ic_other", "BOTH"},
                {"cat_transfer_out", "ic_transfer", "EXPENSE"},
                {"cat_transfer_in", "ic_transfer", "INCOME"}
        };

        for (String[] cat : defaultCategories) {
            ContentValues values = new ContentValues();
            values.put("NAME", cat[0]);
            values.put("ICON", cat[1]);
            values.put("TYPE", cat[2]);
            values.put("IS_DEFAULT", 1);
            db.insert(Constants.TABLE_CATEGORIES, null, values);
        }
    }

    // Seed hũ tài chính và giao dịch mẫu phục vụ mục đích kiểm thử dự án
    private void seedPotsAndTransactions(SQLiteDatabase db) {
        DemoDataSeeder.seedDemoData(db);
    }

    // =============================================
    // ============ POT METHODS ====================
    // =============================================

    public long addPot(Pot pot) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", pot.getName());
        values.put("BALANCE", pot.getBalance());
        values.put("BUDGET_LIMIT", pot.getBudgetLimit());
        values.put("COLOR", pot.getColor());
        values.put("ICON", pot.getIcon());
        values.put("CREATED_AT", DateUtils.getNowDB());
        values.put("IS_ACTIVE", 1);

        long id = db.insert(Constants.TABLE_POTS, null, values);
        db.close();
        return id;
    }

    public List<Pot> getAllActivePots() {
        List<Pot> potList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = "IS_ACTIVE = ?";
        String[] selectionArgs = {"1"};

        Cursor cursor = db.query(Constants.TABLE_POTS, null, selection, selectionArgs,
                null, null, "CREATED_AT DESC");
        try {
            if (cursor.moveToFirst()) {
                do {
                    potList.add(cursorToPot(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return potList;
    }

    public Pot getPotById(int potId) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = "ID = ?";
        String[] selectionArgs = {String.valueOf(potId)};

        Cursor cursor = db.query(Constants.TABLE_POTS, null, selection, selectionArgs,
                null, null, null);
        Pot pot = null;
        try {
            if (cursor.moveToFirst()) {
                pot = cursorToPot(cursor);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return pot;
    }

    public void updatePot(Pot pot) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", pot.getName());
        values.put("BUDGET_LIMIT", pot.getBudgetLimit());
        values.put("COLOR", pot.getColor());
        values.put("ICON", pot.getIcon());

        String whereClause = "ID = ?";
        String[] whereArgs = {String.valueOf(pot.getId())};
        db.update(Constants.TABLE_POTS, values, whereClause, whereArgs);
        db.close();
    }

    public void deletePot(int potId) {
        SQLiteDatabase db = getWritableDatabase();
        // Xóa tất cả giao dịch của hủ trước
        String whereTransactions = "POT_ID = ?";
        String[] whereArgs = {String.valueOf(potId)};
        db.delete(Constants.TABLE_TRANSACTIONS, whereTransactions, whereArgs);

        // Soft delete hủ
        ContentValues values = new ContentValues();
        values.put("IS_ACTIVE", 0);
        db.update(Constants.TABLE_POTS, values, "ID = ?", whereArgs);
        db.close();
    }

    public double getTotalBalance() {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(BALANCE) FROM " + Constants.TABLE_POTS + " WHERE IS_ACTIVE = 1", null);
        try {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return total;
    }

    private Pot cursorToPot(Cursor cursor) {
        Pot pot = new Pot();
        pot.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
        pot.setName(cursor.getString(cursor.getColumnIndexOrThrow("NAME")));
        pot.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow("BALANCE")));
        pot.setBudgetLimit(cursor.getDouble(cursor.getColumnIndexOrThrow("BUDGET_LIMIT")));
        pot.setColor(cursor.getString(cursor.getColumnIndexOrThrow("COLOR")));
        pot.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("ICON")));
        pot.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("CREATED_AT")));
        pot.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("IS_ACTIVE")) == 1);
        return pot;
    }

    // =============================================
    // ========= TRANSACTION METHODS ===============
    // =============================================

    public long addTransaction(Transaction trans) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("POT_ID", trans.getPotId());
        values.put("CATEGORY_ID", trans.getCategoryId());
        values.put("AMOUNT", trans.getAmount());
        values.put("TYPE", trans.getType());
        values.put("DATE", trans.getDate());
        values.put("NOTE", trans.getNote());
        values.put("CREATED_AT", DateUtils.getNowDB());

        long id = db.insert(Constants.TABLE_TRANSACTIONS, null, values);

        // Cập nhật balance của hủ: INCOME → +, EXPENSE → -
        ContentValues potUpdate = new ContentValues();
        Pot pot = getPotByIdInternal(db, trans.getPotId());
        if (pot != null) {
            double newBalance = pot.getBalance();
            if (Constants.TYPE_INCOME.equals(trans.getType())) {
                newBalance += trans.getAmount();
            } else {
                newBalance -= trans.getAmount();
            }
            potUpdate.put("BALANCE", newBalance);
            db.update(Constants.TABLE_POTS, potUpdate, "ID = ?",
                    new String[]{String.valueOf(trans.getPotId())});
        }

        db.close();
        return id;
    }

    public List<Transaction> getAllTransactions() {
        return getTransactionsWithQuery(
                "SELECT T.*, P.NAME AS POT_NAME, C.NAME AS CAT_NAME, C.ICON AS CAT_ICON " +
                        "FROM " + Constants.TABLE_TRANSACTIONS + " T " +
                        "LEFT JOIN " + Constants.TABLE_POTS + " P ON T.POT_ID = P.ID " +
                        "LEFT JOIN " + Constants.TABLE_CATEGORIES + " C ON T.CATEGORY_ID = C.ID " +
                        "ORDER BY T.DATE DESC, T.CREATED_AT DESC",
                null);
    }

    public List<Transaction> getTransactionsByPotId(int potId) {
        return getTransactionsWithQuery(
                "SELECT T.*, P.NAME AS POT_NAME, C.NAME AS CAT_NAME, C.ICON AS CAT_ICON " +
                        "FROM " + Constants.TABLE_TRANSACTIONS + " T " +
                        "LEFT JOIN " + Constants.TABLE_POTS + " P ON T.POT_ID = P.ID " +
                        "LEFT JOIN " + Constants.TABLE_CATEGORIES + " C ON T.CATEGORY_ID = C.ID " +
                        "WHERE T.POT_ID = ? " +
                        "ORDER BY T.DATE DESC, T.CREATED_AT DESC",
                new String[]{String.valueOf(potId)});
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return getTransactionsWithQuery(
                "SELECT T.*, P.NAME AS POT_NAME, C.NAME AS CAT_NAME, C.ICON AS CAT_ICON " +
                        "FROM " + Constants.TABLE_TRANSACTIONS + " T " +
                        "LEFT JOIN " + Constants.TABLE_POTS + " P ON T.POT_ID = P.ID " +
                        "LEFT JOIN " + Constants.TABLE_CATEGORIES + " C ON T.CATEGORY_ID = C.ID " +
                        "ORDER BY T.DATE DESC, T.CREATED_AT DESC " +
                        "LIMIT ?",
                new String[]{String.valueOf(limit)});
    }

    public void deleteTransaction(int transId) {
        SQLiteDatabase db = getWritableDatabase();

        // Lấy thông tin giao dịch trước khi xóa để hoàn lại balance
        Cursor cursor = db.query(Constants.TABLE_TRANSACTIONS, null, "ID = ?",
                new String[]{String.valueOf(transId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("AMOUNT"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("TYPE"));
                int potId = cursor.getInt(cursor.getColumnIndexOrThrow("POT_ID"));

                // Hoàn lại balance: INCOME đã + → giờ -, EXPENSE đã - → giờ +
                Pot pot = getPotByIdInternal(db, potId);
                if (pot != null) {
                    double newBalance = pot.getBalance();
                    if (Constants.TYPE_INCOME.equals(type)) {
                        newBalance -= amount;
                    } else {
                        newBalance += amount;
                    }
                    ContentValues potUpdate = new ContentValues();
                    potUpdate.put("BALANCE", newBalance);
                    db.update(Constants.TABLE_POTS, potUpdate, "ID = ?",
                            new String[]{String.valueOf(potId)});
                }
            }
        } finally {
            cursor.close();
        }

        db.delete(Constants.TABLE_TRANSACTIONS, "ID = ?", new String[]{String.valueOf(transId)});
        db.close();
    }

    public Transaction getTransactionById(int transId) {
        List<Transaction> list = getTransactionsWithQuery(
                "SELECT T.*, P.NAME AS POT_NAME, C.NAME AS CAT_NAME, C.ICON AS CAT_ICON " +
                        "FROM " + Constants.TABLE_TRANSACTIONS + " T " +
                        "LEFT JOIN " + Constants.TABLE_POTS + " P ON T.POT_ID = P.ID " +
                        "LEFT JOIN " + Constants.TABLE_CATEGORIES + " C ON T.CATEGORY_ID = C.ID " +
                        "WHERE T.ID = ?",
                new String[]{String.valueOf(transId)});
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public void updateTransaction(Transaction newTrans) {
        SQLiteDatabase db = getWritableDatabase();

        // 1. Lấy thông tin giao dịch cũ để hoàn lại tiền
        Cursor cursor = db.query(Constants.TABLE_TRANSACTIONS, null, "ID = ?",
                new String[]{String.valueOf(newTrans.getId())}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                double oldAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("AMOUNT"));
                String oldType = cursor.getString(cursor.getColumnIndexOrThrow("TYPE"));
                int oldPotId = cursor.getInt(cursor.getColumnIndexOrThrow("POT_ID"));

                // Hoàn lại tiền cho hủ cũ
                Pot oldPot = getPotByIdInternal(db, oldPotId);
                if (oldPot != null) {
                    double oldBalance = oldPot.getBalance();
                    if (Constants.TYPE_INCOME.equals(oldType)) {
                        oldBalance -= oldAmount;
                    } else {
                        oldBalance += oldAmount;
                    }
                    ContentValues oldPotUpdate = new ContentValues();
                    oldPotUpdate.put("BALANCE", oldBalance);
                    db.update(Constants.TABLE_POTS, oldPotUpdate, "ID = ?",
                            new String[]{String.valueOf(oldPotId)});
                }
            }
        } finally {
            cursor.close();
        }

        // 2. Cập nhật giao dịch mới
        ContentValues values = new ContentValues();
        values.put("POT_ID", newTrans.getPotId());
        values.put("CATEGORY_ID", newTrans.getCategoryId());
        values.put("AMOUNT", newTrans.getAmount());
        values.put("TYPE", newTrans.getType());
        values.put("DATE", newTrans.getDate());
        values.put("NOTE", newTrans.getNote());
        db.update(Constants.TABLE_TRANSACTIONS, values, "ID = ?", new String[]{String.valueOf(newTrans.getId())});

        // 3. Tính tiền cho hủ mới
        Pot newPot = getPotByIdInternal(db, newTrans.getPotId());
        if (newPot != null) {
            double newBalance = newPot.getBalance();
            if (Constants.TYPE_INCOME.equals(newTrans.getType())) {
                newBalance += newTrans.getAmount();
            } else {
                newBalance -= newTrans.getAmount();
            }
            ContentValues newPotUpdate = new ContentValues();
            newPotUpdate.put("BALANCE", newBalance);
            db.update(Constants.TABLE_POTS, newPotUpdate, "ID = ?",
                    new String[]{String.valueOf(newTrans.getPotId())});
        }

        db.close();
    }

    // Tổng thu nhập trong tháng
    public double getTotalIncomeByMonth(int month, int year) {
        return getTotalByTypeAndMonth(Constants.TYPE_INCOME, month, year);
    }

    // Tổng chi tiêu trong tháng
    public double getTotalExpenseByMonth(int month, int year) {
        return getTotalByTypeAndMonth(Constants.TYPE_EXPENSE, month, year);
    }

    private double getTotalByTypeAndMonth(String type, int month, int year) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;
        String monthStr = String.format("%04d-%02d", year, month);

        Cursor cursor = db.rawQuery(
                "SELECT SUM(AMOUNT) FROM " + Constants.TABLE_TRANSACTIONS +
                        " WHERE TYPE = ? AND DATE LIKE ?",
                new String[]{type, monthStr + "%"});
        try {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return total;
    }

    // Helper: lấy danh sách giao dịch từ raw query
    private List<Transaction> getTransactionsWithQuery(String query, String[] args) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, args);
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToTransaction(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    // Helper: lấy Pot theo ID mà không đóng DB (dùng trong transaction)
    private Pot getPotByIdInternal(SQLiteDatabase db, int potId) {
        Cursor cursor = db.query(Constants.TABLE_POTS, null, "ID = ?",
                new String[]{String.valueOf(potId)}, null, null, null);
        Pot pot = null;
        try {
            if (cursor.moveToFirst()) {
                pot = cursorToPot(cursor);
            }
        } finally {
            cursor.close();
        }
        return pot;
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction trans = new Transaction();
        trans.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
        trans.setPotId(cursor.getInt(cursor.getColumnIndexOrThrow("POT_ID")));
        trans.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("AMOUNT")));
        trans.setType(cursor.getString(cursor.getColumnIndexOrThrow("TYPE")));
        trans.setDate(cursor.getString(cursor.getColumnIndexOrThrow("DATE")));
        trans.setNote(cursor.getString(cursor.getColumnIndexOrThrow("NOTE")));
        trans.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("CREATED_AT")));

        // Category ID có thể null
        int catIdIndex = cursor.getColumnIndex("CATEGORY_ID");
        if (catIdIndex >= 0 && !cursor.isNull(catIdIndex)) {
            trans.setCategoryId(cursor.getInt(catIdIndex));
        }

        // Transient fields từ JOIN
        int potNameIndex = cursor.getColumnIndex("POT_NAME");
        if (potNameIndex >= 0) {
            trans.setPotName(cursor.getString(potNameIndex));
        }
        int catNameIndex = cursor.getColumnIndex("CAT_NAME");
        if (catNameIndex >= 0) {
            trans.setCategoryName(cursor.getString(catNameIndex));
        }
        int catIconIndex = cursor.getColumnIndex("CAT_ICON");
        if (catIconIndex >= 0) {
            trans.setCategoryIcon(cursor.getString(catIconIndex));
        }
        return trans;
    }

    // =============================================
    // ========= CATEGORY METHODS ==================
    // =============================================

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_CATEGORIES, null, null, null,
                null, null, "ID ASC");
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    public List<Category> getCategoriesByType(String type) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // Lấy categories phù hợp với type hoặc BOTH
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + Constants.TABLE_CATEGORIES +
                        " WHERE TYPE = ? OR TYPE = 'BOTH' ORDER BY ID ASC",
                new String[]{type});
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    public Category getCategoryById(int categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_CATEGORIES, null, "ID = ?",
                new String[]{String.valueOf(categoryId)}, null, null, null);
        Category category = null;
        try {
            if (cursor.moveToFirst()) {
                category = cursorToCategory(cursor);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return category;
    }

    private Category cursorToCategory(Cursor cursor) {
        Category category = new Category();
        category.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
        category.setName(cursor.getString(cursor.getColumnIndexOrThrow("NAME")));
        category.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("ICON")));
        category.setType(cursor.getString(cursor.getColumnIndexOrThrow("TYPE")));
        category.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow("IS_DEFAULT")) == 1);
        return category;
    }

    // =============================================
    // ========= TRANSFER METHODS ==================
    // =============================================

    /**
     * Lấy ID của danh mục theo tên (resource key).
     * Trả về -1 nếu không tìm thấy.
     */
    public int getCategoryIdByName(String categoryName) {
        SQLiteDatabase database = getReadableDatabase();
        int categoryId = -1;
        Cursor cursor = database.query(
                Constants.TABLE_CATEGORIES,
                new String[]{"ID"},
                "NAME = ?",
                new String[]{categoryName},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                categoryId = cursor.getInt(0);
            }
        } finally {
            cursor.close();
            database.close();
        }
        return categoryId;
    }

    /**
     * Chuyển tiền giữa 2 hủ sử dụng logic kế toán kép (double-entry).
     * - Tạo 1 giao dịch Chi (EXPENSE) ở Hủ Nguồn với danh mục "Chuyển đi"
     * - Tạo 1 giao dịch Thu (INCOME) ở Hủ Đích với danh mục "Nhận từ"
     * - Cập nhật số dư cả 2 hủ
     * Sử dụng SQLite transaction để đảm bảo tính toàn vẹn dữ liệu (atomic).
     *
     * @param sourcePotId      ID hủ nguồn
     * @param destinationPotId ID hủ đích
     * @param amount           Số tiền chuyển
     * @param note             Ghi chú (có thể null)
     * @return true nếu chuyển thành công, false nếu thất bại
     */
    public boolean transferMoney(int sourcePotId, int destinationPotId, double amount, String note) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            // Lấy thông tin 2 hủ
            Pot sourcePot = getPotByIdInternal(database, sourcePotId);
            Pot destinationPot = getPotByIdInternal(database, destinationPotId);
            if (sourcePot == null || destinationPot == null) {
                return false;
            }

            // Kiểm tra số dư hủ nguồn
            if (sourcePot.getBalance() < amount) {
                return false;
            }

            // Lấy ID danh mục hệ thống cho chuyển tiền
            int transferOutCategoryId = getCategoryIdByNameInternal(database, Constants.CAT_TRANSFER_OUT);
            int transferInCategoryId = getCategoryIdByNameInternal(database, Constants.CAT_TRANSFER_IN);

            String todayDate = DateUtils.getTodayDB();
            String nowTimestamp = DateUtils.getNowDB();

            // 1. Tạo giao dịch Chi ở Hủ Nguồn
            ContentValues expenseValues = new ContentValues();
            expenseValues.put("POT_ID", sourcePotId);
            expenseValues.put("CATEGORY_ID", transferOutCategoryId);
            expenseValues.put("AMOUNT", amount);
            expenseValues.put("TYPE", Constants.TYPE_EXPENSE);
            expenseValues.put("DATE", todayDate);
            expenseValues.put("NOTE", note != null ? note : "");
            expenseValues.put("CREATED_AT", nowTimestamp);
            database.insert(Constants.TABLE_TRANSACTIONS, null, expenseValues);

            // 2. Tạo giao dịch Thu ở Hủ Đích
            ContentValues incomeValues = new ContentValues();
            incomeValues.put("POT_ID", destinationPotId);
            incomeValues.put("CATEGORY_ID", transferInCategoryId);
            incomeValues.put("AMOUNT", amount);
            incomeValues.put("TYPE", Constants.TYPE_INCOME);
            incomeValues.put("DATE", todayDate);
            incomeValues.put("NOTE", note != null ? note : "");
            incomeValues.put("CREATED_AT", nowTimestamp);
            database.insert(Constants.TABLE_TRANSACTIONS, null, incomeValues);

            // 3. Cập nhật số dư Hủ Nguồn (trừ tiền)
            ContentValues sourcePotUpdate = new ContentValues();
            sourcePotUpdate.put("BALANCE", sourcePot.getBalance() - amount);
            database.update(Constants.TABLE_POTS, sourcePotUpdate, "ID = ?",
                    new String[]{String.valueOf(sourcePotId)});

            // 4. Cập nhật số dư Hủ Đích (cộng tiền)
            ContentValues destinationPotUpdate = new ContentValues();
            destinationPotUpdate.put("BALANCE", destinationPot.getBalance() + amount);
            database.update(Constants.TABLE_POTS, destinationPotUpdate, "ID = ?",
                    new String[]{String.valueOf(destinationPotId)});

            // Đánh dấu transaction thành công
            database.setTransactionSuccessful();
            return true;
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    /**
     * Lấy ID danh mục theo tên mà không đóng DB (dùng trong transaction nội bộ).
     */
    private int getCategoryIdByNameInternal(SQLiteDatabase database, String categoryName) {
        int categoryId = -1;
        Cursor cursor = database.query(
                Constants.TABLE_CATEGORIES,
                new String[]{"ID"},
                "NAME = ?",
                new String[]{categoryName},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                categoryId = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return categoryId;
    }

    // =============================================
    // ========= STATISTICS METHODS ================
    // =============================================

    /**
     * Lấy danh sách giao dịch chi tiêu theo danh mục và tháng/năm cụ thể.
     */
    public List<Transaction> getTransactionsByCategoryAndMonth(int categoryId, int month, int year) {
        String monthPattern = String.format("%04d-%02d", year, month) + "%";
        return getTransactionsWithQuery(
                "SELECT T.*, P.NAME AS POT_NAME, C.NAME AS CAT_NAME, C.ICON AS CAT_ICON " +
                        "FROM " + Constants.TABLE_TRANSACTIONS + " T " +
                        "LEFT JOIN " + Constants.TABLE_POTS + " P ON T.POT_ID = P.ID " +
                        "LEFT JOIN " + Constants.TABLE_CATEGORIES + " C ON T.CATEGORY_ID = C.ID " +
                        "WHERE T.CATEGORY_ID = ? AND T.DATE LIKE ? " +
                        "ORDER BY T.DATE DESC, T.CREATED_AT DESC",
                new String[]{String.valueOf(categoryId), monthPattern});
    }

    /**
     * Lấy tổng chi tiêu nhóm theo danh mục trong một tháng cụ thể.
     * Loại trừ các giao dịch chuyển tiền (cat_transfer_out) để thống kê đúng chi tiêu thực tế.
     *
     * @param month Tháng cần thống kê (1-12)
     * @param year  Năm cần thống kê
     * @return Danh sách CategoryExpense đã sắp xếp giảm dần theo tổng tiền
     */
    public List<CategoryExpense> getExpensesByCategory(int month, int year) {
        List<CategoryExpense> categoryExpenseList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();

        String monthPattern =String.format("%04d-%02d", year, month) + "%";

        // Truy vấn tổng chi tiêu nhóm theo danh mục, loại trừ chuyển tiền
        String query = "SELECT C.ID, C.NAME, C.ICON, SUM(T.AMOUNT) AS TOTAL " +
                "FROM " + Constants.TABLE_TRANSACTIONS + " T " +
                "INNER JOIN " + Constants.TABLE_CATEGORIES + " C ON T.CATEGORY_ID = C.ID " +
                "WHERE T.TYPE = ? AND T.DATE LIKE ? AND C.NAME != ? " +
                "GROUP BY C.ID, C.NAME, C.ICON " +
                "ORDER BY TOTAL DESC";

        Cursor cursor = database.rawQuery(query,
                new String[]{Constants.TYPE_EXPENSE, monthPattern, Constants.CAT_TRANSFER_OUT});

        // Tính tổng chi tiêu để tính phần trăm cho từng danh mục
        double totalExpense = 0;
        try {
            if (cursor.moveToFirst()) {
                do {
                    CategoryExpense categoryExpense = new CategoryExpense();
                    categoryExpense.setCategoryId(cursor.getInt(0));
                    categoryExpense.setCategoryName(cursor.getString(1));
                    categoryExpense.setCategoryIcon(cursor.getString(2));
                    categoryExpense.setTotalAmount(cursor.getDouble(3));
                    categoryExpenseList.add(categoryExpense);
                    totalExpense += cursor.getDouble(3);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            database.close();
        }

        // Tính phần trăm cho từng danh mục dựa trên tổng chi tiêu
        if (totalExpense > 0) {
            for (CategoryExpense categoryExpense : categoryExpenseList) {
                float percentage = (float) ((categoryExpense.getTotalAmount() / totalExpense) * 100);
                categoryExpense.setPercentage(percentage);
            }
        }

        return categoryExpenseList;
    }

    /**
     * Lấy ngày giao dịch sớm nhất trong toàn bộ cơ sở dữ liệu.
     * Trả về null nếu chưa có giao dịch nào.
     */
    public String getEarliestTransactionDate() {
        SQLiteDatabase database = getReadableDatabase();
        String earliestDate = null;
        Cursor cursor = database.rawQuery(
                "SELECT MIN(DATE) FROM " + Constants.TABLE_TRANSACTIONS, null);
        try {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                earliestDate = cursor.getString(0);
            }
        } finally {
            cursor.close();
            database.close();
        }
        return earliestDate;
    }

    /**
     * Lấy ngày giao dịch muộn nhất trong toàn bộ cơ sở dữ liệu.
     * Trả về null nếu chưa có giao dịch nào.
     */
    public String getLatestTransactionDate() {
        SQLiteDatabase database = getReadableDatabase();
        String latestDate = null;
        Cursor cursor = database.rawQuery(
                "SELECT MAX(DATE) FROM " + Constants.TABLE_TRANSACTIONS, null);
        try {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                latestDate = cursor.getString(0);
            }
        } finally {
            cursor.close();
            database.close();
        }
        return latestDate;
    }

    /**
     * Lấy danh sách các tháng/năm có giao dịch (không trùng lặp).
     * Trả về List<String> dạng "yyyy-MM", sắp xếp giảm dần (mới nhất trước).
     */
    public List<String> getDistinctTransactionMonths() {
        List<String> months = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT DISTINCT SUBSTR(DATE, 1, 7) AS MONTH_YEAR " +
                        "FROM " + Constants.TABLE_TRANSACTIONS +
                        " ORDER BY MONTH_YEAR DESC", null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    months.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            database.close();
        }
        return months;
    }

    // =============================================
    // ========= SAVINGS GOAL METHODS =============
    // =============================================

    /**
     * Thêm mục tiêu tiết kiệm mới (độc lập, không gắn Pot).
     *
     * @param goal Đối tượng SavingsGoal cần lưu
     * @return ID của mục tiêu vừa tạo
     */
    public long addSavingsGoal(SavingsGoal goal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", goal.getName());
        values.put("TARGET_AMOUNT", goal.getTargetAmount());
        values.put("CURRENT_AMOUNT", goal.getCurrentAmount());
        values.put("TARGET_DATE", goal.getTargetDate());
        values.put("ICON", goal.getIcon());
        values.put("COLOR", goal.getColor());
        values.put("CREATED_AT", DateUtils.getNowDB());

        long id = db.insert(Constants.TABLE_SAVINGS_GOALS, null, values);
        db.close();
        return id;
    }

    /**
     * Lấy tất cả mục tiêu tiết kiệm, sắp xếp theo ngày tạo (mới nhất trước).
     */
    public List<SavingsGoal> getAllSavingsGoals() {
        List<SavingsGoal> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_SAVINGS_GOALS, null, null, null,
                null, null, "CREATED_AT DESC");
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToSavingsGoal(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    /**
     * Lấy mục tiêu tiết kiệm theo ID.
     */
    public SavingsGoal getSavingsGoalById(int goalId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_SAVINGS_GOALS, null,
                "ID = ?", new String[]{String.valueOf(goalId)},
                null, null, null);
        SavingsGoal goal = null;
        try {
            if (cursor.moveToFirst()) {
                goal = cursorToSavingsGoal(cursor);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return goal;
    }

    /**
     * Cập nhật thông tin mục tiêu tiết kiệm.
     */
    public void updateSavingsGoal(SavingsGoal goal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", goal.getName());
        values.put("TARGET_AMOUNT", goal.getTargetAmount());
        values.put("TARGET_DATE", goal.getTargetDate());
        values.put("ICON", goal.getIcon());
        values.put("COLOR", goal.getColor());
        db.update(Constants.TABLE_SAVINGS_GOALS, values,
                "ID = ?", new String[]{String.valueOf(goal.getId())});
        db.close();
    }

    /**
     * Xóa mục tiêu tiết kiệm và tất cả lịch sử đóng góp của nó.
     */
    public void deleteSavingsGoal(int goalId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Xóa lịch sử đóng góp trước (foreign key)
            db.delete(Constants.TABLE_GOAL_CONTRIBUTIONS, "GOAL_ID = ?",
                    new String[]{String.valueOf(goalId)});
            // Xóa mục tiêu
            db.delete(Constants.TABLE_SAVINGS_GOALS, "ID = ?",
                    new String[]{String.valueOf(goalId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private SavingsGoal cursorToSavingsGoal(Cursor cursor) {
        SavingsGoal goal = new SavingsGoal();
        goal.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
        goal.setName(cursor.getString(cursor.getColumnIndexOrThrow("NAME")));
        goal.setTargetAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("TARGET_AMOUNT")));
        goal.setCurrentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("CURRENT_AMOUNT")));
        goal.setTargetDate(cursor.getString(cursor.getColumnIndexOrThrow("TARGET_DATE")));
        goal.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("ICON")));
        goal.setColor(cursor.getString(cursor.getColumnIndexOrThrow("COLOR")));
        goal.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("CREATED_AT")));
        return goal;
    }

    // =============================================
    // ======= GOAL CONTRIBUTION METHODS ===========
    // =============================================

    /**
     * Đóng góp tiền vào mục tiêu tiết kiệm.
     * Sử dụng SQLite transaction để đảm bảo tính toàn vẹn:
     * 1. Thêm bản ghi đóng góp vào GOAL_CONTRIBUTIONS
     * 2. Cộng số tiền vào CURRENT_AMOUNT của SAVINGS_GOALS
     *
     * @param contribution Đối tượng GoalContribution
     * @return ID của bản ghi đóng góp
     */
    public long addGoalContribution(GoalContribution contribution) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long id = -1;
        try {
            // 1. Thêm bản ghi đóng góp
            ContentValues values = new ContentValues();
            values.put("GOAL_ID", contribution.getGoalId());
            values.put("AMOUNT", contribution.getAmount());
            values.put("NOTE", contribution.getNote());
            values.put("DATE", contribution.getDate());
            values.put("CREATED_AT", DateUtils.getNowDB());
            id = db.insert(Constants.TABLE_GOAL_CONTRIBUTIONS, null, values);

            // 2. Cộng vào CURRENT_AMOUNT
            db.execSQL("UPDATE " + Constants.TABLE_SAVINGS_GOALS +
                            " SET CURRENT_AMOUNT = CURRENT_AMOUNT + ? WHERE ID = ?",
                    new Object[]{contribution.getAmount(), contribution.getGoalId()});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return id;
    }

    /**
     * Lấy lịch sử đóng góp cho một mục tiêu, mới nhất trước.
     */
    public List<GoalContribution> getContributionsByGoalId(int goalId) {
        List<GoalContribution> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_GOAL_CONTRIBUTIONS, null,
                "GOAL_ID = ?", new String[]{String.valueOf(goalId)},
                null, null, "DATE DESC, CREATED_AT DESC");
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToGoalContribution(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    /**
     * Xóa một bản ghi đóng góp và trừ lại CURRENT_AMOUNT.
     */
    public void deleteGoalContribution(int contributionId, int goalId, double amount) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(Constants.TABLE_GOAL_CONTRIBUTIONS, "ID = ?",
                    new String[]{String.valueOf(contributionId)});

            db.execSQL("UPDATE " + Constants.TABLE_SAVINGS_GOALS +
                            " SET CURRENT_AMOUNT = MAX(0, CURRENT_AMOUNT - ?) WHERE ID = ?",
                    new Object[]{amount, goalId});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private GoalContribution cursorToGoalContribution(Cursor cursor) {
        GoalContribution c = new GoalContribution();
        c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
        c.setGoalId(cursor.getInt(cursor.getColumnIndexOrThrow("GOAL_ID")));
        c.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("AMOUNT")));
        c.setNote(cursor.getString(cursor.getColumnIndexOrThrow("NOTE")));
        c.setDate(cursor.getString(cursor.getColumnIndexOrThrow("DATE")));
        c.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("CREATED_AT")));
        return c;
    }

    // =============================================
    // ===== RECURRING TRANSACTION METHODS =========
    // =============================================

    /**
     * Thêm giao dịch định kỳ mới.
     *
     * @param recurring Đối tượng RecurringTransaction
     * @return ID của giao dịch định kỳ vừa tạo
     */
    public long addRecurringTransaction(RecurringTransaction recurring) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("POT_ID", recurring.getPotId());
        values.put("CATEGORY_ID", recurring.getCategoryId());
        values.put("AMOUNT", recurring.getAmount());
        values.put("TYPE", recurring.getType());
        values.put("NOTE", recurring.getNote());
        values.put("FREQUENCY", recurring.getFrequency());
        values.put("NEXT_DATE", recurring.getNextDate());
        values.put("IS_ACTIVE", recurring.isActive() ? 1 : 0);
        values.put("CREATED_AT", DateUtils.getNowDB());

        long id = db.insert(Constants.TABLE_RECURRING_TRANSACTIONS, null, values);
        db.close();
        return id;
    }

    /**
     * Lấy tất cả giao dịch định kỳ đang hoạt động, kèm thông tin Pot và Category.
     */
    public List<RecurringTransaction> getAllActiveRecurringTransactions() {
        List<RecurringTransaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT R.*, P.NAME AS POT_NAME, C.NAME AS CAT_NAME, C.ICON AS CAT_ICON " +
                "FROM " + Constants.TABLE_RECURRING_TRANSACTIONS + " R " +
                "LEFT JOIN " + Constants.TABLE_POTS + " P ON R.POT_ID = P.ID " +
                "LEFT JOIN " + Constants.TABLE_CATEGORIES + " C ON R.CATEGORY_ID = C.ID " +
                "WHERE R.IS_ACTIVE = 1 " +
                "ORDER BY R.NEXT_DATE ASC";
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToRecurringTransaction(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    /**
     * Lấy các giao dịch định kỳ đến hạn (NEXT_DATE <= today).
     * Được gọi bởi RecurringTransactionWorker hàng ngày.
     *
     * @param todayDate Ngày hôm nay (yyyy-MM-dd)
     * @return Danh sách giao dịch cần được thực hiện
     */
    public List<RecurringTransaction> getDueRecurringTransactions(String todayDate) {
        List<RecurringTransaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT R.*, P.NAME AS POT_NAME, C.NAME AS CAT_NAME, C.ICON AS CAT_ICON " +
                "FROM " + Constants.TABLE_RECURRING_TRANSACTIONS + " R " +
                "LEFT JOIN " + Constants.TABLE_POTS + " P ON R.POT_ID = P.ID " +
                "LEFT JOIN " + Constants.TABLE_CATEGORIES + " C ON R.CATEGORY_ID = C.ID " +
                "WHERE R.IS_ACTIVE = 1 AND R.NEXT_DATE <= ? " +
                "ORDER BY R.NEXT_DATE ASC";
        Cursor cursor = db.rawQuery(query, new String[]{todayDate});
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToRecurringTransaction(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    /**
     * Cập nhật ngày lặp tiếp theo cho giao dịch định kỳ.
     *
     * @param recurringId ID giao dịch định kỳ
     * @param nextDate    Ngày lặp tiếp theo (yyyy-MM-dd)
     */
    public void updateRecurringNextDate(int recurringId, String nextDate) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NEXT_DATE", nextDate);
        db.update(Constants.TABLE_RECURRING_TRANSACTIONS, values,
                "ID = ?", new String[]{String.valueOf(recurringId)});
        db.close();
    }

    /**
     * Dừng (deactivate) giao dịch định kỳ thay vì xóa hẳn.
     */
    public void deactivateRecurringTransaction(int recurringId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("IS_ACTIVE", 0);
        db.update(Constants.TABLE_RECURRING_TRANSACTIONS, values,
                "ID = ?", new String[]{String.valueOf(recurringId)});
        db.close();
    }

    /**
     * Xóa vĩnh viễn giao dịch định kỳ.
     */
    public void deleteRecurringTransaction(int recurringId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Constants.TABLE_RECURRING_TRANSACTIONS, "ID = ?",
                new String[]{String.valueOf(recurringId)});
        db.close();
    }

    private RecurringTransaction cursorToRecurringTransaction(Cursor cursor) {
        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
        recurring.setPotId(cursor.getInt(cursor.getColumnIndexOrThrow("POT_ID")));
        recurring.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("AMOUNT")));
        recurring.setType(cursor.getString(cursor.getColumnIndexOrThrow("TYPE")));
        recurring.setNote(cursor.getString(cursor.getColumnIndexOrThrow("NOTE")));
        recurring.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow("FREQUENCY")));
        recurring.setNextDate(cursor.getString(cursor.getColumnIndexOrThrow("NEXT_DATE")));
        recurring.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("IS_ACTIVE")) == 1);
        recurring.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("CREATED_AT")));

        // Category ID có thể null
        int catIdIndex = cursor.getColumnIndex("CATEGORY_ID");
        if (catIdIndex >= 0 && !cursor.isNull(catIdIndex)) {
            recurring.setCategoryId(cursor.getInt(catIdIndex));
        }

        // Transient fields từ JOIN
        int potNameIndex = cursor.getColumnIndex("POT_NAME");
        if (potNameIndex >= 0) {
            recurring.setPotName(cursor.getString(potNameIndex));
        }
        int catNameIndex = cursor.getColumnIndex("CAT_NAME");
        if (catNameIndex >= 0) {
            recurring.setCategoryName(cursor.getString(catNameIndex));
        }
        int catIconIndex = cursor.getColumnIndex("CAT_ICON");
        if (catIconIndex >= 0) {
            recurring.setCategoryIcon(cursor.getString(catIconIndex));
        }
        return recurring;
    }
}
