package sensor.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Observable;
import rx.schedulers.Schedulers;
import sensor.Cache;
import sensor.UserManager;
import sensor.models.SensorDataModel;
import sensor.net.Services;
import sensor.protos.SensorData;
import sensor.net.Transmitter;


/**
 * The Sensor Data collector service
 */
public class SensorService extends Service {
    public static String transmitServer = null;
    public static String transmitPort = null;

    public class SensorServiceBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    private static final String LOG_TAG = SensorService.class.getSimpleName();

    public volatile int state = SensorData.USER_STATE_UNKNOWN;

    private SensorServiceBinder mBinder = new SensorServiceBinder();

    private boolean sensorRecoding = false;
    private Observable transmitter;
    private UserManager userManager = new UserManager();
    private Cache<SensorDataModel> sensorDataModelCache = new Cache<>();

    private PowerManager.WakeLock wakeLock = null;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setWakeLock();
        setForeground();

        Services.reset(getDefaultServerURL());
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this).name("sensor.realm").build());
        Log.i("SensorService", "on create the service");

        transmitter = Transmitter.createObservable();
        transmitter.subscribeOn(Schedulers.io())
                .subscribe();

        sensorDataModelCache.setFlusher(SensorDataModel::saveMany);

        // app initial
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                SensorDataModel m = new SensorDataModel(
                        event.values[0],
                        event.values[1],
                        event.values[2],
                        event.timestamp,
                        state,
                        userManager.currentUser());
                sensorDataModelCache.put(m);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

    }

    @Override
    public void onDestroy() {
        Log.i("SensorService", "on destroy the service");
        super.onDestroy();

        releaseWakeLock();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SensorService", "on start command received");
        return START_STICKY;
    }

    // setting android's wake lock for running at back
    private void setWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG);
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    // setting the service to Foreground model in case of system killed
    private void setForeground() {
        Intent[] startIntent = new Intent[]{new Intent(getApplicationContext(), SensorActivity.class)};
        startIntent[0].setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivities(this, 0, startIntent, 0);

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.sensor_notification);

        Notification note = new NotificationCompat.Builder(getApplicationContext())
                .setTicker("Sensor data recoding")
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(views)
                .build();

        startForeground(1, note);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    // start to recode
    public void setSensorListener() {
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorRecoding = true;
    }

    // stop to recode
    public void removeSensorListener() {
        sensorManager.unregisterListener(sensorEventListener);
        sensorRecoding = false;
    }

    public boolean isSensorRecoding() {
        return sensorRecoding;
    }

    private String getDefaultServerURL() {
        SharedPreferences p = getSharedPreferences("setting", MODE_PRIVATE);
        return "http://" + p.getString("server.address", "127.0.0.1") + ":" + p.getString("server.port", "80") + "/";
    }
}
