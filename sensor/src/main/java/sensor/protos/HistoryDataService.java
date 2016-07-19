package sensor.protos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HistoryDataService {
    @POST("history/data")
    Call<HistoryDataResponse> uploadHistoryData(@Body HistoryDataGroup group, @Query("uid") String uid);
}
