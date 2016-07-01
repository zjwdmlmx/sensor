package sensor.user;

import android.os.Environment;

import sensor.fs.SensorDataStorageManager;
import sensor.fs.SensorDataStorageRecorder;

import java.io.File;
import java.io.FileFilter;

/**
 * management of users
 */
public class UserManager {
    private String currUser = null;

    private File root;


    public UserManager() {
        root = new File(Environment.getExternalStorageDirectory().toString() + "/sensor");
    }

    public boolean addUser(String name) {
        return new File(root.toString() + "/" + name).mkdirs();
    }

    public boolean delUser(String name) {
        return new File(root.toString() + "/" + name).delete();
    }

    public boolean userExists(String name) {
        return new File(root.toString() + "/" + name).exists();
    }

    public String[] listUsers() {
        return listUsers(false);
    }

    public String[] listUsers(boolean withPath) {
        File[] files = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        if (files == null || files.length == 0) {
            return null;
        }

        final int len = files.length;
        String[] res = new String[len];
        for( int i = 0; i < len; ++i ) {
            res[i] = withPath ? files[i].toString() : files[i].getName();
        }

        return res;
    }

    public String currentUser() {
        return currUser;
    }

    public SensorDataStorageRecorder login(String name) {
        SensorDataStorageRecorder recorder = SensorDataStorageManager.getRecorder(root.toString() + "/" + name);
        if (recorder != null) {
            currUser = name;
        }
        return recorder;
    }
}
