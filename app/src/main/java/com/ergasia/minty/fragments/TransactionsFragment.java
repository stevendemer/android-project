package com.ergasia.minty.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.ergasia.minty.R;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia.minty.TransactionAdapter;
import com.ergasia.minty.entities.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private final List<Transaction> transactions = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "TransactionsFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        recyclerView = view.findViewById(R.id.transactionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(transactions);
        recyclerView.setAdapter(adapter);

        loadTransactions();

        return view;
    }

    private void loadTransactions() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactions.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        transaction.setId(doc.getId());
                        transactions.add(transaction);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading transactions", e));
    }
}
