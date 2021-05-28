package fr.android.photomania;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class PhotoFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERM_REQUEST = 1;
    private final String KEY = "dbname";
    private String dbName;
    private boolean askedOnce = false;
    private Button btn_take;
    private Button btn_save;
    private ImageView affiche_photo;
    private String photopath;
    private Bitmap image;
    private TextView descriptionPhoto;
    private MySQLHelper dbHelper;

    private double latitude, longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    public PhotoFragment(String name) {
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        btn_take = (Button) view.findViewById(R.id.btn_take);
        btn_save = (Button) view.findViewById(R.id.btn_save);
        affiche_photo = (ImageView) view.findViewById(R.id.affiche_photo);

        descriptionPhoto = (TextView) view.findViewById(R.id.descript_photo);

        btn_save.setOnClickListener(savePicture);
        btn_take.setOnClickListener(takePicture);
        // instantiate the helper object to access the database
        dbHelper = new MySQLHelper(getContext(), dbName);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(PhotoFragment.this.getActivity());

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Log.d("FUSED", "New location");
                // get the latest location available
                Location location = locationResult.getLastLocation();
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                LatLng latLng = new LatLng(latitude, longitude);
                System.out.println(latLng);

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    private View.OnClickListener savePicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String textDescription = descriptionPhoto.getText().toString();
            System.out.println("Description : "+textDescription);
            MediaStore.Images.Media.insertImage(getContext().getContentResolver(), image, "Image save", textDescription);
            write(photopath, latitude, longitude, textDescription);
            Intent returnBtn = new Intent(getContext(),
                    MainActivity.class);
            startActivity(returnBtn);
        }
    };

    public void write(String photopath, Double lat, Double lon, String description) {
        // write two (key, value) pairs
        // Gets the data repository in write mode

        // it is potentially a long running operation => do it asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(SQLContract.Entry.COLUMN_PHOTO_PATH, photopath);
                values.put(SQLContract.Entry.COLUMN_PHOTO_LAT, String.valueOf(lat));
                values.put(SQLContract.Entry.COLUMN_PHOTO_LON, String.valueOf(lon));
                values.put(SQLContract.Entry.COLUMN_DESCRIPTION, description);

                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(SQLContract.Entry.TABLE_NAME, null, values);
            }
        }).start();
    }

    private View.OnClickListener takePicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dispatchTakePictureIntent();
        }
    };

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String time = new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date());
        if(getCameraInstance() != null){
            File photoDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File photofile = File.createTempFile("photo"+time, ".jpg", photoDir);
                photopath = photofile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(PhotoFragment.this.requireContext(),
                        PhotoFragment.super.getActivity().getApplicationContext().getPackageName()+".provider",
                        photofile);
                System.out.println(photoUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode== RESULT_OK){
            image = BitmapFactory.decodeFile(photopath);
            affiche_photo.setImageBitmap(image);
        }
    }



    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            System.out.println(Camera.getNumberOfCameras());
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(PhotoFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            requestLocationUpdates();
        else {
            String[] perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            ActivityCompat.requestPermissions(PhotoFragment.this.getActivity(), perms, PERM_REQUEST);
        }

        // check permissions before registering
        if (ActivityCompat.checkSelfPermission(PhotoFragment.this.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            //dispatchTakePictureIntent();
            System.out.println("check permissions before registering");
        else {
            String[] perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.CAMERA};

            ActivityCompat.requestPermissions(PhotoFragment.this.getActivity(), perms, PERM_REQUEST);
            System.out.println("give permissions");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(PhotoFragment.this.getActivity().getMainExecutor(), location -> {
                    // Got last known location. In some rare situations this can be null.

                    if (location != null) {
                        Log.d("FUSED", "Latest location found");
                        // Logic to handle location object
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    } else
                        Log.d("FUSED", "Last Location unsuccessful");
                });

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERM_REQUEST) {
            // check grantResults
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permissions resume");
            }
        }

    }


}