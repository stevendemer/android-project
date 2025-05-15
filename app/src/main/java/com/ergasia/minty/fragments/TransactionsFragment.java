package com.ergasia.minty.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia.minty.R;
import com.ergasia.minty.SortedTransactionsAdapter;
import com.ergasia.minty.TransactionAdapter;
import com.ergasia.minty.entities.ExpenseCategory;
import com.ergasia.minty.entities.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private List<Transaction> transactionList = new ArrayList<>();
    private boolean isFilteredApplied = false;
    private final List<String> categories = Arrays.asList("All", "Date", "Amount");
    private AutoCompleteTextView dropdown;

    SortedTransactionsAdapter adapter;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String TAG = "TransactionsFragment";
    private CollectionReference transactionsRef;

    public TransactionsFragment() {
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        RecyclerView filtetTransactionsRecyclerView = view.findViewById(R.id.filterRecyclerView);
        filtetTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SortedTransactionsAdapter(transactionList);
        dropdown = view.findViewById(R.id.filterMenu);

        String[] categories = getResources().getStringArray(R.array.sort_array);

        filtetTransactionsRecyclerView.setAdapter(adapter);

        dropdown.setDropDownBackgroundResource(R.drawable.filter_dropdown_menu);

        setupDropdownMenu();

        if (userId != null) {
            transactionsRef = db.collection("users").document(userId).collection("transactions");

            fetchAll();
        }

        return view;
    }

    private void fetchTransactionsWithSortOrFilter(String option) {

        isFilteredApplied = true;

        if (userId == null) return;

        CollectionReference transactionsRef = db.collection("users").document(userId).collection("transactions");

        String[] resources = getResources().getStringArray(R.array.sort_array);

        Query query = transactionsRef;

        switch (option) {
            case "Date (Newest)":
                query = query.orderBy("timestamp", Query.Direction.ASCENDING);
                break;
            case "Date (Oldest)":
                query = query.orderBy("timestamp", Query.Direction.DESCENDING);
                break;
            case "Amount (Greatest)":
                query = query.orderBy("amount", Query.Direction.DESCENDING);
                break;
            case "Amount (Least)":
                query = query.orderBy("amount", Query.Direction.ASCENDING);
                break;
            case "Category (Income)":
                query = query.whereEqualTo("category", "INCOME");
                break;
            case "Category (Expense)":
                query = query.whereNotEqualTo("category", "INCOME");
                break;
            case "All":
                isFilteredApplied = false;
                fetchAll();
                return;
        }


        // Pass this to your adapter
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(requireContext(), "No matching transactions found", Toast.LENGTH_SHORT).show();
                adapter.setTransactions(new ArrayList<>());
                return;
            }

            List<Transaction> transactions = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                Transaction transaction = doc.toObject(Transaction.class);
                transactions.add(transaction);
            }

            adapter.setTransactions(transactions);

        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void fetchAll() {
        transactionsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }

                    List<Transaction> transactions = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        if (transaction != null) {
                            transactions.add(transaction);
                        }
                    }
                    adapter.setTransactions(transactions);
                });
    }


    private void applyFilter(String filterOption) {
        if (userId == null) return;

        Query query = transactionsRef;

        switch (filterOption) {
            case "Date (Newest)":
                query = query.orderBy("timestamp", Query.Direction.ASCENDING);
                break;
            case "Date (Oldest)":
                query = query.orderBy("timestamp", Query.Direction.DESCENDING);
                break;
            case "Amount (Greatest)":
                query = query.orderBy("amount", Query.Direction.DESCENDING);
                break;
            case "Amount (Least)":
                query = query.orderBy("amount", Query.Direction.ASCENDING);
                break;
            case "Category (Income)":
                query = query.whereEqualTo("INCOME", true);
                break;
            case "Category (Expense)":
                query = query.whereEqualTo("INCOME", false);
                break;
            case "All":
            default:
                fetchAll();
                return;
        }

        fetchWithQuery(query);
    }

    private void fetchWithQuery(Query query) {
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Transaction> filteredTransactions = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    Transaction transaction = doc.toObject(Transaction.class);
                    if (transaction != null) {
                        filteredTransactions.add(transaction);
                    }
                }
                adapter.setTransactions(filteredTransactions);
            } else {
                Toast.makeText(getContext(), "Error loading filtered data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting filtered documents", task.getException());
            }
        });
    }

    private void setupDropdownMenu() {
        // Get filter options from resources
        String[] filterOptions = getResources().getStringArray(R.array.sort_array);

        // Create adapter for dropdown
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                filterOptions
        );

        dropdown.setAdapter(dropdownAdapter);
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = parent.getItemAtPosition(position).toString();
            fetchTransactionsWithSortOrFilter(selectedOption);
        });
    }


    private void resetSortDropdown() {
        String[] sort = getResources().getStringArray(R.array.sort_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.sort_item,
                R.id.sortTextView,
                sort
        );

        dropdown.setAdapter(adapter);
        dropdown.setText("", false); // clear the selection
        isFilteredApplied = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        resetSortDropdown();
        fetchAll();
    }
}
