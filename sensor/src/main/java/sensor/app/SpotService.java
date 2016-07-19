package sensor.app;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sensor.protos.SensorData;

/**
 * Service to spot the user's activity at a timer
 */

public class SpotService extends Service {
    private List<SensorData> cache = new ArrayList<>(12000);
    private Handler handler = new Handler();
    private SensorManager manager;

    public class SpotServiceBinder extends Binder {
        public SpotService getService() {
            return SpotService.this;
        }
    }

    private SpotServiceBinder binder = new SpotServiceBinder();

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            cache.add(new SensorData(event.values[0],
                    event.values[1],
                    event.values[2],
                    event.timestamp,
                    SensorData.USER_STATE_UNKNOWN));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private Runnable endSpot = () -> {
        manager.unregisterListener(listener);

        Log.i("SpotService", "one spot");
        // TODO get the @{code cache} and spot the activity of user
    };

    private Runnable beginSpot = () -> {
        cache.clear();
        manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        handler.postDelayed(endSpot, 60000);
    };

    private TimerTask spotTask = new TimerTask() {
        @Override
        public void run() {
            handler.postAtFrontOfQueue(beginSpot);
        }
    };

    @Override
    public void onCreate() {
        manager = (SensorManager)getSystemService(SENSOR_SERVICE);

        new Timer().schedule(spotTask, 0, 300000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
