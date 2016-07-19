package sensor.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import sensor.Global;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User list and selector activity
 */
public class UserListActivity extends Activity {
    public static final int RESULT_CODE_OK = 1;
    public static final int RESULT_CODE_FAILED = 2;

    // the only one sensor recoding and data transmit service
    private SensorService theService = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            theService = ((SensorService.SensorServiceBinder)service).getService();
            findUsers();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            theService = null;
            Toast.makeText(UserListActivity.this, "Anyway the Service is gone", Toast.LENGTH_LONG).show();
        }
    };

    private ListView userListView;
    private int position = 0;
    private ArrayList< HashMap<String, Object> > userLists;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.user_list_activity);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_layout);

        Intent serviceIntent = new Intent(this, SensorService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        initTitle();

        userListView = (ListView) findViewById(R.id.user_list_view);
        userListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.e("sss", Integer.toString(position));
                UserListActivity.this.position = position;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == AddUserActivity.RESULT_CODE_OK) {
            findUsers();
        }
    }
    
    private void initTitle() {
        ImageButton user_btn = (ImageButton)findViewById(R.id.header_users_btn);
        TextView title_text = (TextView) findViewById(R.id.header_title);
        if (user_btn != null && title_text != null) {
            title_text.setText(R.string.user_select_activity_title);
            user_btn.setImageResource(R.mipmap.user_add);
            user_btn.setOnClickListener(v -> {
                if (theService != null) {
                    Intent intent = new Intent(UserListActivity.this, AddUserActivity.class);
                    startActivityForResult(intent, 0);
                }
            });
        }
    }

    public void onClick_Ok_user(View view) {
        Intent intent = new Intent(UserListActivity.this, SensorActivity.class);


        if (userLists != null) {
            intent.putExtra("user", (String)userLists.get(position).get("list_item_title"));
            setResult(RESULT_CODE_OK, intent);
        } else {
            setResult(RESULT_CODE_FAILED);
        }

        finish();
    }

    private void findUsers() {
        Global.pool.execute(() -> {
            String[] names = theService.getUserManager().listUsers();

            userLists = new ArrayList<>();

            for (String file : names) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("list_item_title", file);
                userLists.add(map);
            }

            final ListAdapter adapter = new SimpleAdapter(
                    UserListActivity.this, userLists, R.layout.user_list_item, new String[]{"list_item_title"},
                    new int[]{R.id.list_item_title});
            handler.post(() -> UserListActivity.this.userListView.setAdapter(adapter));
        });
    }

    public void onClick_Cancel(View view) {
        setResult(RESULT_CODE_FAILED);
        finish();
    }
}
