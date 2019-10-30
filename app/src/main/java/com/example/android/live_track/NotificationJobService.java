package com.example.android.live_track;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationJobService extends JobService {
    private boolean jobCancelled = false;
    public static String mKey;
    private Location mlocation;

    @Override
    public boolean onStartJob(JobParameters params) {
        Toast.makeText(getApplicationContext(), "Job started", Toast.LENGTH_SHORT).show();

        SharedPreferences keyPref = getBaseContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        mKey = keyPref.getString("keyPref", "");
        doBackgroundWork(params);

        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                LocationListener lmLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mlocation = location;
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                };

                if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)
                        && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, lmLocationListener);

                } else {
                    Toast.makeText(getApplicationContext(), "Permission revoked", Toast.LENGTH_LONG);
                    stopSelf();
                }

                String longitude = mlocation.getLongitude() + "";
                String latitude = mlocation.getLatitude() + "";

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());
                String dateNow = formatter.format(date);

                Data data = new Data(mKey, longitude, latitude, dateNow);
                data.Push(getBaseContext(), mKey);


                Toast.makeText(getApplicationContext(), "Job finished", Toast.LENGTH_SHORT).show();
                jobFinished(params, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobCancelled = true;
        return true;
    }

}
