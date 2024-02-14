package com.example.geofenceandroidapp;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import ch.hsr.geohash.GeoHash;

public class MainActivity extends AppCompatActivity {
    private int REQUEST_LOCATION = 99;
    private Button addressButton;
    private TextView latLongView, centerGeohashView, userGeoHashValueView,tvCheck;

    private FusedLocationProviderClient fusedLocationProviderClient;
    Double destinationLat;
    Double destinationLong;
    String userGeoHashString;
    String centerGeoHashString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializedWidget();
        onLocationButtonClickLister();



    }
    private void initializedWidget() {
        addressButton = findViewById(R.id.bt_get_address);
        latLongView = findViewById(R.id.tv_lat_long);
        centerGeohashView = findViewById(R.id.tv_center_geoHash_value);
        userGeoHashValueView = findViewById(R.id.tv_user_geoHash_value);
        tvCheck = findViewById(R.id.tv_check);
        destinationLat = 23.770261947175413;
        destinationLong =90.40658576109468;
    }
    private void onLocationButtonClickLister() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getLastLocation();
                getCurrentLocation();
                getCenterGeoHasAddress();
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    String cityName = addresses.get(0).getLocality();
                                    String stateName = addresses.get(0).getAdminArea();
                                    String countryName = addresses.get(0).getCountryName();

                                    latLongView.setText("Lat:" + " " + addresses.get(0).getLatitude() + "" + "\n" + "Long:" + "  " + addresses.get(0).getLongitude());


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                    });

        } else {
            askPermission();
        }
    }


    private void getCurrentLocation() {
        CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(5000)
                .build();
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            askPermission();
            return;
        }
        fusedLocationProviderClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.getToken())
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            latLongView.setText("Lat:" + " " + location.getLatitude() + "" + "\n" + "Long:" + "  " + location.getLongitude());
                            getUserGeoHasAddress(location.getLatitude(), location.getLongitude());
                        } else {
                            Log.e(" task is not completed", "not completed");
                        }

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLastLocation();
                getCurrentLocation();
            } else {
                Toast.makeText(MainActivity.this, "Required Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

    }

    private void getCenterGeoHasAddress() {

        GeoHash geohash = GeoHash.withCharacterPrecision(destinationLat, destinationLong, 9);
        centerGeoHashString = geohash.toBase32().substring(0, 7);
        centerGeohashView.setText("Center GeoHash Value:" + " " + centerGeoHashString);
    }

    private void getUserGeoHasAddress(double latitude, double longitude) {

        GeoHash geohash = GeoHash.withCharacterPrecision(latitude, longitude, 9);
        userGeoHashString = geohash.toBase32().substring(0, 7);
        userGeoHashValueView.setText("user GeoHash Value:" + " " + userGeoHashString);
        calculateUserGeoHash(centerGeoHashString, userGeoHashString);
    }

    private void calculateDistance(Double destinationLat, Double destinationLong, double latitude, double longitude) {
        float[] results = new float[1];
        Location.distanceBetween(destinationLat, destinationLong,
                latitude, longitude, results);
        float distance = results[0];

        Log.e("distance between locations", "" + distance);

    }

    private void calculateUserGeoHash(String centerGeoHashString, String userGeoHashString) {
        if (Objects.equals(centerGeoHashString, userGeoHashString)) {
            Toast.makeText(MainActivity.this, "inside center point", Toast.LENGTH_LONG);
            tvCheck.setText("Inside Center Area :"+" "+"true");
        } else {
            Toast.makeText(MainActivity.this, "outside center point", Toast.LENGTH_LONG);
            tvCheck.setText("Inside Center Area :"+" "+"false");
        }

    }

}

