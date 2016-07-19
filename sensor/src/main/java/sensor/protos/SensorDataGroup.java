package sensor.protos;

import java.util.ArrayList;
import java.util.List;


public class SensorDataGroup {
    private List<SensorData> data = new ArrayList<>();

    public void setData(List<SensorData> data) {
        this.data = data;
    }

    public List<SensorData> getData() {
        return data;
    }


}
