package sensor.fs;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import sensor.proto.SensorData;

/**
 * write sensor data to the sensor data storage
 */
public interface SensorDataStorageRecorder {
    /**
     * saved one sensor data
     *
     * @param data The sensor data to saved
     * @throws IOException
     * @throws TransformerException
     */
    void put(SensorData data) throws IOException, TransformerException;
}
