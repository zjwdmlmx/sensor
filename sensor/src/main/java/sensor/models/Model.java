package sensor.models;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Model utils
 */

public final class Model {
    private Model() {}

    public static <T extends RealmObject> void deleteFromRealmList(final List<T> list) {
        Realm.getDefaultInstance().executeTransaction(realm -> {
            for (T i : list) {
                i.deleteFromRealm();
            }
        });
    }
}
