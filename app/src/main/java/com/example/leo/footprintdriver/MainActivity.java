package com.example.leo.footprintdriver;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.AppConfig;
import helper.SQLiteHandler;
import helper.SendLocation;
import helper.SendLocationService;
import helper.SessionManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mvc.imagepicker.*;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    //Variable Section
    private CollapsingToolbarLayout collapsingToolbarLayout = null;
    de.hdodenhof.circleimageview.CircleImageView circlImg;


    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int RC_CODE_PICKER = 2000;
    private static final int RC_CAMERA = 3000;
    protected static final String TAG = "location-updates-sample";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private TextView mCurrentAdd;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 10;
    private FusedLocationProviderApi fusedLocationProviderApi = FusedLocationApi;
    private SendLocation sendLoc = null;
    private SQLiteHandler db;
    private HashMap<String, String> driver;
    private ProgressDialog pDialog;

    private TextView tv_name,tv_bus_no;
    FloatingActionButton fab;
    GoogleMap mGmap;
    Marker mMarker;
    String bus_no,contact_no;
    PolylineOptions polylineoption;
    Polyline poli;
    ArrayList<LatLng> PolyLinePoints = new ArrayList<LatLng>();
    LatLng latLng;
    Button bt;
    Polyline line;
    Intent intent;
    Bitmap icon;

    private static final int LOCATION_REQUEST_CODE = 101;

    private boolean startFLAG =false;
    private SessionManager session;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpToolBar();
        tv_bus_no = (TextView)findViewById(R.id.tv_bus_no);
        tv_name = (TextView)findViewById(R.id.tv_driver_name);

        pDialog = new ProgressDialog(MainActivity.this);
        db=new SQLiteHandler(getApplicationContext());
        driver = db.getUserDetails();
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
        setDriver_info();



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
                    startFLAG =true;
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


    public void setDriver_info()
    {

        tv_bus_no.setText(driver.get("bus_no"));
        tv_name.setText(driver.get("first_name")+" "+driver.get("last_name"));

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
            case R.id.settings_menu:
                return true;
            /*case R.id.edit_details_menu:
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View content =  inflater.inflate(R.layout.bus_no_lay, null);
                final AppCompatEditText bus_no_up = (AppCompatEditText)content.findViewById(R.id.bus_no_edit);
                AlertDialog.Builder alert =  new AlertDialog.Builder(this);
                alert.setTitle("Enter Bus Number");
                alert.setView(content);
                alert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Runnable runnable = new Runnable() {
//                            @Override
//                            public void run() {
                                UpdateBus(bus_no_up.getText().toString());
//                            }
//                        };

                    }
                });

                alert.setNegativeButton("Cancel",null);

                AlertDialog dialog = alert.create();
                dialog.show();
                return true;*/
            case  R.id.log_out:
                    logoutUser();
                return true;


        }
        return false;
    }


    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }



    public void UpdateBus(final String bus_no)
    {
         RequestQueue requestQ;
         Request rqst ;

        String tag_string_req = "req_update_bus";


        contact_no = driver.get("contact_no");
        pDialog.setMessage("Updating Bus no ...");
        showDialog();
        requestQ = Volley.newRequestQueue(this);

        rqst = new StringRequest(Request.Method.POST,
                AppConfig.URL_BUS_UPDATE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                       Toast.makeText(MainActivity.this,"Updated",Toast.LENGTH_SHORT).show();

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Bus no update error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("bus_no",bus_no);

                params.put("contact_no", contact_no);

                return params;
            }

        };


        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq);
        requestQ.add(rqst);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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




    //setup toolbar and appearance
    public void setUpToolBar() {
        mCurrentAdd =(TextView)findViewById(R.id.tv_current_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("DPS");
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);
        toolbarTextAppernce();
    }


    private void toolbarTextAppernce() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
    }


    //checks play service
    public static boolean isPlayServicesAvailable(Context context) {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, resultCode, 2).show();
            return false;
        }
        return true;
    }


    ///initializes google map and some factory methods{

    private void initMap() {
        MapFragment mapfrg = (MapFragment) getFragmentManager().findFragmentById(R.id.map_of_bus);
        mapfrg.getMapAsync(this);

        icon = BitmapFactory.decodeResource(getResources(),R.drawable.bus);
        icon = Bitmap.createScaledBitmap(icon,100,100,true);
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
        mGmap.getUiSettings().setRotateGesturesEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

    }


//// end of factory methods}





///fusedlocation api on first connection

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mCurrentLocation=fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
        if(mCurrentLocation !=null) {
            if(mMarker!=null)
                mMarker.remove();
            List<Address> add;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            try {
                add = geocoder.getFromLocation(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),1);
                mCurrentAdd.setText(add.get(0).getLocality());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mGmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 18));
            LatLng ltlg = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            mMarker = mGmap.addMarker(new MarkerOptions().position(ltlg).icon(BitmapDescriptorFactory.fromBitmap(icon)));
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




    /////Every time location changes it updates the marker
    @Override
    public void onLocationChanged(Location location) {


        if(mCurrentLocation!=null && mMarker !=null)
            if(abs(mCurrentLocation.getLongitude() - location.getLongitude())>.000001 || abs(mCurrentLocation.getLatitude() - location.getLatitude()) > .000001 )
            {rotateMarker(mMarker,new LatLng(location.getLatitude(),location.getLongitude()),mCurrentLocation.bearingTo(location));}


        mCurrentLocation = location;


        //Toast.makeText(getApplicationContext(), String.valueOf(mCurrentLocation.getLatitude()) + " " + String.valueOf(mCurrentLocation.getLatitude()), Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        latLng = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                latLng).zoom(15).build();

        mGmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

//        intent = new Intent(MainActivity.this, SendLocationService.class);
        if(startFLAG) {
            bundle.putString("bus_no", "WB1234");
            bundle.putDouble("latitude", latLng.latitude);
            bundle.putDouble("longitude", latLng.longitude);
            intent.putExtra("bundle", bundle);
            startService(intent);
        }
        //Toast.makeText(getApplicationContext(),latLng.latitude + " " +latLng.longitude,Toast.LENGTH_LONG).show();
//        PolyLinePoints.add(latLng);
//        redrawLine();

    }







///animations

    private  void  rotateMarker(final Marker marker, final LatLng destination, final float bearing) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(800); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, destination);


                        marker.setRotation(bearing);
                        marker.setPosition(newPosition);

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            valueAnimator.start();
        }
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


