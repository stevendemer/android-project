package com.ergasia.minty.repositories;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class UserRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    private final String TAG = "UserRepository";


    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public Task<DocumentSnapshot> getUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        return db.collection("users").document(currentUser.getUid()).get();
    }

    public Task<Integer> getTransactionsCount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        Query transactionQuery = db.collection("users").document(currentUser.getUid()).collection("transactions");

        return transactionQuery.count().get(AggregateSource.SERVER)
                .continueWith((Continuation<AggregateQuerySnapshot, Integer>) task -> {
                    if (task.isSuccessful()) {
                        return (int) task.getResult().getCount();
                    } else {
                        throw task.getException();
                    }
                });

    }

    public Task<Void> updateUsername(String username) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        if (username == null || username.trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Username cannot be empty"));
        }

        return db.collection("users").document(currentUser.getUid()).update("username", username.trim());
    }


}
