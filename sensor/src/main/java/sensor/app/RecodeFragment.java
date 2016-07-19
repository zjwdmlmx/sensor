package sensor.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.TreeMap;

import sensor.protos.SensorData;

/**
 * The Recode fragment
 */

public class RecodeFragment extends Fragment implements View.OnClickListener {
    public interface RecodeListener {
        void onStartRecodeClick(View view);
        void onStopRecodeClick(View view);
        void onActivityStateSelect(View view, Integer state);
    }

    // default empty RecodeListener
    private RecodeListener recodeListener = new RecodeListener() {
        @Override
        public void onStartRecodeClick(View view) {
            // do nothing
        }

        @Override
        public void onStopRecodeClick(View view) {
            // do nothing
        }

        @Override
        public void onActivityStateSelect(View view, Integer state) {
            // do nothing
        }
    };

    private TextView userLoggedText;
    private TextView userStateText;
    private TextView userRecodeText;

    /**
     * Button enable/disable mark bit
     */
    public static final int BUTTON_START_RECODE = 0x00000001;
    public static final int BUTTON_STOP_RECODE  = 0x00000002;
    public static final int BUTTON_NONE         = 0x00000000;

    /**
     * The text name of the user's state
     */
    private static final TreeMap<Integer, String> stateMaps = new TreeMap<Integer, String>(){
        {
            put(SensorData.USER_STATE_BUS, "Bus");
            put(SensorData.USER_STATE_RUN, "Run");
            put(SensorData.USER_STATE_WALK, "Walk");
            put(SensorData.USER_STATE_STAND, "Stand");
            put(SensorData.USER_STATE_CAR, "Car");
            put(SensorData.USER_STATE_CYCLE, "Cycle");
            put(SensorData.USER_STATE_JUMP, "Jump");
        }
    };

    // when user activity selected
    private final View.OnClickListener onActivitySelect = v -> {
        final Integer state = RecodeFragment.this.activityMaps.get(v.getId());
        if (state != null) {
            setUserStateShow(state);
            recodeListener.onActivityStateSelect(v, state);
        }
    };

    private final View.OnClickListener onStartRecode = v -> {
        setButtonStatus(BUTTON_STOP_RECODE);
        setUserRecodeShow(true);
        recodeListener.onStartRecodeClick(v);
    };

    private final View.OnClickListener onStopRecode = v -> {
        setButtonStatus(BUTTON_START_RECODE);
        setUserRecodeShow(false);
        recodeListener.onStopRecodeClick(v);
    };

    /**
     * The click message filter map
     */
    private final TreeMap<Integer, View.OnClickListener> clickListenerMaps = new TreeMap<Integer, View.OnClickListener>() {
        {
            put(R.id.stand_selector_btn, onActivitySelect);
            put(R.id.walk_selector_btn, onActivitySelect);
            put(R.id.run_selector_btn, onActivitySelect);
            put(R.id.car_selector_btn, onActivitySelect);
            put(R.id.bus_selector_btn, onActivitySelect);
            put(R.id.jump_selector_btn, onActivitySelect);
            put(R.id.cycle_selector_btn, onActivitySelect);
            put(R.id.start_recode_btn, onStartRecode);
            put(R.id.stop_recode_btn, onStopRecode);
        }
    };

    private final TreeMap<Integer, Integer> activityMaps = new TreeMap<Integer, Integer>() {
        {
            put(R.id.stand_selector_btn, SensorData.USER_STATE_STAND);
            put(R.id.walk_selector_btn, SensorData.USER_STATE_WALK);
            put(R.id.run_selector_btn, SensorData.USER_STATE_RUN);
            put(R.id.car_selector_btn, SensorData.USER_STATE_CAR);
            put(R.id.bus_selector_btn, SensorData.USER_STATE_BUS);
            put(R.id.jump_selector_btn, SensorData.USER_STATE_JUMP);
            put(R.id.cycle_selector_btn, SensorData.USER_STATE_CYCLE);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View v = inflater.inflate(R.layout.recode_layout, container, false);
        initUI(v);

        return v;
    }

    private void initUI(View v) {
        userLoggedText   = (TextView) v.findViewById(R.id.user_logged);
        userStateText    = (TextView) v.findViewById(R.id.user_state);
        userRecodeText   = (TextView) v.findViewById(R.id.user_recode);

        // bind OnClick Events
        int[] btns = new int[] {
                R.id.stand_selector_btn,
                R.id.walk_selector_btn,
                R.id.car_selector_btn,
                R.id.bus_selector_btn,
                R.id.cycle_selector_btn,
                R.id.run_selector_btn,
                R.id.jump_selector_btn,
                R.id.start_recode_btn,
                R.id.stop_recode_btn
        };

        for(int btn : btns) {
            v.findViewById(btn).setOnClickListener(this);
        }
    }

    /**
     * show the current user state selected by user self
     *
     * @param userState the user's state to set
     *                  @{link sensor.proto.SensorData}
     */
    public void setUserStateShow(int userState) {
        userStateText.setText(stateMaps.get(userState) == null ? "Unknown" : stateMaps.get(userState));
    }

    /**
     * Set the user name who login
     *
     * @param user the user's name to show
     */
    public void setUserLoggedShow(String user) {
        userLoggedText.setText(user == null ? "Not Logged" : user);
    }

    public void setUserRecodeShow(boolean recode) {
        userRecodeText.setText(recode?"Yes":"No");
    }

    public void setButtonStatus(int buttons) {
        Activity activity = getActivity();
        activity.findViewById(R.id.start_recode_btn).setEnabled( (buttons & BUTTON_START_RECODE) > 0);
        activity.findViewById(R.id.stop_recode_btn).setEnabled( (buttons & BUTTON_STOP_RECODE) > 0);
    }

    @Override
    public void onClick(View view) {
        View.OnClickListener listen = clickListenerMaps.get(view.getId());
        if (listen != null) {
            listen.onClick(view);
        }
    }

    public void setRecodeListener(RecodeListener listener) {
        this.recodeListener = listener;
    }
}
