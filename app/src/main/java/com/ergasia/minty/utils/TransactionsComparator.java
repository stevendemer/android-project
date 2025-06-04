package com.ergasia.minty.utils;

import androidx.recyclerview.widget.DiffUtil;

import com.ergasia.minty.entities.Transaction;

import java.util.List;


/**
 * Class used for comparing lists when a new transaction is added, better for performance
 */
public class TransactionsComparator extends DiffUtil.Callback {

    private final List<Transaction> oldList;
    private final List<Transaction> newList;

    public TransactionsComparator(List<Transaction> oldList, List<Transaction> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
