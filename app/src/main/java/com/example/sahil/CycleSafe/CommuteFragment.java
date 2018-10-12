package com.example.sahil.CycleSafe;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;

import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.PlacesAutoCompleteAdapter;
import Modules.Route;
import Modules.WeatherFinder;


public class CommuteFragment extends Fragment implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private Button btnFindPath;
    private AutoCompleteTextView etOrigin;
    private AutoCompleteTextView etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private double locationLong, locationLat;

    //Instantiate weatherFinder object to get weather status
    WeatherFinder weatherFinder = new WeatherFinder(getContext(), "Auckland");


    private com.google.android.gms.maps.model.LatLngBounds LatLngBounds = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136)
    );

    // This method is called when commute mode is first opened
    // ie. the fragment is created
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_commute, null, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Context context = this.getActivity();

        // This chunk of code gets the user's current location
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        //Get device location.
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria,false));

        locationLat = location.getLatitude();
        locationLong = location.getLongitude();


        btnFindPath = (Button) view.findViewById(R.id.btnFindPath);
        etOrigin = (AutoCompleteTextView) view.findViewById(R.id.etOrigin);
        etDestination = (AutoCompleteTextView) view.findViewById(R.id.etDestination);

        //Calls the autocomplete methods
        etOrigin.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item));
        etDestination.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item));


        //When the button is pressed, call the sendRequest method
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        return view;

    }


    /*Executes after "Find" button pressed.
    * Calls DirectionFinder, TrafficFinder, WeatherFinder modules*/
    public void sendRequest() {
        Context context = getActivity().getApplicationContext();

        String latString = String.valueOf(locationLat);
        String longString = String.valueOf(locationLong);

        // Sets the origin and destination variables to user inputs
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();

        if (origin.isEmpty()) { // If no input for origin then set origin to current location
            origin =  latString + "," + longString;
        }
        if (destination.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            weatherFinder.execute();
            new DirectionFinder(context, this, origin, destination,0).execute(); //Creates DirectionFinder OBJECT, calls execute() function
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        mMap.setMyLocationEnabled(true);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.style_json));

            if (!success) {
                Log.e("StyleError", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("StyleErrors", "Can't find style. Error: ", e);
        }

        LatLng latlng = new LatLng(locationLat, locationLong);

        etOrigin.setText("");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    // Called when directionFinder begins its functionality
    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait.",
                "Finding safest route!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    // Called when directionFinder successfully finds the safest route
    // This method draws the safest path onto the map
    @Override
    public void onDirectionFinderSuccess(List<Route> routes, int safestRouteIndex) {

        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        try {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.get(safestRouteIndex).startLocation, 13));
            ((TextView) getView().findViewById(R.id.tvDuration)).setText(routes.get(safestRouteIndex).duration.text);
            ((TextView) getView().findViewById(R.id.tvDistance)).setText(routes.get(safestRouteIndex).distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(routes.get(safestRouteIndex).startAddress)
                    .position(routes.get(safestRouteIndex).startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(routes.get(safestRouteIndex).endAddress)
                    .position(routes.get(safestRouteIndex).endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.GREEN).
                    width(20);

            for (int i = 0; i < routes.get(safestRouteIndex).points.size(); i++)
                polylineOptions.add(routes.get(safestRouteIndex).points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        } catch (Exception e) {
            Toast.makeText(getActivity(), "GOOGLE API PROBLEM", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        showWeatherAlert();

    }

    //Displays the weather dialog box
    public void showWeatherAlert() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());

        String weatherRain;
        String weatherVisibility;
        String weatherWind;


        //getters from weatherFinder to find conditions.
        if (weatherFinder.getRain() == 0) {
            weatherRain = "No Rain!";
        }
        else {
            weatherRain = "It has been raining, roads might be slippery";
        }

        if (weatherFinder.getVisibility() >= 1) {
            weatherVisibility = "Good Visibility (>1km)";
        }

        else {
            weatherVisibility = "Poor Visibilty, ensure you have high vis & Lights";
        }

        if (weatherFinder.getWind() >= 18) {
            weatherWind = "Strong Winds";
        }

        else {
            weatherWind = "Low wind";
        }

        builder1.setMessage(weatherRain + "\n" + weatherVisibility + "\n" + weatherWind);

        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();


    }


}
