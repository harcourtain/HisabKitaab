package com.example.hisabkitab;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.example.hisabkitab.data.TransactionContract;

/**
 * {@link TransactionCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of transaction data as its data source. This adapter knows
 * how to create list items for each row of transaction data in the {@link Cursor}.
 */
public class TransactionCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link TransactionCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public TransactionCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // TODO: Fill out this method and return the list item view (instead of null)
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // TODO: Fill out this method
        // Find individual views that we want to modify in the list item layout
        TextView personTextView = (TextView) view.findViewById(R.id.person);
        TextView itemTextView = (TextView) view.findViewById(R.id.item);
        TextView typeTextView = (TextView) view.findViewById(R.id.type);
        TextView amountTextView = (TextView) view.findViewById(R.id.amount);

        // Find the columns of transaction attributes that we're interested in
        int personColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON);
        int itemColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM);
        int typeColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE);
        int amountColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT);

        // Read the transaction attributes from the Cursor for the current transaction
        String transactionPerson = cursor.getString(personColumnIndex);
        String transactionItem = cursor.getString(itemColumnIndex);
        int t = Integer.parseInt(cursor.getString(typeColumnIndex));
        String transactionType ;
        switch (t){
            case 1 : transactionType = "Credited";
                     break;
            case 2 : transactionType = "Debited";
                     break;
            default : transactionType = "Unknown";

        }
        String transactionAmount = cursor.getString(amountColumnIndex);
        // Update the TextViews with the attributes for the current transaction
        personTextView.setText(transactionPerson);
        itemTextView.setText(transactionItem);
        typeTextView.setText(transactionType);
        amountTextView.setText(transactionAmount);

    }
}
//  All cursor adapter related deletion and modification
//  https://github.com/udacity/ud845-Pets/commit/feca5f24606aa1711362ff1d65ab85ffc94688ae/
// https://classroom.udacity.com/courses/ud845/lessons/1bbcafb9-6903-43e8-a9f6-7827712c2c2e/concepts/ebf9c491-8ff8-40ad-b3dd-8923d0c5510b