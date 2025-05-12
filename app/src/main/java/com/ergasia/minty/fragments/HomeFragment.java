package com.ergasia.minty.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ergasia.minty.R;
import com.ergasia.minty.entities.Transaction;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class HomeFragment extends Fragment {

    private FirebaseUser user;
    private FirebaseFirestore db;
    private TextView usernameTextView;
    private TextView balanceTextView;
    private TextInputEditText transactionTextView;
    private Button makeTransactionButton;
    private TextInputLayout incomeTextInputLayout;

    // differentiates if income or expense
    private MaterialSwitch transactionTypeSwitch;

    private final String TAG = "HomeFragment";

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        usernameTextView = view.findViewById(R.id.usernameTextView);
        makeTransactionButton = view.findViewById(R.id.makeTransactionButton);
        balanceTextView = view.findViewById(R.id.balanceTextView);
        transactionTextView = view.findViewById(R.id.editTransactionText);
        transactionTypeSwitch = view.findViewById(R.id.incomeSwitch);
        incomeTextInputLayout = view.findViewById(R.id.incomeTextInputLayout);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    usernameTextView.setText(documentSnapshot.getString("username"));
                    getUserBalance();
                    Log.d(TAG, "User found");
                } else {
                    Log.d(TAG, "User not found");
                }
            }).addOnFailureListener(v -> {
                Log.d(TAG, "Error finding document");
            });

        } else {
            Log.d(TAG, "User not logged in");
        }

        makeTransactionButton.setOnClickListener(v -> {
            storeTransaction();
        });

        transactionTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // income mode
                incomeTextInputLayout.setHint("Enter income");
                makeTransactionButton.setText(R.string.add_income);
            } else {
                // expense mode
                incomeTextInputLayout.setHint("Enter expense");
                makeTransactionButton.setText(R.string.add_expense);
            }
        });

        return view;
    }

    private void storeTransaction() {
        String amountText = Objects.requireNonNull(transactionTextView.getText()).toString().trim();

        if (user != null) {
            // Update the income of the user
            Transaction transaction = new Transaction(UUID.randomUUID().toString(), user.getUid(), "Salary", Double.parseDouble(amountText), new Date());

            DocumentReference userDocRef = db.collection("users").document(user.getUid());
            double amount = Double.parseDouble(amountText);

            boolean isIncome = transactionTypeSwitch.isChecked();

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

                // store the transaction
                db.collection("users").document(user.getUid()).collection("transactions").document(transaction.getId()).set(transaction).addOnSuccessListener(v -> {
                    Log.d(TAG, "Transaction added");
                    Toast.makeText(getContext(), "Transaction added !", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(v -> {
                    Log.d(TAG, "Transaction failed");
                    Toast.makeText(getContext(), "Transaction failed !", Toast.LENGTH_SHORT).show();
                });

                db.collection("users").document(user.getUid()).collection("transactions").document(transaction.getId()).set(transaction)
                        .addOnSuccessListener(v -> {
                            Log.d(TAG, "Transaction added");
                            transactionTextView.setText("");
                        }).addOnFailureListener(v -> {
                            Log.d(TAG, "Transaction failed");
                            Toast.makeText(getContext(), "Transaction failed !", Toast.LENGTH_SHORT).show();
                            transactionTextView.setText("");
                        });
            });
        }
    }

    private void getTransactionsList() {

        if (user != null) {
            String userId = user.getUid();

            db.collection("users").document(userId).collection("transactions").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Transaction transaction = document.toObject(Transaction.class);
                        assert transaction != null;
                        Log.d(TAG, "Transaction: " + transaction.toString());
                    }
                } else {
                    Log.e(TAG, "Error getting transactions " + task.getException());
                }
            });
        }
    }

    private void getUserBalance() {
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
                    }
                }
            });
        }
    }

}
