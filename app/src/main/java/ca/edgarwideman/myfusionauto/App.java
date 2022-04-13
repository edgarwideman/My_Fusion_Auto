package ca.edgarwideman.myfusionauto;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String GpsService = "gps_service";
    public static final String BluetoothService = "bluetooth_service";
    public static final String AirSeederDataService = "air_seeder_data_service";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }


    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            //GPS Service
            NotificationChannel gpsService = new NotificationChannel(
                    GpsService,
                    "GPS Service",
                    NotificationManager.IMPORTANCE_NONE
            );
            gpsService.setDescription("Foreground service notification for GPS");

            //Bluetooth Service
            NotificationChannel bluetoothService = new NotificationChannel(
                    BluetoothService,
                    "Bluetooth Service",
                    NotificationManager.IMPORTANCE_NONE
            );
            bluetoothService.setDescription("Foreground service notification for Bluetooth connection");

            //Bluetooth Service
            NotificationChannel airSeederDataService = new NotificationChannel(
                    AirSeederDataService,
                    "AirSeeder Data Processor",
                    NotificationManager.IMPORTANCE_NONE
            );
            bluetoothService.setDescription("Foreground service notification for Air Seeder data processor");

            //Create notification channels.
            manager.createNotificationChannel(airSeederDataService);
            manager.createNotificationChannel(gpsService);
            manager.createNotificationChannel(bluetoothService);
        }
    }
}
