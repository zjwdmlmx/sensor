package sensor.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import sensor.UserManager;

/**
 * add a new user
 */
public class AddUserActivity extends Activity {
    public static final int RESULT_CODE_OK = 1;
    public static final int RESULT_CODE_CANCEL = 2;
    private EditText usernameEdit;

    private SensorService theService = null;
    private Handler handler = new Handler();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            theService = ( (SensorService.SensorServiceBinder)service ).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            theService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.add_user_activity);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_layout);

        TextView title_text = (TextView) findViewById(R.id.header_title);
        if (title_text != null) {
            title_text.setText(R.string.add_user_activity_title);
        }

        usernameEdit = (EditText) findViewById(R.id.username_edit);

        bindService(new Intent(this, SensorService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    public void onClick_Create_user(View view) {
        String name = usernameEdit.getText().toString();
        if (name.length() > 0 && theService != null) {
            theService.getUserManager().addUser(name, new UserManager.OnAddUserListener() {
                @Override
                public void onSuccess() {
                    handler.post(()-> {
                        AddUserActivity.this.setResult(RESULT_CODE_OK);
                        AddUserActivity.this.finish();
                    });
                }

                @Override
                public void onFailed(String reason) {
                    handler.post(() -> Toast.makeText(AddUserActivity.this, "create user failed!", Toast.LENGTH_LONG).show());
                }
            });


        }
    }

    public void onClick_Cancel(View view) {
        setResult(RESULT_CODE_CANCEL);
        finish();
    }
}
