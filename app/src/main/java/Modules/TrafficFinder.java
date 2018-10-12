package Modules;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class TrafficFinder {
    private static final String TRAFFIC_API_URL = "http://dev.virtualearth.net/REST/V1/Routes/Driving?";
    private static final String BING_API_KEY = "AqS4Ne5sT_qCkBBMTzu9PrT0nTzTkI0XDl8Npw6AEr1DsvV0UheG0XT4j20CNXuc";

    public static int waitFlag = 0;

    private Coordinate wp1;
    private Coordinate wp2;

    private String congestionString;
    private int congestionScore = 0;

    Context context;

    public TrafficFinder(Context context) {

        this.wp1 = wp1;
        this.wp2 = wp2;
        this.context = context;

    }

    private String createUrl(Coordinate wp1, Coordinate wp2) {

        return TRAFFIC_API_URL + "wp.1=" + wp1.getLat() + "," + wp1.getLon() + "&wp.2=" + wp2.getLat() + "," + wp2.getLon() + "&key=" + BING_API_KEY;
    }

    // Finds the traffic between the two coordinates using the Bing API
    public void execute(Coordinate wp1, Coordinate wp2) throws JSONException {
        waitFlag = 1;
        final String URL = createUrl(wp1, wp2);

        // Creates a new thread
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

                    //congestionScore++;

                } catch (Exception e) {
                    //return 0;
                }

            }
        });
        thread.start();
    }


    // Parses the JSON data received from the API
    private void parseJSon(String data) throws JSONException {

        if (data == null) {
            Log.d("data", "THIS IS NULL");
            return;
        }

        JSONObject jsonData = new JSONObject(data);


        congestionString = (jsonData.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("trafficCongestion")).toString();

        Log.d("CongestionString", congestionString);

        int tempScore = 0;

        // Sets the congestionScore of the route based on the traffic API
        if (congestionString.equals("Unknown")) {
            tempScore = 0;
        } else if (congestionString.equals("Light")) {
            tempScore = 1;
        } else if (congestionString.equals("Mild")) {
            tempScore = 2;
        } else if (congestionString.equals("Medium")) {
            tempScore = 3;
        } else if (congestionString.equals("Heavy")) {
            tempScore = 2;
        } else {
            Log.d("ERROR", "UNDDEFINED!!!!!!!!!!!");
            tempScore = 0;
        }

        congestionScore += tempScore;

        waitFlag = 0;

        Log.d("CONGESTION", "CONGESTION SCORE: " + congestionScore);
    }

    // Returns congestionScore
    public int getCongestionScore() {
        Log.d("CONGESTION", "CONGESTION SCORE-------------------------- " + congestionScore);
        return congestionScore;
    }

    // Resets congestionScore
    public void resetCongestionScore() {
        congestionScore = 0;
    }
}

