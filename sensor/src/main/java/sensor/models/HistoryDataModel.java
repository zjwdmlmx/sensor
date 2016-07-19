package sensor.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import sensor.protos.HistoryData;

/**
 * History data model
 */

public class HistoryDataModel extends RealmObject {
    private long time;
    private short state;
    private double longitude;
    private double latitude;
    private String user;

    public static void saveMany(final List<HistoryDataModel> datas) {
        Realm.getDefaultInstance().executeTransaction(realm -> realm.copyToRealm(datas));
    }

    public static List<HistoryDataModel> getMany(int count) {
        Realm realm = Realm.getDefaultInstance();
        HistoryDataModel first = realm.where(HistoryDataModel.class).findFirst();
        if (first == null) return new ArrayList<>();

        List<HistoryDataModel> results = realm
                .where(HistoryDataModel.class)
                .equalTo("user", first.getUser())
                .findAll();

        final int len = results.size();

        count = count > len ? len : count;
        return results.subList(0, count);
    }

    public HistoryData toHistoryData() {
        HistoryData s = new HistoryData();
        s.setLatitude(this.latitude);
        s.setLongitude(this.longitude);
        s.setState(this.state);
        s.setTime(this.time);

        return s;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
