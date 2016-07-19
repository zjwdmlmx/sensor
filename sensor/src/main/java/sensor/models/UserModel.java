package sensor.models;

import io.realm.RealmObject;

/**
 * Realm User model
 */

public class UserModel extends RealmObject {
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
