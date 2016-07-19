package sensor.net;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import sensor.models.HistoryDataModel;
import sensor.models.Model;
import sensor.models.SensorDataModel;
import sensor.protos.HistoryData;
import sensor.protos.HistoryDataGroup;
import sensor.protos.HistoryDataResponse;
import sensor.protos.HistoryDataService;
import sensor.protos.SensorData;
import sensor.protos.SensorDataGroup;
import sensor.protos.SensorDataResponse;
import sensor.protos.SensorDataService;


/**
 * app data transmitter
 */
public class Transmitter implements Observable.OnSubscribe<String> {
    public static Observable createObservable() {
        return Observable.create(new Transmitter());
    }

    private boolean isStarted = false;

    private interface Command {
        boolean execute() throws IOException;
    }

    private List<Command> commands = Arrays.asList(new Command() {
        SensorDataService sensorDataService = Services.create(SensorDataService.class);
        @Override
        public boolean execute() throws IOException {
            final List<SensorDataModel> res = SensorDataModel.getMany(2000);
            if (res.size() == 0) return false;

            SensorDataGroup group = new SensorDataGroup();
            final List<SensorData> datas = new ArrayList<>(res.size());

            for (SensorDataModel m : res) {
                datas.add(m.toSensorData());
            }

            group.setData(datas);
            Response<SensorDataResponse> response = sensorDataService.uploadSensorData(group, res.get(0).getUser()).execute();
            if (response.isSuccessful() && response.body().getRes() == 0) {
                Log.i("Transmitter", "sensor data upload success");
                Model.deleteFromRealmList(res);
            } else {
                return false;
            }
            return true;
        }
    }, new Command() {
        HistoryDataService historyDataService = Services.create(HistoryDataService.class);
        @Override
        public boolean execute() throws IOException {
            final List<HistoryDataModel> res = HistoryDataModel.getMany(2000);

            if (res.size() == 0) return false;

            HistoryDataGroup group = new HistoryDataGroup();
            final List<HistoryData> datas = new ArrayList<>(res.size());

            for (HistoryDataModel m : res) {
                datas.add(m.toHistoryData());
            }

            group.setData(datas);
            Response<HistoryDataResponse> response = historyDataService.uploadHistoryData(group, res.get(0).getUser()).execute();
            if (response.isSuccessful() && response.body().getRes() == 0) {
                Log.i("Transmitter", "history data upload success");
                Model.deleteFromRealmList(res);
            } else {
                return false;
            }
            return true;
        }
    });

    @Override
    public void call(Subscriber<? super String> subscriber) {
        if (isStarted) {
            return;
        }

        isStarted = true;

        final int cmdLen = commands.size();
        int i = 0;
        while (i < cmdLen) {
            try {
                if (!commands.get(i).execute()) {
                    Thread.sleep(6000);
                } else {
                    subscriber.onNext("uploadOneSuccess");
                }
            } catch(IOException e) {
                try {
                    Thread.sleep(20000);
                } catch (Exception ee) {
                    ee.printStackTrace();
                    break;
                }
            } catch(Exception e) {
                Log.e("Transmitter", e.toString());
                e.printStackTrace();
            }
            i = (i + 1)%cmdLen;
        }

        subscriber.onError(new Throwable("Unknow error to stop the upload process"));

        Log.i("Transmitter", "Transmitter process will shutdown with errors");
    }
}
