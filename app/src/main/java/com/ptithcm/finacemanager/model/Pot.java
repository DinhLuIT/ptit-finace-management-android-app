package com.ptithcm.finacemanager.model;

public class Pot {
    private int id;
    private String name;
    private double balance;
    private double budgetLimit;

    public Pot() {
    }

    public Pot(int id, String name, double balance, double budgetLimit) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.budgetLimit = budgetLimit;
    }

    public Pot(String name, double balance, double budgetLimit) {
        this.name = name;
        this.balance = balance;
        this.budgetLimit = budgetLimit;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }
}
