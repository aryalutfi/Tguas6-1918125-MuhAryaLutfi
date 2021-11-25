package com.example.workmanagerpraktikum6;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button btnOneTimeTask,btnSet,btnCancel;
    private Spinner spKota;
    private TextView tvStatus;
    private  PeriodicWorkRequest periodicWorkRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOneTimeTask = findViewById(R.id.one_time_task);
        btnSet = findViewById(R.id.btn_set);
        btnCancel = findViewById(R.id.btn_cancel);
        spKota = findViewById(R.id.sp_kota);
        tvStatus = findViewById(R.id.tv_status);

        btnOneTimeTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOneTimeTask();
            }
        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPeriodicTask();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canceledPeriodicTask();
            }
        });
    }

    private void startOneTimeTask(){
        tvStatus.setText("Status : ");
        Data data = new Data.Builder()
                .putString(WeatherCityManager.EXTRA_KOTA,spKota.getSelectedItem().toString())
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(WeatherCityManager.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(MainActivity.this).enqueue(oneTimeWorkRequest);

        WorkManager.getInstance(MainActivity.this)
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(MainActivity.this,workInfo -> {
                    String status = workInfo.getState().name();
                    tvStatus.append("\n"+status);
                });
    }

    private void startPeriodicTask(){
        tvStatus.setText("Status : ");
        Data data = new Data.Builder()
                .putString(WeatherCityManager.EXTRA_KOTA,spKota.getSelectedItem().toString())
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        periodicWorkRequest = new PeriodicWorkRequest.Builder(WeatherCityManager.class, 15, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(MainActivity.this).enqueue(periodicWorkRequest);

        WorkManager.getInstance(MainActivity.this)
                .getWorkInfoByIdLiveData(periodicWorkRequest.getId())
                .observe(MainActivity.this,workInfo -> {
                    String status = workInfo.getState().name();
                    tvStatus.append("\n"+status);
                    btnCancel.setEnabled(false);

                    if(workInfo.getState() == WorkInfo.State.ENQUEUED){
                        btnCancel.setEnabled(true);
                    }
                });
    }

    private void canceledPeriodicTask(){
        WorkManager.getInstance(MainActivity.this).cancelWorkById(periodicWorkRequest.getId());
    }
}