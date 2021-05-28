package fr.android.photomania;

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

public class GalerieFragment extends Fragment {
    private MySQLHelper dbHelper;
    private final String KEY = "dbname";
    private String dbName;
    private TextView t;

    public GalerieFragment(String name) {
        dbName = name;
        Bundle b = new Bundle();
        b.putString(KEY, dbName);
        setArguments(b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_galerie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        t = view.findViewById(R.id.show_content_db);
        dbHelper = new MySQLHelper(getContext(), dbName);
        read();
    }


    public void showDetails(View view) {
        getActivity().setContentView(R.layout.fragment_image_details);
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

// How you want the results sorted in the resulting Cursor

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
                    final String photo_path = cursor.getString(cursor.getColumnIndex(SQLContract.Entry.COLUMN_PHOTO_PATH));
                    final String lat = cursor.getString(cursor.getColumnIndex(SQLContract.Entry.COLUMN_PHOTO_LAT));
                    final String lon = cursor.getString(cursor.getColumnIndex(SQLContract.Entry.COLUMN_PHOTO_LON));
                    final String description = cursor.getString(cursor.getColumnIndex(SQLContract.Entry.COLUMN_DESCRIPTION));


                    // display it
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            t.append(String.valueOf(id) + " : " + photo_path + " | " + lat + ", "+ lon + "|" + description + "\n");
                        }
                    });
                }
                cursor.close();
            }
        }).start();

    }
}