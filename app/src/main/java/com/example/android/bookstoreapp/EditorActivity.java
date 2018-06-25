package com.example.android.bookstoreapp;

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
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BookContract.BookEntry;

import java.util.Locale;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;
    /**
     * Global variable for quantity.
     */
    private int quantity;
    /**
     * Content URI for the existing book (null, if it's a new book)
     */
    private Uri mCurrentBookUri;
    /**
     * EditText field to enter the book's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the book's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the book's quantity
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the book's supplier
     */
    private EditText mSupplierNameEditText;
    /**
     * EditText field to enter the book's supplier phone number
     */
    private EditText mSupplierPhoneEditText;
    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;
    /**
     * Text watcher: when an EditText is changed, change the mBookHasChanged to true,
     * which means that the book data has changed
     */
    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mBookHasChanged = true;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the intent used to launch this activity, in order to find out
        // if we are creating a new book or we are editing an existing one
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent does NOT contain a book content URI,
        // then it means we are creating a new book
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar title to "Add a Book"
            setTitle(getString(R.string.editor_activity_title_new_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise, this is an existing book, so change the app bar title to "Edit Book"
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_book_name);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_book_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_book_supplier_name);
        mSupplierPhoneEditText = findViewById(R.id.edit_book_supplier_phone_number);
        Button orderButton = findViewById(R.id.order_button);
        Button plusButton = findViewById(R.id.editor_activity_plus_button);
        Button minusButton = findViewById(R.id.editor_activity_minus_button);

        // Setup text change listener on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.addTextChangedListener(mTextWatcher);
        mPriceEditText.addTextChangedListener(mTextWatcher);
        mQuantityEditText.addTextChangedListener(mTextWatcher);
        mSupplierNameEditText.addTextChangedListener(mTextWatcher);
        mSupplierPhoneEditText.addTextChangedListener(mTextWatcher);

        // Click listener to handle the order button.
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order();
            }
        });

        // Click listener to handle the minus button.
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
            }
        });

        // Click listener to handle the plus button.
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
            }
        });

    }

    /*
     * Get user input from editor and save the book into the database.
     */
    private boolean saveBook() {

        // Read from input fields.
        // Use trim to eliminate leading or trailing white space.
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString();

        // Check if this is supposed to be a new book and
        // check if all the fields in the editor are blank.
        if (mCurrentBookUri == null && TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(supplierPhoneString)) {
            // Since no fields were modified, we can return early without creating a new book.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return true;
        }

        // Check if any of the 5 required fields is empty
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierString) ||
                TextUtils.isEmpty(supplierPhoneString)) {

            // Find the views
            TextView requiredFieldNameEditText = findViewById(R.id.required_label_for_name_field);
            TextView requiredFieldQuantityEditText = findViewById(R.id.required_label_for_quantity_field);
            TextView requiredFieldPriceEditText = findViewById(R.id.required_label_for_price_field);
            TextView requiredFieldSupplierNameEditText = findViewById(R.id.required_label_for_supplier_name_field);
            TextView requiredFieldSupplierPhoneEditText = findViewById(R.id.required_label_for_supplier_phone_field);

            // Let the user know that all the fields are required to be filled in
            Toast.makeText(this, "Please enter all the details for the book", Toast.LENGTH_SHORT).show();

            // If there are empty fields, change the color of the caption "* required" to be
            // more visible. Also show an error sign and move the focus to that field,
            // so he can fill it in.
            if (TextUtils.isEmpty(nameString)) {
                requiredFieldNameEditText.setTextColor(getResources().getColor(R.color.colorAccent));
                mNameEditText.setError("Required field");
                mNameEditText.requestFocus();
            } else if (TextUtils.isEmpty(priceString)) {
                requiredFieldPriceEditText.setTextColor(getResources().getColor(R.color.colorAccent));
                mPriceEditText.setError("Required field");
                mPriceEditText.requestFocus();
            } else if (TextUtils.isEmpty(quantityString)) {
                requiredFieldQuantityEditText.setTextColor(getResources().getColor(R.color.colorAccent));
                mQuantityEditText.setError(null);
                mQuantityEditText.requestFocus();
            } else if (TextUtils.isEmpty(supplierString)) {
                requiredFieldSupplierNameEditText.setTextColor(getResources().getColor(R.color.colorAccent));
                mSupplierNameEditText.setError("Required field");
                mSupplierNameEditText.requestFocus();
            } else if (TextUtils.isEmpty(supplierPhoneString)) {
                requiredFieldSupplierPhoneEditText.setTextColor(getResources().getColor(R.color.colorAccent));
                mSupplierPhoneEditText.setError("Required field");
                mSupplierPhoneEditText.requestFocus();
            }
            return false;
        }

        // Create a ContentValues object where column names are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(BookEntry.COLUMN_PRICE, Integer.parseInt(priceString));
        values.put(BookEntry.COLUMN_QUANTITY, Integer.parseInt(quantityString));
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierString);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);

        // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
        if (mCurrentBookUri == null) {
            // Insert a new book into the provider, returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.editor_insert_book_failed, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    /**
     * This method is called when the plus "+" button is clicked, to increase quantity by 1.
     */
    private void increment() {
        String quantityString = mQuantityEditText.getText().toString().trim();
        // If the quantity already has a value, then increase it by 1.
        // Otherwise, set it to have an initial value of 0
        if ((!TextUtils.isEmpty(quantityString))) {
            quantity = Integer.valueOf(quantityString) + 1;
        } else {
            quantity = 0;
        }
        displayQuantity(quantity);
    }

    /**
     * This method is called when the minus "-" button is clicked, to decrease quantity by 1.
     */
    private void decrement() {
        String quantityString = mQuantityEditText.getText().toString().trim();
        // If the quantity already has a value, then decrease it by 1, only if the value is
        // greater or equal to 1. If the value is smaller than 1, show a toast to let the
        // user know that the quantity cannot be a negative number.
        // Otherwise, if no value was entered, set it to have an initial value of 0.
        if ((!TextUtils.isEmpty(quantityString))) {
            quantity = Integer.valueOf(quantityString);
            if (quantity < 1) {
                // Show an error message as a toast
                Toast.makeText(this, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show();
            } else {
                quantity--;
                displayQuantity(quantity);
            }
        } else {
            quantity = 0;
            displayQuantity(quantity);
        }
    }

    /**
     * This method displays the given quantity value on the screen.
     */
    private void displayQuantity(int number) {
        EditText quantityTextView = findViewById(R.id.edit_book_quantity);
        quantityTextView.setText(String.valueOf(number));
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * This method initializes the contents of the EditorActivity's options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called whenever an item from the options menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save the book into the database.
                // Exit the activity only if the book was saved
                if (saveBook()) {
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
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
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded,
        // saved or the option to keep editing.
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

    /**
     * Instantiate and return a new Loader for the given ID.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that contains all columns from the books table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        //This cursor will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,        // Parent activity context
                mCurrentBookUri,                    // The content URI of the current book
                projection,                         // The columns to return in the resulting cursor
                null,                       // Selection criteria
                null,                    // Selection criteria
                null);                     // Default sort order
    }

    /**
     * This method is called when the previously created loader has finished its load.
     *
     * @param loader The loader created in onCreateLoader
     * @param cursor The cursor from which to get the data.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = cursor.getString(supplierPhoneNumberColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(String.format(Locale.ENGLISH, "%d", price));
            mQuantityEditText.setText(String.format(Locale.ENGLISH, "%d", quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhoneNumber);
        }
    }

    /**
     * This method is called when the previously created loader is being reset.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The loader created in onCreateLoader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder, set the message, and set click
        // listeners for the positive (discard), negative (save) and neutral (keep editing)
        // buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNeutralButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Save" button, so save the book
                saveBook();
                if (saveBook()) {
                    // Exit activity only if the book was saved
                    finish();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
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
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    /**
     * Start intent to contact the supplier when Order button is pressed. Opens the dialer and
     * retrieves the phone number stored in the Supplier's Phone Number EditText field.
     */
    private void order() {
        String phoneNumber = mSupplierPhoneEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        // Start the intent only if there is an activity that can handle the intent AND there
        // is a number stored in the edit text
        if (intent.resolveActivity(getPackageManager()) != null && !phoneNumber.isEmpty()) {
            startActivity(intent);
            // if no number is stored in the edit text, show a toast to inform the user to add a
            // phone number before making a call
        } else if (phoneNumber.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_message_order_button_phone_empty),
                    Toast.LENGTH_LONG).show();
        }
    }
}