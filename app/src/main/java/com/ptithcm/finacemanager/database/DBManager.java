package com.ptithcm.finacemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ptithcm.finacemanager.model.Category;
import com.ptithcm.finacemanager.model.CategoryExpense;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.BuildConfig;
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
        if (BuildConfig.DEBUG) {
            seedPotsAndTransactions(db);
        }
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
        String nowTimestamp = DateUtils.getNowDB();
        String todayDate = DateUtils.getTodayDB();

        // 1. Thêm các hũ mẫu (Pots)
        // Hũ Thiết Yếu (ID: 1)
        ContentValues potNecessities = new ContentValues();
        potNecessities.put("NAME", "Chi tiêu thiết yếu");
        potNecessities.put("BALANCE", 5500000.0);
        potNecessities.put("BUDGET_LIMIT", 10000000.0);
        potNecessities.put("COLOR", "#4CAF50");
        potNecessities.put("ICON", "🍜");
        potNecessities.put("CREATED_AT", nowTimestamp);
        potNecessities.put("IS_ACTIVE", 1);
        long potNecessitiesId = db.insert(Constants.TABLE_POTS, null, potNecessities);

        // Hũ Tiết Kiệm (ID: 2)
        ContentValues potSavings = new ContentValues();
        potSavings.put("NAME", "Tích lũy & Tiết kiệm");
        potSavings.put("BALANCE", 3000000.0);
        potSavings.put("BUDGET_LIMIT", 5000000.0);
        potSavings.put("COLOR", "#2196F3");
        potSavings.put("ICON", "💰");
        potSavings.put("CREATED_AT", nowTimestamp);
        potSavings.put("IS_ACTIVE", 1);
        long potSavingsId = db.insert(Constants.TABLE_POTS, null, potSavings);

        // Hũ Giáo Dục (ID: 3)
        ContentValues potEducation = new ContentValues();
        potEducation.put("NAME", "Học tập & Phát triển");
        potEducation.put("BALANCE", 1500000.0);
        potEducation.put("BUDGET_LIMIT", 3000000.0);
        potEducation.put("COLOR", "#FF9800");
        potEducation.put("ICON", "📚");
        potEducation.put("CREATED_AT", nowTimestamp);
        potEducation.put("IS_ACTIVE", 1);
        long potEducationId = db.insert(Constants.TABLE_POTS, null, potEducation);

        // Hũ Hưởng Thụ (ID: 4)
        ContentValues potPlay = new ContentValues();
        potPlay.put("NAME", "Giải trí & Hưởng thụ");
        potPlay.put("BALANCE", 800000.0);
        potPlay.put("BUDGET_LIMIT", 2000000.0);
        potPlay.put("COLOR", "#9C27B0");
        potPlay.put("ICON", "🎮");
        potPlay.put("CREATED_AT", nowTimestamp);
        potPlay.put("IS_ACTIVE", 1);
        long potPlayId = db.insert(Constants.TABLE_POTS, null, potPlay);

        // 2. Lấy ID danh mục mẫu để liên kết giao dịch
        int categoryFoodId = getCategoryIdByNameInternal(db, "cat_food");
        int categorySalaryId = getCategoryIdByNameInternal(db, "cat_salary");
        int categoryEducationId = getCategoryIdByNameInternal(db, "cat_education");
        int categoryEntertainmentId = getCategoryIdByNameInternal(db, "cat_entertainment");
        int categoryTransferOutId = getCategoryIdByNameInternal(db, Constants.CAT_TRANSFER_OUT);
        int categoryTransferInId = getCategoryIdByNameInternal(db, Constants.CAT_TRANSFER_IN);

        // 3. Thêm giao dịch mẫu (Transactions)
        // Giao dịch 1: Thu nhập lương vào hũ Thiết Yếu
        ContentValues transSalary = new ContentValues();
        transSalary.put("POT_ID", potNecessitiesId);
        transSalary.put("CATEGORY_ID", categorySalaryId);
        transSalary.put("AMOUNT", 8000000.0);
        transSalary.put("TYPE", Constants.TYPE_INCOME);
        transSalary.put("DATE", todayDate);
        transSalary.put("NOTE", "Nhận lương tháng này");
        transSalary.put("CREATED_AT", nowTimestamp);
        db.insert(Constants.TABLE_TRANSACTIONS, null, transSalary);

        // Giao dịch 2: Tiền ăn uống hàng ngày từ hũ Thiết Yếu
        ContentValues transFood = new ContentValues();
        transFood.put("POT_ID", potNecessitiesId);
        transFood.put("CATEGORY_ID", categoryFoodId);
        transFood.put("AMOUNT", 500000.0);
        transFood.put("TYPE", Constants.TYPE_EXPENSE);
        transFood.put("DATE", todayDate);
        transFood.put("NOTE", "Đi chợ và mua sắm thực phẩm tuần");
        transFood.put("CREATED_AT", nowTimestamp);
        db.insert(Constants.TABLE_TRANSACTIONS, null, transFood);

        // Giao dịch 3: Tiền học khóa học online từ hũ Giáo Dục
        ContentValues transBook = new ContentValues();
        transBook.put("POT_ID", potEducationId);
        transBook.put("CATEGORY_ID", categoryEducationId);
        transBook.put("AMOUNT", 500000.0);
        transBook.put("TYPE", Constants.TYPE_EXPENSE);
        transBook.put("DATE", todayDate);
        transBook.put("NOTE", "Mua khóa học lập trình Android");
        transBook.put("CREATED_AT", nowTimestamp);
        db.insert(Constants.TABLE_TRANSACTIONS, null, transBook);

        // Giao dịch 4: Mua game từ hũ Hưởng Thụ
        ContentValues transGame = new ContentValues();
        transGame.put("POT_ID", potPlayId);
        transGame.put("CATEGORY_ID", categoryEntertainmentId);
        transGame.put("AMOUNT", 200000.0);
        transGame.put("TYPE", Constants.TYPE_EXPENSE);
        transGame.put("DATE", todayDate);
        transGame.put("NOTE", "Mua game giải trí cuối tuần");
        transGame.put("CREATED_AT", nowTimestamp);
        db.insert(Constants.TABLE_TRANSACTIONS, null, transGame);

        // Giao dịch 5 & 6: Chuyển khoản mẫu (Chuyển 2.000.000đ từ Thiết yếu sang Tiết kiệm)
        ContentValues transTransferOut = new ContentValues();
        transTransferOut.put("POT_ID", potNecessitiesId);
        transTransferOut.put("CATEGORY_ID", categoryTransferOutId);
        transTransferOut.put("AMOUNT", 2000000.0);
        transTransferOut.put("TYPE", Constants.TYPE_EXPENSE);
        transTransferOut.put("DATE", todayDate);
        transTransferOut.put("NOTE", "Trích tiền tiết kiệm hàng tháng");
        transTransferOut.put("CREATED_AT", nowTimestamp);
        db.insert(Constants.TABLE_TRANSACTIONS, null, transTransferOut);

        ContentValues transTransferIn = new ContentValues();
        transTransferIn.put("POT_ID", potSavingsId);
        transTransferIn.put("CATEGORY_ID", categoryTransferInId);
        transTransferIn.put("AMOUNT", 2000000.0);
        transTransferIn.put("TYPE", Constants.TYPE_INCOME);
        transTransferIn.put("DATE", todayDate);
        transTransferIn.put("NOTE", "Trích tiền tiết kiệm hàng tháng");
        transTransferIn.put("CREATED_AT", nowTimestamp);
        db.insert(Constants.TABLE_TRANSACTIONS, null, transTransferIn);
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
}
