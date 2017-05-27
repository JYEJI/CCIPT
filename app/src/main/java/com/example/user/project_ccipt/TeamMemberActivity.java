package com.example.user.project_ccipt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by user on 2017-04-26.
 */

public class TeamMemberActivity extends Activity {

    private static final String currentTeamName = TeamListActivity.currentTeamName;
    private static final String TAG = "TeamMemberActivity";

    Button brainstorming_button,appointment_button,teamchat_button,teammember_button,plus_button;

    ListView memberList;
    ArrayList<String> members = new ArrayList<>();
    ArrayList<String> images = new ArrayList<>();

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference memberRef = database.getReference("Teams").child(currentTeamName).child("members");
    DatabaseReference userRef = database.getReference().child("Users");

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    String getMemberName = "", getMemberUid = "", getMemberPhoto = "";
    boolean duplicatedFlag = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teammember);

        TextView teamNameTextView = (TextView) findViewById(R.id.TeamMemberTeamName);
        teamNameTextView.setText(currentTeamName);

        brainstorming_button=(Button)findViewById(R.id.brainstorming_button);
        appointment_button=(Button)findViewById(R.id.appointment_button);
        teamchat_button=(Button)findViewById(R.id.teamchat_button);
        teammember_button=(Button)findViewById(R.id.teammember_button);
        plus_button=(Button)findViewById(R.id.memberplus_button);

        brainstorming_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TeamMemberActivity.this, BrainstormingActivity.class);
                startActivity(newIntent);
            }
        });

        appointment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TeamMemberActivity.this, AppointmentActivity.class);
                startActivity(newIntent);
            }
        });

        teamchat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TeamMemberActivity.this, TeamChatActivity.class);
                startActivity(newIntent);
            }
        });

        teammember_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(TeamMemberActivity.this, TeamMemberActivity.class);
                startActivity(newIntent);
            }
        });

        plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(TeamMemberActivity.this);
                builder.setTitle("Added Member Name");

                // Set up the input
                final EditText input = new EditText(TeamMemberActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        findMember(input.getText().toString().trim());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        memberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                members.clear();
                images.clear();
                for (DataSnapshot contact : contactChildren) {

                    images.add(contact.child("memberPhoto").getValue().toString());
                    members.add(contact.child("memberName").getValue().toString());
                }

                makeCustomList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

    }


    private void findMember(final String plusMember) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                Log.d("childrenValue::", contactChildren.toString());

                for (DataSnapshot contact : contactChildren) {

                    if(contact.child("name").getValue().toString().equals(plusMember)) {
                        getMemberUid = contact.child("uid").getValue().toString();
                        getMemberName = contact.child("name").getValue().toString();
                        getMemberPhoto = contact.child("photoUrl").getValue().toString();
                    }
                }

                if(getMemberName.equals("") && getMemberUid.equals("")) {
                    Toast.makeText(getBaseContext(), "There isn't that user.", Toast.LENGTH_LONG).show();
                } else {
                    checkDuplicated(getMemberUid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    };

    private void checkDuplicated(final String memberUid) {
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                Log.d("childrenValue::", contactChildren.toString());

                for (DataSnapshot contact : contactChildren) {

                    if(contact.child("memberUid").getValue().toString().equals(memberUid)) {
                        duplicatedFlag = true;
                    } else {
                        duplicatedFlag = false;
                    }
                }

                if(duplicatedFlag) {
                    Toast.makeText(getBaseContext(), "That user already joined this team.", Toast.LENGTH_LONG).show();
                } else {
                    writeNewMember();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    };

    public void makeCustomList() {
        CustomList adapter = new CustomList(this);
        memberList = (ListView)findViewById(R.id.memberListView);
        memberList.setAdapter(adapter);

        memberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), members.get(+position), Toast.LENGTH_LONG).show();
            }
        });
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context) {
            super(context, R.layout.memberview, members);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.memberview, null, true);
            TextView member = (TextView) rowView.findViewById(R.id.memberName);
            ImageView image = (ImageView) rowView.findViewById(R.id.memberImage);

            Uri photoUri = Uri.parse(images.get(+position));
            Glide.with(TeamMemberActivity.this).load(photoUri).into(image);
            member.setText(members.get(+position));
            return rowView;
        }
    }

    private static class Member {
        Member() {};
        Member(String name, String user, String photo) {
            memberName = name;
            memberUid = user;
            memberPhoto = photo;
        };

        //TODO : Developing userAddress
        private String memberName, memberUid, memberPhoto;

        public String getMemberName() {
            return memberName;
        };
        public String getMemberUid() {
            return memberUid;
        };
        public String getMemberPhoto() {
            return memberPhoto;
        };
    }


    private void writeNewMember() {
        Member member = new Member(getMemberName, getMemberUid, getMemberPhoto);

        memberRef.push().setValue(member);
    }

}
