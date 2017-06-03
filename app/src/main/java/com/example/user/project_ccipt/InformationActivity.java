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

import java.io.IOException;
import java.util.List;

/**
 * Created by user on 2017-04-26.
 */

public class InformationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "InformationActivity";

    Geocoder geocoder;

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    String userProviderId, userName, userUid,userCountryName,userAddress;
    boolean userinput = false;
    double userLatitude, userLongitude;
    Uri userPhotoUrl;
    List<Address> userlist;

    EditText user_name,user_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);

        geocoder = new Geocoder(this);

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

                userlist = null;


                String str = user_location.getText().toString().trim();
                try {
                    userlist = geocoder.getFromLocationName(
                            str, // 지역 이름
                            10); // 읽을 개수
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
                }

                if (userlist != null) {
                    if (userlist.size() == 0) {
                        Toast.makeText(getApplicationContext(),"해당되는 주소 정보는 없습니다",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),userlist.get(0).toString(),Toast.LENGTH_LONG).show();
                        userAddress = userlist.get(0).getAddressLine(0);
                        userCountryName = userlist.get(0).getCountryName();  // 국가명
                        userLatitude = userlist.get(0).getLatitude();        // 위도
                        userLongitude = userlist.get(0).getLongitude();    // 경도
                    }
                }
                userinput = true;
                writeNewUser(userName, userUid, userPhotoUrl, userAddress ,userCountryName, userLatitude, userLongitude,userinput);

                Intent intent = new Intent(this,TeamListActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }


    public class User {
        User() {};
        User(String userName, String userUid, Uri userPhotoUrl,String userAddress,String userCountryName, double userLatitude, double userLongitude, boolean userinput) {
            name = userName;
            uid = userUid;
            photoUrl = userPhotoUrl;
            countryName = userCountryName;
            latitude = userLatitude;
            longitude = userLongitude;
            address = userAddress;
            input = userinput;
        };

        //TODO : Developing userAddress
        private String name, uid, countryName,address;
        private Uri photoUrl;
        private double latitude,longitude;
        private boolean input;

        public Uri getPhotoUrl() {
            return photoUrl;
        }
        public String getUid() {
            return uid;
        }
        public String getName() {
            return name;
        }
        public String getAddress(){ return address;}
        public String getCountryName(){ return countryName;}
        public double getLatitude(){ return latitude;}
        public double getLongitude(){ return longitude; }
        public boolean getInput(){ return input; }
    }

    private void writeNewUser(String name, String uid, Uri photourl, String address,String countryName, double latitude, double longitude, boolean input) {
        User user = new User(name, uid, photourl, address, countryName,latitude,longitude,input);

        database.getReference().child("Users").child(uid).child("name").setValue(user.getName());
        database.getReference().child("Users").child(uid).child("uid").setValue(user.getUid());
        database.getReference().child("Users").child(uid).child("photoUrl").setValue(user.getPhotoUrl().toString());
        database.getReference().child("Users").child(uid).child("address").setValue(user.getAddress().toString());
        database.getReference().child("Users").child(uid).child("countryName").setValue(user.getCountryName().toString());
        database.getReference().child("Users").child(uid).child("latitude").setValue(user.getLatitude());
        database.getReference().child("Users").child(uid).child("longitude").setValue(user.getLongitude());
        database.getReference().child("Users").child(uid).child("input").setValue(user.getInput());
    }
}
