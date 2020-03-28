package com.offbeat.apps.tasks.UI;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.offbeat.apps.tasks.R;
import com.offbeat.apps.tasks.Utils.Utils;

public class SplashScreen extends AppCompatActivity {

    private static final String TAG = SplashScreen.class.getSimpleName();
    FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    ImageView imageView;
    boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            Utils.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            Utils.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_splash_screen);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        imageView = findViewById(R.id.splash_image);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                        /*if (!prefManager.isFirstTimeLaunch()) {*/
                            if (firebaseUser == null) {
                                loggedIn = false;
                                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                                finish();
                            } else {
                                loggedIn = true;
                                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                                startActivity(i);
                                finish();

                        } /*else {
                            startActivity(new Intent(SplashScreen.this, WelcomeActivity.class));
                            finish();
                        }*/
                    }
                }, 1500);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
