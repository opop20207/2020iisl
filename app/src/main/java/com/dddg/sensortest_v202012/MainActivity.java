package com.dddg.sensortest_v202012;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Comments 2020-12-26
 * Pass three values from MainActivity.java to WriteActivity.class
 *  1. fileName - name of file (파일명)
 *  2. timeInterval - interval between two rows of record (파일 출력 간격)
 *  3. s - delay of sensor (센서 딜레이)
 */
public class MainActivity extends AppCompatActivity {

    // Widgets
    Button btnStart;
    RadioGroup rg;
    EditText etxtFileName, etxtTimeInterval;

    // permissions
    boolean fileRead=false, fileWrite=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        rg = (RadioGroup) findViewById(R.id.rg);
        etxtFileName = (EditText) findViewById(R.id.etxtFileName);
        etxtTimeInterval = (EditText) findViewById(R.id.etxtTimeInterval);

        // check permissions
        checkFilePermission();

        // if the user allow permissions, move to writeActivity
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileRead&&fileWrite){
                    goToWriteActivity();
                }
                else{
                    checkFilePermission();
                }
            }
        });
    }

    public void goToWriteActivity(){
        Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
        intent.putExtra("fileName", etxtFileName.getText().toString());
        intent.putExtra("timeInterval", Integer.parseInt(etxtTimeInterval.getText().toString()));
        int s=0;
        if(rg.getCheckedRadioButtonId() == R.id.rbtn1){
            s = SensorManager.SENSOR_DELAY_FASTEST;
        }else if(rg.getCheckedRadioButtonId() == R.id.rbtn2){
            s = SensorManager.SENSOR_DELAY_GAME;
        }else if(rg.getCheckedRadioButtonId() == R.id.rbtn3){
            s = SensorManager.SENSOR_DELAY_UI;
        }else{
            s = SensorManager.SENSOR_DELAY_NORMAL;
        }
        intent.putExtra("sensorSpeed", s);
        startActivity(intent);
    }

    public void checkFilePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            fileRead=true;
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            fileWrite=true;
        }
        if(!(fileRead&&fileWrite)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }
}