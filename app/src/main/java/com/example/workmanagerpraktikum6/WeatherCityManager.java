package com.example.workmanagerpraktikum6;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.channels.AsynchronousByteChannel;
import java.security.cert.CertPathBuilder;
import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class WeatherCityManager extends Worker {
    private static final String TAG = WeatherCityManager.class.getSimpleName();
    private static final String CHANNEL_ID = "Work_Manager_Chanel1 ";
    private static final CharSequence CHANNEL_NAMA = "WorkManagerChanel1";
    public  static final String EXTRA_KOTA = "kota";
    private Result resultstatus;

    public WeatherCityManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String kota = getInputData().getString(EXTRA_KOTA);
        return getCurrentWeather(kota);
    }

    private Result getCurrentWeather(String kota){
        Log.d(TAG, "getCurrentWeather Start ...");
        Looper.prepare();
        SyncHttpClient client = new SyncHttpClient();
        String url = "https://www.weatherbit.io/" + kota + "&appid=" + BuildConfig.Apikey;
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG,result);

                try{
                    JSONObject responseObjek = new JSONObject(result);
                    String currentWeather = responseObjek.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObjek.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responseObjek.getJSONObject("main").getDouble("temp");
                    double tempInCelcius = tempInKelvin - 273;
                    String temperature = new DecimalFormat("##.##").format(tempInCelcius);

                    String title = "Cuaca di" + kota ;
                    String massage = currentWeather + ", " + description + " dengan "+ temperature + "celcius";
                    int notifid = 210;
                    showNotif(getApplicationContext(),title, massage,notifid);
                    
                    Log.d(TAG,"onSuccess : finished");
                    resultstatus = Result.success();
                }catch (JSONException e){
                    e.printStackTrace();
                    showNotif(getApplicationContext(),"Not Success",e.getMessage(),201);
                    Log.d(TAG,"onSuccess : failed");
                    resultstatus = Result.failure();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showNotif(getApplicationContext(),"Not Success",error.getMessage(),201);
                Log.d(TAG,"onFailur : failed");
                resultstatus = Result.failure();
            }
        });

        return resultstatus;
    }

    private void showNotif(Context context,String title,String message,int notidId){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alaramSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_replay_30_24dp)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(new long[]{ 1000, 1000, 1000, 1000})
                .setSound(alaramSound)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAMA,NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern( new long[]{1000,1000,1000,1000});
            mBuilder.setChannelId(CHANNEL_ID);

            if (notificationManager != null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Notification notification = mBuilder.build();

        if(notificationManager != null){
            notificationManager.notify(notidId,notification);
        }
    }

}
