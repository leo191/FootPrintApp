package helper;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request.Method;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.leo.footprintdriver.MainActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import config.AppConfig;

/**
 * Created by leo on 20/06/17.
 */

public class SendLocation implements Runnable{
    RequestQueue reQ;
    Request rqst;
    private Context context;
    private Double latitude,longitude;
    private String bus_no;
    Bundle bundle;
    public SendLocation(Context context, String bus_no,double latitude,double longitude)
    {
        reQ=Volley.newRequestQueue(context);
        this.bus_no=bus_no;
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;


    }

    public SendLocation(Context context,String bus_no)
    {
        this.bus_no=bus_no;
        this.context = context;
    }
//    public double getLatitude() {
//        return latitude;
//    }
//
    public synchronized void setLatitude(double latitude) {
        this.latitude = latitude;
    }
//
//    public double getLongitude() {
//        return longitude;
//    }
//
    public synchronized void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public void run() {
        while(true) {
            rqst = new StringRequest(Method.POST,
                    AppConfig.URL_SEND_LOCATION, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {


                           // Toast.makeText(context, "Sucess", Toast.LENGTH_SHORT).show();

//                            latitude = Double.parseDouble(bus_location.getString("latitude"));
//                            longitude = Double.parseDouble(bus_location.getString("longitude"));



                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(context,
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context,
                            error.getMessage(), Toast.LENGTH_LONG).show();

                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to login url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("bus_no", bus_no);
                    params.put("latitude", String.valueOf(latitude));
                    params.put("longitude", String.valueOf(longitude));



                    return params;
                }

            };


            // Adding request to request queue
            //AppController.getInstance().addToRequestQueue(strReq);
            reQ.add(rqst);


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }




    }

}
