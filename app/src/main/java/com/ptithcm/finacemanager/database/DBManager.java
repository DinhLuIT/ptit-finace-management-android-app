package com.ptithcm.finacemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DBManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "finance_manager.sqlite";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_POTS = "CREATE TABLE POTS (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NAME TEXT NOT NULL, " +
            "BALANCE REAL DEFAULT 0, " +
            "BUDGET_LIMIT REAL NOT NULL)";

    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE TRANSACTIONS (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "POT_ID INTEGER NOT NULL, " +
            "AMOUNT REAL NOT NULL, " +
            "TYPE TEXT NOT NULL, " +
            "DATE TEXT NOT NULL, " +
            "NOTE TEXT, " +
            "FOREIGN KEY(POT_ID) REFERENCES POTS(ID))";

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_POTS);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TRANSACTIONS");
        db.execSQL("DROP TABLE IF EXISTS POTS");
        onCreate(db);
    }

    public void addPot(Pot pot){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("NAME", pot.getName());
        values.put("BALANCE", pot.getBalance());
        values.put("BUDGET_LIMIT", pot.getBudgetLimit());

        db.insert("POTS", null, values);
        db.close();
    }

    public List<Pot> getAllPots(){
        List<Pot> potList = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM POTS", null);

        if(cursor.moveToFirst()){
            do {
                Pot pot = new Pot();
                pot.setId(cursor.getInt(0)); // ID
                pot.setName(cursor.getString(1)); // NAME
                pot.setBalance(cursor.getDouble(2)); // BALANCE
                pot.setBudgetLimit(cursor.getDouble(3)); // BUDGET_LIMIT
                potList.add(pot);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return potList;
    }

    public void addTransaction(Transaction trans){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("POT_ID", trans.getPotId());
        values.put("AMOUNT", trans.getAmount());
        values.put("TYPE", trans.getType());
        values.put("DATE", trans.getDate());
        values.put("NOTE", trans.getNote());

        db.insert("TRANSACTIONS", null, values);

        // nếu thu nhập là INCOME thì + còn EXPENSE thì -
        String operator = trans.getType().equals("INCOME") ? "+" : "-";
        String updateSql = "UPDATE POTS SET BALANCE = BALANCE " + operator + " " + trans.getAmount() +
                " WHERE ID = " + trans.getPotId();
        db.execSQL(updateSql);

        db.close();
    }


}
