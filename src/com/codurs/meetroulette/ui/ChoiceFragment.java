package com.codurs.meetroulette.ui;

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import com.codurs.meetroulette.R;
import com.esri.android.map.MapView;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.*;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;

import java.util.ArrayList;

/**
 * Created by Adeel on 6/7/14.
 */
public class ChoiceFragment extends Fragment {

    MapView mMapView;
    Graphic graphic;
    PictureMarkerSymbol symbol;
    GraphicsLayer graphicsLayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMapView = (MapView) getView().findViewById(R.id.map);
        mMapView.addLayer(new ArcGISTiledMapServiceLayer("" + "http://e1.onemap.sg/arcgis/rest/services/SM128/MapServer"));





        graphicsLayer = new GraphicsLayer();
        //plots the user's points
        for(int i=0;i<StaticObject.x.length;i++)
        {
            Point point = new Point();
            point.setX(StaticObject.x[i]);
            point.setY(StaticObject.y[i]);


            SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED, 5, SimpleMarkerSymbol.STYLE.CIRCLE);
            graphic = new Graphic(point, symbol);


            graphicsLayer.addGraphic(graphic);
        }





        //drawing a polyline
        MultiPath polyline;
        ArrayList mArrayList = new ArrayList<Point>();


        Point point = mMapView.toMapPoint(new Point(motionEvent.getX(), motionEvent.getY()));
        mArrayList.add(point);


        polyline = new Polygon();

        polyline.startPath(StaticObject.x[0],StaticObject.y[0]);

        for (int i = 1; i < mArrayList.size(); i++) {
            polyline.lineTo(StaticObject.x[i],StaticObject.y[i]);
        }

        Graphic graphic = new Graphic(polyline, new SimpleLineSymbol(Color.BLUE,4));

        graphicsLayer.addGraphic(graphic);







        Button yesButton = (Button) getView().findViewById(R.id.yesButton);
        Button noButton = (Button) getView().findViewById(R.id.noButton);

        yesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        return inflater.inflate(R.layout.fragment_choice, container, false);







    }
}