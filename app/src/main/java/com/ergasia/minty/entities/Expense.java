package com.ergasia.minty.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "expenses", foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
))
public class Expense {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id", index = true)
    public int userId;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "note")
    public String note;

    public double amount;

    @ColumnInfo(name = "date")
    public Date date;


    public Expense(int id, int userId, String category, String note, double amount, Date date) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.note = note;
        this.amount = amount;
        this.date = date;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    @NonNull
    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", userId=" + userId +
                ", category='" + category + '\'' +
                ", note='" + note + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}
