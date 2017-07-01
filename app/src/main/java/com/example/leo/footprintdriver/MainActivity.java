package com.example.leo.footprintdriver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.text.DecimalFormat;
import java.util.ArrayList;

import helper.SendLocation;
import helper.SendLocationService;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mvc.imagepicker.*;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    //Variable Section
    private CollapsingToolbarLayout collapsingToolbarLayout = null;
    de.hdodenhof.circleimageview.CircleImageView circlImg;


    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int RC_CODE_PICKER = 2000;
    private static final int RC_CAMERA = 3000;
    protected static final String TAG = "location-updates-sample";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 10;
    private FusedLocationProviderApi fusedLocationProviderApi = FusedLocationApi;

    private SendLocation sendLoc = null;
    FloatingActionButton fab;
    GoogleMap mGmap;
    Marker mMarker;
    PolylineOptions polylineoption;
    Polyline poli;
    ArrayList<LatLng> PolyLinePoints = new ArrayList<LatLng>();
    LatLng latLng;
    Button bt;
    Polyline line;
    Intent intent;
    private static final int LOCATION_REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpToolBar();


        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_REQUEST_CODE);

        if (!isPlayServicesAvailable(this)) {
            finish();
        }

        initMap();
        buildGoogleApiClient();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGmap != null && mCurrentLocation != null) {
                    mGmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 18));
                }

            }
        });

        circlImg = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.profile_image);
        circlImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.pickImage(MainActivity.this, "Pick an Image");

            }
        });


        bt = (Button) findViewById(R.id.bt_em);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            }
        });


    }

    protected void requestPermission(String permissionType, int requestCode) {
        int permission = ContextCompat.checkSelfPermission(this,
                permissionType);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permissionType}, requestCode
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Unable to show location - permission required", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.basic_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu:
                return true;
            case R.id.edit_details_menu:
                Intent intent = new Intent(this, EditDetailsActivity.class);
                startActivity(intent);
                return true;

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
        if (bitmap != null) {
            circlImg.setImageBitmap(bitmap);
            //TODO: need to save in DB..
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("DPS");
        toolbarTextAppernce();
    }


    private void toolbarTextAppernce() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
    }


    //checks
    public static boolean isPlayServicesAvailable(Context context) {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, resultCode, 2).show();
            return false;
        }
        return true;
    }


    ///Location based Function

    private void initMap() {
        MapFragment mapfrg = (MapFragment) getFragmentManager().findFragmentById(R.id.map_of_bus);
        mapfrg.getMapAsync(this);
    }


    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGmap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

    }






    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location locnorm=fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
        if(locnorm !=null) {
            mGmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locnorm.getLatitude(), locnorm.getLongitude()), 18));
            LatLng ltlg = new LatLng(locnorm.getLatitude(),locnorm.getLongitude());

            intent = new Intent(MainActivity.this, SendLocationService.class);
            Bundle bund =new Bundle();

            bund.putString("bus_no","WB1234");
            bund.putDouble("latitude",ltlg.latitude);
            bund.putDouble("longitude",ltlg.longitude);
            intent.putExtra("bundle",bund);
            startService(intent);


        }
       startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;

        //Toast.makeText(getApplicationContext(), String.valueOf(mCurrentLocation.getLatitude()) + " " + String.valueOf(mCurrentLocation.getLatitude()), Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        latLng = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
//        intent = new Intent(MainActivity.this, SendLocationService.class);
        bundle.putString("bus_no","WB1234");
        bundle.putDouble("latitude",latLng.latitude);
        bundle.putDouble("longitude",latLng.longitude);
        intent.putExtra("bundle",bundle);
        startService(intent);

        Toast.makeText(getApplicationContext(),latLng.latitude + " " +latLng.longitude,Toast.LENGTH_LONG).show();
        PolyLinePoints.add(latLng);
        redrawLine();

    }


    public  LatLng stablize(Location loc)
    {
        LatLng stLatln = new LatLng(Double.parseDouble(new DecimalFormat("##.####").format(loc.getLatitude())),
                Double.parseDouble(new DecimalFormat("##.####").format(loc.getLongitude())));
        return stLatln;
    }


    public void addMarker()
    {

        mMarker=mGmap.addMarker(new MarkerOptions().position(latLng));

    }

    private void redrawLine(){

        mGmap.clear();  //clears all Markers and Polylines

        PolylineOptions options = new PolylineOptions().width(12).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < PolyLinePoints.size(); i++) {
            LatLng point = PolyLinePoints.get(i);
            options.add(point);
        }
        addMarker(); //add Marker in current position
        line = mGmap.addPolyline(options); //add Polyline
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }
}


