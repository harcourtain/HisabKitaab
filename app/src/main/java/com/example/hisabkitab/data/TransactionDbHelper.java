package com.example.hisabkitab.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import com.example.transaction.data.TransactionContract.TransactionEntry;

/**
 * Database helper for Pets app. Manages database creation and version management.
 */
public class TransactionDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = TransactionDbHelper.class.getSimpleName();
    /** Name of the database file */
    private static final String DATABASE_NAME = "transactions.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;
    /**
     * Constructs a new instance of {@link TransactionDbHelper}.
     *
     * @param context of the app
     */
    public TransactionDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE = "CREATE TABLE "+ TransactionContract.TransactionEntry.TABLE_NAME +" ("+
                TransactionContract.TransactionEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON+" TEXT NOT NULL, "
                +TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM+ " TEXT, "
                +TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE+" INTEGER NOT NULL, "+
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT+" INTEGER NOT NULL DEFAULT 0);";
     db.execSQL(SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
