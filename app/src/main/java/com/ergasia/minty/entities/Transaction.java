package com.ergasia.minty.entities;

import androidx.annotation.NonNull;

import java.util.Date;

public class Transaction {

    private String id;
    private String title;
    private double amount;
    private String category; // only used for expenses
    private String userId;
    private Date timestamp;
    private TransactionType transactionType;

    public Transaction() {
    }


    // when the transaction is income
    public Transaction(String id, String userId, String title,  double amount, Date timestamp) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.timestamp = timestamp;
        this.category = null;
        this.transactionType = TransactionType.INCOME;
    }

    // for expenses
    public Transaction(String id, String userId, String title, double amount, Date timestamp, ExpenseCategory category) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.category = category.name(); // store enum as string
        this.transactionType = TransactionType.EXPENSE;
        this.timestamp = timestamp;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @NonNull
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                ", transactionType=" + transactionType +
                '}';
    }
}
