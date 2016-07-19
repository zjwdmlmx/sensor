package sensor;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensor.models.UserModel;
import sensor.net.Services;
import sensor.protos.User;
import sensor.protos.UserResponse;
import sensor.protos.UserService;


/**
 * management of users
 */
public class UserManager {
    private String currUser = null;

    public interface OnAddUserListener {
        void onSuccess();
        void onFailed(String reason);
    }

    public UserManager() { }

    public void addUser(final String name, final OnAddUserListener listener) {

        UserService service = Services.create(UserService.class);

        service.registUser(new User(name)).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                UserResponse res = response.body();
                if (res.getRes() == UserResponse.ERR_USER_EXISTS) {
                    listener.onFailed("User exists");
                } else if (res.getRes() == UserResponse.SUCCESS) {
                    Realm.getDefaultInstance().executeTransaction(realm -> {
                        UserModel m = realm.createObject(UserModel.class);
                        m.setUser(name);
                    });
                    listener.onSuccess();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                listener.onFailed(t.getMessage());
            }
        });
    }

    public boolean userExists(String name) {
        return false;
    }

    public String[] listUsers( ) {

        RealmResults<UserModel> results = Realm.getDefaultInstance().where(UserModel.class).findAll();
        String[] res = new String[results.size()];
        int i = 0;
        for (UserModel u : results) {
            res[i++] = u.getUser();
        }

        return res;
    }

    public String currentUser() {
        return currUser;
    }

    public void login(String name) {
        currUser = name;
    }
}
