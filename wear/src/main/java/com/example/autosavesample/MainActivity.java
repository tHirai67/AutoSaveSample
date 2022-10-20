package com.example.autosavesample;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private final String TAG = MainActivity.class.getName();
    private GoogleApiClient mGoogleApiClient = null;
    private TextView tTextView,xTextView,yTextView,zTextView;
    private SensorManager mSensorManager;
    private String mNode;
    private int button_flag = 0;
    private double pastime, time, count;
    private float x, y, z;
    private Button start_btn, reset_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tTextView = (TextView) findViewById(R.id.Time);
        xTextView = (TextView) findViewById(R.id.xText);
        yTextView = (TextView) findViewById(R.id.yText);
        zTextView = (TextView) findViewById(R.id.zText);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        start_btn = (Button) findViewById(R.id.b1);
        reset_btn = (Button) findViewById(R.id.b2);

        start_btn.setVisibility(View.VISIBLE);
        reset_btn.setVisibility(View.INVISIBLE);

        start_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                button_flag = 1;
                start_btn.setVisibility(View.INVISIBLE);
            }

        });

        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_flag = 2;
                start_btn.setVisibility(View.VISIBLE);
                reset_btn.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG,"onConnected");
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                if(nodes.getNodes().size() > 0){
                    mNode = nodes.getNodes().get(0).getId();
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed : "+connectionResult.toString());
    }

    @Override
    protected void onResume(){
        super.onResume();
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = 100;
        if(button_flag == 0){
            pastime = 0;
            time = 0;
            tTextView.setText(String.format("Time:%.3f",time));
        }else if(button_flag == 1){
            if(pastime == 0){
                pastime = System.currentTimeMillis();
            }
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                type = 0;
                time = ((System.currentTimeMillis() - pastime) / 1000);
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                tTextView.setText(String.format("Time:%.3f",time));
                xTextView.setText(String.format("%f",x));
                yTextView.setText(String.format("%f",y));
                zTextView.setText(String.format("%f",z));
                String SEND_DATA = String.format(Locale.getDefault(),"%d,%f,%f,%f,%f",type,time,x,y,z);
                sendMessage(SEND_DATA);
                //自動保存の条件
                //Ｘ軸の値が５未満になって5秒たったら保存メッセージを送る
                if(x >= 5){
                    count = time;
                }
                if(time - count >= 5.0){
                    button_flag = 3;
                }
            }
        }else if(button_flag == 2){
            sendMessage("2");
            button_flag = 0;
        }else if(button_flag == 3){
            sendMessage("3");
            reset_btn.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void sendMessage(String s){
        if(mNode != null){
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, s, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(@NonNull MessageApi.SendMessageResult result) {
                    if(!result.getStatus().isSuccess()){
                        Log.d(TAG,"ERROR:failed to send Message" + result.getStatus());
                    }
                }
            });
        }
    }

}