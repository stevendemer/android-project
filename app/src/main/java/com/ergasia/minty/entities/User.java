package com.ergasia.minty.entities;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Map;

public class User {


    private String uid;
    private String username;
    private String email;
    private String profileImageUrl;
    private Map<String, Expense> expenses;
    private double income;


    public User() {
        // required for firebase
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profileImageUrl = "";
    }

    public User(String uid, String username, String email, double income) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.income = income;
        this.profileImageUrl = "";
    }

    public User(String uid, String username, String email, double income, Map<String, Expense> expenses) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.expenses = expenses;
        this.income = income;
        this.profileImageUrl = "";
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Map<String, Expense> expenses) {
        this.expenses = expenses;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public void removeExpense(Expense expense) {
        if (this.expenses != null) {
            this.expenses.remove(expense.getUid());
        }
    }

    public void addExpense(Expense expense, String category) {
        if (this.expenses != null) {
            this.expenses.put(category, expense);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", profileImageUrl=" + profileImageUrl + '\'' +
                ", expenses=" + expenses +
                ", income=" + income +
                '}';
    }
}
