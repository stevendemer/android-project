package com.ergasia.minty.views;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ergasia.minty.TransactionAdapter;
import com.ergasia.minty.entities.Transaction;
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
import java.util.Collections;
import java.util.List;

public class TransactionsViewModel extends ViewModel {


    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final String TAG = "TransactionsViewModel";
    private ListenerRegistration transactionsListener;

    private TransactionAdapter transactionAdapter;

    public TransactionsViewModel() {
        loadAllTransactions();
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    public LiveData<String> getErrorLiveData() {
        return this.errorLiveData;
    }


    public void loadAllTransactions() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        transactionsListener = db.collection("users").document(currentUser.getUid()).collection("transactions").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed ", error);
                        return;
                    }
                    if (value == null || value.isEmpty()) {
                        transactions.setValue(new ArrayList<>());
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


    /**
     * If limit is 0, all transactions will be fetched, default case is order by timestamp (Firestore timestamp) by descending order (Newest first)
     *
     * @param field
     * @param ascending
     */
    public void fetchSortedTransactions(String field, boolean ascending, int limit) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        Log.d(TAG, "Fetching sorted transactions");

        Query.Direction direction = ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;

        Query query = db.collection("users").document(currentUser.getUid())
                .collection("transactions")
                .orderBy(field, direction);

        if (limit > 0) {
            query = query.limit(limit);
        }

        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) list.add(t);
                    }

                    Log.d(TAG, "Filtered transactions " + list.toString());

                    transactions.setValue(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading sorted transactions ", e);
                    this.transactions.setValue(Collections.emptyList());
                    errorLiveData.setValue(e.getMessage());
                });
    }


    /**
     * Category can be  - INCOME, OTHER, GROCERIES etc
     * We check in the database and filter out the  documents
     *
     * @param type
     */
    public void fetchTransactionsByType(String type) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorLiveData.setValue("User not logged in");
            return;
        }

        Log.d(TAG, "Type is " + type);

        Query query = db.collection("users").document(currentUser.getUid()).collection("transactions")
                .whereEqualTo("category", type.toUpperCase().trim());


        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Transaction> filteredList = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Transaction t = doc.toObject(Transaction.class);
                if (t != null) {
                    t.setId(doc.getId());
                    filteredList.add(t);
                }
            }
            Log.d(TAG, "Filtered transactions: " + filteredList);
            transactions.setValue(filteredList);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to filter transactions ", e);
            this.errorLiveData.setValue(e.getMessage());
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
                        loadAllTransactions();
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
        if (transactionsListener != null) {
            transactionsListener.remove();
        }
    }
}
