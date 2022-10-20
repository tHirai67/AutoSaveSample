package com.example.autosavesample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private static final String TAG = MainActivity.class.getName();
    private GoogleApiClient mGoogleApiClient;
    TextView tTextView,xTextView,yTextView,zTextView;
    private String filename = getNowDate() + ".csv";
    private File file;
    private StringBuilder text = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tTextView = (TextView) findViewById(R.id.time);
        xTextView = (TextView) findViewById(R.id.xValue);
        yTextView = (TextView) findViewById(R.id.yValue);
        zTextView = (TextView) findViewById(R.id.zValue);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Context context = getApplicationContext();
        file = new File(context.getFilesDir(), filename);

        Button saveBtn = (Button) findViewById(R.id.save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileSave();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(null != mGoogleApiClient && mGoogleApiClient.isConnected()){
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    //オプションメニューの表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        return true;
    }
    //オプションメニュー選択時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"onConnected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed:"+connectionResult.toString());
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        //xTextView.setText(messageEvent.getPath());
        String msg = messageEvent.getPath();
        String[] value = msg.split(",",0);
        int type = Integer.valueOf(value[0]);
        if(type == 0){
            double time = Double.valueOf(value[1]);
            String x = String.valueOf(value[2]);
            String y = String.valueOf(value[3]);
            String z = String.valueOf(value[4]);
            tTextView.setText(String.format("%f",time));
            xTextView.setText(x);
            yTextView.setText(y);
            zTextView.setText(z);

            text.append(time+","+x+","+y+","+z+"\n");
        }else if(type == 2){
            tTextView.setText(String.format("0"));
            xTextView.setText(String.format("0"));
            yTextView.setText(String.format("0"));
            zTextView.setText(String.format("0"));
        }else if(type == 3){
            fileSave();
        }



    }

    public void fileSave(){
        String t = text.toString();

        try(FileWriter writer = new FileWriter(file)){
            writer.write(t);
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }
}