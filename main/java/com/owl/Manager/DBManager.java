package com.owl.Manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by Koo on 2016-08-01.
 */
public class DBManager {
    // DB관련 상수 선언
    private static final String DB_NAME = "OWL_GUIDRONE_exp.db";
    private static final String TABLE_NAME = "EXP_journal";
    public static final int dbVersion = 1;

    // DB관련 객체 선언
    private OpenHelper opener; // DB opener
    private SQLiteDatabase db; // DB controller

    // 부가적인 객체들
    private Context context;

    // 생성자
    public DBManager(Context context) {
        this.context = context;
        this.opener = new OpenHelper(context, DB_NAME, null, dbVersion);
        db = opener.getWritableDatabase();
        //opener.onCreate(db);
    }

    private class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
            super(context, name, null, version);
            // TODO Auto-generated constructor stub
        }

        // 생성된 DB가 없을 경우에 한번만 호출됨
        @Override
        public void onCreate(SQLiteDatabase arg0) {

            String createSql = "create table " + TABLE_NAME + " ("
                    + "id integer primary key autoincrement, "
                    + "BGPSlat double, " + "BGPSlong double, " + "BGPSalt double, "
                    + "MGPSlat double, " + "MGPSlong double, " + "MGPSalt double, "+ "distance double,"
                    + "expName text )";
            arg0.execSQL(createSql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
        }
    }

    // 데이터 추가
    public void insertData(double BGPSlat, double BGPSlong, double BGPSalt,
                           double MGPSlat, double MGPSlong, double MGPSalt, double distance,String expName) {
        String sql = "insert into " + TABLE_NAME + " values(NULL," +
                BGPSlat+", " + BGPSlong+", " + BGPSalt +", " +
                MGPSlat+", " + MGPSlong+", " + MGPSalt +", " +distance+"," + expName+ ");";
        db.execSQL(sql);
    }

    public void deleteRow(String id){   //id 기준으로 삭제
        String deleteSql = "delete from "+ TABLE_NAME +" where id =  '"+id+"';";
        db.execSQL(deleteSql);
    }

    public String selectRow(String id){
        String selectSql = "select * from "+ TABLE_NAME+" where id = '"+id+"';";
        Cursor result = db.rawQuery(selectSql, null);

        // result(Cursor 객체)가 비어 있으면 null 리턴
        if (result.moveToFirst()) {
            String res = result.getString(0) + "," + result.getDouble(1)+ "," + result.getDouble(2)+ "," + result.getDouble(3)+ "," + result.getDouble(4);
            result.close();
            return res;
        }

        result.close();
        return null;
    }

    public ArrayList<String> selectAll(){
        String sql = "select * from " + TABLE_NAME+ ";";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        ArrayList<String> infos = new ArrayList<String>();

        while (!results.isAfterLast()) {
            String info = results.getString(0)+"\t"+results.getDouble(1)+"\t"+results.getDouble(2)+"\t"+results.getDouble(3)+"\t"+results.getDouble(4)
                    +results.getDouble(5)+"\t"+results.getDouble(6)+"\t"+results.getDouble(7)+"\t"+results.getDouble(8)+"\n";
            infos.add(info);
            results.moveToNext();
        }
        results.close();
        return infos;

    }

    public boolean backupDB(){
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String currentDBPath = db.getPath();
                String backupDBPath = "//OWL_Backup_exp_1029.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void removeAll(){
        String sql = "drop table " + TABLE_NAME ;
        db.execSQL(sql);
    }

}