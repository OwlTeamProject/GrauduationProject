package com.owl.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.owl.Manager.DBManager;
import com.owl.Manager.PathConvertManager;
import com.owl.R;
import com.parrot.arsdk.ARSDK;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.util.ArrayList;

public class A3_SelectPathActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    //testing App API key
    private String APIkey = "40feb38b-f9f4-3552-8d37-c74409dc2cb9";
    private final String TAG = "A3_SelectPathActivity";

    // to use Tmap API
    TMapView tmapView;
    RelativeLayout relativeLayout;
    TMapGpsManager tMapGpsManager;
    TMapPolyLine pathLine;
    TMapPoint savedPoint;
    TMapPoint startPoint=null, destPoint=null;
    TMapMarkerItem startMarker;

    Button startBtn;

    private final int LOAD_PATH_REQUEST = 700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a3_select_path);

        ARSDK.loadSDKLibs();

        // to hold path info. for later use. saved path
        pathLine = new TMapPolyLine();

        // reselect button 클릭시 나타나는 알림창 설정
        AlertDialog.Builder reselectBuilder = new AlertDialog.Builder(A3_SelectPathActivity.this);
        reselectBuilder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startPoint=null;
                destPoint = null;
                tmapView.removeAllMarkerItem();
                tmapView.removeTMapPath();
            }
        });
        reselectBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });
        reselectBuilder.setMessage("경로를 다시 선택하시겠습니까?");
        reselectBuilder.setTitle("경로 선택");
        //reselectBuilder.setMessage("목적지에 도착했습니다! 안내를 종료합니다");
        //reselectBuilder.setTitle("목적지 도착");

        final AlertDialog reselectDialog = reselectBuilder.create();

        // store path 버튼 클릭시 나타나는 알림창 설정
        AlertDialog.Builder storeBuilder = new AlertDialog.Builder(A3_SelectPathActivity.this);
        final EditText edittext = new EditText(A3_SelectPathActivity.this);
        edittext.setHint("출발지 -> 목적지");
        storeBuilder.setMessage("경로를 저장하시겠습니까?");
        storeBuilder.setTitle("경로 저장");

        storeBuilder.setView(edittext);

        storeBuilder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String pathID = edittext.getText().toString();
                DBManager dbManager = new DBManager(A3_SelectPathActivity.this);
                //dbManager.insertRow(pathID,startPoint.getLatitude(),startPoint.getLongitude(), destPoint.getLatitude(), destPoint.getLongitude());

                Log.d(TAG,"save "+pathID+"_startPoint; "+startPoint.getLatitude()+","+startPoint.getLongitude());
            }
        });

        storeBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        final AlertDialog storeDialog = storeBuilder.create();

        // showing map on the screen
        tmapView = new TMapView(this);
        tmapView.setSKPMapApiKey(APIkey);
        tmapView.setIconVisibility(true);
        tmapView.setZoomLevel(16);
        tmapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapView.setCompassMode(false);
        tmapView.setTrackingMode(true);
        tmapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint) {
                savedPoint = tMapPoint;

                TMapMarkerItem temp = new TMapMarkerItem();
                temp.setTMapPoint(tMapPoint);
                temp.setVisible(TMapMarkerItem.VISIBLE);
                // TODO: UI, 지도상의 아이콘 바꾸기
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.icon);
                temp.setIcon(bitmap);
                temp.setPosition(0.5f,1.0f);

                if(startPoint==null) {
                    Log.d(TAG,"start point set");
                    startPoint = savedPoint;
                    startMarker = temp;
                    startMarker.setID("start");
                    tmapView.addMarkerItem("start",startMarker);
                }
                else if(destPoint==null) {
                    Log.d(TAG,"dest point set");
                    destPoint = savedPoint;
                    tmapView.removeAllMarkerItem();
                    drawPedestrianPath(startPoint,destPoint);
                }

            }
        });
        findViewById(R.id.reselectBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reselectDialog.show();
            }
        });

        // setting map view on screen
        relativeLayout = (RelativeLayout) findViewById(R.id.mapView);
        relativeLayout.addView(tmapView);

        // to get mobile's GPS info.
        tMapGpsManager = new TMapGpsManager(this);
        tMapGpsManager.setMinTime(500);
        tMapGpsManager.setMinDistance(2);
        tMapGpsManager.setProvider(TMapGpsManager.NETWORK_PROVIDER);
        tMapGpsManager.OpenGps();

        // start button 클릭시 수행
        startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pathLine.getDistance()>0){
                    PathConvertManager converter = new PathConvertManager(getApplicationContext());
                    if(converter.convertPathToMavFile(pathLine)){
                        Intent intent = new Intent(A3_SelectPathActivity.this,A5_ConnectActivity.class);
                        intent.putExtra("mavlinkFilePath",converter.getMavlinkFilePath());
                        intent.putExtra("startLat",startPoint.getLatitude());
                        intent.putExtra("startLong",startPoint.getLongitude());
                        intent.putExtra("destLat",destPoint.getLatitude());
                        intent.putExtra("destLong",destPoint.getLongitude());
                        intent.putExtra("pathDistance",pathLine.getDistance()); //TODO: activity 통해서 넘겨주기
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Error; Cannot start guide",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(),"경로를 먼저 선택해주세요!",Toast.LENGTH_SHORT).show();
            }
        });

        //load button 클릭 시 DB로부터 경로정보를 가져옴
        findViewById(R.id.loadBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"load button click");

                Intent intent = new Intent(A3_SelectPathActivity.this,A4_StoredPathActivity.class);
                startActivityForResult(intent,LOAD_PATH_REQUEST);
            }
        });

        //Store button 클릭 시 DB에 경로 정보를 저장함
        findViewById(R.id.storeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeDialog.show();
            }
        });

    }

    public void drawPedestrianPath(TMapPoint source, TMapPoint destination){
        TMapData tMapData = new TMapData();
        tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, source, destination, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapPolyLine.setLineColor(Color.CYAN);
                tmapView.addTMapPath(tMapPolyLine);

                //Toast.makeText(A3_SelectPathActivity.this,"size:"+tMapPolyLine.getPassPoint().size(),Toast.LENGTH_SHORT).show();
                Log.d("mapView","size:"+tMapPolyLine.getLinePoint().size());

                //save path
                pathLine=tMapPolyLine;
            }
        });
    }

    @Override
    protected void onDestroy(){
        // when application end, close GPS
        tMapGpsManager.CloseGps();
        super.onDestroy();
    }

    @Override
    public void onLocationChange(Location location) {
        // when mobile's location changed, update current location and set current location to map's center
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        tmapView.setLocationPoint(lon,lat);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        if (requestCode == LOAD_PATH_REQUEST) {
            if (resultCode == RESULT_OK) {
                // 경로정보 가져온 경우
                Log.d(TAG,"get path info from activity");
                startPoint = new TMapPoint(data.getDoubleExtra("startLat",-1.0),data.getDoubleExtra("startLong",-1.0));
                destPoint = new TMapPoint(data.getDoubleExtra("destLat",-1.0),data.getDoubleExtra("destLong",-1.0));
                if(startPoint.getLatitude()!=-1 && startPoint.getLongitude()!=-1 &&
                        destPoint.getLatitude()!=-1 && destPoint.getLongitude()!=-1) {
                    drawPedestrianPath(startPoint, destPoint);
                    tmapView.setCenterPoint(startPoint.getLongitude(),startPoint.getLatitude());
                    tmapView.setTrackingMode(false);
                }
                else {
                    startPoint = null;
                    destPoint = null;
                    tmapView.removeTMapPath();
                }
            }
            else {
                // 경로정보 가져오지 못한 경우
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
