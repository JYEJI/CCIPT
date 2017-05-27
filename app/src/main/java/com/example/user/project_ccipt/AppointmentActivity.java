package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by user on 2017-04-26.
 */

public class AppointmentActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String currentTeamName = TeamListActivity.currentTeamName;
    Team currentTeam = new Team(currentTeamName);

    Button brainstorming_button,appointment_button,teamchat_button,teammember_button;
    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView teamNameTextView = (TextView) findViewById(R.id.AppointmentTeamName);
        teamNameTextView.setText(currentTeam.getTitle());

        brainstorming_button=(Button)findViewById(R.id.brainstorming_button);
        appointment_button=(Button)findViewById(R.id.appointment_button);
        teamchat_button=(Button)findViewById(R.id.teamchat_button);
        teammember_button=(Button)findViewById(R.id.teammember_button);

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng ajou = new LatLng(37.2821251, 127.0463559);
        mMap.addMarker(new MarkerOptions().position(ajou).title("Marker in Ajou University"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ajou, 14));

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
}
