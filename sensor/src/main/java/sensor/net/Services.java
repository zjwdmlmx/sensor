package sensor.net;

import com.google.gson.Gson;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit custom service factory
 */

public final class Services {
    public static void reset(String baseUrl) {
        rest = builder.baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static Retrofit.Builder builder = new Retrofit.Builder();
    private static Retrofit rest = builder.baseUrl("http://192.168.31.6/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();


    public static <T> T create(Class<T> service) {
        return rest.create(service);
    }

    private Services() {}
}
