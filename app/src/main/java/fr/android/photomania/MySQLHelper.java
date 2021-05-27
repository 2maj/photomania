package fr.android.photomania;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SQLContract.Entry.TABLE_NAME + " (" +
                    SQLContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    SQLContract.Entry.COLUMN_PHOTO_PATH + " TEXT," +
                    SQLContract.Entry.COLUMN_PHOTO_LAT + " TEXT," +
                    SQLContract.Entry.COLUMN_PHOTO_LON + " TEXT," +
                    SQLContract.Entry.COLUMN_DESCRIPTION + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SQLContract.Entry.TABLE_NAME;


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static String DATABASE_NAME;

    public MySQLHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
        DATABASE_NAME = dbName;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
