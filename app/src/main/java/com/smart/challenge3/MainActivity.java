package com.smart.challenge3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;

public class MainActivity extends AppCompatActivity {

    private TextView accelerometerText;
    private SensorManager manager;
    private SensorEventListener listener;

    private Handler handler = new Handler();
    private Runnable runnable;
    private int delay = 3 * 1000; //run program intervals of sec * 1000

    private ArrayList<Double> classValues = new ArrayList<>();

    private MediaPlayer mediaPlayer;

    private J48 j48;
    private ArrayList<Attribute> atts = new ArrayList<>();
    private ArrayList<String> activityVal = new ArrayList<>();
    Instances dataCollected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accelerometerText = findViewById(R.id.accelerometer);

        try {
            j48 = (J48) weka.core.SerializationHelper.read(getResources().openRawResource(R.raw.j48));

            activityVal.add("walking");
            activityVal.add("standing");
            activityVal.add("jogging");
            activityVal.add("sitting");
            activityVal.add("biking");
            activityVal.add("upstairs");
            activityVal.add("downstairs");
            atts.add(new Attribute("Right_pocket_Ax"));
            atts.add(new Attribute("Right_pocket_Ay"));
            atts.add(new Attribute("Right_pocket_Az"));
            atts.add(new Attribute("Activity", activityVal));

            dataCollected = new Instances("CollectedData", atts, 0);
            dataCollected.setClassIndex(dataCollected.numAttributes() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        manager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        
        listener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                dataCollected.add(new DenseInstance(1.0, new double[]{event.values[0],event.values[1],event.values[2],-1}));
                accelerometerText.setText(String.format("Ax:%s\nAy:%s\nAz:%s", event.values[0], event.values[1], event.values[2]));
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable = () -> {

            manager.unregisterListener(listener);
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }

            try {
                predict();
            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.postDelayed(runnable, delay);
            manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 20000);
        }, delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }

    private void predict() throws Exception {

        for (int i=0; i < dataCollected.numInstances(); i++){
            classValues.add(j48.classifyInstance(dataCollected.get(i)));
        }

        int predMovementIndex = mostCommon(classValues);
        System.out.println(classValues.toString());
        System.out.println("classified:" + dataCollected.classAttribute().value(predMovementIndex));

        switch(predMovementIndex) {
            case 0:
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.walking);
                mediaPlayer.start();
                break;
            case 1:
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.standing);
                mediaPlayer.start();
                break;
            case 2:
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.jogging);
                mediaPlayer.start();
                break;
            case 3:
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sitting);
                mediaPlayer.start();
                break;
            case 4:
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.biking);
                mediaPlayer.start();
                break;
            case 5:
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.up);
                mediaPlayer.start();
                break;
            case 6:
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.down);
                mediaPlayer.start();
                break;
        }

        ///may or may not delete
        dataCollected.delete();
        classValues.clear();
    }

    public int mostCommon(List<Double> list) {
        int result = -1;
        List<Double> checkedList = new ArrayList<>();
        int count = 0;
        int countTemp = 0;

        for (Double dPotential : list) {
            if(!checkedList.contains(dPotential)){
                for (Double dCompare : list) {
                    if(dCompare.equals(dPotential)){
                        countTemp++;
                    }
                }
                if(countTemp>count){
                    result = dPotential.intValue();
                    count = countTemp;
                }
                countTemp = 0;
                checkedList.add(dPotential);
            } else if (checkedList.size()==7){
                break;
            }
        }

        return result;
    }
}