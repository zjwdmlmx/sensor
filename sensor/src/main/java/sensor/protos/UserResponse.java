package sensor.protos;


public class UserResponse {
    public static final int ERR_USER_EXISTS = 1;
    public static final int SUCCESS = 0;

    private int res;

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }
}
