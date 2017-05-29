package com.example.user.project_ccipt;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by user on 2017-04-26.
 */

public class AppointmentActivity extends FragmentActivity{

    private static final String currentTeamName = TeamListActivity.currentTeamName;
    Team currentTeam = new Team(currentTeamName);

    Button brainstorming_button,appointment_button,teamchat_button,teammember_button, plus_btn;

    int PLACE_PICKER_REQUEST = 1;
    final String TAG = "AppointmentActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment);

        TextView teamNameTextView = (TextView) findViewById(R.id.AppointmentTeamName);
        teamNameTextView.setText(currentTeam.getTitle());

        brainstorming_button = (Button) findViewById(R.id.brainstorming_button);
        appointment_button = (Button) findViewById(R.id.appointment_button);
        teamchat_button = (Button) findViewById(R.id.teamchat_button);
        teammember_button = (Button) findViewById(R.id.teammember_button);
        plus_btn = (Button) findViewById(R.id.plus_button);

        brainstorming_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(AppointmentActivity.this, BrainstormingActivity.class);
                startActivity(newIntent);
            }
        });

        appointment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(AppointmentActivity.this, AppointmentActivity.class);
                startActivity(newIntent);
            }
        });

        teamchat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(AppointmentActivity.this, TeamChatActivity.class);
                startActivity(newIntent);
            }
        });

        teammember_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(AppointmentActivity.this, TeamMemberActivity.class);
                startActivity(newIntent);
            }
        });

        plus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                //37.282944 127.046250
                LatLng boundSW = new LatLng(37.282844,127.046150);
                LatLng boundNE = new LatLng(37.283044,127.046350);
                LatLngBounds latLngBounds = new LatLngBounds(boundSW, boundNE);
                builder.setLatLngBounds(latLngBounds);
                try {
                    startActivityForResult(builder.build(AppointmentActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static class Team {
        Team() {};
        Team(String title) {
            teamTitle = title;
        };

        //TODO : Developing userAddress
        private String teamTitle, teamImage;

        public String getTitle() {
            return teamTitle;
        };
        public void setTitle(String currentTeam) {
            teamTitle = currentTeam;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String name = String.format("Place: %s", place.getName());
                String address = String.format("Place: %s", place.getAddress());
                Toast.makeText(this, "name :: " + name + "address :: " + address, Toast.LENGTH_LONG).show();
            }
        }
    }
}
