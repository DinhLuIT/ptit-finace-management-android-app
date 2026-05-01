package com.ptithcm.finacemanager.model;

public class Transaction {
    private int id;
    private int potId;
    private double amount;
    private String type; // "INCOME" || "EXPENSE"
    private String date;
    private String note;

    public Transaction() {
    }

    public Transaction(int potId, double amount, String type, String date, String note) {
        this.potId = potId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPotId() { return potId; }
    public void setPotId(int potId) { this.potId = potId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
