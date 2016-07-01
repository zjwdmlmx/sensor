package sensor.fs;

/**
 * read profile of sensor data storage
 */
public interface SensorDataStorageProfiler {
    /**
     * get the next to going  to upload file name
     *
     * @return the filename to ready to upload
     */
    String getUploadFilename();

    /**
     * move to next file to uploaded
     *
     * @throws Exception
     */
    void nextUploadFilename() throws Exception;

    /**
     * Clean all the uploaded file
     */
    void clean();

    /**
     * Getting the uuid of the user
     */
    String getUUID();
}
