package Modules;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DirectionFinder  {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyCItvwaTJJTh1QVjstdnmIOaN0YVc-Royk";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private int exerciseFlag;

    Context context;

    public DirectionFinder(Context context, DirectionFinderListener listener, String origin, String destination,int exerciseFlag) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.context = context;
        this.exerciseFlag = exerciseFlag;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());

    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");
        int distance = 0;
        if (exerciseFlag == 1) {
            distance = Integer.parseInt(destination);
            if (distance < 4) {
                urlDestination = "Wintergarden Rd, Parnell, Auckland 1010"; //2k
            }
            else if (distance < 6) {
                urlDestination = "Ayr Reserve, Parnell";
            }
            else if (distance < 8) {
                urlDestination = "Albert Park, Bowen Ln, Auckland"; //6k
            }
            else if (distance < 9) {
                urlDestination = "Grey Lynn Park, 75 Dryden St, Grey Lynn, Auckland 1021"; // 8k
            }
            else {
                urlDestination = "Cornwall Park, Green Ln W, Epsom, Auckland 1051"; //10k
            }
        }

        if (exerciseFlag == 0) {
            return DIRECTION_URL_API + "&origin=" + urlOrigin + "&destination=" + urlDestination + "&alternatives=true" + "&mode=bicycling" + "&key=" + GOOGLE_API_KEY;
        }
        else {
            if (distance > 8) {
                return DIRECTION_URL_API + "&origin=" + urlOrigin + "&destination=" + urlOrigin + "&alternatives=true" + "&mode=bicycling&waypoints="+ urlDestination + "|20+Maungawhau+Rd,+Epsom,+Auckland&key=" + GOOGLE_API_KEY;
            }
            else {
                return DIRECTION_URL_API + "&origin=" + urlOrigin + "&destination=" + urlOrigin + "&alternatives=true" + "&mode=bicycling&waypoints="+ urlDestination + "|98+park+rd+grafton&key=" + GOOGLE_API_KEY;
            }
        }

    }


    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        Coordinate wp1_start = new Coordinate(0,0);
        Coordinate wp2_end = new Coordinate(0,0);


        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        int safestRouteIndex = 0;

        //Log.d("data","data: " + data);
        //Log.d("data2","data: " + jsonData);


        TrafficFinder trafficFinderNew = new TrafficFinder(context); //TRAFFIC API


        for (int i = 0; i < jsonRoutes.length(); i++) { //number of routes

            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);

            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);

            JSONArray jsonSteps = jsonLeg.getJSONArray("steps");
            JSONObject jsonStep = jsonSteps.getJSONObject(0);


            //Log.d("jsonSteps","jsonSteps: " + jsonSteps.length());

            for (int j=0; j<jsonSteps.length(); j++) { //number of steps

                jsonStep = jsonSteps.getJSONObject(j);

                //Log.d("tempEndLat","tempEndLat: " + jsonStep.getJSONObject("end_location").get("lat"));

                double tempEndLat = (double)(jsonStep.getJSONObject("end_location").get("lat"));
                double tempEndLong = (double)(jsonStep.getJSONObject("end_location").get("lng"));

                double tempStartLat = (double)(jsonStep.getJSONObject("start_location").get("lat"));
                double tempStartLong = (double)(jsonStep.getJSONObject("start_location").get("lng"));

                wp1_start.setCoordinate(tempStartLat, tempStartLong);
                wp2_end.setCoordinate(tempEndLat, tempEndLong);

                trafficFinderNew.execute(wp1_start, wp2_end);

                while (trafficFinderNew.waitFlag == 1) {
                    Log.d("LOOP","WAITING");
                }
            }

            route.bingCongestionScore = trafficFinderNew.getCongestionScore();
            Log.d("Route", "Score from ROUTE: " + route.bingCongestionScore);


            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));

            //route.steps = jsonLeg.getString("")

            routes.add(route);

            route.bingCongestionScore = route.bingCongestionScore/jsonSteps.length(); //This calculates the average 'safety' score based on congestion
            Log.d("AVG", "Average Congestion: " + route.bingCongestionScore);
            trafficFinderNew.resetCongestionScore(); //Reset congestion score because new route

        }


        for (int k = 0; k < routes.size()-1; k++) {
            if (routes.get(k + 1).bingCongestionScore < routes.get(safestRouteIndex).bingCongestionScore) {
                safestRouteIndex = k + 1;
            }
        }




        listener.onDirectionFinderSuccess(routes,safestRouteIndex);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
