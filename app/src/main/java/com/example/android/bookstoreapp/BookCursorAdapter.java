package com.example.android.bookstoreapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BookContract;

import java.util.Locale;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
class BookCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
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
        //Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        final TextView quantityTextView = view.findViewById(R.id.quantity);
        Button saleButton = view.findViewById(R.id.sale_button);

        // Find the columns of book attributes that we're interested in
        final int idColumnIndex = cursor.getColumnIndex(BookContract.BookEntry._ID);
        final int nameColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRODUCT_NAME);
        final int priceColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);

        // Read the book attributes from the Cursor for the current book
        final int id = cursor.getInt(idColumnIndex);
        String bookName = cursor.getString(nameColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current book
        nameTextView.setText(bookName);
        priceTextView.setText(String.format(Locale.ENGLISH, "%d euro", price));
        quantityTextView.setText(String.format(Locale.ENGLISH, "%d", quantity));

        // Setup SALE Button click listener.
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the quantity is greater or equal to 1, when the SALE button is clicked,
                // the quantity is decreased by 1 (so it will be zero).
                // Otherwise, if the quantity is zero, show a toast message, informing
                // the user that the book is out of stock.
                if (quantity > 0) {
                    int newQuantity = quantity - 1;
                    quantityTextView.setText(String.valueOf(newQuantity));

                    //Form the content URI that represents the specific book that was clicked on,
                    // by appending the "id" (passed as input to this method) onto the
                    // {@link BookEntry#CONTENT_URI}.
                    Uri uriQuantity = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, id);

                    // Create a ContentValues object where column names are the keys,
                    // and book attributes from the editor are the values.
                    // Insert the new value and update the book with the new quantity
                    ContentValues values = new ContentValues();
                    values.put(BookContract.BookEntry.COLUMN_QUANTITY, newQuantity);
                    context.getContentResolver().update(uriQuantity, values, null, null);
                } else {
                    // The quantity is 0, so inform the user that the book is out of stock
                    Toast.makeText(context, "This book is out of stock!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}