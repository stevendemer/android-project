package com.ergasia.minty;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia.minty.entities.Transaction;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Provide a reference to the type of views that you are using
     * ViewHolder provides all the functionality for the list items
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView incomeView;
        private final TextView dateTextView;
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

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

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
        // Create a new view, which defines the UI of the list item
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

        Log.d(TAG, "Element is at " + transaction + " set.");

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