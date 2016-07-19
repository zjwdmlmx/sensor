package sensor.protos;


public class User {
    private String uid;

    public User() {}

    public User(String name) {
        this.uid = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
