package sensor.protos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface UserService {
    @POST("/user")
    Call<UserResponse> registUser(@Body User user);
}
