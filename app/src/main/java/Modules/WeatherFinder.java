package Modules;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.sahil.project67.CommuteFragment;
import com.example.sahil.project67.MainActivity;
import com.example.sahil.project67.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherFinder {

    private static final String WEATHER_URL = "http://api.wunderground.com/api/";
    private static final String WEATHER_API_KEY = "75fdf1feb356c73c";

    private String location = "Auckland";
    private Context context;

    private double rain;
    private double visibility;
    private double wind;



    public WeatherFinder(Context context, String location) {
        this.location = location;
        this.context = context;
    }


    private String createUrl(String location) {

        return WEATHER_URL + WEATHER_API_KEY + "/conditions/q/nz/" + location + ".json";
    }


    public void execute()  {

        final String URL = createUrl("Auckland");

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                //code to do the HTTP request
                HttpURLConnection conn = null;
                try {
                    java.net.URL url = new URL(URL);
                    conn = (HttpURLConnection) url.openConnection();
                } catch (Exception e) {
                    Log.d("EXCEPTION", "exception");
                }

                try {
                    conn.setRequestMethod("GET");
                } catch (Exception e) {
                    Log.d("EXCEPTION", "exception");
                }

                int responseCode;
                try {
                    responseCode = conn.getResponseCode();
                } catch (IOException e) {
                    Log.d("DEBUG", e.getMessage());
                }

                //Log.d("DEBUG", "Response code: " + responseCode);

                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();

                    String jsonString = response.toString();

                    Log.d("DEBUG", "API CALL!");

                    parseJSon(response.toString());

                } catch (Exception e) {
                    //return 0;
                }

            }
        });
        thread.start();
    }


    private void parseJSon(String data) throws JSONException {

        if (data == null)
            return;


        JSONObject jsonData = new JSONObject(data);

        Log.d("DATA", "JSONDATA: " + jsonData);

        visibility = jsonData.getJSONObject("current_observation").getInt("visibility_km");
        rain = jsonData.getJSONObject("current_observation").getInt("precip_today_metric");
        wind = jsonData.getJSONObject("current_observation").getInt("wind_kph");


        Log.d("DATA", "Rain: " + rain);
        Log.d("DATA", "Visibility: " + visibility);
        Log.d("DATA", "Wind (km/h): " + wind);

    }

    public double getRain() {
        return rain;
    }

    public double getWind() {
        return wind;
    }

    public double getVisibility() {
        return visibility;
    }



}
