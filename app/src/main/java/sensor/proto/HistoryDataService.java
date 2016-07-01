package sensor.proto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by ikether on 6/30/16.
 */

public interface HistoryDataService {
    @POST("history/data")
    Call<HistoryDataResponse> uploadHistoryData(@Body HistoryDataGroup group);
}
