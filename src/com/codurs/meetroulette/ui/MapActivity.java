package com.codurs.meetroulette.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import com.codurs.meetroulette.core.BootstrapApplication;
import com.codurs.meetroulette.pusher.PusherService;
import com.esri.android.map.MapView;
import com.codurs.meetroulette.R;
import com.codurs.meetroulette.pusher.PusherService;
import com.codurs.meetroulette.pusher.PusherService.LocalBinder;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.google.gson.Gson;


/**
 * Created by Adeel on 6/7/14.dsdas
 */
public class MapActivity extends Activity {

    public static final String EVENT_MARKER_CLICKED = "client-marker-clicked-event";

    MapView mMapView;

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

        FragmentManager fragmentManager = getFragmentManager();

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ChoiceFragment f = new ChoiceFragment();
        ft.replace(android.R.id.content,f);
        ft.commit();

        /*ft.add(f,"");

        if (f.isHidden()) {
            ft.show(f);
            layout.setVisibility(View.VISIBLE);
            b.setText("Hide");
        } else {
            ft.hide(f);
            b.setText("Show");
            layout.setVisibility(View.GONE);
        }
        ft.commit();*/


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
                    }
                }
            }
        }
    }

}