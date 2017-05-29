package com.example.user.project_ccipt;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

/**
 * Created by user on 2017-04-26.
 */

public class TeamListActivity extends Activity implements View.OnClickListener {

    //?ъ쭊?쇰줈 ?꾩넚???섎룎??諛쏆쓣 踰덊샇
    static int REQUEST_PICTURE=1;
    //?⑤쾾?쇰줈 ?꾩넚???뚮젮諛쏆쓣 踰덊샇
    static int REQUEST_PHOTO_ALBUM=2;

    ImageView photo_imageview;
    Bitmap photo;

    public static String currentTeamName;
    ListView teamList;
    String teamName;
    String teamImage;
    String TAG = "TeamListActivity";

    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference teamRef = database.getReference("Teams");

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    // Assume thisActivity is the current activity
    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teamlist);

        BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.ccipt);
        photo = drawable.getBitmap();

        findViewById(R.id.teamplus_button).setOnClickListener(this);
        Log.d("outListener::", "true");
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

    protected void onStart() {
        super.onStart();
    }

    public void makeCustomList() {
        CustomList adapter = new CustomList(TeamListActivity.this);
        teamList = (ListView)findViewById(R.id.teamListView);
        teamList.setAdapter(adapter);

        teamList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentTeamName = titles.get(+position);
                Toast.makeText(getBaseContext(), currentTeamName, Toast.LENGTH_LONG).show();

                Intent newIntent = new Intent(TeamListActivity.this, BrainstormingActivity.class);

                startActivity(newIntent);
                finish();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.teamplus_button:


                AlertDialog.Builder builder = new AlertDialog.Builder(TeamListActivity.this);
                builder.setTitle("Make a Team")
                        .setMessage("Team Name");

                final View customLayout=View.inflate(TeamListActivity.this,R.layout.teamlist_dialog,null);
                builder.setView(customLayout);

                final EditText teamNameBox = (EditText) customLayout.findViewById(R.id.input_teamname);
                photo_imageview = (ImageView)customLayout.findViewById(R.id.photo_imageview);

                photo_imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                                    } else {
                                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                                    }
                                } else {
                                    doTakeAlbumAction();
                                }

                            }
                        };
                        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialog.cancel();
                            }
                        };
                        new AlertDialog.Builder(TeamListActivity.this)
                                .setTitle("Select Image")
                                //.setPositiveButton("Take a Photo",cameraListener)
                                .setNeutralButton("Select Album",albumListener)
                                .setNegativeButton("Cancel",cancelListener)
                                .show();
                    }
                });

                // Setting Positive "Yes" Button
                builder.setPositiveButton("Make",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {

                                String encodedImage = encodeToBase64(photo, Bitmap.CompressFormat.JPEG, 100);

                                teamImage = encodedImage;

                                if(teamNameBox.getText().toString().trim().length() > 20) {
                                    Toast.makeText(getBaseContext(), "Length of team title is too long.\nYou should enter less than 20 characters", Toast.LENGTH_LONG).show();

                                } else if(!teamNameBox.getText().toString().trim().equals("")) {
                                    teamName = teamNameBox.getText().toString().trim();
                                }



                                writeNewTeam(teamName, teamImage);
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
                break;
        }
    }

    private static class Team {
        Team() {};
        Team(String title, String image, Leader members) {
            teamTitle = title;
            teamImage = image;
            teamMember = members;
        };

        //TODO : Developing userAddress
        private String teamTitle, teamImage;
        private Leader teamMember;

        public String getTitle() {
            return teamTitle;
        };
        public String getImage() {
            return teamImage;
        };
        public void setTitle(String currentTeam) {
            teamTitle = currentTeam;
        }
        public Leader getMembers() {
            return teamMember;
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
        }
    }

    private static class Leader {
        Leader() {};
        Leader(Member member) {
            leaderMember = member;
        }

        private Member leaderMember;

        public Member getLeaderMember() {
            return leaderMember;
        };
    }


    private void writeNewTeam(String title, String image) {
        Member member = new Member(currentUser.getDisplayName(), currentUser.getUid(), currentUser.getPhotoUrl().toString());
        Leader leader = new Leader(member);
        Team team = new Team(title, image, leader);

        teamRef.child(team.getTitle()).setValue(team);
        teamRef.child(team.getTitle()).child("members").push().setValue(member);
        teamRef.child(team.getTitle()).child("members").child("dummyMember").removeValue();
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void doTakeAlbumAction(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,REQUEST_PHOTO_ALBUM);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){

            if(requestCode==REQUEST_PHOTO_ALBUM) {
                photo_imageview.setImageURI(data.getData());
                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(),data.getData());
                    photo = bm;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                Log.d(TAG, "5");
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doTakeAlbumAction();
                } else {
                    Toast.makeText(this, "If you wanna use this, you permit.", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }
}

