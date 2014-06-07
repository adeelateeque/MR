package com.codurs.meetroulette.ui;

import android.app.Activity;
import android.os.Bundle;
import com.codurs.meetroulette.R;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

public class MainActivity extends Activity {
    MapView mMapView;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Retrieve the map and initial extent from XML layout
        mMapView = (MapView)findViewById(R.id.map);
        // Add dynamic layer to MapView
        mMapView.addLayer(new ArcGISTiledMapServiceLayer("" +"http://e1.onemap.sg/arcgis/rest/services/SM128/MapServer"));

        //why onemap ??
    }
}
