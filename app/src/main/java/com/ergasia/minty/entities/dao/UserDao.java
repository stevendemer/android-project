package com.ergasia.minty.entities.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ergasia.minty.entities.User;

import java.util.List;

import io.reactivex.Completable;

/**
 * Asynchronous DAO methods using RxJava
 * One-shot queries are database operations that only run once and grab a snapshot
 * of data at the time of execution
 */
@Dao
public interface UserDao {

    @Query("SELECT * FROM users")
    public List<User> getAll();

    @Query("SELECT * FROM users WHERE id = :userId")
    public User findUserById(int userId);

    @Query("SELECT * FROM users WHERE username LIKE :username")
    public User findByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertAll(List<User> users);

    @Delete
    public Completable deleteUsers(List<User> user);

    @Update
    public Completable updateUsers(List<User> users);

    @Insert
    void registerUser(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

}
