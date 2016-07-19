package sensor.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Setting fragment
 */

public class SettingFragment extends Fragment {
    private SharedPreferences preferences;

    private EditText editAddress;
    private EditText editPort;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View v =  inflater.inflate(R.layout.setting_layout, container, false);
        preferences = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        initUI(v);
        bindListener(v);
        return v;
    }

    private void initUI(View v) {
        editAddress = (EditText) v.findViewById(R.id.setting_server_address);
        editPort    = (EditText) v.findViewById(R.id.setting_server_port);

        editAddress.setText(preferences.getString("server.address", "127.0.0.1"));
        editPort.setText(preferences.getString("server.port", "80"));
    }

    private void bindListener(View v) {
        v.findViewById(R.id.setting_save_btn).setOnClickListener(v1 -> {
            String address = editAddress.getText().toString();
            String port = editPort.getText().toString();
            // TODO: checking Edit text valid
            preferences.edit()
                    .putString("server.address", address)
                    .putString("server.port", port)
                    .apply();
            Toast.makeText(v1.getContext(), "Saved", Toast.LENGTH_SHORT).show();
        });
    }
}
