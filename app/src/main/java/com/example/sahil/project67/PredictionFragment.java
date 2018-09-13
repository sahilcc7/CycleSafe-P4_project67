package com.example.sahil.project67;

import android.app.Fragment;
import android.graphics.Color;
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
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.AsyncTask;

import static android.content.ContentValues.TAG;

public class PredictionFragment extends android.support.v4.app.Fragment {

    private Button getPrediction;
    List<Double> congestionArray = new ArrayList<>();
    List<Integer> hours = new ArrayList<>();
    private static final String PATH_TO_CSV = "data.csv";
    GraphView mGraph;
    LineGraphSeries<DataPoint> series;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_prediction, null, false);

        getPrediction = (Button) view.findViewById(R.id.getPredictionBtn);

        getPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double x,y;
                final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                y = 0;
                x=0;

                readCSV();


                mGraph = (GraphView) view.findViewById(R.id.graph);

                series = new LineGraphSeries<DataPoint>();

//                DataPoint[] dp = new DataPoint[] {
//
//                        new DataPoint(new Date().getTime(), 1),
//                        new DataPoint(new Date().getTime(), 1),
//                        new DataPoint(new Date().getTime(), 1),
//                        new DataPoint(new Date().getTime(), 1),
//                };

                for (int i = 0; i < congestionArray.size(); i++) {
                    x = x + 1;
                    y = congestionArray.get(i);
                    series.appendData(new DataPoint(x, y), true, congestionArray.size());
                }

                mGraph.addSeries(series);

                mGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {

                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if(isValueX) {
                            //return sdf.format(new Date((long)value));
                            return "";
                        }
                        else {
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });

                series.setThickness(10);
            }

        });

        return view;

    }


        private void createLineGraph(List<String[]> result) {
            DataPoint[] dataPoints = new DataPoint[result.size()];
            for (int i = 0; i < result.size(); i++){
                String [] rows = result.get(i);
                Log.d(TAG, "Output " + Integer.parseInt(rows[0] + " " + Integer.parseInt(rows[1])));

                dataPoints[i] = new DataPoint(Integer.parseInt(rows[0]), Integer.parseInt(rows[1]));
            }
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
            mGraph.addSeries(series);
        }

     public void readCSV() {

        try {

            CSVReader reader = new CSVReader(new InputStreamReader(getResources().openRawResource(R.raw.data)));
            String [] nextLine;

            reader.readNext();


            while ((nextLine = reader.readNext()) != null) {

                congestionArray.add(Double.parseDouble(nextLine[1]));
         }


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(), "The specified file was not found", Toast.LENGTH_SHORT).show();

        }

     }

}