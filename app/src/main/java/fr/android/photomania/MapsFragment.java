package fr.android.photomania;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private static final int PERM_REQUEST = 1;
    private GoogleMap mMap;
    private Marker marker;
    private EditText latitudeText;
    private EditText longitudeText;

    private double latitude, longitude;

    // fused location version; don't forget to add the dependency to your build.gradle file
    // https://developers.google.com/android/guides/setup
    private FusedLocationProviderClient fusedLocationClient;
    private boolean askedOnce = false;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private MapView mapView;
    private Button btnLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        latitudeText = (EditText) view.findViewById(R.id.latitude);
        longitudeText = (EditText) view.findViewById(R.id.longitude);
        btnLocation = (Button) view.findViewById(R.id.btn_location);
        btnLocation.setOnClickListener(updateLocation);

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsFragment.this.getActivity());

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
                //System.out.println(latLng);

                System.out.println("** lat : " + latitude);
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Latitude : " + latitude + " Longitude : " + longitude  ));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        };

    }

    public View.OnClickListener updateLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            latitude = Double.valueOf(latitudeText.getText().toString());
            longitude = Double.valueOf(longitudeText.getText().toString());

            LatLng latLng = new LatLng(latitude, longitude);

            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Latitude : " + latitude + " Longitude : " + longitude  ));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(37.4219983,-122.084);
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(MapsFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            requestLocationUpdates();
        else {
            String[] perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            ActivityCompat.requestPermissions(MapsFragment.this.getActivity(), perms, PERM_REQUEST);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        askedOnce = true;

        if (requestCode == PERM_REQUEST) {
            // check grantResults
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(MapsFragment.this.getActivity().getMainExecutor(), location -> {
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
    public void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}