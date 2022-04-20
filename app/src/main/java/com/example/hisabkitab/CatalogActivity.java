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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hisabkitab.data.TransactionContract;
import com.example.hisabkitab.data.TransactionDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private TransactionDbHelper mTransactionDbHelper;
    // for cursor loader we add
    private static final int TRANSACTION_LOADER = 0;
    TransactionCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        // find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.text_view_pet);

        // Find and set empty view in the ListView , so that it only when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the cursor.
        // There is no pet data yet (until Loader finishes ) so pass in null for cursor.
        mCursorAdapter = new TransactionCursorAdapter(this,null);
        petListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // From the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(TransactionContract.TransactionEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });
//        petListView.setOnClickListener((adapterView, view, position, id)->{
//Log.e("id",id+" ");
//Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);
//
//Uri currentPetUri;
//                    currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI,
//                            id);
//
//                    intent.setData(currentPetUri);
//
//startActivity(intent);
//        }
//        );

        // kick off the loader
//        LoaderManager.getInstance(this).initLoader(0, null, this);
        getLoaderManager().initLoader(TRANSACTION_LOADER, null, this);
//        displayDatabaseInfo();
//        mPetDbHelper = new PetDbHelper(this);

//        SQLiteDatabase db = mDbHelper.getReadableDatabase();
    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        displayDatabaseInfo();
//    }
    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
      */
    // display Table data
//    private void displayDatabaseInfo() {
//        // To access our database, we instantiate our subclass of SQLiteOpenHelper
//        // and pass the context, which is the current activity.
////        PetDbHelper mDbHelper = new PetDbHelper(this);
//
//        // Create and/or open a database to read from it
////        SQLiteDatabase db = mDbHelper.getReadableDatabase();
//
//        // Perform this raw SQL query "SELECT * FROM pets"
//        // to get a Cursor that contains all rows from the pets table.
////        Cursor cursor = db.rawQuery("SELECT * FROM " + PetContract.PetEntry.TABLE_NAME, null);
////        Above 3 lines Are About RawQuery
//
//        String[] projection = {
//                PetEntry._ID, //Just add this line !
//                PetEntry.COLUMN_PET_NAME,
//                PetEntry.COLUMN_PET_BREED,
//                PetEntry.COLUMN_PET_GENDER,
//                PetEntry.COLUMN_PET_WEIGHT};
////        Cursor cursor = db.query(PetContract.PetEntry.TABLE_NAME,  // making cursor object
////                projection,
////                null,
////                null,
////                null,
////                null,
////                null);
//        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI,projection,null,null,null);
////        TextView displayView = (TextView) findViewById(R.id.text_view_pet);
//
//        // Find the ListView which will be populated with the pet data
//        ListView petListView = (ListView) findViewById(R.id.text_view_pet); //doubt
//
//
//        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.   Koii item nahi hai tab
//        View emptyView = findViewById(R.id.empty_view);
//        petListView.setEmptyView(emptyView);
//
//
//        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
//        PetCursorAdapter adapter = new PetCursorAdapter(this, cursor);
//
//        // Attach the adapter to the ListView.
//        petListView.setAdapter(adapter);
//
////        try {
////            // Display the number of rows in the Cursor (which reflects the number of rows in the
////            // pets table in the database).
////            // Display the number of rows in the Cursor (which reflects the number of rows in the
////            // pets table in the database).
////
////            // Create a header in the Text View that looks like this:
////            //
////            // The pets table contains <number of rows in Cursor> pets.
////            // _id - name - breed - gender - weight
////            //
////            // In the while loop below, iterate through the rows of the cursor and display
////            // the information from each column in this order.
//////
//////            displayView.setText("The pets table contains " + cursor.getCount() + " pets.\n\n");
//////            displayView.append(PetContract.PetEntry._ID + " - " +
////                    PetContract.PetEntry.COLUMN_PET_NAME + " - " +
////                    PetEntry.COLUMN_PET_BREED + " - " +
////                    PetEntry.COLUMN_PET_GENDER + " - " +
////                    PetEntry.COLUMN_PET_WEIGHT + "\n");
////
////            // Figure out the index of each column
////            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
////            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
////            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
////            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
////            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
////
////            // Iterate through all the returned rows in the cursor
////            while (cursor.moveToNext()) {
////                // Use that index to extract the String or Int value of the word
////                // at the current row the cursor is on.
////                int currentID = cursor.getInt(idColumnIndex);
////                String currentName = cursor.getString(nameColumnIndex);
////                String currentBreed = cursor.getString(breedColumnIndex);
////                int currentGender = cursor.getInt(genderColumnIndex);
////                int currentWeight = cursor.getInt(weightColumnIndex);
////                // Display the values from each column of the current row in the cursor in the TextView
//////                displayView.append(("\n" + currentID + " - " +currentName + " - " +
//////                        currentBreed + " - " +
//////                        currentGender + " - " +
//////                        currentWeight));
////            }
////        } finally {
////            // Always close the cursor when you're done reading from it. This releases all its
////            // resources and makes it invalid.
//////            cursor.close();
////        }
//    }
    // adding overflow menu aka menu in android
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                 insertPet();
//                displayDatabaseInfo();

                // Do nothing for now
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                deleteAllPets();
                Toast.makeText(this,"All Transaction Data Deleted In The Table",Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(TransactionContract.TransactionEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from transaction database");
    }

    // taking data form edit text and inserting in database\
    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPet() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
//       SQLiteDatabase db = mPetDbHelper.getWritableDatabase() ;

        ContentValues values= new ContentValues();  //Inseting int table of database -pet.db
        // id not given because it will override itslef
        values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON," Kirana Store");
        values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE, TransactionContract.TransactionEntry.TYPE_DEBIT);
        values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT,80);
        values.put(TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM,"FastCard , Matches , GoodNight Refill");
        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(TransactionContract.TransactionEntry.CONTENT_URI,values);
        // insertion method for database table
        // returns -1 if data is not inserted , else insert the data and return row id
//        long newRowId =  db.insert(PetContract.PetEntry.TABLE_NAME,null,values);
//        Log.e("CatalogActivity","New Row "+newRowId);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection = {
                TransactionContract.TransactionEntry._ID, //Just add this line !
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_PERSON,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_ITEM,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_TYPE,
                TransactionContract.TransactionEntry.COLUMN_TRANSACTION_AMOUNT};
        return new CursorLoader(this,
                TransactionContract.TransactionEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
         mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}


//  all code present here
//  https://github.com/udacity/ud845-Pets/tree/a53dd16846606a7980c8569fc23205e35fa85ea2/app