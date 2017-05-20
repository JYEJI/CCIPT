package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by user on 2017-04-26.
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                //Intent intent = new Intent(SplashActivity.this,TeamListActivity.class);

                // to InformationActivity after developed other Activities
                Intent intent = new Intent(SplashActivity.this,SigninActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handler.sendEmptyMessageDelayed(0,3000);


    }
}
