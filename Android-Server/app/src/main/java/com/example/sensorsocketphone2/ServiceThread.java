package com.example.sensorsocketphone2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class ServiceThread extends Thread implements SensorEventListener {

    private Socket client;
    private Context context;
    private List<String> sensorsList = new ArrayList<>();
    private final static String TAG = "";
    private DataOutputStream outputStream;
    private InputStreamReader inputStream;
    private boolean stopThread = false;

    private final static String ACC_STR = "acc";


    public ServiceThread(Context context, Socket client) {
        this.client = client;
        this.context = context;
        // get stream resources
        if (client != null) {
            try {
                outputStream = new DataOutputStream(client.getOutputStream());
                inputStream = new InputStreamReader(client.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e(TAG, "constructor (ServiceThread): failed to create the service thread");
                try {
                    this.client.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                    Log.e(TAG, "constructor (ServiceThread): failed to close the client socket");
                }
            }
        }
    }

    @Override
    public void run() {
        HashMap<String, Integer> req;
        while (!stopThread) {
            if (inputStream != null) {
                try {
                    System.out.println("waiting for requests");
                    req = readRequest(inputStream);
                    handleRequest(req);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.e(TAG, "run (ServiceThread): failed to read from the client input stream");
                    // reset the stream
                    //inputStream = resetInputStream();
                    //TODO: handle error by setting the server in the waiting req mode if the client exists
                }
            }
        }
    }

    private HashMap<String, Integer> readRequest(InputStreamReader inputStream) throws IOException {
        int n;
        StringBuilder strBuilder = new StringBuilder();
        n = inputStream.read();
        while (n != -1) {
            strBuilder.append((char) n);
            n = inputStream.read();
            if ((char) n == '?') {
                break;
            }
        }

        return new Gson().fromJson(strBuilder.toString(),
                new TypeToken<HashMap<String, Integer>>() {}.getType());
    }

    private void handleRequest(HashMap<String, Integer> requests) {
        List<String> sensors = new ArrayList<>();
        for (Map.Entry<String, Integer> request : requests.entrySet()) {
            if (request.getValue() == 1) {
                sensors.add(request.getKey());
            }
        }
        updateService(sensors);
    }

    private void updateService(List<String> newSensorList) {
        // register newly subscribed sensors
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        for (String requestedSensor : newSensorList) {
            if (!sensorsList.contains(requestedSensor)) {
                int type = getSensorType(requestedSensor);
                if (sensorManager != null) {
                    Sensor sensor = sensorManager.getDefaultSensor(type);
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
            }
        }
        // unregister unsubscribed sensors
        sensorsList = newSensorList;
    }

    private int getSensorType(String sensor) {
        int type = -300;
        if (sensor.equals(ACC_STR)) {
            return Sensor.TYPE_ACCELEROMETER;
        }
        return type;
    }

    public void stopSensorService() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        stopThread = true;
    }

    // sensors-related methods
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        System.out.println(sensorEvent.values[0]);
        new SensorDataTransmit().execute(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class SensorDataTransmit extends AsyncTask<SensorEvent, Void, Void> {

        @Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];
            try {
                String stringJson = createJSonString(event);
                System.out.println(stringJson);
                outputStream.writeUTF(stringJson);
                outputStream.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        private String createJSonString(SensorEvent event) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("s", ACC_STR);
            jsonObj.addProperty("t",System.currentTimeMillis());
            jsonObj.addProperty("v", event.values[0]);

            return jsonObj.toString();
        }
    }
}
