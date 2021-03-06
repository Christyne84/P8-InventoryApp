package com.example.android.bookstoreapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.bookstoreapp.data.BookContract.BookEntry;

/**
 * Database helper for Books app. Manages database creation and version management.
 */
class BookDbHelper extends SQLiteOpenHelper {
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Constructs a new instance of {@link BookDbHelper}.
     *
     * @param context of the app
     */
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOKS_TABLE =
                "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                        + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                        + BookEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                        + BookEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                        + BookEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                        + BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
