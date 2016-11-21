package com.owl.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.owl.R;
import com.tsengvn.typekit.TypekitContextWrapper;

/**
 * Created by Koo on 2016-09-17.
 */
public class A1_SplashActivity extends AppCompatActivity implements Animation.AnimationListener {

    // Images
    ImageView schoolLogo;
    ImageView owlLogo;

    // Animation
    Animation animFadein;
    Animation animFadeout;

    //Typeface owlFont;
    //Typeface owlFontB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a1__splash);


        //owlFont = Typeface.createFromAsset(this.getAssets(),"font/neotricc_r.ttf");
        //owlFontB = Typeface.createFromAsset(this.getAssets(),"font/neorticc_bold.ttf");

        schoolLogo = (ImageView) findViewById(R.id.ewhaImage);
        owlLogo = (ImageView) findViewById(R.id.owlImage);

        // load the animation
        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        // set animation listener
        animFadein.setAnimationListener(this);
        animFadeout.setAnimationListener(this);

        // visible images
        // schoolLogo.setVisibility(View.VISIBLE);

        // start the animation
        schoolLogo.startAnimation(animFadeout);
        owlLogo.startAnimation(animFadein);

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // Take any action after completing the animation

        // check for fade in animation
        if (animation == animFadein) {
            //Toast.makeText(getApplicationContext(), "Animation Stopped", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(A1_SplashActivity.this, A2_MainActivity.class);
            startActivity(intent);

            finish();
        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}