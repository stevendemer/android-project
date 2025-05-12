package com.ergasia.minty.entities;


import androidx.annotation.NonNull;

import java.util.Map;

public class User {
    private String id;
    private String username;
    private String email;
    private String profileImageUrl;
     private Map<String, Transaction> expenses;
    private double balance;

    public User() {
        // required for firebase
    }

    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.profileImageUrl = "";
    }

    public User(String id, String username, String email, double balance) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.balance = balance;
        this.profileImageUrl = "";
    }

    public User(String id, String username, String email, Map<String, Transaction> expenses, double balance) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.expenses = expenses;
        this.balance = balance;
        this.profileImageUrl = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Map<String, Transaction> getExpenses() {
        return expenses;
    }

    public void setExpenses(Map<String, Transaction> expenses) {
        this.expenses = expenses;
    }

    public double getBalance() {
        return this.balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void removeExpense(Transaction expense) {
        if (this.expenses != null) {
            this.expenses.remove(expense.getId());
        }
    }

    public void addExpense(Transaction expense, String category) {
        if (this.expenses != null) {
            this.expenses.put(category, expense);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", profileImageUrl=" + profileImageUrl + '\'' +
                ", expenses=" + expenses +
                ", balance=" + balance +
                '}';
    }
}
