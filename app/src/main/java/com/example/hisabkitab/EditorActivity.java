/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.hisabkitab;
// done
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.hisabkitab.data.TransactionContract;
import com.example.hisabkitab.data.TransactionDbHelper;

/**
 * Allows user to create a new transaction or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Content URI for the existing transaction (null if it's a new transaction) */
    private Uri mCurrentTransactionUri;

    /** EditText field to enter the transaction's name */
    private EditText mPersonEditText;

    /** EditText field to enter the transaction's Items */
    private EditText mItemsEditText;

    /** EditText field to enter the transaction's amount */
    private EditText mAmountEditText;

    /** EditText field to enter the transaction's type */
    private Spinner mTypeSpinner;

    private TransactionDbHelper mTransactionDbHelper;  //  instance of PetDbHelper
    private static final int EXISTING_TRANSACTION_LOADER = 0;
    private boolean mTransactionHasChanged = false;
    /**
     * Type of the transaction. The possible values are:
     * 0 for unknown gender, 1 for credit, 2 for debit.
     */
    private int mType = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentTransactionUri = intent.getData();
        if(mCurrentTransactionUri == null){
            setTitle(R.string.editor_activity_title_new_transaction);
//            invalidateOptionsMenu();    // not use till now
        }else{
            setTitle(R.string.editor_activity_title_edit_transaction);
            getLoaderManager().initLoader(EXISTING_TRANSACTION_LOADER,null,this);// Edit Transaction
        }

        // Find all relevant views that we will need to read user input from
        mPersonEditText = (EditText) findViewById(R.id.edit_transaction_name);
        mItemsEditText = (EditText) findViewById(R.id.edit_transaction_items);
        mAmountEditText = (EditText) findViewById(R.id.edit_transaction_amount);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);

        /*
            if you have touched any (name, items, type, amount) it will show dialog box ->
            discard your changes and quit editing? -> KeepEditing, Discard.
         */
        mPersonEditText.setOnTouchListener(mTouchListener);
        mItemsEditText.setOnTouchListener(mTouchListener);
        mAmountEditText.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();  // down list to select type of transaction


    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the transaction.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        // R.array.array_gender_options -> res/values/arrays.xml
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line  -- inbuild hoti hai
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mTypeSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.transaction_credit))) {
                        mType = TransactionContract.TransactionEntry.TYPE_CREDIT; // Male
                    } else if (selection.equals(getString(R.string.transaction_debit))) {
                        mType = TransactionContract.TransactionEntry.TYPE_DEBIT; // Female
                    } else {
                        mType = TransactionContract.TransactionEntry.TYPE_UNKNOWN; // Unknown

                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = TransactionContract.TransactionEntry.TYPE_UNKNOWN; // Unknown
            }
        });
    }



    // Creating menu in EditTransaction Details -> save & delete.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
            return true;
    }

    // adding functionality to the selected menu option.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet(); // inserting current data to transaction.db
                Toast.makeText(this,"New Data Inserted To The Table",Toast.LENGTH_SHORT).show();
                finish(); // go back to mainactivity page
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                // Do nothing for now
                return true;
            // Respond to a click on the "Back" arrow button in the app bar
            // if any field is clicked then showing dialog
            case android.R.id.home:
                // If the transaction hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mTransactionHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.

//                if any of the fields is/are clicked by user i.e. suspected to be edited
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Get user input from editor and save new transaction into database.
     */
    private void savePet() {

        // getting data from textFeilds and Inserting into database
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
            String person = mPersonEditText.getText().toString().trim();
            String item = mItemsEditText.getText().toString().trim();
            String amountString = mAmountEditText.getText().toString().trim();
            int amount = 0;
            if (!TextUtils.isEmpty(amountString)) {
                 amount = Integer.parseInt(amountString);
             }

        if(TextUtils.isEmpty(item)) item = "Unknown Item";

        if (mCurrentTransactionUri == null &&
                TextUtils.isEmpty(person) && TextUtils.isEmpty(item) &&
                TextUtils.isEmpty(amountString) && mType == TransactionContract.TransactionEntry.TYPE_UNKNOWN) {
            return;
        }
            if(person.equals("") || item.equals("") || amount==0 || mType ==0) {
                Toast.makeText(this,"Data Entry Error, Make sure no entry is Empty",Toast.LENGTH_SHORT).show();
                return;
            }
            // Create a ContentValues object where column names are the keys,
            // and transaction attributes from the editor are the values.

        // HashMap vs ContentValues below
        /*
            1)HashMap is a general utility class that resides in java.util.
            ContentValues on the other hand is a specific class in android.content
            designed to comply with Android classes like SQLiteDatabase and ContentResolver
            Note that they implement different interfaces according to aforementioned designation:
            - HashMap implements Cloneable and Serializable
            - ContentValues implements Parcelable
            2) ContentValues has a member that is HashMap with String keys:
               private HashMap<String, Object> mValues
            3) ContentValues has a number of methods to get and put typed values (like getAsFloat() etc)
        */
            ContentValues values = new ContentValues();
            values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON, person);
            values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM, item);
            values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE,mType );
            values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT, amount);

        if (mCurrentTransactionUri == null) { // new data Entry

            // Insert a new transaction into the provider, returning the content URI for the new transaction.
            Uri newUri = getContentResolver().insert(TransactionContract.TransactionEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_transaction_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_transaction_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            // EXISTING DATA
            // Otherwise this is an EXISTING transaction, so update the transaction with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.

            // gives 1 if successful updation
            int rowsAffected = getContentResolver().update(mCurrentTransactionUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_transaction_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_transaction_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }



    /* since -> implements LoaderManager.LoaderCallbacks<Cursor> so Override
        1) onCreateLoader
        2) onLoadFinished
        3) onLoaderReset
    */

    /*
    What is a cursor loader?
     # A CursorLoader runs an asynchronous query in the background against a ContentProvider ,
      and returns the results to the Activity or FragmentActivity from which it was called.
      This allows the Activity or FragmentActivity to continue to interact with the user while the query is ongoing.

     # A CursorLoader is a specialized member of Android’s loader framework specifically designed
      to handle cursors. In a typical implementation, a CursorLoader uses a ContentProvider
      to run a query against a database, then returns the cursor produced from the
     ContentProvider back to an activity or fragment.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Since the editor shows all transactions attributes, define a projection that contains
        // all columns (String[] as projection -> order not matter) from the transactions table
        String[] projection = {
                TransactionContract.TransactionEntry._ID,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT
         };

        // This loader will execute the ContentProvider's query method on a BACKGROUND THREAD
        return new CursorLoader(this,   // Parent activity context
                mCurrentTransactionUri,         // Query the content URI for the current Transaction
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.e("uri Extractor : ","name++items++types++amount");
        if (cursor == null || cursor.getCount() < 1) { // cursor is empty
            return;
        }
        if(cursor.moveToFirst()) {
            // Find the columns of transactions attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON);
            int itemsColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM);
            int typesColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE);
            int amountColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT);
             // Extract out the value from the Cursor for the given column index
             String name = cursor.getString(nameColumnIndex);
             String items = cursor.getString(itemsColumnIndex);
             int type = cursor.getInt(typesColumnIndex);
             int amount = cursor.getInt(amountColumnIndex);
             mPersonEditText.setText(name);
             mItemsEditText.setText(items);
             mAmountEditText.setText(Integer.toString(amount));
             // Type is a dropdown spinner, so map the constant value from the database
             // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
             // Then call setSelection() so that option is displayed on screen as the current selection.
             switch (type) {
                 case TransactionContract.TransactionEntry.TYPE_CREDIT:
                     mTypeSpinner.setSelection(1);
                     break;
                 case TransactionContract.TransactionEntry.TYPE_DEBIT:
                     mTypeSpinner.setSelection(2);
                     break;
                 default:
                     mTypeSpinner.setSelection(0);
                     break;
             }
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mPersonEditText.setText("");
        mItemsEditText.setText("");
        mAmountEditText.setText("");
        mTypeSpinner.setSelection(0);

    }

// OnTouchListener that listens for any user touches on a View, implying that they are modifying
// the view, and we change the mTransactionHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTransactionHasChanged = true;
            return false;
        }
    };

    // Dialog Box
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the transaction.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
        // If the transaction hasn't changed, continue with handling back button press
        if (!mTransactionHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    // delete menu option
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new transaction, hide the "Delete" menu item.
        if (mCurrentTransactionUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    // adding functionality to delete menu option
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the transaction.
                deleteTransaction();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the transaction.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the Transaction in the database.
     */
    private void deleteTransaction() {
        // Only perform the delete if this is an existing transaction.
        if (mCurrentTransactionUri != null) {
            // Call the ContentResolver to delete the transaction at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the transaction that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentTransactionUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_transaction_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_transaction_successful),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();
        }
    }}