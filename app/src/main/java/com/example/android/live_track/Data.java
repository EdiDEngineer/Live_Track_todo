package com.example.android.live_track;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Scanner;

public class Data {
    static String mKey;
    String mLongitude;
    String mLatitiude;
    String mDate;
    URL mUrl;

    public Data(String key, String longitude, String latitude, String date){
        mKey = key;
        mLatitiude = longitude;
        mLatitiude = latitude;
        mDate = date;
    }

    public void Push(Context context, String key){

        try {
            URL url = new URL("http://live-track-api.herokuapp.com/api/users/save_track?accountKey=" + key);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject("location");
            jsonParam.put("longitude", mLongitude);
            jsonParam.put("latitude", mLatitiude);
            jsonParam.put("actionDate", mDate);
            jsonParam.put("userKey", key);


            //Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Toast.makeText(context,"Push succssful", Toast.LENGTH_SHORT);

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Toast.makeText(context,"Push Successful", Toast.LENGTH_SHORT);
    }


}



