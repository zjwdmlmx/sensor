package sensor.net;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sensor.app.SensorService;
import sensor.common.EventEmit;
import sensor.common.EventEmitter;
import sensor.fs.SensorDataStorageManager;
import sensor.fs.SensorDataStorageProfiler;
import sensor.proto.SensorDataResponse;
import sensor.proto.SensorDataService;
import sensor.user.UserManager;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.concurrent.locks.ReentrantLock;


class Utils {
    // making the multipart boundary
    public static String makeBoundary() throws NoSuchAlgorithmException {
        Random rd = new Random();
        Date now = new Date();
        byte[] bt = new byte[8];

        rd.setSeed(now.getTime());
        rd.nextBytes(bt);

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        sha1.update(now.toString().getBytes());
        sha1.update(bt);

        return "--------" + digestByte2String(sha1.digest());
    }

    public static String digestByte2String(byte[] digest) {
        StringBuilder sb = new StringBuilder(40);
        for (byte b : digest) {
            sb.append(b);
        }

        return sb.toString();
    }
}


/**
 * Transmitting the sensor data saved in files
 */
class Transmit extends Thread {
    private InetAddress serverAddress;
    private int serverPort = 0;
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.31.6/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private SensorDataService sensorProto = retrofit.create(SensorDataService.class);

    private UserManager userManager;
    private ReentrantLock lock = new ReentrantLock();

    public Transmit(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setServer(InetAddress address, int port) {
        lock.lock();
        this.serverAddress = address;
        this.serverPort = port;
        lock.unlock();
    }

    public void uploadUsersSensorData() {
        String[] paths = userManager.listUsers(true);

        for(String path : paths) {
            SensorDataStorageProfiler profiler = SensorDataStorageManager.getProfiler(path);
            final String uuid = profiler.getUUID();

            while(true) {
                String toUpFile = profiler.getUploadFilename();
                if (toUpFile != null) {
                    if (uploadFile(toUpFile, uuid)) {
                        try {
                            profiler.nextUploadFilename();
                        } catch (Exception e) {
                            Log.e("Transmitter", e.toString());
                        }
                    } else {
                        profiler.clean();
                        break;
                    }
                } else {
                    profiler.clean();
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        while(true) {
            if (serverPort != 0 && serverAddress != null) {
                uploadUsersSensorData();
            }

            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                Log.e("Transmitter", e.toString());
            }
        }
    }

    private boolean uploadFile(String filename, String uuid) {
        Log.i("Transmitter", filename);
        File f = new File(filename);
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), f);

        Call<SensorDataResponse> result = sensorProto.uploadSensorDataFile(MultipartBody.Part.createFormData("sensordata", f.getName(), body));
        int res = -1;
        try {

            res = result.execute().body().getRes();
        } catch (Exception e) {
            Log.e("Transmitter", e.toString());
        }

        return res == 0;
    }
}

/**
 * Finding the Server with Broadcast protocol
 */
class FindServer extends EventEmitter implements Runnable {
    private final static int MAX_BUFFER = 32;
    private final static long PROTO_BEGIN = 0xE42D23FF34340088L;
    public static final int PORT = 8096;

    private InetAddress serverAddress;
    private int serverPort;

    @Override
    public void run() {
        DatagramSocket sock;

        try {
            sock = new DatagramSocket(PORT);
        } catch (Exception e) {
            Log.e("Transmitter", e.toString());
            Map<String, Object> args = new HashMap<>();
            args.put("what", e);
            emit("error", args);
            return;
        }


        DatagramPacket packet = new DatagramPacket(new byte[MAX_BUFFER], MAX_BUFFER);

        while (true) {
            try {
                sock.receive(packet);

                byte[] data = packet.getData();
                Log.i("Transmitter", "udp data recived!, length = " + data.length);
                byte[] ipData = Arrays.copyOfRange(data, 10, 14);

                DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(data));
                if (dataStream.readLong() == PROTO_BEGIN) {
                    InetAddress address = InetAddress.getByAddress(ipData);
                    int port = dataStream.readShort();

                    if (port != serverPort || !address.equals(serverAddress)) {
                        serverAddress = address;
                        serverPort = port;

                        Map<String, Object> args = new HashMap<>();
                        args.put("address", serverAddress);
                        args.put("port", serverPort);
                        emit("result", args);
                    }
                }

                // in case of unused packet received
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.e("Transmitter", "network error: " + e.toString());
                Map<String, Object> args = new HashMap<>();
                args.put("what", e);
                emit("readError", args);
            }
        }
    }
}

/**
 * app data transmitter
 */
public class Transmitter {
    private Transmit  transmit;
    private boolean isStarted = false;

    private UserManager userManager;

    public Transmitter(UserManager userManager) {
        this.userManager = userManager;
    }

    public void start() {
        if (isStarted) {
            return;
        }

        isStarted = true;

        transmit = new Transmit(userManager);
        transmit.start();

        FindServer finder = new FindServer();
        finder.on("result", new EventEmit.Callable() {
            @Override
            public void call(Map<String, Object> args) {
                InetAddress address = (InetAddress) args.get("address");
                int port = (int) args.get("port");

                transmit.setServer(address, port);
                SensorService.transmitServer = address.getHostAddress();
                SensorService.transmitPort = Integer.toString(port);
            }
        });

        finder.on("readError", new EventEmit.Callable() {
            @Override
            public void call(Map<String, Object> args) {
                System.err.println(args.get("what").toString());
            }
        });

        new Thread(finder).start();
    }
}
