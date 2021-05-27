package fr.android.photomania;

import android.provider.BaseColumns;

public class SQLContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SQLContract() {
    }

    /* Inner class that defines the table contents */
    public static class Entry implements BaseColumns {
        public static final String DB_NAME = "photomania_db";
        public static final String TABLE_NAME = "photomania_table";
        public static final String COLUMN_PHOTO_PATH = "photo_path";
        public static final String COLUMN_DESCRIPTION = "description";
    }
}
