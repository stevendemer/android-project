package com.ergasia.minty.views;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ergasia.minty.entities.ExpenseCategory;
import com.ergasia.minty.entities.Transaction;
import com.ergasia.minty.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private ListenerRegistration userListener;
    private ListenerRegistration transactionsListener;

    private final String TAG = "HomeViewModel";

    public HomeViewModel() {
        loadTransactions();
        loadUserProfile();
    }

    public LiveData<List<Transaction>> getTransactions() {
        return this.transactions;
    }

    public LiveData<User> getUserProfile() {
        return this.user;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    private void loadTransactions() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        transactionsListener = db.collection("users").document(currentUser.getUid()).collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        transactions.setValue(new ArrayList<>());
                        return;
                    }

                    List<Transaction> transactionList = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Transaction transaction = doc.toObject(Transaction.class);

                        if (transaction != null) {
                            transaction.setId(doc.getId());
                            transactionList.add(transaction);
                        }
                    }
                    transactions.setValue(transactionList);
                });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        userListener = db.collection("users").document(currentUser.getUid()).addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists()) return;

            User userObj = snapshot.toObject(User.class);
            user.setValue(userObj);
        });
    }

    private ExpenseCategory mapExpenseCategory(String category) {
        if (category == null) return ExpenseCategory.OTHER;

        switch (category.toLowerCase().trim()) {
            case "education":
                return ExpenseCategory.EDUCATION;
            case "health":
                return ExpenseCategory.HEALTH;
            case "groceries":
                return ExpenseCategory.GROCERIES;
            case "entertainment":
                return ExpenseCategory.ENTERTAINMENT;
            case "transportation":
                return ExpenseCategory.TRANSPORTATION;
            default:
                return ExpenseCategory.OTHER;
        }
    }


    public void storeTransaction(double amount, boolean isIncome, String selectedCategory) {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in.");
            return;
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());


        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            Double currentBalance = documentSnapshot.getDouble("balance");
            if (currentBalance == null) currentBalance = 0.0;

            if (!isIncome && amount > currentBalance) {
                errorLiveData.setValue("Insufficient funds.");
                return;
            }

            double newBalance = isIncome ? currentBalance + amount : currentBalance - amount;

            userDocRef.update("balance", newBalance).addOnSuccessListener(task -> Log.d(TAG, "Balance updated")).addOnFailureListener(e -> Log.e(TAG, "Balance update failed", e));

            Transaction transaction = isIncome ? new Transaction(UUID.randomUUID().toString(), currentUser.getUid(), amount, ExpenseCategory.INCOME) : new Transaction(UUID.randomUUID().toString(), currentUser.getUid(), amount, mapExpenseCategory(selectedCategory));

            Map<String, Object> txMap = new HashMap<>();
            txMap.put("id", transaction.getId());
            txMap.put("userId", transaction.getUserId());
            txMap.put("amount", transaction.getAmount());
            txMap.put("category", transaction.getCategory());
            txMap.put("timestamp", FieldValue.serverTimestamp());

            DocumentReference txRef = db.collection("users").document(currentUser.getUid()).collection("transactions").document(transaction.getId());

            db.collection("users").document(currentUser.getUid()).collection("transactions").document(transaction.getId()).set(txMap).addOnSuccessListener(v -> {
                Log.i(TAG, "Transaction added");


            }).addOnFailureListener(e -> {
                Log.e(TAG, "Transaction failed ", e);
                errorLiveData.setValue("Transaction failed!");
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load user data");
            errorLiveData.setValue("Failed to load user data.");
        });
    }

    public LiveData<Boolean> deleteTransaction(String transactionId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            result.setValue(false);
            return result;
        }

        String userId = currentUser.getUid();
        DocumentReference txRef = db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId);

        txRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                result.setValue(false);
                return;
            }

            Transaction transaction = task.getResult().toObject(Transaction.class);
            if (transaction == null) {
                result.setValue(false);
                return;
            }

            double amount = transaction.getAmount();
            boolean isIncome = "INCOME".equalsIgnoreCase(transaction.getCategory());
            double balanceChange = isIncome ? -amount : amount;

            // Create batch for atomic operations
            WriteBatch batch = db.batch();

            DocumentReference userRef = db.collection("users").document(userId);
            batch.update(userRef, "balance", FieldValue.increment(balanceChange));
            batch.delete(txRef);

            batch.commit()
                    .addOnSuccessListener(unused -> {
                        // Optionally refresh transactions and balance
                        getTransactions();
                        getUserProfile();
                        result.setValue(true);
                    })
                    .addOnFailureListener(e -> {
                        result.setValue(false);
                    });
        });

        return result;
    }


    @Override
    protected void onCleared() {
        super.onCleared();

        if (userListener != null) userListener.remove();
        if (transactionsListener != null) transactionsListener.remove();
    }
}
