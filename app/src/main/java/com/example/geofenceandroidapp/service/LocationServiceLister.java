package com.example.geofenceandroidapp.service;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.geofenceandroidapp.MainActivity;



public class LocationServiceLister  implements LocationListener {
    private Context context;
    public LocationServiceLister(MainActivity context) {
        this.context=context;
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(context, "" + location.getLatitude() + location.getLongitude(),
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(context, provider + "'s status changed to " + status + "!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(context, "Provider " + provider + " enabled!",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(context, "Provider " + provider + " disabled!",
                Toast.LENGTH_SHORT).show();
    }
}
