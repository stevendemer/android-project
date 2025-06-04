package com.ergasia.minty;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia.minty.entities.Transaction;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    public interface OnDeleteListener {
        void onDeleteClick(Transaction transaction);
    }

    private List<Transaction> transactions;

    private OnDeleteListener onDeleteListener;

    public TransactionAdapter() {
        this.transactions = new ArrayList<>();
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.onDeleteListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * ViewHolder provides all the functionality for the list items
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView incomeView;
        private final TextView dateTextView;
        private Button deleteButton;

        private final String TAG = "Element";

        public TransactionViewHolder(View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element is " + getAdapterPosition() + " clicked");
                }
            });

            textView = (TextView) view.findViewById(R.id.transactionTitleText);
            incomeView = (TextView) view.findViewById(R.id.transactionAmountText);
            dateTextView = (TextView) view.findViewById(R.id.dateTextView);
            deleteButton = (Button) view.findViewById(R.id.deleteButton);
        }

        public View getTextView() {
            return textView;
        }

        public View getIncomeView() {
            return incomeView;
        }

        public View getDateView() {
            return dateTextView;
        }
    }


    private void deleteTransactionFromFirestore(Transaction transaction, int position) {
        String transactionId = transaction.getId();

        if (transactionId == null) {
            Log.e("TransactionAdapter", "Transaction id is null");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(user.getUid()).collection("transactions").document(transactionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TransactionAdapter", "Transaction deleted successfully");


                })
                .addOnFailureListener(e -> {
                    Log.e("TransactionsAdapter", "Failed to delete transactions ", e);
                });


    }


    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public Transaction getTransactionAt(int position) {
        return transactions.get(position);
    }

    public void removeTransaction(int position) {
        transactions.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, transactions.size());
    }

//    public void updateTransactions(List<Transaction> newTransactions) {
//    }

    /**
     * Create new views (invoked by the layout manager)
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     */
    @NonNull
    @Override
    public TransactionAdapter.TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Creates a new view, for the list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);

        return new TransactionViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.TransactionViewHolder viewHolder, int position) {

        String TAG = "TransactionAdapter";

        // Get element from your dataset at this position and replace the contents
        // of the view with that element
        Transaction transaction = transactions.get(position);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewHolder.itemView.getLayoutParams();


        // add extra space to the last element
        if (position == getItemCount() - 1) {
            params.bottomMargin = 32;
        } else {
            params.bottomMargin = 0;
        }

        viewHolder.itemView.setLayoutParams(params);

        Log.d(TAG, "Transaction is at " + transaction + " and " + position);

        Timestamp timestamp = transaction.getTimestamp();

        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());
            viewHolder.dateTextView.setText(sdf.format(date));
        } else {
            viewHolder.dateTextView.setText("");
        }

        viewHolder.deleteButton.setOnClickListener(v -> {
//            new AlertDialog.Builder(viewHolder.itemView.getContext()).setTitle("Delete transaction").setMessage("Are you sure you want to delete this transaction ?").setPositiveButton("Yes", (dialog, which) -> {
                if (onDeleteListener != null) {
                    onDeleteListener.onDeleteClick(transaction);
                }
//            }).setNegativeButton("Cancel", null).show();
        });

        viewHolder.textView.setText(transaction.getCategory().trim());

        boolean isIncome = "income".equalsIgnoreCase(transaction.getCategory());

        String prefix = isIncome ? "+" : "-";
        @SuppressLint("DefaultLocale") String formattedAmount = String.format("%s$%.1f", prefix, transaction.getAmount());

        int color = isIncome ? Color.GREEN : Color.RED;

        viewHolder.incomeView.setText(formattedAmount);
        viewHolder.incomeView.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}