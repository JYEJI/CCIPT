package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

/**
 * Created by user on 2017-04-26.
 */

public class InformationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "InformationActivity";

    final Geocoder geocoder = new Geocoder(this);

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

                List<Address> list = null;


                String str = user_location.getText().toString();
                try {
                    list = geocoder.getFromLocationName(
                            str, // 지역 이름
                            10); // 읽을 개수
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
                }

                if (list != null) {
                    if (list.size() == 0) {
                        Toast.makeText(getApplicationContext(),"해당되는 주소 정보는 없습니다",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),list.get(0).toString(),Toast.LENGTH_LONG).show();
                        //          list.get(0).getCountryName();  // 국가명
                        //          list.get(0).getLatitude();        // 위도
                        //          list.get(0).getLongitude();    // 경도
                    }
                }

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
