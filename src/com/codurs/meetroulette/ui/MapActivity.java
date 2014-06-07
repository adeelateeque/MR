package com.codurs.meetroulette.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import com.codurs.meetroulette.core.BootstrapApplication;
import com.codurs.meetroulette.pusher.PusherService;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.codurs.meetroulette.R;
import com.codurs.meetroulette.pusher.PusherService;
import com.codurs.meetroulette.pusher.PusherService.LocalBinder;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.google.gson.Gson;

import java.util.ArrayList;


/**
 * Created by Adeel on 6/7/14.dsdas
 */
public class MapActivity extends Activity {

    public static final String EVENT_MARKER_CLICKED = "client-marker-clicked-event";

    MapView mMapView;
    Graphic graphic;
    PictureMarkerSymbol symbol;
    GraphicsLayer graphicsLayer;
    
    
    private MessageReceiver messageReceiver = new MessageReceiver();
    private IntentFilter messageFilter = new IntentFilter();
    private PusherService mPusherService;

    private boolean isBound;

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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        BootstrapApplication.getInstance().unbindService(mConnection);
        BootstrapApplication.getInstance().unregisterReceiver(messageReceiver);
    }

    private void fetchVenuesNear(String latlng)
    {
    }

    protected void onResume() {
        super.onResume();
        mMapView.unpause();

        if (!isBound)
        {
            Intent pusherService = new Intent(BootstrapApplication.getInstance(), PusherService.class);
            BootstrapApplication.getInstance().bindService(pusherService, mConnection, Context.BIND_AUTO_CREATE);
        }
        BootstrapApplication.getInstance().registerReceiver(messageReceiver, messageFilter);
    }

    /** Defines callback for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mPusherService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            isBound = false;
        }
    };


    public class MessageReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(PusherService.ACTION_EVENT_RECEIVED))
            {
                Gson gson = new Gson();
                String eventName = intent.getStringExtra(PusherService.EXTRA_EVENT_NAME);
                String eventData = intent.getStringExtra(PusherService.EXTRA_EVENT_DATA);
                if (eventName.startsWith(EVENT_MARKER_CLICKED))
                {
                    com.esri.core.geometry.Point point = gson.fromJson(eventData,
                            com.esri.core.geometry.Point.class);
                    if (mMapView != null)
                    {
                        MultiPath polyline;
                        ArrayList mArrayList = new ArrayList<Point>();


                        point = mMapView.toMapPoint(new Point(motionEvent.getX(), motionEvent.getY()));
                        mArrayList.add(point);


                        polyline = new Polygon();

                        polyline.startPath(StaticObject.x[0], StaticObject.y[0]);

                        for (int i = 1; i < mArrayList.size(); i++) {
                            polyline.lineTo(StaticObject.x[i], StaticObject.y[i]);
                        }

                        Graphic graphic = new Graphic(polyline, new SimpleLineSymbol(Color.BLUE,4));

                        graphicsLayer.addGraphic(graphic);
                    }
                }
            }
        }
    }

}