package sensor.proto;

import java.util.ArrayList;

/**
 * Created by ikether on 6/30/16.
 */

public class HistoryDataGroup {
    private ArrayList<HistoryData> data = new ArrayList<>();

    public ArrayList<HistoryData> getData() {
        return data;
    }

    public void setData(ArrayList<HistoryData> data) {
        this.data = data;
    }
}
