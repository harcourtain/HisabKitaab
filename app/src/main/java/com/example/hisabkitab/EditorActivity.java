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
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Content URI for the existing pet (null if it's a new pet) */
    private Uri mCurrentTransactionUri;

    /** EditText field to enter the pet's name */
    private EditText mPersonEditText;

    /** EditText field to enter the pet's breed */
    private EditText mItemsEditText;

    /** EditText field to enter the pet's weight */
    private EditText mAmountEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mTypeSpinner;

    private TransactionDbHelper mTransactionDbHelper;  //  instance of PetDbHelper
    private static final int EXISTING_TRANSACTION_LOADER = 0;
    private boolean mTransactionHasChanged = false;
    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
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
            invalidateOptionsMenu();
        }else{
            setTitle(R.string.editor_activity_title_edit_transaction);
            getLoaderManager().initLoader(EXISTING_TRANSACTION_LOADER,null,this);// Edit Pet
        }

        // Find all relevant views that we will need to read user input from
        mPersonEditText = (EditText) findViewById(R.id.edit_transaction_name);
        mItemsEditText = (EditText) findViewById(R.id.edit_transaction_items);
        mAmountEditText = (EditText) findViewById(R.id.edit_transaction_amount);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);
//        constructing database pet.db with given schema
//        mPetDbHelper = new PetDbHelper(this);
        mPersonEditText.setOnTouchListener(mTouchListener);
        mItemsEditText.setOnTouchListener(mTouchListener);
        mAmountEditText.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();  // down list to select gender

//        getLoaderManager().initLoader(EXISTING_PET_LOADER,null,this);

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        if (mCurrentPetUri == null) {
//            // This is a new pet, so change the app bar to say "Add a Pet"
//            setTitle(getString(R.string.editor_activity_title_new_pet));
//
//            // Invalidate the options menu, so the "Delete" menu option can be hidden.
//            // (It doesn't make sense to delete a pet that hasn't been created yet.)
//            invalidateOptionsMenu();
//            return false;
//        } else { //other stuff
            // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
            return true;
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet(); // inserting current data to pet.db
//                Toast.makeText(this,"New Data Inserted To The Table",Toast.LENGTH_SHORT).show();
                finish(); // go back to mainactivity page
                // Do nothing for now
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
//                // Navigate back to parent activity (CatalogActivity)
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mTransactionHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
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
     * Get user input from editor and save new pet into database.
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
//            int gender = (int) mGenderSpinner.getSelectedItem();
       // Create a ContentValues object where column names are the keys,
            // and pet attributes from the editor are the values.
//        ||| using content provider insted of this direct method
//        SQLiteDatabase db = mPetDbHelper.getWritableDatabase();
        if(TextUtils.isEmpty(item)) item = "Unknown Item";

        if (mCurrentTransactionUri == null &&
                TextUtils.isEmpty(person) && TextUtils.isEmpty(item) &&
                TextUtils.isEmpty(amountString) && mType == TransactionContract.TransactionEntry.TYPE_UNKNOWN) {
            return;
        }
            if(person.equals("") || item.equals("") || amount==0 || mType ==0) {
                Toast.makeText(this,"Error Data",Toast.LENGTH_SHORT).show();
                return;
            }
            // Create a ContentValues object where column names are the keys,
            // and pet attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON, person);
            values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM, item);
            values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE,mType );
            values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT, amount);

        if (mCurrentTransactionUri == null) {

            // Insert a new pet into the provider, returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(TransactionContract.TransactionEntry.CONTENT_URI, values);
//        Long rowIdNew = db.insert(PetContract.PetEntry.TABLE_NAME,null,values);
//        Log.e("Editor Activity","row id "+rowIdNew);
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
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                TransactionContract.TransactionEntry._ID,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT
         };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentTransactionUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.e("uri Extractor : ","name++breed++gender++weight");
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if(cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON);
            int breedColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM);
            int genderColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE);
            int weightColumnIndex = cursor.getColumnIndex(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT);
             // Extract out the value from the Cursor for the given column index
             String name = cursor.getString(nameColumnIndex);
             String breed = cursor.getString(breedColumnIndex);
             int gender = cursor.getInt(genderColumnIndex);
             int weight = cursor.getInt(weightColumnIndex);
             mPersonEditText.setText(name);
             mItemsEditText.setText(breed);
             mAmountEditText.setText(Integer.toString(weight));
             // Gender is a dropdown spinner, so map the constant value from the database
             // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
             // Then call setSelection() so that option is displayed on screen as the current selection.
             switch (gender) {
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
// the view, and we change the mPetHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTransactionHasChanged = true;
            return false;
        }
    };
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
                // and continue editing the pet.
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
        // If the pet hasn't changed, continue with handling back button press
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
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentTransactionUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
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
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentTransactionUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
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