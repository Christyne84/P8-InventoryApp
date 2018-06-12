package com.example.android.bookstoreapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.bookstoreapp.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOKS_TABLE =
                "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                        + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                        + BookEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                        + BookEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                        + BookEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                        + BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " INTEGER NOT NULL);";
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
