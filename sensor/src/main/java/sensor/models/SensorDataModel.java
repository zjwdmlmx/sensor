package sensor.models;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import sensor.protos.SensorData;

/**
 * Sensor data model with Realm Framework
 */

public class SensorDataModel extends RealmObject {
    private double x;
    private double y;
    private double z;
    private long timestamp;
    private int state;
    private String user;

    public static void saveMany(final List<SensorDataModel> datas) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(datas);
            }
        });
    }

    public static List<SensorDataModel> getMany(int count) {
        Realm realm = Realm.getDefaultInstance();
        SensorDataModel first = realm.where(SensorDataModel.class).findFirst();
        if (first == null) return new ArrayList<>();

        List<SensorDataModel> results = realm
                .where(SensorDataModel.class)
                .equalTo("user", first.getUser())
                .findAll();

        final int len = results.size();

        count = count > len ? len : count;
        return results.subList(0, count);
    }

    public SensorDataModel() {}

    public SensorDataModel(double x, double y, double z, long timestamp, int state, String user) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
        this.state = state;
        this.user = user;
    }

    public SensorData toSensorData() {
        SensorData s =  new SensorData();
        s.x = this.x;
        s.y = this.y;
        s.z = this.z;
        s.state = this.state;
        s.timestamp = this.timestamp;
        return s;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
