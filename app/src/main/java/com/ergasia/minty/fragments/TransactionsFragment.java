package com.ergasia.minty.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia.minty.R;
import com.ergasia.minty.TransactionAdapter;
import com.ergasia.minty.views.TransactionsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private TransactionsViewModel transactionsViewModel;
    private TransactionAdapter transactionAdapter;
    private AutoCompleteTextView filterMenu;
    private FloatingActionButton backTopButton;
    private RecyclerView recyclerView;
    private final String TAG = "TransactionsFragment";

    public TransactionsFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionsViewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);
        transactionAdapter = new TransactionAdapter();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        recyclerView = view.findViewById(R.id.filteredTransactionsRecyclerView);

        setupFilterDropdown(view);

        setupViews(view);

        backTopButton = view.findViewById(R.id.scrollToTopButton);

        transactionsViewModel.getTransactions().observe(getViewLifecycleOwner(), transactionsList -> {
            Log.d(TAG, "All transactions are : " + transactionsList.toString());

            transactionAdapter.setTransactions(transactionsList);
        });

        backTopButton.setOnClickListener(v -> recyclerView.scrollToPosition(0));

        // show / hide button based on scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem > 3) {
                        backTopButton.show();  // show button when scrolling down
                    } else {
                        backTopButton.hide();  // hide button at the top
                    }
                }
            }
        });

        transactionAdapter.setOnDeleteListener(transaction -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete transaction")
                    .setMessage("Are you sure you want to delete this transaction ?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        transactionsViewModel.deleteTransaction(transaction.getId())
                                .observe(getViewLifecycleOwner(), success -> {
                                    if (success) {
                                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        return view;
    }

    private void setupViews(View view) {
        recyclerView = view.findViewById(R.id.filteredTransactionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(transactionAdapter);
        recyclerView.setHasFixedSize(true);
    }


    private void setupFilterDropdown(View view) {
        filterMenu = view.findViewById(R.id.filterMenu);
        String[] filterOptions = getResources().getStringArray(R.array.sort_array);

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, R.id.dropdownItemText, filterOptions);

        filterMenu.setAdapter(dropdownAdapter);

        filterMenu.setDropDownBackgroundResource(R.drawable.filter_dropdown_menu);

        filterMenu.setOnItemClickListener((parent, view1, position, id) -> {

            String selectedFilter = parent.getItemAtPosition(position).toString();

            Log.e(TAG, "Filter is " + selectedFilter);

            handleFilterSelection(selectedFilter);
        });
    }

    private void handleFilterSelection(String selectedFilter) {
        recyclerView.scrollToPosition(0);

        if (selectedFilter.equalsIgnoreCase("income")) {
            transactionsViewModel.fetchTransactionsByType("INCOME");
            return;
        }

        List<String> expenseCategories = Arrays.asList("HEALTH", "EDUCATION", "GROCERIES", "TRANSPORTATION", "OTHER", "ENTERTAINMENT");

        String upperCaseFilter = selectedFilter.toUpperCase();

        if (expenseCategories.contains(upperCaseFilter)) {
            transactionsViewModel.fetchTransactionsByType(upperCaseFilter);
            return;
        }

        String field = "timestamp";
        boolean ascending = selectedFilter.contains("↑");

        if (selectedFilter.contains("amount")) {
            field = "amount";
        }

        transactionsViewModel.fetchSortedTransactions(field, ascending, 0);
    }

    private void resetSortDropdown() {
        String[] sort = getResources().getStringArray(R.array.sort_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.sort_item,
                R.id.sortTextView,
                sort
        );

        filterMenu.setAdapter(adapter);
        filterMenu.setText("", false); // clear the selection
    }


    @Override
    public void onResume() {
        super.onResume();
        resetSortDropdown();

        // fetch all transactions
        transactionsViewModel.loadAllTransactions();
    }
}
