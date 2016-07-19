package sensor.protos;

import java.util.ArrayList;
import java.util.List;


public class HistoryDataGroup {
    private List<HistoryData> data = new ArrayList<>();

    public List<HistoryData> getData() {
        return data;
    }

    public void setData(List<HistoryData> data) {
        this.data = data;
    }
}
