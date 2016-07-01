package sensor.proto;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SensorDataService {
    @POST("sensor/data")
    Call<SensorDataResponse> uploadSensorData(@Body SensorDataGroup group);

    @Multipart
    @POST("sensor/data")
    Call<SensorDataResponse> uploadSensorDataFile(@Part MultipartBody.Part sensorData);
}
