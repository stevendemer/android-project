package com.ergasia.minty.entities.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ergasia.minty.entities.Expense;
import com.ergasia.minty.entities.ExpenseCategory;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Query("SELECT * FROM expenses")
    List<Expense> getAll();
    @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC")
    List<Expense> getExpensesFromUser(int userId);

    @Query("SELECT SUM(amount) FROM expenses WHERE user_id = :userId")
    double getTotalExpensesFromUser(int userId);

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    Expense getExpenseById(int expenseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Expense... expenses);

    @Delete
    void delete(Expense expense);

    @Update
    void updateExpenses(Expense... expenses);
}
