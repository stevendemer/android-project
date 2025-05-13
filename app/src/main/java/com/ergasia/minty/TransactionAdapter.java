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

import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private final String TAG = "TransactionAdapter";

    /**
     * Provide a reference to the type of views that you are using
     * ViewHolder provides all the functionality for the list items
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView incomeView;
//        private final TextView dateTextView;

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
//            dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        }

        public View getTextView() {
            return textView;
        }

        public View getIncomeView() {
            return incomeView;
        }

//        public View getDateTextView() {
//            return dateTextView;
//        }
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

        Log.d(TAG, "Element is at " + position + " set.");

        // Get element from your dataset at this position and replace the contents
        // of the view with that element
        Transaction transaction = transactions.get(position);

        if (transaction.isExpense()) {
            viewHolder.textView.setText(transaction.getCategory().trim());
            viewHolder.textView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textView.setText(R.string.income);
//            viewHolder.textView.setVisibility(View.GONE);
        }

        String prefix = transaction.isExpense() ? "-" : "+";
        @SuppressLint("DefaultLocale") String formattedAmount = String.format("%s$%.2f", prefix, transaction.getAmount());

        int color = transaction.isExpense() ? Color.RED : Color.GREEN;

        viewHolder.incomeView.setText(formattedAmount);
        viewHolder.incomeView.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}