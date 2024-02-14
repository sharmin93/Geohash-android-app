package com.example.geofenceandroidapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geofenceandroidapp.MainActivity;
import com.example.geofenceandroidapp.R;
import com.example.geofenceandroidapp.constant.AppConstants;
import com.example.geofenceandroidapp.utils.GpsUtils;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;
import java.util.Objects;

import ch.hsr.geohash.GeoHash;

public class LocationActivity extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationClient;

    private double latitude = 0.0, longitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Button btnLocation;
    private TextView locationView, geoHashView, centerGeoHashView, geoCheckView;
    private boolean isGPS = false;
    String userGeoHashString;
    String userGeoHashSevenString;
    Double destinationLat;
    Double destinationLong;
    String centerGeoHashString;
    String centerGeoHashSevenString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        initializedWidget();
        onButtonClickLister();

    }

    private void initializedWidget() {
        locationView = (TextView) findViewById(R.id.tv_location);
        this.btnLocation = (Button) findViewById(R.id.btnLocation);
        geoHashView = findViewById(R.id.tv_geohash);
        centerGeoHashView = findViewById(R.id.tv_center_geohash);
        geoCheckView = findViewById(R.id.tv_check);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds
//        destinationLat = 23.770261947175413;
        destinationLat = 23.7698085;
        destinationLong = 90.4071030;
        getCenterGeoHasAddress();
        getGPSStatus();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        locationView.setText(String.format(Locale.US, "%s - %s", latitude, longitude));

                        if (mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };
    }

    private void getGPSStatus() {
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                /** turn on GPS**/
                isGPS = isGPSEnable;
            }
        });
    }

    private void onButtonClickLister() {
        btnLocation.setOnClickListener(v -> {

            if (!isGPS) {
                Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
                return;
            }
//            getLocation();
            getCurrentLocation();
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LocationActivity.this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(LocationActivity.this, location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    locationView.setText(String.format(Locale.US, "%s - %s", latitude, longitude));
                    getUserGeoHasAddress(latitude, longitude);
                } else {
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

                }
            });


        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(5000)
                .build();
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LocationActivity.this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
            mFusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.getToken())
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                Location location = task.getResult();
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    locationView.setText(String.format(Locale.US, "%s - %s", latitude, longitude));
                                    getUserGeoHasAddress(latitude, longitude);
                                } else {

                                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                                }

                            } else {
                                Log.e(" task is not completed", "not completed");
                            }

                        }
                    });
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient.getLastLocation().addOnSuccessListener(LocationActivity.this, location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            locationView.setText(String.format(Locale.US, "%s - %s", latitude, longitude));
                            getUserGeoHasAddress(latitude, longitude);
                        } else {
                            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);


                        }
                    });

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void getUserGeoHasAddress(double latitude, double longitude) {

        GeoHash geohash = GeoHash.withCharacterPrecision(latitude, longitude, 8);
//        userGeoHashString = geohash.toBase32().substring(0, 4);
        userGeoHashSevenString = geohash.toBase32().substring(0, 7);
        userGeoHashString =  geohash.toBase32().toString();
        geoHashView.setText("user GeoHash Value:" + " " + userGeoHashString);
        calculateUserGeoHash(centerGeoHashString, userGeoHashString);

    }

    private void getCenterGeoHasAddress() {

        GeoHash geohash = GeoHash.withCharacterPrecision(destinationLat, destinationLong, 8);
//        centerGeoHashString = geohash.toBase32().substring(0, 4);
        centerGeoHashString = geohash.toBase32().toString();

        centerGeoHashView.setText("Center GeoHash Value:" + " " + centerGeoHashString);

    }

    private void calculateUserGeoHash(String centerGeoHashString, String userGeoHashString) {
        if (Objects.equals(centerGeoHashString, userGeoHashString)) {
            geoCheckView.setText("Inside Center Area :" + " " + "true");
        } else if (centerGeoHashString.startsWith("07")==(userGeoHashString.startsWith("07"))) {
            geoCheckView.setText("Inside Center Area 7:" + " " + "true");
            Toast.makeText(this,"you are inside center",Toast.LENGTH_LONG);
        } else {
            geoCheckView.setText("Inside Center Area :" + " " + "false");
            Toast.makeText(this,"you are inside center",Toast.LENGTH_LONG);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }
}
