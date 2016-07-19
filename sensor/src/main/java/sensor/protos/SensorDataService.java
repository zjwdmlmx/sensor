package sensor.protos;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface SensorDataService {
    @POST("sensor/data")
    Call<SensorDataResponse> uploadSensorData(@Body SensorDataGroup group, @Query("uid") String uid);

    @Multipart
    @POST("sensor/data")
    Call<SensorDataResponse> uploadSensorDataFile(@Part MultipartBody.Part sensorData, @Query("uid") String uid);
}
