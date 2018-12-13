package com.example.namtn.mapoverlay;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class GoogleApiClientBase implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
        .OnConnectionFailedListener, LocationListener {

    //map
    public GoogleApiClient googleApiClient;
    private Location location;
    public static final int PLAY_SERVICE_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000;

    //permission
    private ArrayList<String> permissionToRequest;
    private ArrayList<String> permissionRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    //integer for permission result request
    private static final int ALL_PERMISSION_RESULT = 912;

    //Context
    private Context context;
    private Activity activity;

    public GoogleApiClientBase(Context context) {
        this.context = context;
        activity = (Activity) context;
        //create new data for permission get location android
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionToRequest = permissionToRequest(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionToRequest.size() > 0) {
                activity.requestPermissions(permissionToRequest.toArray(new
                        String[permissionToRequest.size()]), ALL_PERMISSION_RESULT);
            }
        }
        googleApiClient = new com.google.android.gms.common.api.GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private ArrayList<String> permissionToRequest(ArrayList<String> permissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : permissions) {
            if (!hashPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hashPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionToRequest.size() > 0) {
                return activity.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
            }
        }
        return true;
    }

    public void connectGoogleApiClient() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    public boolean checkPlayService() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                availability.getErrorDialog(activity, resultCode, PLAY_SERVICE_RESOLUTION_REQUEST);
            } else {
                activity.finish();
            }
        }
        return true;
    }

    public boolean removeLocationUpdate(){
        if (googleApiClient!= null && googleApiClient.isConnected()){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
