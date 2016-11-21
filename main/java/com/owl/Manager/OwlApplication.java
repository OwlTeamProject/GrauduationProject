package com.owl.Manager;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

/**
 * Created by robinitic on 2016. 9. 28..
 */

public class OwlApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this, "fonts/neotricc_r.ttf"))
                .addBold(Typekit.createFromAsset(this, "fonts/neotricc_bold.ttf"))
                .add("defaultfont",Typekit.createFromAsset(this,"fonts/neotricc_r.ttf"));

    }
}
