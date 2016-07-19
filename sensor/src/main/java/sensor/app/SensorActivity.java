package sensor.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * The Main Activity
 */

public class SensorActivity extends Activity {
    private ImageButton user_btn;

    public static final int REQ_CODE_SELECT_USER = 1;

    private RecodeFragment recodeFragment = new RecodeFragment();
    private SettingFragment settingFragment = new SettingFragment();
    private MoreFragment moreFragment = new MoreFragment();
    private TreeMap<Integer, Fragment> fragmentMap = new TreeMap<>();

    // the only one sensor recoding and data transmit service
    private SensorService sensorService = null;
    private SpotService spotService = null;

    private ServiceConnection sensorServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sensorService = ((SensorService.SensorServiceBinder)service).getService();
            setButtonInitStatus();
            setShowInitStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sensorService = null;
            recodeFragment.setButtonStatus(RecodeFragment.BUTTON_NONE);
            Toast.makeText(SensorActivity.this, "Anyway the Sensor Service is gone", Toast.LENGTH_LONG).show();
            finish();
        }
    };

    private ServiceConnection spotServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            spotService = ((SpotService.SpotServiceBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            spotService = null;
            Toast.makeText(SensorActivity.this, "Anyway the Spot Service is gone", Toast.LENGTH_LONG).show();
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_layout);

        user_btn = (ImageButton)findViewById(R.id.header_users_btn);
        TextView title_text = (TextView) findViewById(R.id.header_title);
        if (user_btn != null && title_text != null) {
            title_text.setText(R.string.main_activity_title);
            user_btn.setImageResource(R.mipmap.users);
            user_btn.setOnClickListener(v -> {
                Intent intent = new Intent(SensorActivity.this, UserListActivity.class);
                startActivityForResult(intent, REQ_CODE_SELECT_USER);
            });
        }

        initFragment();
        bindFragments(fragmentMap);
        initServices();

        Log.i("sensorActivity", "activity onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(sensorServiceConnection);
        unbindService(spotServiceConnection);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("SensorActivity", "activity onRestart ");


    }

    private void initServices() {
        // sensor service
        Intent sensorServiceIntent = new Intent(this, SensorService.class);
        startService(sensorServiceIntent);
        bindService(sensorServiceIntent, sensorServiceConnection, Context.BIND_AUTO_CREATE);

        // spot service
        Intent spotServiceIntent = new Intent(this, SpotService.class);
        startService(spotServiceIntent);
        bindService(spotServiceIntent, spotServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.content, moreFragment)
                .add(R.id.content, settingFragment)
                .add(R.id.content, recodeFragment)
                .commit();

        recodeFragment.setRecodeListener(new RecodeFragment.RecodeListener() {
            @Override
            public void onStartRecodeClick(View view) {
                if (sensorService != null) {
                    user_btn.setEnabled(false);
                    sensorService.setSensorListener();
                }
            }

            @Override
            public void onStopRecodeClick(View view) {
                if (sensorService != null) {
                    user_btn.setEnabled(true);
                    sensorService.removeSensorListener();
                }
            }

            @Override
            public void onActivityStateSelect(View view, Integer state) {
                if (sensorService != null && state != null) {
                    sensorService.state = state;
                }
            }
        });
        fragmentMap.put(R.id.recode_show_btn, recodeFragment);
        fragmentMap.put(R.id.setting_show_btn, settingFragment);
        fragmentMap.put(R.id.more_show_btn, moreFragment);
    }

    private void bindFragments(final Map<Integer, Fragment> map) {
        final Set<Integer> keys = map.keySet();
        for (int k : keys) {
            final int theKey = k;
            findViewById(k).setOnClickListener(v -> {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                for (int k1 : keys) {
                    transaction.hide(map.get(k1));
                }
                transaction.show(map.get(theKey));
                transaction.commit();
                // ??? v.setBackgroundColor(0x10101011);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SELECT_USER && resultCode == UserListActivity.RESULT_CODE_OK && data != null) {
            String user = data.getStringExtra("user");
            sensorService.getUserManager().login(user);

            setButtonInitStatus();
            recodeFragment.setUserLoggedShow(user);

        }
    }

    // setting the buttons enable/disable at the activity's start
    private void setButtonInitStatus() {
        if(sensorService != null && sensorService.getUserManager().currentUser() != null) {
            if (sensorService.isSensorRecoding()) {
                recodeFragment.setButtonStatus(RecodeFragment.BUTTON_STOP_RECODE);
                user_btn.setEnabled(false);
            } else {
                recodeFragment.setButtonStatus(RecodeFragment.BUTTON_START_RECODE);
                user_btn.setEnabled(true);
            }
        } else {
            recodeFragment.setButtonStatus(RecodeFragment.BUTTON_NONE);
        }
    }

    private void setShowInitStatus() {
        if (sensorService != null) {
            recodeFragment.setUserLoggedShow(sensorService.getUserManager().currentUser());
            recodeFragment.setUserRecodeShow(sensorService.isSensorRecoding());
            recodeFragment.setUserStateShow(sensorService.state);
        }
    }

}
