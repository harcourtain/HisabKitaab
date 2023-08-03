package com.example.hisabkitab.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class TransactionContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private TransactionContract() {}

    /**
     * The "Content authority" is
     * a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.hisabkitab";   /// peNding

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.transactions/transactions/ is a valid path for
     * looking at transaction data. content://com.example.android.transactions/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_TRANSACTION = "transaction";   // it should be transaction since my table name is transaction instead of transactions

    /**
     * Inner class that defines constant values for the transactions database table.
     * Each entry in the table represents a single transaction.
     */

    public static final class TransactionEntry implements BaseColumns{
          // the content uri to access transaction data in transactionprovider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_TRANSACTION);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of transaction.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTION;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single transaction.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTION;
        public final static String TABLE_NAME="transactions";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_TRANSACTION_PERSON ="person";
        public final static String COLUMN_TRANSACTION_ITEM ="item";
        public final static String COLUMN_TRANSACTION_TYPE ="type";
        public final static String COLUMN_TRANSACTION_AMOUNT="amount";

        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_CREDIT = 1;
        public static final int TYPE_DEBIT = 2;



        /**
         * Returns whether or not the given gender is {@link #TYPE_UNKNOWN, {@link #TYPE_CREDIT},
         * or {@link #TYPE_DEBIT}.
         */
        public static boolean isValidType(int type) {
            if (type == TYPE_UNKNOWN || type == TYPE_CREDIT || type == TYPE_DEBIT) {
                return true;
            }
            return false;
        }
    }
}
