package com.ergasia.minty.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

public class Expense {
    private String uid;
    private String title;
    private double amount;
    private String category;
    private long timestamp;

    public Expense() {
    }

    public Expense(String uid, String title, double amount, long timestamp, ExpenseCategory category) {
        this.uid = uid;
        this.title = title;
        this.amount = amount;
        this.timestamp = timestamp;
        this.category = category.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return "Expense{" +
                "uid='" + uid + '\'' +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
