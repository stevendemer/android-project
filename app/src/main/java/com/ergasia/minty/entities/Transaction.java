package com.ergasia.minty.entities;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Transaction {

    private String id;
    private double amount;
    private String category; // only used for expenses
    private String userId;
    private Date timestamp;

    private String description;
    private boolean expense;
    private TransactionType transactionType;

    public Transaction() {
    }


    // when the transaction is income
    public Transaction(String id, String userId, double amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = null;
        this.transactionType = TransactionType.INCOME;
        this.timestamp = new Date();
        this.description = null;
    }

    // for expenses
    public Transaction(String id, String userId, double amount, ExpenseCategory category) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category.name(); // store enum as string
        this.transactionType = TransactionType.EXPENSE;
        this.timestamp = new Date();
        this.description = null;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isExpense() {
        return this.transactionType == TransactionType.EXPENSE;
    }

    public boolean isIncome() {
        return this.transactionType == TransactionType.INCOME;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setExpense(boolean expense) {
        this.expense = expense;
    }

    @NonNull
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", userId='" + userId + '\'' +
                ", transactionType=" + transactionType +
                '}';
    }
}
