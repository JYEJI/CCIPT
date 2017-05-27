package com.example.user.project_ccipt;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by user on 2017-04-26.
 */

public class InformationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "InformationActivity";


    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    String userProviderId, userName, userUid;
    Uri userPhotoUrl;

    EditText user_name,user_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);

        user_name=(EditText) findViewById(R.id.input_name);
        user_location=(EditText) findViewById(R.id.input_location);

        findViewById(R.id.submit_btn).setOnClickListener(this);

        if (currentUser!= null) {
            // User is signed in
            userProviderId = currentUser.getProviderId();
            userName = currentUser.getDisplayName();
            userUid = currentUser.getUid();
            userPhotoUrl = currentUser.getPhotoUrl();

            // If the above were null, iterate the provider data
            // and set with the first non null data
            for (UserInfo userInfo : currentUser.getProviderData()) {
                if (userProviderId == null && userInfo.getProviderId() != null) {
                    userProviderId = userInfo.getProviderId();
                }
                if (userName == null && userInfo.getDisplayName() != null) {
                    userName = userInfo.getDisplayName();
                }
                if (userPhotoUrl == null && userInfo.getPhotoUrl() != null) {
                    userPhotoUrl = userInfo.getPhotoUrl();
                }
                if (userUid == null && userInfo.getUid() != null) {
                    userUid = userInfo.getUid();
                }
            }

            user_name.setText(userName);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_btn: {

                writeNewUser(userName, userUid, userPhotoUrl);

                Intent intent = new Intent(this,TeamListActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }


    private class User {
        User() {};
        User(String userName, String userUid, Uri userPhotoUrl) {
            name = userName;
            uid = userUid;
            photoUrl = userPhotoUrl;
        };

        //TODO : Developing userAddress
        private String name, uid;
        private Uri photoUrl;

        public Uri getPhotoUrl() {
            return photoUrl;
        }
        public String getUid() {
            return uid;
        }
        public String getName() {
            return name;
        }
    }

    private void writeNewUser(String name, String uid, Uri photourl) {
        User user = new User(name, uid, photourl);

        database.getReference().child("Users").child(uid).child("name").setValue(user.getName());
        database.getReference().child("Users").child(uid).child("uid").setValue(user.getUid());
        database.getReference().child("Users").child(uid).child("photoUrl").setValue(user.getPhotoUrl().toString());
    }
}
