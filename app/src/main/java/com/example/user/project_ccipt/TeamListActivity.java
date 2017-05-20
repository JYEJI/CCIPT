package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

    ListView teamList;
    public static String currentTeamName;
    String teamName;
    String teamImage;
    String TAG = "TeamListActivity";

    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference teamRef = database.getReference("Teams");
    DatabaseReference teamUserRef = database.getReference("TeamUser");

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


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

                //titles.add(title);
                Log.d("avalue::", dataSnapshot.getKey());

                //DataSnapshot contactSnapshot = dataSnapshot.child("");
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                Log.d("childrenValue::", contactChildren.toString());

                titles.clear();
                images.clear();
                for (DataSnapshot contact : contactChildren) {
                    Log.d("titlevalue:: ", contact.child("title").getValue().toString());
                    Log.d("imagevalue:: ", "" + contact.child("image").getValue().toString());
                    Log.d("uservalue:: ", "" + contact.child("userUid").getValue().toString());

                    Bitmap decodedImage = decodeBase64(contact.child("image").getValue().toString());
                    if(contact.child("userUid").getValue().toString().equals(currentUser.getUid())) {
                        titles.add(contact.child("title").getValue().toString());
                        images.add(decodedImage);
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
                                doTakeAlbumAction();
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
                                Log.d("edcodedImage:: ", encodedImage);


                                teamImage = encodedImage;

                                if(teamNameBox.getText().toString().equals("")) {
                                    teamName = "None";
                                } else {
                                    teamName = teamNameBox.getText().toString();
                                }

                                //TODO : How we handle image data?
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
        Team(String title, String image, String user) {
            teamTitle = title;
            teamImage = image;
            userUid = user;
        };

        //TODO : Developing userAddress
        private String teamTitle, teamImage, userUid;

        public String getTitle() {
            return teamTitle;
        };
        public String getImage() {
            return teamImage;
        };
        public String getUserUid() {
            return userUid;
        }
        public void setTitle(String currentTeam) {
            teamTitle = currentTeam;
        }
    }


    private void writeNewTeam(String title, String image) {
        Team team = new Team(title, image, currentUser.getUid());

        teamRef.push().setValue(team);

    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input)
    {
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
}

