package com.leadstepapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashActivity.this, login.class);
                startActivity(intent);
                System.out.println("SPLASH");
                finish();
            }
        }, 2000);
    }
}