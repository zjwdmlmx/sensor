package sensor.proto;

/**
 * Created by ikether on 6/30/16.
 */

public class HistoryData {
    private long time;
    private short state;
    private double longitude;
    private double latitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public short getState() {
        return state;
    }

    public long getTime() {
        return time;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setState(short state) {
        this.state = state;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
