package com.owl.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.owl.Bebop.DroneDiscoverer;
import com.owl.R;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.ArrayList;
import java.util.List;

public class A5_ConnectActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_SERVICE = "EXTRA_DEVICE_SERVICE";
    public static double MavlinkFilePath = 0.0;
    public static double startLat =  0.0;
    public static double startLong =  0.0;
    public static double destLat =  0.0;
    public static double destLong =  0.0;

    private static final String TAG = "A5_ConnectActivity";

    public DroneDiscoverer mDroneDiscoverer;

    private final List<ARDiscoveryDeviceService> mDronesList = new ArrayList<>();

    static {
        ARSDK.loadSDKLibs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a5_connect);

        Intent intent = getIntent();

        MavlinkFilePath = intent.getDoubleExtra("mavlinkFilePath", -1.0);
        startLat = intent.getDoubleExtra("startLat", -1.0);
        startLong = intent.getDoubleExtra("startLong", -1.0);
        destLat = intent.getDoubleExtra("destLat", -1.0);
        destLong = intent.getDoubleExtra("destLong", -1.0);


        final ListView listView = (ListView) findViewById(R.id.list);

        // Assign adapter to ListView
        listView.setAdapter(mAdapter);
        mDroneDiscoverer = new DroneDiscoverer(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // setup the drone discoverer and register as listener
        mDroneDiscoverer.setup();
        mDroneDiscoverer.addListener(mDiscovererListener);

        // start discovering
        mDroneDiscoverer.startDiscovering();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // clean the drone discoverer object
        mDroneDiscoverer.stopDiscovering();
        mDroneDiscoverer.cleanup();
        mDroneDiscoverer.removeListener(mDiscovererListener);
    }

    private final DroneDiscoverer.Listener mDiscovererListener = new  DroneDiscoverer.Listener() {

        @Override
        public void onDronesListUpdated(List<ARDiscoveryDeviceService> dronesList) {
            mDronesList.clear();
            mDronesList.addAll(dronesList);

            mAdapter.notifyDataSetChanged();
        }
    };

    private final BaseAdapter mAdapter = new BaseAdapter()
    {
        @Override
        public int getCount()
        {
            return mDronesList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mDronesList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.activity_a5_connect_button, null);
            }

            final String text = "drone_selector is pressed";
            final Button toA6_GuideActivity = (Button)v.findViewById(R.id.toA6_GuideActivity);

            toA6_GuideActivity.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    //Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                    Intent intent = null;

                    ARDiscoveryDeviceService service = (ARDiscoveryDeviceService) mAdapter.getItem(0);                          // position = 0 으로 hardcoded
                    ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
                    switch (product) {
                        case ARDISCOVERY_PRODUCT_ARDRONE:
                        case ARDISCOVERY_PRODUCT_BEBOP_2:
                            intent = new Intent(A5_ConnectActivity.this, A6_GuideActivity.class);
                            break;

                        default:
                            Log.e(TAG, "The type " + product + " is not supported by this sample");
                    }

                    if (intent != null) {
                        intent.putExtra(EXTRA_DEVICE_SERVICE, service);
                        intent.putExtra("mavlinkFilePath",MavlinkFilePath);
                        intent.putExtra("startLat",startLat);
                        intent.putExtra("startLong",startLong);
                        intent.putExtra("destLat",destLat);
                        intent.putExtra("destLong",destLong);
                        startActivity(intent);
                    }

                }
            });

            return v;

        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}