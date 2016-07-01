package sensor.fs;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import sensor.proto.SensorData;


class Configer {
    public class Executor {
        private Document document = null;
        private Element root = null;
        private String filename = null;


        /**
         * Loading config from xml file
         *
         * @param filename xml config file's name
         * @throws ParserConfigurationException
         * @throws IOException
         * @throws SAXException
         */
        public void load(String filename) throws ParserConfigurationException, IOException, SAXException {
            this.filename = filename;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            document = factory.newDocumentBuilder().parse("file://" + filename);
            root = document.getDocumentElement();

        }

        /**
         * Saving to the file
         */
        public void save() throws IOException, TransformerException {
            FileOutputStream fs = new FileOutputStream(filename);
            Source source = new DOMSource(document);
            Result result = new StreamResult(fs);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
            fs.close();
        }

        public void set(String key, String value) {
            NodeList eles = root.getElementsByTagName(key);
            if (eles.getLength() > 0) {
                eles.item(0).setTextContent(value);
            } else {
                Element theEle = document.createElement(key);
                theEle.setTextContent(value);
                root.appendChild(theEle);
            }
        }

        public String get(String key) {
            NodeList eles = root.getElementsByTagName(key);
            return eles.getLength() > 0 ? eles.item(0).getTextContent() : null;
        }
    }

    private Executor executor = new Executor();
    /**
     * Loading config from xml file
     *
     * @param filename xml config file's name
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public synchronized void load(String filename) throws ParserConfigurationException, IOException, SAXException {
        executor.load(filename);

    }

    /**
     * Saving to the file
     */
    public synchronized void save() throws IOException, TransformerException {
        executor.save();
    }

    public synchronized void set(String key, String value) {
        executor.set(key, value);
    }

    public synchronized String get(String key) {
        return executor.get(key);
    }

    public synchronized ArrayList<String> multi(MultiExecutor multiExecutor) throws Exception{
        return multiExecutor.execute(executor);
    }

    static interface MultiExecutor {
        ArrayList<String> execute(Executor executor) throws Exception;
    }
}

/**
 * sensor data storage's manager
 */
public class SensorDataStorageManager implements SensorDataStorageRecorder, SensorDataStorageProfiler {
    private String storagePath = null;
    private File storageDir = null;
    private SensorDataStorage writer = null;

    private Configer config = new Configer();

    private static TreeMap<File, SensorDataStorageManager> managerMap = new TreeMap<>();

    private static SensorDataStorageManager getManager(String path) {
        File f = new File(path);
        synchronized (managerMap) {
            if (managerMap.containsKey(f)) {
                return managerMap.get(f);
            }
        }

        SensorDataStorageManager manager = new SensorDataStorageManager();
        if (manager.init(path)) {
            synchronized (managerMap) {
                managerMap.put(new File(path), manager);
            }
            return manager;
        }

        return null;
    }

    public static SensorDataStorageProfiler getProfiler(String path) {
        return getManager(path);
    }

    public static SensorDataStorageRecorder getRecorder(String path) {
        return getManager(path);
    }


    private SensorDataStorageManager() {}

    /**
     * initialing the Sensor-data-storage manager
     *
     * <p><strong>Warning: must call before other operate</strong></p>
     *
     * @return true if success
     */
    public boolean init(String storagePath) {
        boolean res = initFs(storagePath);

        try {
            if (res) writer = this.makeSensorDataStorage();
        } catch (Exception e) {
            return false;
        }

        return res;
    }

    @Override
    public String getUploadFilename() {
        ArrayList<String> results;
        try {
            results = config.multi(new Configer.MultiExecutor() {
                @Override
                public ArrayList<String> execute(Configer.Executor executor) throws Exception {
                    ArrayList<String> results = new ArrayList<>();
                    results.add(executor.get("currentNumber"));
                    results.add(executor.get("uploadedNumber"));

                    return results;
                }
            });
        } catch (Exception e) {
            Log.e("Sensor Data configer", e.toString());
            return null;
        }

        int curr = Integer.parseInt(results.get(0));
        int toUp = Integer.parseInt(results.get(1));

        if (toUp < curr - 1) {
            return storagePath + "/" + toUp + ".csv";
        }
        return null;
    }

    @Override
    public void nextUploadFilename() throws Exception {
        config.multi(new Configer.MultiExecutor() {
            @Override
            public ArrayList<String> execute(Configer.Executor executor) throws Exception {
                int toUp = Integer.parseInt(executor.get("uploadedNumber"));
                executor.set("uploadedNumber", Integer.toString(toUp + 1));
                try {
                    executor.save();
                } catch (Exception e) {
                    // rollback and rethrow the exception
                    executor.set("uploadedNumber", Integer.toString(toUp));
                    throw e;
                }
                return  null;
            }
        });
    }

    @Override
    public void clean() {
        final int toUp = Integer.parseInt(config.get("uploadedNumber"));

        File[] files = storageDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                boolean res = false;
                try {
                    res = Integer.parseInt(filename.substring(0, filename.length() - 4)) < toUp;
                } catch (Exception e) {
                    return false;
                }
                return res;
            }
        });

        for (File f : files) {
            Log.i("Sensor storage", "deleted: " + f.toString());
            f.delete();
        }
    }

    @Override
    public void put(SensorData data) throws IOException, TransformerException {
        writer.put(data);

        if (writer.isOverflow()) {
            writer.finish();

            writer = makeSensorDataStorage();

        }
    }

    @Override
    public String getUUID() {
        return config.get("uuid");
    }



    // make a new SensorDataStorage for a new recoding file
    private SensorDataStorage makeSensorDataStorage() throws IOException, TransformerException {
        if (storagePath == null) return null;

        String currentNumber = config.get("currentNumber");
        int current = Integer.parseInt(currentNumber);

        StringBuilder builder = new StringBuilder(256);
        builder.append(storagePath).append('/').append(currentNumber).append(".csv");

        SensorDataStorage s = new SensorDataStorage.CSVSensorDataStorage(new FileOutputStream(builder.toString()));
        config.set("currentNumber", Integer.toString(current + 1));
        config.save();

        return s;
    }

    // making a new config file if not exists
    private boolean makeConfigFile(String parent) {
        File fconfig = new File(parent + "/config.xml");
        String fconfigPath = fconfig.toString();

        try {
            if (!fconfig.exists()) {
                if (!fconfig.createNewFile()) {
                    Log.e("Sensor storage manager", "configure file create failed!");
                    return false;
                }

                FileOutputStream stream = new FileOutputStream(fconfigPath);
                stream.write("<root></root>".getBytes());
                stream.close();

                config.load(fconfigPath);
                config.set("currentNumber", "0");
                config.set("uploadedNumber", "0");
                config.set("uuid", UUID.randomUUID().toString());
                config.save();
                return true;
            } else {
                config.load(fconfigPath);
            }
        } catch (Exception e) {
            Log.e("Sensor storage manager", e.toString());
            return false;
        }
        return true;
    }

    // Initialing the filesystem for recode sensor data
    private boolean initFs(String path) {
        File sensorDir = new File(path);

        boolean res = sensorDir.isDirectory() && makeConfigFile(path);

        if (res) {
            storagePath = path;
            storageDir = sensorDir;
        }

        return res;
    }
}
