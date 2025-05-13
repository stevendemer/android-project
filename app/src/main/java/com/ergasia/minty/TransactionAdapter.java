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

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Locale;
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Provide a reference to the type of views that you are using
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView incomeView;
        private final TextView dateView;


        private final String TAG = "Element";

        public TransactionViewHolder(View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element is " + getAdapterPosition() + " clicked");
                }
            });


            textView = (TextView) view.findViewById(R.id.titleText);
            incomeView = (TextView) view.findViewById(R.id.amountText);
            dateView = (TextView) view.findViewById(R.id.dateText);
        }

        public View getTextView() {
            return textView;
        }

        public View getIncomeView() {
            return incomeView;
        }
        public TextView getDateView() { return dateView; }
    }


    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Create new views (invoked by the layout manager)
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
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

        // Get element from your dataset at this position and replace the contents
        // of the view with that element
        Transaction transaction = transactions.get(position);

        viewHolder.textView.setText(transaction.getTitle());
        viewHolder.textView.setTextColor(Color.WHITE);

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        viewHolder.dateView.setText(formatter.format(transaction.getTimestamp()));
        viewHolder.dateView.setTextColor(Color.WHITE);
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