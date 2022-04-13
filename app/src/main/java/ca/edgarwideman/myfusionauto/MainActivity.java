package ca.edgarwideman.myfusionauto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ca.edgarwideman.myfusionauto.services.GpsServices;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private BroadcastReceiver GpsDataReceiver;
    private BroadcastReceiver statusReceiver;
    LatLng latlng = new LatLng(43.5638885, -80.6687718);
    private Marker marker = null;

    TextView speed;
    TextView gpsStatus;



    int zoom = 17;
    boolean firstFix = true;
    boolean autostarting = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapReady);


        speed = findViewById(R.id.tv_m_speed);
        gpsStatus = findViewById(R.id.tv_m_no_gps);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        int nightModeFlags =
                this.getApplicationContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                Log.d(TAG, "onMapReady: Night Mode ON");
                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.style_json));

                    if (!success) {
                        Log.e(TAG, "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Can't find style. Error: ", e);
                }        // Instantiates a new CircleOptions object and defines the center and radius
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                Log.d(TAG, "onMapReady: Night Mode OFf");
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                Log.d(TAG, "onMapReady: I have no idea if night mode is on or not. 🤷‍️");
                break;
        }

        // Add a marker in Sydney and move the camera
        if(marker == null){
            Log.d(TAG, "onMapReady: Setting Marker");
            marker = mMap.addMarker(new MarkerOptions()
                    .position(latlng)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    .flat(true));
        }
        else{
            marker.setPosition(latlng);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));
        mMap.setBuildingsEnabled(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(!isMyServiceRunning(GpsServices.class)) {
            Log.d(TAG, "onResume: Starting GpsService");
            Intent serviceIntent = new Intent(getApplicationContext(), GpsServices.class);
            serviceIntent.putExtra("intervals", "1");
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            Log.d(TAG, "onResume: Service Started");
        }

        if (GpsDataReceiver == null) {
            Log.d(TAG, "onResume: GpsDataReceiver is null");
            GpsDataReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (firstFix) {
                        Log.d(TAG, "onReceive: First fix");
                        gpsStatus.setVisibility(View.INVISIBLE);
                        firstFix = false;
                    }
                    double lat = (double) intent.getExtras().get("lat");
                    double lng = (double) intent.getExtras().get("lng");
                    float bearing = (float) intent.getExtras().get("bearing");
                    String Speed = intent.getExtras().get("speed").toString();
                    LatLng location = new LatLng(lat, lng);
                    if (marker == null) {
                        Log.d(TAG, "onReceive: Marker is null");
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title("Current Location")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                .flat(true));
                    } else {
                        Log.d(TAG, "onReceive: Set marker position");
                        marker.setPosition(location);
                    }
                    int speedInt = Integer.parseInt(Speed);
                    zoom = 11;
                    if(speedInt < 99){
                        zoom = 15;
                    } else if(speedInt < 79){
                        zoom = 16;
                    } else if(speedInt < 59){
                        zoom = 17;
                    } else if(speedInt < 49){
                        zoom = 18;
                    } else if(speedInt < 40){
                        zoom = 19;
                    } else if(speedInt < 30){
                        zoom = 20;
                    }
                    else if(speedInt < 65)
                    marker.setRotation(bearing);
                    speed.setText(Speed);
                    CameraPosition newCamPos = new CameraPosition(location,
                            zoom,
                            60,
                            bearing);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), 1000, null);
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
                    Log.d(TAG, "onReceive: Lat/Lng: " + lat + " | " + lng + " Bearing: " + bearing + " Speed: " + Speed);
                }
            };
        }
        if (statusReceiver == null) {
            statusReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String status = intent.getExtras().get("status").toString();
                    gpsStatus.setText(status);
                    Log.d(TAG, "onReceive: status: " + status);
                }
            };
        }
        registerReceiver(statusReceiver, new IntentFilter("status_update"));
        registerReceiver(GpsDataReceiver, new IntentFilter("location_update"));
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}