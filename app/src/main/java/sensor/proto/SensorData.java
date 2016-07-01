package sensor.proto;


/**
 * The Sensor data
 */
public class SensorData {
    public double x;
    public double y;
    public double z;
    public long timestamp;
    public int state;

    public SensorData() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.timestamp = 0;
        this.state = 0;
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
