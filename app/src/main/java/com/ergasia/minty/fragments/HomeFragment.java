package com.ergasia.minty.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia.minty.R;
import com.ergasia.minty.TransactionAdapter;
import com.ergasia.minty.views.HomeViewModel;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private TextView usernameTextView;
    private TextView balanceTextView;
    private TextInputEditText transactionTextView;
    private Button makeTransactionButton;
    private AutoCompleteTextView categoriesDropdown;
    private TextInputLayout incomeTextInputLayout;
    private TextInputLayout categoriesMenu;

    // whether income or expense
    private MaterialSwitch transactionTypeSwitch;
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private final String TAG = "HomeFragment";

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        transactionAdapter = new TransactionAdapter();
    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setupViews(view);

        setupUI(view);

        setupObservers();

        ConstraintLayout rootLayout = view.findViewById(R.id.homeRootLayout);

        categoriesDropdown.setDropDownBackgroundResource(R.drawable.filter_dropdown_menu);

        String[] categories = getResources().getStringArray(R.array.categories_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, R.id.dropdownItemText, categories);

        // specify the layout to use when the list appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets systemBars = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
                }

                // apply top inset as padding
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }

                return insets;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, insets) -> {

            int bottomInset = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                bottomInset = insets.getInsets(WindowInsets.Type.statusBars()).bottom;
            }
            int left = v.getPaddingLeft();
            int right = v.getPaddingRight();
            int top = v.getPaddingTop();

            v.setPadding(left, top, right, bottomInset);

            return insets;
        });

        makeTransactionButton.setOnClickListener(v -> onSubmitTransaction());

        transactionTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // income mode
                incomeTextInputLayout.setHint("Enter income");
                makeTransactionButton.setText(R.string.add_income);
                transactionTypeSwitch.setText(R.string.income);
                categoriesMenu.setVisibility(View.GONE);
            } else {
                // expense mode
                incomeTextInputLayout.setHint("Enter expense");
                makeTransactionButton.setText(R.string.add_expense);
                transactionTypeSwitch.setText(R.string.expense);
                categoriesMenu.setVisibility(View.VISIBLE);
            }
        });


        return view;
    }


    private void setupUI(View view) {

        recyclerView = view.findViewById(R.id.expensesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        usernameTextView = view.findViewById(R.id.usernameTextView);
        makeTransactionButton = view.findViewById(R.id.makeTransactionButton);
        balanceTextView = view.findViewById(R.id.balanceTextView);

        // number entered in the input
        transactionTextView = view.findViewById(R.id.editTransactionText);
        transactionTypeSwitch = view.findViewById(R.id.incomeSwitch);
        incomeTextInputLayout = view.findViewById(R.id.incomeTextInputLayout);
        categoriesMenu = view.findViewById(R.id.categoriesMenu);
        categoriesDropdown = view.findViewById(R.id.categoriesDropdown);

        categoriesDropdown.setDropDownBackgroundResource(R.drawable.filter_dropdown_menu);
    }

    private void setupViews(View view) {

        recyclerView = view.findViewById(R.id.expensesRecyclerView);
        recyclerView.setAdapter(transactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    @SuppressLint("DefaultLocale")
    public void setupObservers() {
        homeViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            Log.d(TAG, "Transactions updated: " + transactions.size());
            transactionAdapter.setTransactions(transactions);
            transactionAdapter.notifyDataSetChanged();

            if (!transactions.isEmpty()) {
                recyclerView.scrollToPosition(0);
            }
        });

        homeViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            usernameTextView.setText(user.getUsername());
            double balance = user.getBalance();
            balanceTextView.setText(String.format("Balance: €%.2f", balance));
            balanceTextView.setTextColor(balance < 10 ? Color.RED : ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
        });

        homeViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        transactionAdapter.setOnDeleteListener(transaction -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete transaction")
                    .setMessage("Are you sure you want to delete this transaction ?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        homeViewModel.deleteTransaction(transaction.getId())
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
    }

    private void onSubmitTransaction() {

        String amountText = Objects.requireNonNull(transactionTextView.getText()).toString().trim();
        boolean isIncome = transactionTypeSwitch.isChecked();
        String selectedCategory = categoriesDropdown.getText().toString().trim();

        if (amountText.isEmpty()) {
            transactionTextView.setError("Please enter a valid amount");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            transactionTextView.setError("Invalid number format");
            return;
        }

        if (amount == 0) {
            transactionTextView.setError("Transaction must be greater than zero.");
            return;
        }

        // store the transaction
        homeViewModel.storeTransaction(amount, isIncome, selectedCategory);

        // clear input after submission
        transactionTextView.setText("");

        // clear previous errors
        transactionTextView.setError(null);

        // reset category selection
        categoriesDropdown.setText("", false);

        recyclerView.scrollToPosition(0);

        Toast.makeText(requireContext(), "Transaction added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetCategoriesDropdown();
    }


    private void resetCategoriesDropdown() {
        String[] categories = getResources().getStringArray(R.array.categories_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, R.id.dropdownItemText, categories);

        categoriesDropdown.setAdapter(adapter);
        // Clear the selection but don't trigger dropdown
        categoriesDropdown.setText("", false);
    }

    public void onDeleteTransaction(String transactionId) {
        homeViewModel.deleteTransaction(transactionId).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to delete transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
