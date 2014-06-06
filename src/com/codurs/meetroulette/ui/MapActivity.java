package com.codurs.meetroulette.ui;

import android.app.Activity;
import android.os.Bundle;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledServiceLayer;
import com.codurs.meetroulette.R;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

/**
 * Created by Adeel on 6/7/14.dsdas
 */
public class MapActivity extends Activity {
    MapView mMapView;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Retrieve the map and initial extent from XML layout
        mMapView = (MapView)findViewById(R.id.map);
        // Add dynamic layer to MapView
        mMapView.addLayer(new ArcGISTiledMapServiceLayer("" +
                "http://e1.onemap.sg/arcgis/rest/services/SM128/MapServer"));

    }

    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }
}