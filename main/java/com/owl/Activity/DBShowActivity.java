package com.owl.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.owl.Manager.DBManager;
import com.owl.R;

import java.util.ArrayList;

public class DBShowActivity extends AppCompatActivity {

    private TextView showTxt;
    private ArrayList<String> info;
    private DBManager mDBmanager;
    String temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbshow);

        showTxt = (TextView) findViewById(R.id.dbShowTxt);
        showTxt.setMovementMethod(new ScrollingMovementMethod());
        mDBmanager = new DBManager(this);
        info = mDBmanager.selectAll();
        for(int i =0; i< info.size();i++){
            temp = info.get(i);
            if(temp != null)
                showTxt.setText(showTxt.getText() + temp);
        }
    }

}
