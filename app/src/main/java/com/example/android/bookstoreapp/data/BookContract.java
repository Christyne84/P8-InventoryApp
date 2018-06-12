package com.example.android.bookstoreapp.data;

import android.provider.BaseColumns;

public final class BookContract {

    private BookContract() {
    }

    public static final class BookEntry implements BaseColumns {
        public final static String TABLE_NAME = "books";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "product";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_QUANTITY = "quantity";
        public final static String COLUMN_SUPPLIER_NAME = "supplier";
        public final static String COLUMN_SUPPLIER_PHONE_NUMBER = "phone";
    }
}
