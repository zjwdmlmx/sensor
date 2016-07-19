package sensor.protos;


/**
 * The Sensor data
 */
public class SensorData {
    public double x;
    public double y;
    public double z;
    public long timestamp; // timestamp from sensor service
    public int state;

    public static final int USER_STATE_UNKNOWN  = 0;
    public static final int USER_STATE_RUN      = 1;
    public static final int USER_STATE_WALK     = 2;
    public static final int USER_STATE_STAND    = 3;
    public static final int USER_STATE_JUMP     = 4;
    public static final int USER_STATE_CYCLE    = 5;
    public static final int USER_STATE_CAR      = 6;
    public static final int USER_STATE_BUS      = 7;

    public SensorData() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.timestamp = 0;
        this.state = USER_STATE_UNKNOWN;
    }

    public SensorData(double x, double y, double z, long timestamp, int state) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
        this.state = state;
    }

    private String toCSV() {
        StringBuilder builder = new StringBuilder(128);
        return builder
                .append(x).append(',')
                .append(y).append(',')
                .append(z).append(',')
                .append(timestamp).append(',')
                .append(state).append("\n").toString();
    }

    @Override
    public String toString() {
        return toCSV();
    }

    public String toString(String type) {
        if (type.equals("csv")) {
            return toCSV();
        }

        // default to csv
        return toCSV();
    }


}
