package com.example.sahil.CycleSafe;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Distance;
import Modules.Duration;
import Modules.Route;

public class ExerciseFragment extends Fragment implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private Button btnFindPath;
    private AutoCompleteTextView etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private com.google.android.gms.maps.model.LatLngBounds LatLngBounds = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136)
    );


    // This method is called when commute mode is first opened
    // ie. the fragment is created
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_exercise, null, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnFindPath = (Button) view.findViewById(R.id.btnFindPath);
        etDestination = (AutoCompleteTextView) view.findViewById(R.id.etDestination);



        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });


        return view;

    }


    //Execute below once "Find" button pressed
    private void sendRequest() {
        Context context = getActivity().getApplicationContext();
        String origin = "University of Auckland, Newmarket";
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //In exercise mode, the exerciseFlag variable is set to 1 when creating a directionFinder object
            new DirectionFinder(context, this, origin, destination,1).execute(); //Creates DirectionFinder OBJECT, calls execute() function
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

        LatLng latlng = new LatLng(-36.8816822, 174.7559136);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Auckland NZ")
                .position(latlng)));

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

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.get(safestRouteIndex).startLocation, 16));

            Duration actualDuration = routes.get(safestRouteIndex).duration;
            int durationValue = actualDuration.value * 2;
            durationValue = (int)Math.ceil((double)durationValue / 60);

            Distance actualDistance = routes.get(safestRouteIndex).distance;
            float distanceValue = actualDistance.value * 2;
            distanceValue = distanceValue / (float)1000;

            DecimalFormat df = new DecimalFormat("#.#");
            String distanceString = df.format(distanceValue);

            ((TextView) getView().findViewById(R.id.tvDuration)).setText(durationValue + " mins");
            ((TextView) getView().findViewById(R.id.tvDistance)).setText(distanceString + " km");

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
    }


}