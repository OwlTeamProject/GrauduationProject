package com.owl.Manager;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.parrot.arsdk.armavlink.ARMavlinkException;
import com.parrot.arsdk.armavlink.ARMavlinkFileGenerator;
import com.parrot.arsdk.armavlink.ARMavlinkMissionItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;

import java.util.ArrayList;

/**
 * Created by robinitic on 2016. 7. 31..
 */

public class PathConvertManager {
    private static String mavlinkFilePath;

    public PathConvertManager(Context context){
        ContextWrapper conwrap = new ContextWrapper(context);
        mavlinkFilePath = conwrap.getFilesDir().toString()+"/flightPlan.mavlink";
    }

    public static String getMavlinkFilePath(){
        return mavlinkFilePath;
    }

    public boolean convertPathToMavFile(TMapPolyLine TmapPath){

        boolean converted = false;

        if(TmapPath.getDistance()!=0){
            ArrayList<TMapPoint> list = TmapPath.getLinePoint();

            try {
                ARMavlinkFileGenerator fileGenerator = new ARMavlinkFileGenerator();
                double nextLat,curLat=list.get(0).getLatitude();
                double nextLong,curLong=list.get(0).getLongitude();
                double x,y,brng;

                for (int i = 0; i < list.size(); i++) {
                    nextLat = list.get(i).getLatitude();
                    nextLong = list.get(i).getLongitude();

                    //calculate bearing (direction)
                    y = Math.sin(curLong-nextLong)*Math.cos(curLat);
                    x = Math.cos(nextLat)*Math.sin(curLat) - Math.sin(nextLat)*Math.cos(curLat)*Math.cos(curLong-nextLong);
                    brng =Math.toDegrees( Math.atan2(y, x) );
                    brng = (brng+360)%360;

                    //add mission item. from index 1~end
                    // fileGenerator.addMissionItem(ARMavlinkMissionItem.CreateMavlinkNavWaypointMissionItem((float)nextLat,(float)nextLong,1.5f,(float)brng));
                    fileGenerator.addMissionItem(ARMavlinkMissionItem.CreateMavlinkMissionItemWithAllParams(5,5,0,(float)brng,(float)nextLat,(float)nextLong,1.5f,16,0,3,0,1));

                    curLat = nextLat;
                    curLong = nextLong;

                    //fileGenerator.addMissionItem(ARMavlinkMissionItem.CreateMavlinkNavWaypointMissionItem(tempLat,tempLong,1f,0));
                    //fileGenerator.addMissionItem(ARMavlinkMissionItem.CreateMavlinkMissionItemWithAllParams(3,1000,0,0,tempLat,tempLong, 1f,16,0,3,0,1));
                    //pram1(radius), pram2(time), pram3(orbit), pram4(yaw), lat, long, alt, command, seq,frame, current, auto continue

                }

                //fileGenerator.addMissionItem(ARMavlinkMissionItem.CreateMavlinkMissionItemWithAllParams(5,1000,0,180,37.566695f,126.948225f, 1f,16,0,3,0,1));

                fileGenerator.CreateMavlinkFile(mavlinkFilePath);
                //mavlink file 만들고 문제없으면 converted = true 로 지정!
                Log.d("flightPlan","from converter, mavlink converted");

                converted = true;

            }
            catch (ARMavlinkException e) {
                Log.i("flightPlan", "exception occurred!");
                e.printStackTrace();
            }

        }

        return converted;
    }
}

