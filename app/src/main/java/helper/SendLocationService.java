package helper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import config.AppConfig;

/**
 * Created by leo on 1/07/17.
 */

public class SendLocationService extends Service {


    Thread worker;
    LocSenderThread locSenderThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       Bundle bundle  = new Bundle();
        bundle = intent.getBundleExtra("bundle");
        Toast.makeText(this,"hi" + bundle.getString("bus_no"),Toast.LENGTH_SHORT).show();

        locSenderThread = new LocSenderThread(getBaseContext(),bundle.getString("bus_no"),bundle.getDouble("latitude"),bundle.getDouble("longitude"));

        worker = new Thread(locSenderThread);
        worker.start();
       stopSelf();

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {

        Toast.makeText(this,"Hiiiiii",Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }
}






class LocSenderThread implements Runnable{

    RequestQueue reQ;
    Request rqst;
    private Double latitude,longitude;
    private String bus_no;


    public LocSenderThread(Context context,String bus_no,Double latitude,Double longitude)
    {
            reQ= Volley.newRequestQueue(context);
            this.bus_no = bus_no;
            this.latitude = latitude;
            this.longitude = longitude;

    }


    @Override
    public void run() {


        rqst = new StringRequest(Request.Method.POST,
                AppConfig.URL_SEND_LOCATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {



                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
//                        Toast.makeText(context,
//                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
//                    Toast.makeText(context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(context,
//                        error.getMessage(), Toast.LENGTH_LONG).show();

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



    }











}