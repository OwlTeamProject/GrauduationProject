package com.owl.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.owl.Bebop.BebopDrone;
import com.owl.Bebop.BebopVideoView;
import com.owl.Manager.DBManager;
import com.owl.Manager.PathConvertManager;
import com.owl.R;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.skp.Tmap.MapUtils;
import com.skp.Tmap.TMapGpsManager;

public class A6_GuideActivity extends AppCompatActivity  implements TMapGpsManager.onLocationChangedCallback {
    private static final String TAG = "A6_GuideActivity";
    private BebopDrone mBebopDrone;

    // Connection Progress
    private ProgressDialog mConnectionProgressDialog;

    // Buttons
    private Button mTakeOffLandBt;
    private Button flightPlanBtn;
    //private Button expTestBtn;

    // TextViews
    private TextView mBatteryLabel;
    private TextView BGPSLabel;
    //private TextView mRSSILabel;
    private TextView MGPSLabel;
    private TextView distLabel;
    //private TextView velocityLabel;
    private EditText expText;

    // Values - Bebop's GPS , Mobile's GPS
    private double BGPSlong;
    private double BGPSlat;
    public static double BGPSalt;
    private double MGPSlong;
    private double MGPSlat;
    private double MGPSalt;
    private double distance;

    private DBManager mDBManager;

    TMapGpsManager tMapGpsManager;
    //// TODO: 2016-08-01 BebopVideoView 추가 후 확인하기
    private BebopVideoView mVideoView;

    private PathConvertManager convertManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a6_guide);

        initIHM();
        mDBManager = new DBManager(this);
        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(A5_ConnectActivity.EXTRA_DEVICE_SERVICE);

        mBebopDrone = new BebopDrone(this, service);
        mBebopDrone.addListener(mBebopListener);


        // to get mobile's GPS info.
        tMapGpsManager = new TMapGpsManager(this);
        tMapGpsManager.setMinTime(500);
        tMapGpsManager.setMinDistance(2);
        tMapGpsManager.setProvider(TMapGpsManager.GPS_PROVIDER);
        tMapGpsManager.OpenGps();

        // mDBManager.removeAll();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the bebop drone is connecting
        if ((mBebopDrone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mBebopDrone.getConnectionState()))) {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            // if the connection to the Bebop fails, finish the activity
            if (!mBebopDrone.connect()) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy(){
        if(mDBManager.backupDB())
            Toast.makeText(getApplicationContext(),"SD save complete",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(),"SD save error",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mBebopDrone != null) {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Disconnecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            if (!mBebopDrone.disconnect()) {
                finish();
            }
        }
    }

    //initIHM : setting buttons
    private void initIHM() {
        mVideoView = (BebopVideoView) findViewById(R.id.videoView);
        mBatteryLabel = (TextView) findViewById(R.id.batteryLabel);
        //velocityLabel = (TextView) findViewById(R.id.velocityLabel);
        //mRSSILabel = (TextView) findViewById(R.id.RSSILabel);
        BGPSLabel = (TextView) findViewById(R.id.GPSLabel);
        MGPSLabel = (TextView) findViewById(R.id.MGPSLabel);
        distLabel = (TextView) findViewById(R.id.DistLabel);


        mTakeOffLandBt = (Button) findViewById(R.id.takeOffOrLandBt);
        mTakeOffLandBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (mBebopDrone.getFlyingState()) {
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                        mBebopDrone.takeOff();
                        break;
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                        mBebopDrone.land();
                        break;
                    default:
                }
            }
        });

        findViewById(R.id.gazUpBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setGaz((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.gazDownBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setGaz((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.yawLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.yawRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.forwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.backBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.rollLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.rollRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        //flight plan test
        flightPlanBtn = (Button) findViewById(R.id.flightPlanBtn);
        flightPlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertManager = new PathConvertManager(getApplicationContext());
                //Toast.makeText(getApplicationContext(),convertManager.getMavlinkFilePath()+" ",Toast.LENGTH_SHORT).show();

                Toast.makeText(A6_GuideActivity.this,"Start flight plan",Toast.LENGTH_SHORT).show();
                mBebopDrone.startFlightPlan(ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM.ARCOMMANDS_COMMON_MAVLINK_START_TYPE_FLIGHTPLAN);
            }
        });

        /*testAltBtn = (Button) findViewById(R.id.maxAltTestBtn);
        testAltBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TestAlt","Call on Activity");
                mBebopDrone.testAlt();
            }
        });*/

        expText = (EditText) findViewById(R.id.ExpEditText);
        final CountDownTimer expTimer = new CountDownTimer(1000*31,1000) {
            String expName = expText.getText().toString();
            int tick = 0;
            @Override
            public void onTick(long l) {
                if(tick==0){
                    expName = expText.getText().toString();
                Toast.makeText(getApplicationContext(), "Start saving", Toast.LENGTH_SHORT).show();
                }
                mDBManager.insertData(BGPSlat,BGPSlong,BGPSalt,MGPSlat,MGPSlong,MGPSalt,distance,expName);

                Log.d("timer", "tick : " + (++tick));
                Log.d("timer", "   l : "+l);

                if(tick%10==0 && tick!=30) {
                    Toast.makeText(getApplicationContext(), tick + "sec", Toast.LENGTH_SHORT).show();
                }
                if(tick>30){
                    tick = 0;
                }
            }

            @Override
            public void onFinish() {
                Toast.makeText(A6_GuideActivity.this,"Timer ended",Toast.LENGTH_SHORT).show();
                mDBManager.backupDB();
            }
        };
        
        /* for exp - using setMax*/
        findViewById(R.id.expBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String expName = expText.getText().toString();
                if(!expName.isEmpty()){
                    //TODO : exp start
                    expTimer.start();
                }
                else{
                    Toast.makeText(A6_GuideActivity.this,"enter exp name first",Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.showBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(A6_GuideActivity.this, DBShowActivity.class);
                startActivity(intent);
            }
        });
    }

    //handling drone's state
    private final BebopDrone.Listener mBebopListener = new BebopDrone.Listener() {

        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state) {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    mConnectionProgressDialog.dismiss();
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    mConnectionProgressDialog.dismiss();

                    // finish();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
        }

        @Override
        public void onPilotingStateChanged(ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
            switch (state) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    mTakeOffLandBt.setText("Take off");
                    mTakeOffLandBt.setEnabled(true);
                    //mDownloadBt.setEnabled(true);
                    break;
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    mTakeOffLandBt.setText("Land");
                    mTakeOffLandBt.setEnabled(true);
                    //mDownloadBt.setEnabled(false);
                    break;
                default:
                    mTakeOffLandBt.setEnabled(false);
                    //mDownloadBt.setEnabled(false);
            }
        }

        @Override
        public void onPictureTaken(ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
            mVideoView.configureDecoder(codec);
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
            mVideoView.displayFrame(frame);
        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {

        }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) {

        }

        @Override
        public void onDownloadComplete(String mediaName) {

        }

        @Override
        public void onRSSIChanged(short rssi) { //added by Seona
            // 드론과 핸드폰 사이의 RSSI 값이 바꼈을 떄 불리는 함수
            //rssiValue = rssi;
            //mRSSILabel.setText(rssi + " dbm");
        }

        @Override
        public void onGPSChanged(double latitude, double longitude, double altitude) {  //added by Seona
            // 드론의 GPS가 바꼈을 때 불리는 함수
            BGPSlat = latitude;
            BGPSlong = longitude;
            BGPSalt = altitude;

            BGPSLabel.setText(String.format("%.3f, %.3f, %.3f",latitude,longitude,altitude));

            mBebopDrone.setMaxSpeed(MapUtils.getDistance(MGPSlat,MGPSlong,BGPSlat,BGPSlong));
            distance = MapUtils.getDistance(MGPSlat,MGPSlong,BGPSlat,BGPSlong);
            distLabel.setText(distance+" m");
        }
    };

    @Override
    public void onLocationChange(Location location) {
        // 핸드폰의 GPS가 바꼈을 때 불리는 함수
        MGPSlat = location.getLatitude();
        MGPSlong = location.getLongitude();
        MGPSalt = location.getAltitude();

        MGPSLabel.setText(String.format("%.3f, %.3f, %.3f",MGPSlat,MGPSlong,MGPSalt));

        mBebopDrone.setMaxSpeed(MapUtils.getDistance(MGPSlat,MGPSlong,BGPSlat,BGPSlong));
        distance = MapUtils.getDistance(MGPSlat,MGPSlong,BGPSlat,BGPSlong);
        distLabel.setText(distance+" m");
    }
}