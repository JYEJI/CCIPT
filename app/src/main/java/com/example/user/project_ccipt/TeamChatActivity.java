package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by user on 2017-04-26.
 */

public class TeamChatActivity extends Activity {

    private static final String currentTeamName = TeamListActivity.currentTeamName;
    Team currentTeam = new Team(currentTeamName);

    Button brainstorming_button,appointment_button,teamchat_button,teammember_button;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teamchat);

        TextView teamNameTextView = (TextView) findViewById(R.id.TeamChatTeamName);
        teamNameTextView.setText(currentTeam.getTitle());

        brainstorming_button=(Button)findViewById(R.id.brainstorming_button);
        appointment_button=(Button)findViewById(R.id.appointment_button);
        teamchat_button=(Button)findViewById(R.id.teamchat_button);
        teammember_button=(Button)findViewById(R.id.teammember_button);

        brainstorming_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TeamChatActivity.this, BrainstormingActivity.class);
                startActivity(newIntent);
            }
        });

        appointment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TeamChatActivity.this, AppointmentActivity.class);
                startActivity(newIntent);
            }
        });

        teamchat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TeamChatActivity.this, TeamChatActivity.class);
                startActivity(newIntent);
            }
        });

        teammember_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TeamChatActivity.this, TeamMemberActivity.class);
                startActivity(newIntent);
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
}
