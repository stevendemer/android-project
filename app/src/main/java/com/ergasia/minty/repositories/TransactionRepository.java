package com.ergasia.minty.repositories;

import android.util.Log;

import com.ergasia.minty.entities.ExpenseCategory;
import com.ergasia.minty.entities.Transaction;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class TransactionRepository {

    private final String TAG = "TransactionRepository";
    private final FirebaseFirestore db;
    private final CollectionReference transactionsRef;
    private final FirebaseAuth mAuth;

    public TransactionRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();

        this.transactionsRef = db.collection("users").document(Objects.requireNonNull(mAuth.getUid())).collection("transactions");
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


    public Task<List<Transaction>> getTransactions(int limit, String field) {

        Query query = transactionsRef.orderBy(field, Query.Direction.DESCENDING);

        if (limit != 0) {
            query = query.limit(limit);
        }

        return transactionsRef.get().continueWith(task -> {
            if (task.isSuccessful()) {
                Log.w(TAG, "Transactions fetched");
                List<Transaction> transactions = task.getResult().toObjects(Transaction.class);
                Log.d(TAG, "Added " + transactions.size() + " transactions");

                return transactions;
            } else {
                Log.e(TAG, "Error getting transactions ", task.getException());
                throw task.getException();
            }
        });
    }

    public Task<List<Transaction>> getFilteredTransactions(String category) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        return db.collection("users").document(currentUser.getUid()).collection("transactions").whereEqualTo("category", category.toUpperCase().trim()).orderBy("timestamp", Query.Direction.DESCENDING).get().continueWith(task -> {
            if (task.isSuccessful()) {
                List<Transaction> transactions = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    Transaction t = doc.toObject(Transaction.class);
                    if (t != null) {
                        t.setId(doc.getId());
                        transactions.add(t);
                    }
                }
                return transactions;
            } else {
                throw task.getException();
            }
        });
    }

    public Task<List<Transaction>> getSortedTransactions(String field, boolean ascending) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        List<String> validFields = Arrays.asList("amount", "timestamp", "category");

        if (!validFields.contains(field)) {
            field = "timestamp"; // default to timestamp
        }

        Query.Direction direction = ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;

        return db.collection("users").document(currentUser.getUid()).collection("transactions").orderBy(field, direction).get().continueWith(task -> {
            if (task.isSuccessful()) {
                List<Transaction> transactions = new ArrayList<>();

                for (DocumentSnapshot doc : task.getResult()) {
                    Transaction transaction = doc.toObject(Transaction.class);
                    if (transaction != null) {
                        transaction.setId(doc.getId());
                        transactions.add(transaction);
                    }
                }
                return transactions;
            } else {
                throw task.getException();
            }

        });
    }

    public Task<Void> storeTransaction(double amount, boolean isIncome, String selectedCategory) {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        if (amount <= 0) {
            return Tasks.forException(new IllegalArgumentException("Amount must be greater than 0"));
        }

        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        return userRef.get().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            DocumentSnapshot document = task.getResult();
            Double currentBalance = document.getDouble("balance");

            if (currentBalance == null) currentBalance = 0.0;

            if (!isIncome && amount > currentBalance) {
                throw new Exception("Insufficient funds");
            }

            double newBalance = isIncome ? currentBalance + amount : currentBalance - amount;

            Transaction transaction = isIncome ? new Transaction(UUID.randomUUID().toString(), userId, amount, ExpenseCategory.INCOME) : new Transaction(UUID.randomUUID().toString(), userId, amount, mapExpenseCategory(selectedCategory));

            // for atomic operations - ensure both operations happen or none at all
            WriteBatch batch = db.batch();

            // update user's balance
            batch.update(userRef, "balance", newBalance);

            // add transaction
            DocumentReference txRef = transactionsRef.document(transaction.getId());
            Map<String, Object> txData = new HashMap<>();
            txData.put("id", transaction.getId());
            txData.put("userId", userId);
            txData.put("amount", amount);
            txData.put("category", transaction.getCategory());
            txData.put("timestamp", FieldValue.serverTimestamp());

            batch.set(txRef, txData);

            return batch.commit();
        });

    }

    public Task<Void> deleteTransaction(String transactionId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        String userId = currentUser.getUid();
        DocumentReference txRef = db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId);

        // First get the transaction to determine balance adjustment
        return txRef.get().continueWithTask(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                throw new Exception("Transaction not found");
            }

            Transaction transaction = task.getResult().toObject(Transaction.class);
            if (transaction == null) {
                throw new Exception("Failed to parse transaction");
            }

            double amount = transaction.getAmount();
            boolean isIncome = "INCOME".equalsIgnoreCase(transaction.getCategory());
            double balanceChange = isIncome ? -amount : amount;

            // Create batch for atomic operations
            WriteBatch batch = db.batch();

            // Update user balance
            DocumentReference userRef = db.collection("users").document(userId);
            batch.update(userRef, "balance", FieldValue.increment(balanceChange));

            // Delete transaction
            batch.delete(txRef);

            return batch.commit();
        });
    }
}
