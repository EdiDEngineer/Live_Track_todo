package com.example.android.live_track;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.poovam.pinedittextfield.PinField;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import static com.example.android.live_track.NotificationService.CHANNEL_ID;
import static com.example.android.live_track.SharedPreferenceUtil.initPref;

public class MainActivity extends AppCompatActivity {
    PinField pinField;
    SharedPreferences mSettings;
    private ProgressBar mProgressBar;
    public static URL mUrl;
    Boolean mWrong = false;
    PinField input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        mSettings = initPref(getApplicationContext());

        if (!mSettings.getBoolean("signedUp", true)) {
            Intent intent = new Intent(this, DetailActivity.class);
            startActivity(intent);

        }

        requestPermissions();

        if (!getConnectivityStatus(getApplicationContext())) {
            Intent intent = new Intent(this, NoInternetActivity.class);
            startActivity(intent);
        }

        createNotificationChannel();
        Intent intent = new Intent(this, NotificationService.class);
        ContextCompat.startForegroundService(this, intent);

    }


    private void requestPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 101);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "LiveTrack",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

    }
    public void verifyUser(View view) {
        input = findViewById(R.id.pin);
        final String key = Objects.requireNonNull(input.getText()).toString();
        SharedPreferenceUtil.setPrefString(view.getContext(), "keyPref", key);
        mProgressBar = findViewById(R.id.progressBar);

        @SuppressLint("StaticFieldLeak") AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String url = "http://live-track-api.herokuapp.com/api/users/verify?accountKey=" + key;

                try {
                    String json = getJson(url);
                    JSONObject jsonObject = new JSONObject(json);
                    if (!jsonObject.getString("ok").equals("true")) {
                        mWrong = true;
                        return null;
                    }
                    JSONObject contacts = jsonObject.getJSONObject("data");
                    SharedPreferenceUtil.setPrefString(getApplicationContext(), "namePref", contacts.getString("name"));
                    SharedPreferenceUtil.setPrefString(getApplicationContext(), "emailPref", contacts.getString("email"));
                } catch (Exception e) {
                    finishActivity(1);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                mProgressBar.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Object o) {
                mProgressBar.setVisibility(View.INVISIBLE);
                super.onPostExecute(o);
            }
        };

        task.execute();

        if (mWrong) {
            new AlertDialog.Builder(this)
                    .setTitle("Login Error!")
                    .setMessage("Retype your password")
                    .show();
        } else {
            mSettings.edit().putBoolean("signedUp", false).apply();
            Intent intent = new Intent(this, DetailActivity.class);
            startActivity(intent);
        }

    }


    public static boolean getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    public String getJson(String url) throws IOException {

        try {
            mUrl = new URL(url);
        } catch (Exception ignored) {
        }

        HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
        InputStream stream = connection.getInputStream();
        Scanner scanner = new Scanner(stream);
        scanner.useDelimiter("\\A");
        if (scanner.hasNext()) {
            connection.disconnect();
            return scanner.next();
        } else {
            Toast.makeText(this.getApplicationContext(), "No data", Toast.LENGTH_LONG).show();
            connection.disconnect();
            return null;
        }


    }


}
