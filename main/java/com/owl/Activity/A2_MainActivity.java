package com.owl.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.owl.R;
import com.tsengvn.typekit.TypekitContextWrapper;

/**
 * Created by Koo on 2016-09-17.
 */
public class A2_MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a2__main);

        Button toA3_SelectPathActivity = (Button) findViewById(R.id.toA3_SelectPathActivity);

        toA3_SelectPathActivity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(A2_MainActivity.this, A3_SelectPathActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}