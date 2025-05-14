package com.ergasia.minty.fragments;

import android.annotation.SuppressLint;
import android.graphics.Insets;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia.minty.R;
import com.ergasia.minty.TransactionAdapter;
import com.ergasia.minty.entities.ExpenseCategory;
import com.ergasia.minty.entities.Transaction;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class HomeFragment extends Fragment {

    private FirebaseUser user;
    private FirebaseFirestore db;
    private TextView usernameTextView;
    private TextView balanceTextView;
    private TextInputEditText transactionTextView;
    private Button makeTransactionButton;
    private AutoCompleteTextView categoriesDropdown;
    private TextInputLayout incomeTextInputLayout;
    private TextInputLayout categoriesMenu;

    private MaterialDivider divider;

    // differentiates if is income or expense
    private MaterialSwitch transactionTypeSwitch;
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionsList = new ArrayList<>();
    private final String TAG = "HomeFragment";

    public HomeFragment() {
    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        recyclerView = view.findViewById(R.id.expensesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(transactionsList);

        usernameTextView = view.findViewById(R.id.usernameTextView);
        makeTransactionButton = view.findViewById(R.id.makeTransactionButton);
        balanceTextView = view.findViewById(R.id.balanceTextView);
        transactionTextView = view.findViewById(R.id.editTransactionText);
        transactionTypeSwitch = view.findViewById(R.id.incomeSwitch);
        incomeTextInputLayout = view.findViewById(R.id.incomeTextInputLayout);
        categoriesMenu = view.findViewById(R.id.categoriesMenu);

        ConstraintLayout rootLayout = view.findViewById(R.id.homeRootLayout);


        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView.setAdapter(transactionAdapter);

        String[] categories = getResources().getStringArray(R.array.categories_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                R.id.dropdownItemText,
                categories
        );

        categoriesDropdown = view.findViewById(R.id.categoriesDropdown);

        categoriesDropdown.setAdapter(adapter);
        // specify the layout to use when the list appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        DividerItemDecoration divider = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL
        );

        Drawable dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.divider);

        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }

        recyclerView.addItemDecoration(divider);
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
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
            int left = v.getPaddingLeft();
            int right = v.getPaddingRight();
            int top = v.getPaddingTop();

            v.setPadding(left, top, right, bottomInset);

            return insets;
        });

        fetchTransactions();

        // load the username and the current user's balance
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    getBalanceAndUsername();
                    Log.d(TAG, "User found");
                } else {
                    Log.d(TAG, "User not found");
                }
            }).addOnFailureListener(v -> {
                Log.d(TAG, "Error finding document");
            });

        } else {
            Log.d(TAG, "User is not logged in");
        }

        makeTransactionButton.setOnClickListener(v -> {
            storeTransaction();
        });

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

    private void storeTransaction() {
        String amountText = Objects.requireNonNull(transactionTextView.getText()).toString().trim();

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


        // add to the balance
        boolean isIncome = transactionTypeSwitch.isChecked();

        if (user != null) {
            DocumentReference userDocRef = db.collection("users").document(user.getUid());

            userDocRef.get().addOnSuccessListener(snapshot -> {
                Double currentBalance = snapshot.getDouble("balance");
                if (currentBalance == null) currentBalance = 0.0;

                double newBalance;

                if (isIncome) {
                    newBalance = currentBalance + amount;
                } else {
                    if (amount > currentBalance) {
                        transactionTextView.setError("Insufficient balance for this expense");
                        return;
                    }
                    newBalance = currentBalance - amount;
                }

                // update the db with the new user balance
                userDocRef.update("balance", newBalance).addOnSuccessListener(updatedTask -> {
                    Log.d(TAG, "Balance updated");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update balance " + e.toString());
                });

                if (isIncome) {
                    Transaction transaction = new Transaction(UUID.randomUUID().toString(), user.getUid(), amount);

                    db.collection("users").document(user.getUid()).collection("transactions").document(transaction.getId()).set(transaction)
                            .addOnSuccessListener(v -> {
                                Log.d(TAG, "Transaction added");
                                transactionTextView.setText("");
                            }).addOnFailureListener(v -> {
                                Log.d(TAG, "Transaction failed");
                                Toast.makeText(getContext(), "Transaction failed !", Toast.LENGTH_SHORT).show();
                                transactionTextView.setText("");
                            });

                } else {

                    String selectedCategory = categoriesDropdown.getText().toString().trim();
                    ExpenseCategory expenseCategory = mapExpenseCategory(selectedCategory);
                    Transaction transaction = new Transaction(UUID.randomUUID().toString(), user.getUid(), amount, expenseCategory);
                    db.collection("users").document(user.getUid()).collection("transactions").document(transaction.getId()).set(transaction)
                            .addOnSuccessListener(v -> {
                                Log.d(TAG, "Transaction added");
                                transactionTextView.setText("");
                            }).addOnFailureListener(v -> {
                                Log.d(TAG, "Transaction failed");
                                Toast.makeText(getContext(), "Transaction failed !", Toast.LENGTH_SHORT).show();
                                transactionTextView.setText("");
                            });
                }
            });
        }
    }

    private void fetchTransactions() {

        if (user == null) return;

        // fetch the most recent transactions from the db
        db.collection("users").document(user.getUid()).collection("transactions").orderBy("timestamp", Query.Direction.DESCENDING).limit(10).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Error fetching real time transactions");
                    return;
                }

                if (value != null) {
                    // clear for duplicates
                    transactionsList.clear();
                    for (DocumentSnapshot document : value.getDocuments()) {
                        Transaction transaction = document.toObject(Transaction.class);
                        if (transaction != null) {
                            transactionsList.add(transaction);
                        }
                    }
                }

                Log.d(TAG, "Fetched : " + transactionsList.size() + " transactions");
                // notify adapter
                transactionAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getBalanceAndUsername() {
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed " + e.toString());
                        return;
                    }
                    if (value != null && value.exists()) {
                        Double balance = value.getDouble("balance");
                        if (balance == null) balance = 0.0;

                        String balanceText = getString(R.string.your_balance, balance);
                        balanceTextView.setText(balanceText);
                        usernameTextView.setText(value.getString("username"));
                    }
                }
            });
        }
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
            case "transport":
                return ExpenseCategory.TRANSPORT;
            default:
                return ExpenseCategory.OTHER;
        }
    }

    private void resetCategoriesDropdown() {
        String[] categories = getResources().getStringArray(R.array.categories_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                R.id.dropdownItemText,
                categories
        );

        categoriesDropdown.setAdapter(adapter);
        categoriesDropdown.setText("", false); // Clear the selection but don't trigger dropdown
    }

    @Override
    public void onResume() {
        super.onResume();

        resetCategoriesDropdown();
    }
}
