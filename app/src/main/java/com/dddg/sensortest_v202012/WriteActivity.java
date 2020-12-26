package com.dddg.sensortest_v202012;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteActivity extends AppCompatActivity implements SensorEventListener {
    private Sensor gyroSensor, accSensor;
    private SensorManager sensorManager;
    private Button btnStop;
    private TextView txvNum;

    private double gyroX, gyroY, gyroZ;
    private double accX, accY, accZ;
    //private double pitch,roll,yaw;

    private double velX,velZ,preX,preZ;
    // private double angleZX,angleZY;
    private double timestamp;
    private double dt;

    private double xSum,zSum,zDist;
    private int xDet,times;
    private int Freq;

    private double RAD2DGR=180/Math.PI;
    private static final double NS2S = 1.0f/1000000000.0f;

    private String filename="";
    private int timeInterval=500;
    private int sensorSpeed=0;
    private int cnt=0;

    private boolean isError=false;
    private boolean isRead=false;

    private myThread thread;

    class myThread extends Thread{
        @Override
        public void run(){
            try{
                while(!Thread.currentThread().isInterrupted()){
                    writeFile(filename);
                    Thread.sleep(timeInterval);
                }
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        sensorSpeed = getIntent().getIntExtra("sensorSpeed",R.id.rbtn1);
        switch(sensorSpeed){
            case R.id.rbtn1:
                sensorSpeed = SensorManager.SENSOR_DELAY_FASTEST;
            case R.id.rbtn2:
                sensorSpeed = SensorManager.SENSOR_DELAY_GAME;
            case R.id.rbtn3:
                sensorSpeed = SensorManager.SENSOR_DELAY_UI;
            case R.id.rbtn4:
                sensorSpeed = SensorManager.SENSOR_DELAY_NORMAL;
        }
        Log.e("sensorSpeed",sensorSpeed+"!");

        filename = getIntent().getStringExtra("fileName");
        timeInterval = getIntent().getIntExtra("timeInterval", 500);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(this,gyroSensor,sensorSpeed);
        sensorManager.registerListener(this,accSensor,sensorSpeed);

        btnStop = (Button)findViewById(R.id.btnStop);
        txvNum = (TextView)findViewById(R.id.txvNum);

        xSum=zSum=velX=velZ=zDist=0.0;
        xDet=0;
        Freq=0;
        times=0;
        thread=new myThread();
        threadStart();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        dt=(event.timestamp-timestamp)*NS2S;
        timestamp=event.timestamp;

        if(event.sensor==gyroSensor){
            gyroX=event.values[0];
            gyroY = event.values[1];
            gyroZ = event.values[2];

            if((gyroX>2||gyroY>2||gyroZ>2)&&isRead){
                isError=true;
            }
        }

        if(event.sensor==accSensor){
            accX=event.values[0];
            accY=event.values[1];
            accZ=event.values[2];
        }

        if (dt - timestamp*NS2S != 0) {
            velZ=velZ+accZ*dt;
            velX=velX+accX*dt;

            if(times<3) {
                if (velX < -0.1 && (xDet % 2 == 1||times==0)){
                    times++;
                    xDet = xDet * 2;
                }
                if (velX > 0.1 && (xDet % 2 == 0||times==0)) {
                    times++;
                    xDet = xDet * 2 + 1;
                }
            }
            zDist+=velZ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,gyroSensor,sensorSpeed);
        sensorManager.registerListener(this,accSensor,sensorSpeed);
    }

    // 잠시 종료하는 경우 (홈 버튼 누르는거 같이)
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if(thread!=null) threadStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        if(thread!=null) threadStop();
    }

    private void threadStart(){
        thread.setDaemon(true);
        thread.start();
    }

    private void threadStop(){
        thread.interrupt();
    }

    public void writeFile(String filename){
        try{
            String dirpath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/myApp";
            File dir=new File(dirpath);
            if(!dir.exists()){
                dir.mkdir();
            }

            File file=new File(dir+"/"+filename+".txt");
            if(!file.exists()) {
                file.createNewFile();
            }
            File file2=new File(dir+"/"+filename+"_accgyro.txt");
            if(!file2.exists()) {
                file2.createNewFile();
            }

            FileWriter fw=new FileWriter(file,true);
            fw.write(velX+","+xDet+","+times+"\n");
            FileWriter fw2=new FileWriter(file2,true);
            fw2.write(accX+","+accY+","+accZ+","+gyroX+","+gyroY+","+gyroZ+"\n");

            xSum+=accX>0?accX:-accX;
            zSum+=accZ>0?accZ:-accZ;
            Freq+=1;

            fw.flush();
            fw.close();
            fw2.flush();
            fw2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}