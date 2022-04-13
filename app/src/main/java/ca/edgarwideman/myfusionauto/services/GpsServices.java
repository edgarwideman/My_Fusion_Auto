package ca.edgarwideman.myfusionauto.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ca.edgarwideman.myfusionauto.GpsData;
import ca.edgarwideman.myfusionauto.MainActivity;
import ca.edgarwideman.myfusionauto.R;

import static ca.edgarwideman.myfusionauto.App.GpsService;

public class GpsServices extends Service {
    private static final Object REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String TAG = "GpsService";
    private LocationManager mLocationManager;


    Location lastLocation = new Location("last");
    GpsData data = new GpsData();

    double currentLon = 0;
    double currentLat = 0;
    double lastLon = 0;
    double lastLat = 0;
    int speed = 0;

    int updateInterval;

    String responseCode = "Connecting...";
    String satellites = "0/0";


    private LocationListener listener;
    private LocationManager locationManager;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                if (location.hasSpeed()) {
                    speed = (int) (location.getSpeed() * 3.6);
                    if(speed == 0){
                    }
                }
                Intent i = new Intent("location_update");
                i.putExtra("lat",  location.getLatitude());
                i.putExtra("lng", location.getLongitude());
                i.putExtra("speed", speed);
                i.putExtra("bearing", location.getBearing());
                Log.d(TAG, "onLocationChanged: Sending data");
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String s, int event, Bundle bundle) {
                Log.d(TAG, "onStatusChanged: ");
                String status = "";

                switch (event) {
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        Log.d(TAG, "onGpsStatusChanged: Satellite_event");


                        GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                        int satsInView = 0;
                        int satsUsed = 0;
                        Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
                        for (GpsSatellite sat : sats) {
                            satsInView++;
                            if (sat.usedInFix()) {
                                satsUsed++;
                            }
                        }
                        satellites = (satsUsed + " / " + satsInView);
                        if (satsUsed == 0) {
                            status = "Waiting for GPS";
                        }
                        break;

                    case GpsStatus.GPS_EVENT_STOPPED:
                        Log.d(TAG, "onGpsStatusChanged: Stoped");
                        status = "GPS_EVENT_STOPPED";
                        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                    showGpsDisabledDialog();
                        }
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        status = "GPS Acquired";
                        Log.d(TAG, "onGpsStatusChanged: First Fix");
                        break;
                }

                Intent i = new Intent("status_update");
                i.putExtra("status", status);
                i.putExtra("sats", satellites);
                sendBroadcast(i);
            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };




    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, GpsService)
                .setContentTitle("GPS Running")
                .setSmallIcon(R.drawable.ic_my_location_black_24dp)
                .setPriority(NotificationManagerCompat.IMPORTANCE_LOW)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        updateInterval = Integer.parseInt((String) intent.getExtras().get("intervals")) * 1000;
        Log.d(TAG, "onStartCommand: Intervals = " + updateInterval);


        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //noinspection MissingPermission
        Log.d(TAG, "onCreate: GPSERV " + updateInterval);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateInterval, 0, listener);

        //do heavy work on a background thread
        return START_STICKY;
    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
