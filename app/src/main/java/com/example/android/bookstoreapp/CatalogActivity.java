package com.example.android.bookstoreapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.bookstoreapp.data.BookContract;
import com.example.android.bookstoreapp.data.BookDbHelper;

import java.util.Locale;

/**
 * Displays list of books that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    /**
     * Database helper that will provide us access to the database
     */
    private BookDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new BookDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the books database.
     */
    private void displayDatabaseInfo() {

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.COLUMN_PRODUCT_NAME,
                BookContract.BookEntry.COLUMN_PRICE,
                BookContract.BookEntry.COLUMN_QUANTITY,
                BookContract.BookEntry.COLUMN_SUPPLIER_NAME,
                BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        Cursor cursor = db.query(
                BookContract.BookEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        TextView displayView = findViewById(R.id.text_view_book);

        try {
            // Create a header in the Text View that looks like this:
            //
            // The books table contains <number of rows in Cursor> books.
            // _id - product - price - quantity - supplier - phone
            //
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.setText(String.format(Locale.ENGLISH, "%s %d %s\n\n", getString(R.string.inventory_contains), cursor.getCount(), getString(R.string.inventory_product)));
            displayView.append(BookContract.BookEntry._ID + "\t" +
                    BookContract.BookEntry.COLUMN_PRODUCT_NAME + "\t" +
                    BookContract.BookEntry.COLUMN_PRICE + "\t" +
                    BookContract.BookEntry.COLUMN_QUANTITY + "\t" +
                    BookContract.BookEntry.COLUMN_SUPPLIER_NAME + "\t" +
                    BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(BookContract.BookEntry._ID);
            int productColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentProduct = cursor.getString(productColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplier = cursor.getString(supplierColumnIndex);
                int currentSupplierPhone = cursor.getInt(supplierPhoneColumnIndex);

                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" +
                        currentID + " - " +
                        currentProduct + "\t" +
                        currentPrice + "\t" +
                        currentQuantity + "\t" +
                        currentSupplier + " \t" +
                        currentSupplierPhone));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    /**
     * Helper method to insert hardcoded book data into the database. For debugging purposes only.
     */
    private void insertBook() {
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and BookX's attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_PRODUCT_NAME, getString(R.string.dummy_product_name));
        values.put(BookContract.BookEntry.COLUMN_PRICE, getString(R.string.dummy_price_value));
        values.put(BookContract.BookEntry.COLUMN_QUANTITY, getString(R.string.dummy_quantity_value));
        values.put(BookContract.BookEntry.COLUMN_SUPPLIER_NAME, getString(R.string.dummy_supplier_name));
        values.put(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, getString(R.string.dummy_supplier_phone_number));

        // Insert a new row for BookX in the database, returning the ID of that new row.
        // The first argument for db.insert() is the books table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for BookX.
        long newRowId = db.insert(BookContract.BookEntry.TABLE_NAME, null, values);

        Log.v("CatalogActivity", "New row ID " + newRowId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}