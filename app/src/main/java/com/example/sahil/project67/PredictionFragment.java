package com.example.sahil.project67;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PredictionFragment extends android.support.v4.app.Fragment {

    private Button getPrediction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_prediction, null, false);

        getPrediction = (Button) view.findViewById(R.id.getPredictionBtn);

        getPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_LONG).show();

                GraphView graph = (GraphView) view.findViewById(R.id.graph);
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                        new DataPoint(0, 1),
                        new DataPoint(1, 5),
                        new DataPoint(2, 3),
                        new DataPoint(3, 2),
                        new DataPoint(4, 6)
                });
                graph.addSeries(series);

            }
        });

        return view;
    }

     public void readCSV() throws IOException {

         InputStream instream = new FileInputStream("data.csv");
         InputStreamReader inputreader = new InputStreamReader(instream);

         BufferedReader reader= new BufferedReader(inputreader);

         List<LatLng> latLngList = new ArrayList<LatLng>();
         String line = "";

         while( (line = reader.readLine()) != null) // Read until end of file
         {
             Log.d("data","data " + inputreader);
             double lat = Double.parseDouble(line.split(",")[0]);
             double lon = Double.parseDouble(line.split(",")[1]);
             latLngList.add(new LatLng(lat, lon));
         }

     }

}
