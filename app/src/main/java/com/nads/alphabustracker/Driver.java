package com.nads.alphabustracker;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.BundleCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Driver extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks {
    private FirebaseAuth firebaseAuth;
    private final String LOG_TAG = "nadsTestapp";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderApi mFusedLocationClient = LocationServices.FusedLocationApi;
    private static final int REQUEST_LOCATION = 2;
    private LocationCallback mLocationCallback;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    protected Location mLastLocation;
    private static PendingResult<LocationSettingsResult> result;
    private static LocationSettingsRequest.Builder builder;
    private AddressResultReceiver mResultReceiver;
    private static final String REGISTER_URL = "https://us-central1-alphabustracker.cloudfunctions.net/alphatrack";
    private static final String TAG = "MainActivity";
    private static final String ID_TOKEN = "idToken";
    private static final String AREA_CODE = "area";
    private static final String S_E = "se";



                    protected void createLocationRequest() {
                        mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(300000);
                        mLocationRequest.setFastestInterval(300000);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    }

                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        setContentView(R.layout.activity_driver);
                        if (!isGooglePlayServicesAvailable()) {

                            finish();
                        } else {
                            createLocationRequest();
                            mGoogleApiClient = new GoogleApiClient
                                    .Builder(this)
                                    .addApi(Places.GEO_DATA_API)
                                    .addApi(Places.PLACE_DETECTION_API)
                                    .addConnectionCallbacks(this)
                                    .addOnConnectionFailedListener(this)
                                    .addApi(LocationServices.API)
                                    .enableAutoManage(this, this)
                                    .build();
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.map);
                            mapFragment.getMapAsync(this);
                            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                            if (ActivityCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                                    .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                // if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                //     Manifest.permission.ACCESS_FINE_LOCATION)) {

                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_PERMISSIONS_REQUEST_CODE);
                                // Show an explanation to the user *asynchronously* -- don't block
                                // this thread waiting for the user's response! After the user
                                // sees the explanation, try again to request the permission.

                                //}
                            }
            /*else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
                //return;
            }*/
                            final TextView textView11 = (TextView) findViewById(R.id.textView11);
                            if (locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)) {
                                locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        double log = location.getLongitude();
                                        double lat = location.getLatitude();
                                        LatLng latlng = new LatLng(lat, log);


                                        try {
                                            Geocoder geocoder = new Geocoder(Driver.this);
                                            List<Address> addressList = geocoder.getFromLocation(lat, log, 1);
                                            String srt = addressList.get(0).getLocality();
                                            //mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in Nadeem"));
                                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.6f));



                                            sender(srt);
                                            CameraPosition position = CameraPosition.builder()
                                                    .target(new LatLng(lat,
                                                            log))
                                                    .zoom(16f)
                                                    .bearing(0.0f)
                                                    .tilt(0.0f)
                                                    .build();
                                            //map.setMapType(MAP_TYPES[curMapTypeIndex]);
                                            //map.setTrafficEnabled(true);
                                            if (ActivityCompat.checkSelfPermission(Driver.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Driver.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                // TODO: Consider calling
                                                //    ActivityCompat#requestPermissions
                                                // here to request the missing permissions, and then overriding
                                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                //                                          int[] grantResults)
                                                // to handle the case where the user grants the permission. See the documentation
                                                // for ActivityCompat#requestPermissions for more details.
                                                return;
                                            }
                                            mMap.setMyLocationEnabled(true);
                                            // mFusedLocationClient.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationCallback, null);
                                            // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
                                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                                            textView11.setText(srt);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

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
                });
            } else if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double log = location.getLongitude();
                        double lat = location.getLatitude();
                        LatLng latlng = new LatLng(lat, log);

                        try { Geocoder geocoder = new Geocoder(Driver.this);
                            List<Address> addressList = geocoder.getFromLocation(lat, log, 1);
                            String srt = addressList.get(0).getLocality();
                            //  mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in Nadeem"));
                            // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.6f));
                            textView11.setText(srt);
                            //sender(srt);
                            CameraPosition position = CameraPosition.builder()
                                    .target(new LatLng(lat,
                                            log))
                                    .zoom(16f)
                                    .bearing(0.0f)
                                    .tilt(0.0f)
                                    .build();
                            //map.setMapType(MAP_TYPES[curMapTypeIndex]);
                            //map.setTrafficEnabled(true);
                            if (ActivityCompat.checkSelfPermission(Driver.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Driver.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            mMap.setMyLocationEnabled(true);
                                // mFusedLocationClient.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationCallback, null);
                                // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
                                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
                    });
                }


/*
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    //TextView textView = (TextView) findViewById(R.id.textView11);
                    for (Location location : locationResult.getLocations()) {
                        double lats = location.getLongitude();
                        double lags = location.getLatitude();
                        // startIntentService();
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(lats, lags, 1);
                            String str = addressList.get(0).getLocality();
                            //  mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in Nadeem"));
                            // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.6f));
                            //textView.setText(str);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);

          /*  FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            mUser.getToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                               // sender(idToken);
                            } else {
                                Log.d(TAG, "Error");
                                Toast.makeText(Driver.this, "failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });*/
            Button signout = (Button)findViewById(R.id.button4);
            signout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(Driver.this, AlphaLoginActivity.class);
                    intent.putExtra("finish", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            });
        }
    }
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        // TextView textView = (TextView) findViewById(R.id.textView11);

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.

            String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            //displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                //showToast(getString(R.string.address_found));
            }
        }

    }
public void onDestroy(){
    super.onDestroy();

}
    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        //startIntentService();
    }
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
            return;
        }
    }
    public void onConnectionSuspended(int sta) {
        Log.i(LOG_TAG, "Google cient suspended");
    }
    public void onConnected(Bundle connectionHint) {
      /*  mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(50);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);*/
       // LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        //builder.build();
/*
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            //     Manifest.permission.ACCESS_FINE_LOCATION)) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            //}
        }
            /*else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
                //return;
            }*/
        /*
        final TextView textView11 = (TextView) findViewById(R.id.textView11);
        if (locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double log = location.getLongitude();
                    double lat = location.getLatitude();
                    LatLng latlng = new LatLng(lat, log);

                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(lat, log, 1);
                        String str = addressList.get(0).getLocality();
                        //mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in Nadeem"));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.6f));
                        textView11.setText(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            });
        } else if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double log = location.getLongitude();
                    double lat = location.getLatitude();
                    LatLng latlng = new LatLng(lat, log);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(lat, log, 1);
                        String str = addressList.get(0).getLocality();
                        //  mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in Nadeem"));
                        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.6f));
                        textView11.setText(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            });
        }*/
       // initCamera(mLastLocation);
        // startIntentService();
    }

    private void initCamera(Location mLastLocation) {
        TextView textView = (TextView) findViewById(R.id.textView11);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        double lats = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude();
        double lags = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(lats, lags, 1);
            String str = addressList.get(0).getLocality();
            String srt = addressList.get(0).getSubLocality();
            //mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in Nadeem"));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.6f));
            textView.setText("lags");
            if (srt == null) {
                textView.setText(str);
            } else {
                textView.setText(srt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
     CameraPosition position = CameraPosition.builder()
                .target(new LatLng(lats,
                        lags))
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();
        //map.setMapType(MAP_TYPES[curMapTypeIndex]);
        //map.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);
        // mFusedLocationClient.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationCallback, null);
        // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
        // LatLng Perth = new LatLng(-31.952854, 115.857342);
        // mMap.addMarker(new MarkerOptions().position(Perth).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(Perth));
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
      /*  if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
*/
        Log.i(LOG_TAG, "Google api client failed");
    }
    @Override
   public void onLocationChanged(Location location) {

      /*  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            double latse = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude();

            double lagse = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude();
            Geocoder geocoder = new Geocoder(getApplicationContext());

            TextView textView = (TextView) findViewById(R.id.textView11);
            try {
                List<Address> addressList = geocoder.getFromLocation(latse, lagse, 1);

                final String str = addressList.get(0).getSubLocality();
                String srt = addressList.get(0).getLocality();
                final String se;
                textView.setText((int) lagse);
                if (str == null) {
                    //mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in Nadeem"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.6f));
                    se = srt;
                    //textView.setText(srt);

                } else {
                    //textView.setText(str);
                    textView.setText((int) lagse);
                    se = str;
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
            //startIntentService();
        }*/
    }
    public void sender(final String area){
       RequestQueue queue = Volley.newRequestQueue(Driver.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        }
                    }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "there's Error", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(AREA_CODE, area);
                return params;
            }
        };
        queue.add(stringRequest);

    }
    public void onStatusChanged(String s, int g, Bundle b) {

    }

    public void onProviderDisabled(String s) {

    }

    public void onProviderEnabled(String h) {

    }
  /*
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {


            switch (requestCode) {
                case REQUEST_CHECK_SETTINGS:
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            // All required changes were successfully made
                            break;
                        case Activity.RESULT_CANCELED:
                            // The user was asked to change settings, but chose not to
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
    */

}
