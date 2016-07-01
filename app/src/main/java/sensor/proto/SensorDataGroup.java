package sensor.proto;

import java.util.ArrayList;

/**
 * Created by ikether on 6/30/16.
 */

public class SensorDataGroup {
    private ArrayList<SensorData> data = new ArrayList<>();

    public void setData(ArrayList<SensorData> data) {
        this.data = data;
    }

    public ArrayList<SensorData> getData() {
        return data;
    }


}
