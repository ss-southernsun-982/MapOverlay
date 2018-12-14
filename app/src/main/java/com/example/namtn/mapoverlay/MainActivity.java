package com.example.namtn.mapoverlay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnTouchListener, GoogleMap.OnMapLongClickListener, GoogleMap
                .OnCameraMoveListener, GoogleMap.OnCameraMoveCanceledListener, GoogleMap
                .OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener {

    //map
    private SupportMapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private GoogleMap mMap;
    private Location currentLocation;
    public static final int PLAY_SERVICE_RESOLUTION_REQUEST = 9000;
    private static final long UPDATE_INTERVAL = 5000;
    private Polyline polyline;

    //drawer map
    FrameLayout fram_map;
    Boolean mapMove;
    private boolean screenLeave = false;
    int source = 0;
    int destination = 1;
    private ArrayList<LatLng> listLocationDraw;
    private ArrayList<LatLng> listLocation;
    private ArrayList<Polyline> polylineArrayList;
    String TAG = "DRAW MAP";
    private Projection projection;
    private double latitude, longitude, myLat, myLng;
    private PolygonOptions rectOptions;
    private Polygon polygon;
    private PolygonOptions polygonOptions;
    private MenuItem menuSearch;
    private int zoomWidth, zoomHeight, zoomPadding;
    private ArrayList<Marker> markers;
    private GroundOverlayOptions newarkMap;
    private GroundOverlay imageOverlay;
    private LatLngBounds bounds;
    private ArrayList<LatLng> listLocationInsideScreen;
    //permission
    private ArrayList<String> permissionToRequest;
    private ArrayList<String> permissionRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    //integer for permission result request
    private static final int ALL_PERMISSION_RESULT = 912;
    private boolean isMaptouched = false;

    //Scan wifi 9.0

    private boolean mLocationPermissionApproved = false;
    List<ScanResult> mAccessPointsSupporting80211mc;
    private WifiManager mWifiManager;
    private WifiScanReceiver mWifiScanReceiver;


    private Boolean typeSearch = true;
    private ArrayList<LatLng> listList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();
        mAccessPointsSupporting80211mc = new ArrayList<>();

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiScanReceiver = new WifiScanReceiver(mWifiManager, mLocationPermissionApproved,
                mAccessPointsSupporting80211mc);
        zoomWidth = getResources().getDisplayMetrics().widthPixels;
        zoomHeight = getResources().getDisplayMetrics().heightPixels;
        zoomPadding = 5; // offset from edges of the map 12% of screen

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fram_map = (FrameLayout) findViewById(R.id.fram_map);
        mapMove = false;
        listLocation = new ArrayList<>();
        listLocationDraw = new ArrayList<>();
        polylineArrayList = new ArrayList<>();
        listLocationInsideScreen = new ArrayList<>();
        markers = new ArrayList<>();
        fram_map.setOnTouchListener(this);
    }

    private void getPermission() {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionToRequest = permissionToRequest(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionToRequest.size() > 0) {
                requestPermissions(permissionToRequest.toArray(new
                        String[permissionToRequest.size()]), ALL_PERMISSION_RESULT);
            }
        }
        googleApiClient = new GoogleApiClient.Builder(this)
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
            return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        Log.d("LIFE_Cyrcle", "Start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPlayService()) {
            Toast.makeText(this, "Install play service", Toast.LENGTH_SHORT).show();
        }
        registerReceiver(
                mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Log.d("LIFE_Cyrcle", "Resum");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        unregisterReceiver(mWifiScanReceiver);
        Log.d("LIFE_Cyrcle", "Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LIFE_Cyrcle", "Stop");
    }

    public void onClickFindDistancesToAccessPoints(View view) {
        if (mLocationPermissionApproved) {
            Toast.makeText(this, "No wifi connection", Toast.LENGTH_SHORT).show();
            mWifiManager.startScan();

        } else {
            // On 23+ (M+) devices, fine location permission not granted. Request permission.
            Toast.makeText(this, "Location permission", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkPlayService() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                availability.getErrorDialog(this, resultCode, PLAY_SERVICE_RESOLUTION_REQUEST);
            } else {
                finish();
            }
        }
        return true;
    }

    @SuppressLint({"MissingPermission", "ClickableViewAccessibility"})
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMaxZoomPreference(50f);
        mMap.setTrafficEnabled(false);
        bounds = new LatLngBounds(
                new LatLng(10.385022, 106.361557),  //s
                new LatLng(11.174867, 107.019938)); //n
        newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.maphcm))
                .positionFromBounds(bounds)
                .transparency(0.0f);
        imageOverlay = mMap.addGroundOverlay(newarkMap);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        listLocation.add(new LatLng(10.827316269863829, 106.79021503776312));
        listLocation.add(new LatLng(10.968236781111319, 106.74283649772406));
        listLocation.add(new LatLng(10.76526305891829, 106.61237418651581));
        listLocation.add(new LatLng(10.908235588251706, 106.64739310741425));
        listLocation.add(new LatLng(10.502745464307432, 106.4867180585861));
        listLocation.add(new LatLng(10.855640641428007, 106.47023856639862));
        listLocation.add(new LatLng(10.745700155039904, 106.63022696971893));
        listLocation.add(new LatLng(10.825967423408425, 106.65219962596893));
        listLocation.add(new LatLng(11.3858050455127, 106.11773259937765));
        listLocation.add(new LatLng(11.956645507742296, 106.06934119015932));
        addMarker(listLocation);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            myLat = location.getLatitude();
            myLng = location.getLongitude();
            if (mMap != null) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(myLat, myLng)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLat, myLng)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10f));
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (currentLocation != null) {
        }

        getLocationUpdate();
    }

    private void getLocationUpdate() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Enable permission location", Toast.LENGTH_SHORT).show();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSION_RESULT:
                for (String perm : permissionToRequest) {
                    if (!hashPermission(perm)) {
                        permissionRejected.add(perm);
                    }
                }
                if (permissionRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionRejected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Enable location permission")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                                requestPermissions(permissionRejected.toArray(new
                                                                String[permissionRejected.size()]),
                                                        ALL_PERMISSION_RESULT);
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create()
                                    .show();
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mapMove) {
            float x = event.getX();
            float y = event.getY();

            int x_co = Math.round(x);
            int y_co = Math.round(y);

//                Projection projection = googleMap.getProjection();
            Point x_y_points = new Point(x_co, y_co);

            LatLng latLng = mMap.getProjection().fromScreenLocation(x_y_points);
            latitude = latLng.latitude;
            longitude = latLng.longitude;

            System.out.println("LatLng : " + latitude + " : " + longitude);

            LatLng point = new LatLng(latitude, longitude);

            int eventaction = event.getAction();
            switch (eventaction) {
                case MotionEvent.ACTION_DOWN:
                    // finger touches the screen
                    screenLeave = false;
//                            System.out.println("ACTION_DOWN");

//                            listLocationDraw.add(new LatLng(latitude, longitude));
                case MotionEvent.ACTION_MOVE:
                    // finger moves on the screen
//                            System.out.println("ACTION_MOVE");
                          /*  if (listLocationDraw.size()==3){
                                listLocationDraw.remove(1);
                            }*/

                    listLocationDraw.add(new LatLng(latitude, longitude));
                    screenLeave = false;
                    drawMap();
                case MotionEvent.ACTION_UP:

//                            System.out.println("ACTION_UP");
                    if (!screenLeave) {
                        screenLeave = true;
                        menuSearch.setTitle("Drawing...");
                    } else {
                        mapMove = false; // to detect map is movable
                        source = 0;
                        destination = 1;
                        menuSearch.setTitle("Search Draw");
//                        drawMapFinal();
                        if (listLocationDraw.size() > 5) {
                            drawPolygon();
                            Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    // finger leaves the screen
//                            mapMove = false; // to detect map is movable
//                            Draw_Map();
                    break;
                default:
                    break;
            }

            if (mapMove) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void drawMap() {
        if (listLocationDraw.size() > 2) {
            polyline = mMap.addPolyline(new PolylineOptions().add(listLocationDraw.get(source),
                    listLocationDraw.get
                            (destination)).width(4).color(ContextCompat.getColor(this, R.color
                    .colorRed)));
            polylineArrayList.add(polyline);
            source++;
            destination++;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_drawing, menu);
        menuSearch = menu.findItem(R.id.menu_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean search = false;
        switch (item.getItemId()) {
            case R.id.menu_clear:
                clearDrawing();
                typeSearch = true;
                break;
            case R.id.menu_search:
                typeSearch = false;
                menuSearch = item.setTitle("Draw");
                mapMove = !mapMove;
                Log.d("SEARCH_SSSSSSSSSSSS", typeSearch + " button");
                polygonOptions = new PolygonOptions();
                clearDrawing();
                fram_map.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d("LOcationImage", latLng.latitude + "," + latLng.longitude);
    }

    @Override
    public void onCameraMoveCanceled() {
        Log.d("MOVE_CAMERA", "camera move cancel");
    }

    @Override
    public void onCameraMove() {
        Log.d("MOVE_CAMERA", "camera move");
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            Log.d("MOVEEE", "The user gestured on the map.");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            Log.d("MOVEEE", "The user tapped something on the map.");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            Log.d("MOVEEE", "The app moved the camera.");
        }
        if (typeSearch) {
            showInBoundsScreen();
            Log.d("SEARCH_SSSSSSSSSSSS", typeSearch + " map");
        }
        Toast.makeText(this, "stated 1", Toast.LENGTH_SHORT).show();
        Log.d("OOOOOOOO", "stated 1");
    }

    @Override
    public void onCameraIdle() {
        int zoom = Math.round(mMap.getCameraPosition().zoom);
        if (zoom > 13) {
            if (mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE)
                mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
            if (mMap.isTrafficEnabled() == true) mMap.setTrafficEnabled(false);
            imageOverlay.setTransparency(1.0f);
        } else if (zoom > 8 && zoom < 14) {
            if (mMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL)
                mMap.setMapType(mMap.MAP_TYPE_NORMAL);
            if (mMap.isTrafficEnabled() == false) mMap.setTrafficEnabled(true);
            float op = 0.0f;
            switch (zoom) {
                case 9:
                case 13:
                    op = 0.6f;
                    break;
                case 10:
                case 12:
                    op = 0.3f;
                    break;
                case 11:
                    op = 0.0f;
                    break;
                default:
                    op = 0.0f;
                    break;
            }
            imageOverlay.setTransparency(op);
        } else if (zoom < 9) {
            if (mMap.isTrafficEnabled() == false) mMap.setTrafficEnabled(true);
            imageOverlay.setTransparency(1.0f);
        }
        Log.d("OOOOOOOO", "stated 2");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("OOOOOOOO", "stated 2 delay");
            }
        }, 2000);
    }

    public void showInBoundsScreen() {
        bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        listLocationInsideScreen.clear();
        for (int i = 0; i < listLocation.size(); i++) {
            if (bounds.contains(listLocation.get(i))) {
                //point is visible
                listLocationInsideScreen.add(listLocation.get(i));
            }
        }
        if (listLocationInsideScreen.size() > 0) {
            addMarker(listLocationInsideScreen);
            Log.d("FIND_SCREEN", "------>" + listLocationInsideScreen.size());
        } else {
            Toast.makeText(this, "Isn't location in here", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawPolygon() {
        checkLocationInsideDraw();
        for (int i = 0; i < polylineArrayList.size(); i++) {
            polylineArrayList.get(i).remove();
        }
        markers.clear();
        listLocationDraw.add(listLocationDraw.get(0));
        polygonOptions.addAll(listLocationDraw);
        polygonOptions.strokeColor(ContextCompat.getColor(this, R.color.colorRed));
        polygonOptions.strokeWidth(4);
        polygon = mMap.addPolygon(polygonOptions);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < listLocationDraw.size(); i++) {
            builder.include(listLocationDraw.get(i));
        }
        LatLngBounds bounds1 = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds1, zoomHeight,
                zoomWidth, zoomPadding));
    }

    public void checkLocationInsideDraw() {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (int i = 0; i < listLocation.size(); i++) {
            boolean isInside = PolyUtil.containsLocation(listLocation.get(i), listLocationDraw,
                    true);
            if (isInside) {
                latLngs.add(listLocation.get(i));
            }
        }
        Log.d("INSIDE", " " + latLngs.size());
        if (latLngs.size() == 0) {
            Toast.makeText(this, "Not location you want", Toast.LENGTH_SHORT).show();
        } else {
            addMarker(latLngs);
        }
    }

    public void clearDrawing() {
        if (listLocationDraw.size() >= 0 && polyline != null &&
                polygon != null) {
            listLocationDraw.clear();
            polygon.remove();
        }
        addMarker(listLocation);
    }

    public void addMarker(ArrayList<LatLng> list) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).remove();
        }
        markers.clear();
        for (int i = 0; i < list.size(); i++) {
            Marker marker = mMap.addMarker(new MarkerOptions().position(list.get(i)).icon
                    (BitmapDescriptorFactory.fromResource(R.drawable.ic_add_location_black_24dp)));
            markers.add(marker);
            builder.include(list.get(i));
        }
        bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoomHeight,
                zoomWidth, zoomPadding));
    }
}
