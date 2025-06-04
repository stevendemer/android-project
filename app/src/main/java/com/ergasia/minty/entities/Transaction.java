package com.ergasia.minty.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.util.Objects;


public class Transaction {

    private String id;
    private double amount;
    private String category; // only used for expenses
    private String userId;
    private Timestamp timestamp;
    private String description;
    private TransactionType transactionType;

    public Transaction() {
    }


    // when the transaction is income
    public Transaction(String id, String userId, double amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.transactionType = TransactionType.INCOME;
        this.description = null;
        this.timestamp = null;
        this.category = null;
    }

    // for expenses
    public Transaction(String id, String userId, double amount, ExpenseCategory category) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category.name(); // store enum as string
        this.transactionType = TransactionType.EXPENSE;
        this.description = null;
        this.timestamp = null;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isExpense() {
        return !("income".equalsIgnoreCase(category));
    }

    public boolean isIncome() {
        return "income".equalsIgnoreCase(category);
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
        return category != null ? category : TransactionType.INCOME.name();
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Transaction that = (Transaction) obj;
        return id.equals(that.id) && userId.equals(that.userId) && Objects.equals(category, that.category) && Objects.equals(timestamp, that.timestamp) && amount == that.amount;
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
