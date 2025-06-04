package com.ergasia.minty.views;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ergasia.minty.repositories.UserRepository;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;

    private final MutableLiveData<Integer> transactionsCount = new MutableLiveData<>();
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();

    private final MutableLiveData<Double> balance = new MutableLiveData<>();

    private final String TAG = "UserViewModel";
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public UserViewModel() {
        this.userRepository = new UserRepository();
        getProfile();
    }

    public LiveData<String> getUsername() {
        return username;
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Integer> getTransactionsCount() {
        return transactionsCount;
    }

    public MutableLiveData<Double> getBalance() {
        return balance;
    }

    public void updateUsername(String newUsername) {
        userRepository.updateUsername(newUsername).addOnSuccessListener(aVoid -> {
            errorLiveData.setValue(null);
            username.setValue(newUsername);

        }).addOnFailureListener(e -> {
            errorLiveData.setValue(e.getMessage());
            Log.e(TAG, "Failed to update profile", e);
        });
    }

    private void loadTransactionsCount() {
        this.userRepository.getTransactionsCount().addOnSuccessListener(transactionsCount::setValue).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load transactions count");
            transactionsCount.setValue(0);
        });
    }

    public void getProfile() {
        userRepository.getUserProfile().addOnSuccessListener(documentSnapshot -> {
            errorLiveData.setValue(null);
            username.setValue(documentSnapshot.getString("username"));
            email.setValue(documentSnapshot.getString("email"));
            balance.setValue(documentSnapshot.getDouble("balance"));
            loadTransactionsCount();
        }).addOnFailureListener(e -> {
            errorLiveData.setValue(e.getMessage());
            Log.e(TAG, "Failed to get profile");
        });
    }
}
