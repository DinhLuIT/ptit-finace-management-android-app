package com.ptithcm.finacemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ptithcm.finacemanager.model.Category;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.DateUtils;

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
        seedCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Trong giai đoạn dev, drop và tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_POTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USER_SETTINGS);
        onCreate(db);
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
                {"cat_other", "ic_other", "BOTH"}
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
        Category cat = new Category();
        cat.setId(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
        cat.setName(cursor.getString(cursor.getColumnIndexOrThrow("NAME")));
        cat.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("ICON")));
        cat.setType(cursor.getString(cursor.getColumnIndexOrThrow("TYPE")));
        cat.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow("IS_DEFAULT")) == 1);
        return cat;
    }
}
