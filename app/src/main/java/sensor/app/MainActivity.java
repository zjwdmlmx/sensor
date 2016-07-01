package sensor.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import sensor.fs.SensorDataStorageRecorder;

import java.util.TreeMap;


public class MainActivity extends Activity {
    public static final int BUTTON_START_RECODE = 0x00000001;
    public static final int BUTTON_STOP_RECODE  = 0x00000002;
    public static final int BUTTON_NONE         = 0x00000000;

    public static final int USER_STATE_UNKNOWN  = 0;
    public static final int USER_STATE_RUN      = 1;
    public static final int USER_STATE_WALK     = 2;
    public static final int USER_STATE_STAND    = 3;
    public static final int USER_STATE_JUMP     = 4;
    public static final int USER_STATE_CYCLE    = 5;
    public static final int USER_STATE_CAR      = 6;
    public static final int USER_STATE_BUS      = 7;

    private static final int SCAN_TIMER = 500;

    private static final TreeMap<Integer, Integer> activityMaps = new TreeMap<>();
    private static final TreeMap<Integer, String> stateMaps = new TreeMap<>();

    static {
        // initial stateMaps
        stateMaps.put(USER_STATE_BUS, "Bus");
        stateMaps.put(USER_STATE_RUN, "Run");
        stateMaps.put(USER_STATE_WALK, "Walk");
        stateMaps.put(USER_STATE_STAND, "Stand");
        stateMaps.put(USER_STATE_CAR, "Car");
        stateMaps.put(USER_STATE_CYCLE, "Cycle");
        stateMaps.put(USER_STATE_JUMP, "Jump");
    }

    public static final int REQ_CODE_SELECT_USER = 1;

    private Intent serviceIntent = null;

    private Handler handler = new Handler();

    private TextView userLoggedText;
    private TextView userStateText;
    private TextView userRecodeText;
    private ImageButton user_btn;
    private TextView transmitIPText;
    private TextView transmitPortText;

    // the only one sensor recoding and data transmit service
    private SensorService theService = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            theService = ((SensorService.SensorServiceBinder)service).getService();
            MainActivity.this.setButtonInitStatus();
            setShowInitStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            theService = null;
            setButtonStatus(BUTTON_NONE);
            Toast.makeText(MainActivity.this, "Anyway the Service is gone", Toast.LENGTH_LONG).show();
            finish();
        }
    };

    private Runnable scanTask = new Runnable() {
        @Override
        public void run() {

            setTransmitState(SensorService.transmitServer, SensorService.transmitPort);

            handler.postDelayed(scanTask, SCAN_TIMER);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_layout);

        initUI();

        serviceIntent = new Intent(this, SensorService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        user_btn = (ImageButton)findViewById(R.id.header_users_btn);
        TextView title_text = (TextView) findViewById(R.id.header_title);
        if (user_btn != null && title_text != null) {
            title_text.setText(R.string.main_activity_title);
            user_btn.setImageResource(R.mipmap.users);
            user_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, UserListActivity.class);
                    startActivityForResult(intent, REQ_CODE_SELECT_USER);
                }
            });
        }

        // initial scan task
        handler.postDelayed(scanTask, SCAN_TIMER);

        // initial activityMap
        activityMaps.put(R.id.run_selector_btn, USER_STATE_RUN);
        activityMaps.put(R.id.walk_selector_btn, USER_STATE_WALK);
        activityMaps.put(R.id.stand_selector_btn, USER_STATE_STAND);
        activityMaps.put(R.id.jump_selector_btn, USER_STATE_JUMP);
        activityMaps.put(R.id.cycle_selector_btn, USER_STATE_CYCLE);
        activityMaps.put(R.id.car_selector_btn, USER_STATE_CAR);
        activityMaps.put(R.id.bus_selector_btn, USER_STATE_BUS);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        setButtonInitStatus();
        setShowInitStatus();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SELECT_USER && resultCode == UserListActivity.RESULT_CODE_OK && data != null) {
            String user = data.getStringExtra("user");
            SensorDataStorageRecorder recorder = theService.getUserManager().login(user);
            if (recorder != null) {
                theService.setRecorder(recorder);
                setButtonInitStatus();
                setUserLoggedShow(user);
            }
        }
    }

    public void onActivitySelect(View view) {
        final Integer state = activityMaps.get(view.getId());
        if (theService != null && state != null) {
            theService.state = state;
            setUserStateShow(state);
        }
    }

    public void setButtonStatus(int buttons) {
        findViewById(R.id.start_recode_btn).setEnabled( (buttons & BUTTON_START_RECODE) > 0);
        findViewById(R.id.stop_recode_btn).setEnabled( (buttons & BUTTON_STOP_RECODE) > 0);
    }

    public void onClick_StartRecode_btn(View view) {
        if (theService != null) {
            setButtonStatus(BUTTON_STOP_RECODE);
            setUserRecodeShow(true);
            user_btn.setEnabled(false);
            theService.setSensorListener();
        }

    }

    public void onClick_StopRecode_btn(View view) {
        if (theService != null) {
            setButtonStatus(BUTTON_START_RECODE);
            setUserRecodeShow(false);
            user_btn.setEnabled(true);
            theService.removeSensorListener();
        }

    }

    private void initUI() {
        userLoggedText = (TextView)findViewById(R.id.user_logged);
        userStateText = (TextView) findViewById(R.id.user_state);
        userRecodeText = (TextView) findViewById(R.id.user_recode);
        transmitIPText = (TextView) findViewById(R.id.transmit_server);
        transmitPortText = (TextView) findViewById(R.id.transmit_port);
    }

    // setting the buttons enable/disable at the activity's start
    private void setButtonInitStatus() {
        if(theService != null && theService.getUserManager().currentUser() != null) {
            if (theService.isSensorRecoding()) {
                setButtonStatus(BUTTON_STOP_RECODE);
            } else {
                setButtonStatus(BUTTON_START_RECODE);
            }
        } else {
            setButtonStatus(BUTTON_NONE);
        }
    }

    private void setShowInitStatus() {
        if (theService != null) {
            setUserLoggedShow(theService.getUserManager().currentUser());
            setUserRecodeShow(theService.isSensorRecoding());
            setUserStateShow(theService.state);
        }
    }

    // show the current user-set user-state
    private void setUserStateShow(int userState) {
        userStateText.setText(stateMaps.get(userState) == null ? "Unknown" : stateMaps.get(userState));
    }

    private void setTransmitState(String ip, String port) {
        transmitIPText.setText(ip == null ? "Unknown" : ip);
        transmitPortText.setText(port == null ? "Unknown" : port);
    }

    private void setUserLoggedShow(String user) {
        userLoggedText.setText(user == null ? "Not Logged" : user);
    }

    private void setUserRecodeShow(boolean recode) {
        userRecodeText.setText(recode?"Yes":"No");
    }
}
