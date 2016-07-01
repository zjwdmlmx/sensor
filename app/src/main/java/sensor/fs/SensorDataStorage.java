package sensor.fs;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sensor.proto.SensorData;

/**
 * Sensor data's storage
 */
public abstract class SensorDataStorage {
    protected long maxSize = 1024000;
    protected long curSize = 0;
    /**
     * put one sensor data to the storage
     *
     * @param data see (@link sensor.proto.SensorData). The data to store.
     */
    public abstract void put(SensorData data) throws IOException;


    /**
     * finish and close the storage
     */
    public abstract void finish() throws IOException;

    /**
     * is the current size bigger than the max-size
     *
     * @return true if the sensor data's size if bigger than the max-size of The Storage
     */
    public final boolean isOverflow() {
        return curSize >= maxSize;
    }

    /**
     * Setting the max-size of the Storage
     *
     * @param maxsize the max-size of the storage to set.
     */
    public final void setMaxSize(long maxsize) {
        maxSize = maxsize;
    }

    /**
     * get the max-size of the storage
     *
     * @return the storage's max-size
     */
    public final long getMaxSize() {
        return this.maxSize;
    }

    /**
     * get the current size of the storage
     *
     * @return the current size of the storage
     */
    public final long getCurSize() {
        return curSize;
    }

    /**
     * sensor data storage with csv file format
     */
    static class CSVSensorDataStorage extends SensorDataStorage {
        BufferedOutputStream stream = null;
        final static int BUFFER_SIZE = 12288;

        public CSVSensorDataStorage(OutputStream stream) {
            this.stream = new BufferedOutputStream(stream, BUFFER_SIZE);
        }

        @Override
        public void put(SensorData data) throws IOException {
            byte[] towrite = data.toString().getBytes();
            stream.write(towrite);
            curSize += towrite.length;
        }

        @Override
        public void finish() throws IOException {
            stream.close();
        }
    }
}
