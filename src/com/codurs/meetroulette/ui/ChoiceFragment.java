package com.codurs.meetroulette.ui;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.codurs.meetroulette.R;
import com.esri.android.map.MapView;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.ags.
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;

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
        //calculate mid point






        Point midpoint=null;
        //draw line to mid point
        Polyline line;
        MultiPath path;

        for(int i=0;i<StaticObject.x.length;i++)
        {
            line= new Polyline();




            p.setX(StaticObject.x[i]);
            p.setY(StaticObject.y[i]);

            Point[]path = new Point[1];
            path[0]=orgin;
            path[1]=midpoint;

            line.addPath(path);


        }



        mMapView.addLayer(graphicsLayer);


        return inflater.inflate(R.layout.fragment_choice, container, false);





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

    }
}