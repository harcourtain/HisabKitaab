package com.example.hisabkitab.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class TransactionProvider extends ContentProvider {
    /** Tag for the log messages -Errors */

    public final String LOG_TAG = TransactionProvider.class.getSimpleName();
    /**
     * Initialize the provider and the database helper object.
     */
    private TransactionDbHelper mTransactionDbHelper;

    /** URI matcher code for the content URI for the pets table */
    private static final int TRANSACTION = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int TRANSACTION_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(TransactionContract.CONTENT_AUTHORITY,TransactionContract.PATH_TRANSACTION,TRANSACTION);
        sUriMatcher.addURI(TransactionContract.CONTENT_AUTHORITY,TransactionContract.PATH_TRANSACTION+"/#",TRANSACTION_ID);
    }

    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mTransactionDbHelper = new TransactionDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mTransactionDbHelper.getReadableDatabase();
        Cursor cursor = null;
        int match = sUriMatcher.match(uri);
        switch (match){
            case TRANSACTION:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                // TODO: Perform database query on pets table
                cursor = db.query(TransactionContract.TransactionEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);

                break;
            case TRANSACTION_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array
                 selection = TransactionContract.TransactionEntry._ID+"=?";
                 selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                 cursor = db.query(TransactionContract.TransactionEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default: throw new IllegalArgumentException("cannot query Unknown  URI : "+uri);
        }
        // Set Notification  URI on the Cursor,
        // so we know that the content URI the Crujrsor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        // return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTION:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a person");
        }
        Integer gender = values.getAsInteger(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE);
        if (gender == null || !TransactionContract.TransactionEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid type");
        }
        // TODO: Finish sanity checking the rest of the attributes in ContentValues
        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = values.getAsInteger(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid amount");
        }
        // No need to check the breed, any value is valid (including null).

        // TODO: Insert a new pet into the pets database table with the given ContentValues
        // Get Writeable Database
        SQLiteDatabase database = mTransactionDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(TransactionContract.TransactionEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
//       Notify All Listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri,null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }
    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTION:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case TRANSACTION_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = TransactionContract.TransactionEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
// If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON)) {
            String name = values.getAsString(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON);
            if (name == null) {
                throw new IllegalArgumentException("Transaction requires a Person");
            }
            //       Notify All Listeners that the data has changed for the pet content URI
            getContext().getContentResolver().notifyChange(uri,null);
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE)) {
            Integer gender = values.getAsInteger(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE);
            if (gender == null || !TransactionContract.TransactionEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Transaction requires valid Type");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Transaction requires valid amount");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // TODO: Update the selected pets in the pets database table with the given ContentValues
        SQLiteDatabase database = mTransactionDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(TransactionContract.TransactionEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     * https://classroom.udacity.com/courses/ud845/lessons/3973647d-3d4f-451f-bb86-1f51b20d85e8/concepts/3c1bb174-6e60-4179-b327-ad51ad0d0e75
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mTransactionDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        //       Notify All Listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri,null);
        switch (match) {
            case TRANSACTION:
                // Delete all rows that match the selection and selection args
                // Delete all rows that match the selection and selection args
                // For  case PETS:
               int rowsDeleted = database.delete(TransactionContract.TransactionEntry.TABLE_NAME, selection, selectionArgs);

                // For case PET_ID:
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(TransactionContract.TransactionEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case TRANSACTION_ID:
                // Delete a single row given by the ID in the URI
                selection = TransactionContract.TransactionEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(TransactionContract.TransactionEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }
    /**
     * Returns the MIME type of data for the content URI.
     *
     * You might have noticed that there is another required method in the PetProvider that we need to override:
     * the getType(Uri uri) method. The purpose of this method is to return a String that describes the type
     * of the data stored at the input Uri. This String is known as the MIME type, which can also be referred
     * to as content type.
     * https://classroom.udacity.com/courses/ud845/lessons/3973647d-3d4f-451f-bb86-1f51b20d85e8/concepts/68d872bc-94ec-419e-846d-599e8b795530
     */
    /**
     * The MIME type of a {@link # CONTENT_URI} subdirectory of a single
     * person.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact";
    /**
     * The MIME type of {@link # CONTENT_URI} providing a directory of
     * people.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact";
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTION:
                return TransactionContract.TransactionEntry.CONTENT_LIST_TYPE;
            case TRANSACTION_ID:
                return TransactionContract.TransactionEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
//    You May Refer Here
  //  https://github.com/udacity/ud845-Pets/commit/6861be56c0c5c3673f0a0b8a04922bfee823582b
}
