package com.ergasia.minty;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia.minty.entities.Transaction;
import com.google.firebase.firestore.CollectionReference;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SortedTransactionsAdapter extends RecyclerView.Adapter<SortedTransactionsAdapter.SortedTransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    private boolean isSorted = false;

    public static class SortedTransactionViewHolder extends RecyclerView.ViewHolder {

        public final TextView categoryTextView;
        public final TextView dateTextView;
        private final TextView amountTextView;

        private AutoCompleteTextView sortDropdown;

        private final String TAG = "SortedTransactionsAdapter";

        private CollectionReference collectionReference;

        public SortedTransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            categoryTextView = (TextView) itemView.findViewById(R.id.sortTextView);
            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            amountTextView = (TextView) itemView.findViewById(R.id.amountTextView);
        }

        public View getAmountView() {
            return this.amountTextView;
        }

        public View getDateTextView() {
            return this.dateTextView;
        }

        public View getCategoryView() {
            return this.categoryTextView;
        }
    }

    public SortedTransactionsAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public SortedTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sort_item, parent, false);
        return new SortedTransactionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull SortedTransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Make sure you're setting all required fields
        holder.categoryTextView.setText(transaction.getCategory());


        // Format the date if timestamp exists
        if (transaction.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy, HH:mm", Locale.getDefault());
            String dateString = sdf.format(transaction.getTimestamp().toDate());
            holder.dateTextView.setText(dateString);
        }

        // Set amount with proper formatting
        String amountText = String.format(Locale.getDefault(), "$ %.1f", transaction.getAmount());

        String prefix = transaction.isIncome() ? "+" : "-";
        @SuppressLint("DefaultLocale") String formattedAmount = String.format("%s$%.2f", prefix, transaction.getAmount());

        holder.amountTextView.setText(amountText);
    }

    @Override
    public int getItemCount() {
        return this.transactions.size();
    }

    // Update this method to notify adapter
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged(); // This is crucial!
    }

}
