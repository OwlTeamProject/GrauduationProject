package com.owl.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.owl.Manager.DBManager;
import com.owl.R;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class A4_StoredPathActivity extends AppCompatActivity {
    private final String TAG = "A3_SelectPathActivity";

    ListView pathList;
    TextView titleText;

    boolean longclicked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a4__stored_path);

        longclicked = false;
        pathList = (ListView) findViewById(R.id.pathListView);

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/neotricc_r.ttf");
        titleText = (TextView) findViewById(R.id.titleText);
        titleText.setTypeface(type);

        final DBManager dbManager = new DBManager(this);
        //dbManager.backupDB();
        //Log.d(TAG,"db backup");
        final ArrayList<String> temp = dbManager.selectAll();

        final ArrayList<String> value = new ArrayList<String>();
        for (int i = 0; i < temp.size(); i++) {
            StringTokenizer stringTokenizer = new StringTokenizer(temp.get(i), "\t");
            value.add(stringTokenizer.nextToken());
        }

        pathList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, value));
        pathList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!longclicked) {
                    Intent returnIntent = new Intent();
                    StringTokenizer stringTokenizer = new StringTokenizer(temp.get(i), "\t");
                    Log.d(TAG, "path ID:" + stringTokenizer.nextToken());
                    returnIntent.putExtra("startLat", Double.parseDouble(stringTokenizer.nextToken()));
                    returnIntent.putExtra("startLong", Double.parseDouble(stringTokenizer.nextToken()));
                    returnIntent.putExtra("destLat", Double.parseDouble(stringTokenizer.nextToken()));
                    returnIntent.putExtra("destLong", Double.parseDouble(stringTokenizer.nextToken()));

                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                else{
                    longclicked=false;
                }
            }
        });

        pathList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                longclicked = true;
                dbManager.deleteRow(value.get(i));
                value.remove(i);
                pathList.setAdapter(new ArrayAdapter<String>(A4_StoredPathActivity.this, android.R.layout.simple_list_item_1, value));
                Log.d("StoredPathActivity","path remove done");
                return false;
            }
        });
    }
}