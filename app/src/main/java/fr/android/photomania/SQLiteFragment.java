package fr.android.photomania;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SQLiteFragment extends Fragment {

    private final String KEY = "dbname";
    private String dbName;
    private TextView t;
    private MySQLHelper dbHelper;

    public SQLiteFragment(String name) {
        dbName = name;
        Bundle b = new Bundle();
        b.putString(KEY, dbName);
        setArguments(b);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            dbName = getArguments().getString(KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_storage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // the fragment/view has been created by now
        t = view.findViewById(R.id.show_content);

        view.findViewById(R.id.read).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read();
            }
        });

        view.findViewById(R.id.write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write();
            }
        });

        // instantiate the helper object to access the database
        dbHelper = new MySQLHelper(getContext(), dbName);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    public void write() {
        // write two (key, value) pairs
        // Gets the data repository in write mode

        // it is potentially a long running operation => do it asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(SQLContract.Entry.COLUMN_PHOTO_PATH, "prénom");
                values.put(SQLContract.Entry.COLUMN_DESCRIPTION, "nom");

// Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(SQLContract.Entry.TABLE_NAME, null, values);
            }
        }).start();


    }

    public void read() {
        // read everything
        // it is potentially a long running operation => do it asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
                String[] projection = {
                        BaseColumns._ID,
                        SQLContract.Entry.COLUMN_PHOTO_PATH,
                        SQLContract.Entry.COLUMN_DESCRIPTION
                };

// Filter results WHERE "title" = 'My Title'
                String selection = SQLContract.Entry.COLUMN_PHOTO_PATH + " = ?";
                String[] selectionArgs = {"My Title"};

// How you want the results sorted in the resulting Cursor
                String sortOrder =
                        SQLContract.Entry.COLUMN_DESCRIPTION + " DESC";

                Cursor cursor = db.query(
                        SQLContract.Entry.TABLE_NAME,   // The table to query
                        null,             // The array of columns to return (pass null to get all)
                        null,              // The columns for the WHERE clause
                        null,          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        null               // The sort order
                );

                // runOnUiThread is a wrapper method that uses the handler and the message queue to update the interface
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        t.setText("");
                    }
                });

                while (cursor.moveToNext()) {
                    final long id = cursor.getLong(cursor.getColumnIndex(SQLContract.Entry._ID));
                    final String nm = cursor.getString(cursor.getColumnIndex(SQLContract.Entry.COLUMN_PHOTO_PATH));
                    final String snm = cursor.getString(cursor.getColumnIndex(SQLContract.Entry.COLUMN_DESCRIPTION));

                    // display it
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            t.append(String.valueOf(id) + " : " + nm + " " + snm + "\n");
                        }
                    });
                }
                cursor.close();
            }
        }).start();

    }
}