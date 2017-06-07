package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by user on 2017-06-06.
 */

public class TeamSetting extends AppCompatActivity {

    ImageView photo_imageview;
    Bitmap photo;

    public static String currentTeamName;
    ListView teamList;
    String teamName;
    String teamImage;
    String TAG = "TeamListActivity";

    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    String userimage,username,useremail;

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference teamRef = database.getReference("Teams");

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teamsetting);

        teamRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                titles.clear();
                images.clear();
                for (DataSnapshot contact : contactChildren) {

                    if(contact.child("members").getChildrenCount() == 0) {
                        String contactName = contact.getKey();
                        teamRef.child(contactName).removeValue();
                    } else{
                        Iterable<DataSnapshot> contactChildren2 = contact.child("members").getChildren();
                        for(DataSnapshot contact2 : contactChildren2) {
                            if(contact2.child("memberUid").getValue().toString().equals(currentUser.getUid())) {
                                titles.add(contact.child("title").getValue().toString());
                                Bitmap decodedImage = decodeBase64(contact.child("image").getValue().toString());
                                images.add(decodedImage);
                            }
                        }
                    }
                }

                makeCustomList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

    }

    public void makeCustomList() {
        TeamSetting.CustomList adapter = new TeamSetting.CustomList(TeamSetting.this);
        teamList = (ListView)findViewById(R.id.teamListView_setting);
        teamList.setAdapter(adapter);

        teamList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentTeamName = titles.get(+position);
                Toast.makeText(getBaseContext(), currentTeamName, Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamSetting.this);
                builder.setTitle("Do you want to leave your team?");
                // Setting Positive "Yes" Button
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                teamRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                                        titles.clear();
                                        images.clear();
                                        for (DataSnapshot contact : contactChildren) {

                                            if(contact.child("members").getChildrenCount() == 0) {
                                                String contactName = contact.getKey();
                                                teamRef.child(contactName).removeValue();
                                            } else{
                                                Iterable<DataSnapshot> contactChildren2 = contact.child("members").getChildren();
                                                for(DataSnapshot contact2 : contactChildren2) {
                                                    if(contact2.child("memberUid").getValue().toString().equals(currentUser.getUid())) {
                                                        teamRef.child(contact.getKey()).removeValue();

                                                        titles.remove(contact.child("title").getValue().toString());
                                                        Bitmap decodedImage = decodeBase64(contact.child("image").getValue().toString());
                                                        images.remove(decodedImage);
                                                    }
                                                }
                                            }
                                        }

                                        makeCustomList();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                    }
                                });
                            }});

                // Setting Negative "NO" Button
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog
                                dialog.cancel();
                            }
                        });
                builder.show();
            }
        });
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context) {
            super(context, R.layout.teamlistview, titles);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.teamlistview, null, true);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.teamImage);
            TextView title = (TextView) rowView.findViewById(R.id.teamTitle);
            title.setText(titles.get(+position));
            imageView.setImageBitmap(images.get(+position));
            return rowView;
        }
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean isKill = intent.getBooleanExtra("KILL_APP",true);
        if (isKill) {
            moveTaskToBack(true);
            finish();
        }
    }

}

