package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/*TODO : Prove Image save problem*/


/**
 * Created by user on 2017-04-26.
 */

public class BrainstormingActivity extends Activity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String currentTeamName = TeamListActivity.currentTeamName;
    private static final String TAG = "BrainstormingActivity";

    //사진으로 전송시 되돌려 받을 번호
    static int REQUEST_PICTURE=1;
    //앨범으로 전송시 돌려받을 번호
    static int REQUEST_PHOTO_ALBUM=2;
    //첫번째 이미지 아이콘 샘플 이다.
    static String SAMPLEIMG="ic_launcher.png";


    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    Button brainstorming_button, appointment_button, teamchat_button, teammember_button, plus_button;

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> descriptions = new ArrayList<>();
    ArrayList<Bitmap> images = new ArrayList<>();
    ArrayList<String> key = new ArrayList<>();

    ImageView photo_imageview;
    Bitmap photo;
    String brainstormImage = "", brainstormTitle = "", brainstormDescription = "";
    int index;

    ListView brainstormListView;
    RelativeLayout relativeLayout;
    View headerLayout;
    ImageView nav_userImage;
    TextView nav_userName,nav_userEmail;

    String userimage,username,useremail;

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference brainstormRef = database.getReference("Teams").child(currentTeamName).child("Brainstorms");

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


    ImageButton setting_bt;
    NavigationView navigationView;

    boolean buttoncliked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brainstorming);

        BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.ccipt);
        photo = drawable.getBitmap();
        brainstormListView = (ListView)findViewById(R.id.brainstormlistview);
        relativeLayout=(RelativeLayout)findViewById(R.id.buttons);

        TextView teamNameTextView = (TextView) findViewById(R.id.BrainStormTeamName);
        teamNameTextView.setText(currentTeamName);

        brainstorming_button = (Button) findViewById(R.id.brainstorming_button);
        appointment_button = (Button) findViewById(R.id.appointment_button);
        teamchat_button = (Button) findViewById(R.id.teamchat_button);
        teammember_button = (Button) findViewById(R.id.teammember_button);
        plus_button = (Button) findViewById(R.id.plus_button);

        brainstorming_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(BrainstormingActivity.this, BrainstormingActivity.class);
                startActivity(newIntent);
            }
        });

        appointment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(BrainstormingActivity.this, AppointmentActivity.class);
                startActivity(newIntent);
            }
        });

        teamchat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(BrainstormingActivity.this, TeamChatActivity.class);
                startActivity(newIntent);
            }
        });

        teammember_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(BrainstormingActivity.this, TeamMemberActivity.class);
                startActivity(newIntent);
            }
        });

        plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BrainstormingActivity.this);
                builder.setTitle("Enter idea")
                        .setMessage("Title and Description");

                View customLayout=View.inflate(BrainstormingActivity.this,R.layout.brainstorming_dialog,null);
                builder.setView(customLayout);

                final EditText titleBox = (EditText) customLayout.findViewById(R.id.input_title);
                final EditText descriptionBox = (EditText)customLayout.findViewById(R.id.input_description);
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
                        new AlertDialog.Builder(BrainstormingActivity.this)
                                .setTitle("Select Image")
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

                                brainstormImage = encodedImage;

                                if(titleBox.getText().toString().trim().length() > 10) {
                                    Toast.makeText(getBaseContext(), "Length of team title is too long.\nYou should enter less than 10 characters", Toast.LENGTH_LONG).show();

                                } else if (titleBox.getText().toString().trim().equals("") || descriptionBox.getText().toString().trim().equals("")){
                                    Toast.makeText(getBaseContext(), "You should fill the boxes", Toast.LENGTH_LONG).show();
                                } else {
                                    brainstormTitle = titleBox.getText().toString().trim();
                                    brainstormDescription = descriptionBox.getText().toString().trim();
                                }

                                writeNewBrainstorm(brainstormTitle, brainstormDescription, brainstormImage);
                            }});

                // Setting Negative "NO" Button
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog
                                dialog.cancel();
                            }
                        });
                builder.create();
                builder.show();
            }
        });

        brainstormRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                titles.clear();
                descriptions.clear();
                images.clear();
                for (DataSnapshot contact : contactChildren) {

                    Bitmap decodedImage = decodeBase64(contact.child("image").getValue().toString());
                    images.add(decodedImage);
                    titles.add(contact.child("title").getValue().toString());
                    descriptions.add(contact.child("description").getValue().toString());
                }


                makeCustomList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        setting_bt = (ImageButton) findViewById(R.id.setting_bt);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        headerLayout = navigationView.getHeaderView(0);
        nav_userImage=(ImageView) headerLayout.findViewById(R.id.UserImage);
        nav_userName=(TextView)headerLayout.findViewById(R.id.userName);
        nav_userEmail=(TextView)headerLayout.findViewById(R.id.userEmail);

        userimage = currentUser.getPhotoUrl().toString();
        username = currentUser.getDisplayName().toString();
        useremail = currentUser.getEmail();

        Uri photoUri = Uri.parse(userimage);
        Glide.with(BrainstormingActivity.this).load(photoUri).into(nav_userImage);
        nav_userName.setText(username);
        if(useremail==null)
            nav_userEmail.setText(" ");
        else
            nav_userEmail.setText(useremail);

        setting_bt = (ImageButton) this.findViewById(R.id.br_setting_bt);
        navigationView = (NavigationView)this.findViewById(R.id.nav_view);

        final DrawerLayout drawer = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        setting_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Opens the Drawer
                if(!buttoncliked)
                {
                    drawer.openDrawer(navigationView);
                    drawer.bringToFront();
                    buttoncliked=true;
                }
                else
                {
                    drawer.closeDrawer(navigationView);
                    brainstormListView.bringToFront();
                    relativeLayout.bringToFront();
                    buttoncliked=false;
                }


            }
        });
        navigationView.setNavigationItemSelectedListener(this);

    }

    public void makeCustomList() {
        CustomList adapter = new CustomList(this);
        brainstormListView = (ListView)findViewById(R.id.brainstormlistview);
        brainstormListView.setAdapter(adapter);

        brainstormListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BrainstormingActivity.this);
                builder.setTitle("Idea")
                        .setMessage("Title and Description");

                View customLayout=View.inflate(BrainstormingActivity.this,R.layout.brainstorming_dialog,null);
                builder.setView(customLayout);

                final EditText titleBox = (EditText) customLayout.findViewById(R.id.input_title);
                final EditText descriptionBox = (EditText)customLayout.findViewById(R.id.input_description);
                photo_imageview = (ImageView)customLayout.findViewById(R.id.photo_imageview);

                index = position;

                titleBox.setText(titles.get(+position));
                descriptionBox.setText(descriptions.get(+position));
                photo_imageview.setImageBitmap(images.get(+position));

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
                        new AlertDialog.Builder(BrainstormingActivity.this)
                                .setTitle("Select Image")
                                .setNeutralButton("Select Album",albumListener)
                                .setNegativeButton("Cancel",cancelListener)
                                .show();
                    }
                });

                builder.setPositiveButton("Modify",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {

                                String encodedImage = encodeToBase64(photo, Bitmap.CompressFormat.JPEG, 100);

                                brainstormImage = encodedImage;

                                if(titleBox.getText().toString().trim().length() > 10) {
                                    Toast.makeText(getBaseContext(), "Length of team title is too long.\nYou should enter less than 10 characters", Toast.LENGTH_LONG).show();

                                } else if (titleBox.getText().toString().trim().equals("") || descriptionBox.getText().toString().trim().equals("")){
                                    Toast.makeText(getBaseContext(), "You should fill the boxes", Toast.LENGTH_LONG).show();
                                } else {
                                    brainstormTitle = titleBox.getText().toString().trim();
                                    brainstormDescription = descriptionBox.getText().toString().trim();
                                }

                                brainstormRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                                        key.clear();
                                        for (DataSnapshot contact : contactChildren) {
                                            key.add(contact.getKey());
                                        }

                                        Brainstorm brainstorm = new Brainstorm(brainstormTitle, brainstormDescription, brainstormImage, currentUser.getDisplayName());

                                        brainstormRef.child(key.get(index)).setValue(brainstorm);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                    }
                                });


                                //writeNewBrainstorm(brainstormTitle, brainstormDescription, brainstormImage);
                            }});

                // Setting Negative "NO" Button
                builder.setNegativeButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog
                                brainstormRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                                        key.clear();
                                        for (DataSnapshot contact : contactChildren) {
                                            key.add(contact.getKey());
                                        }

                                        brainstormRef.child(key.get(index)).setValue(null);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                    }
                                });

                            }
                        });
                builder.create();
                builder.show();
            }
        });

        registerForContextMenu(brainstormListView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_group) {
            Intent teamSetting = new Intent(getApplicationContext(), TeamSetting.class);
            teamSetting.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            teamSetting.putExtra("KILL_APP", true);
            startActivity(teamSetting);
        }
        else if (id == R.id.nav_location) {
            Intent information = new Intent(getApplicationContext(), InformationActivity.class);
            startActivity(information);
        }
        else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent killApp = new Intent(getApplicationContext(), SigninActivity.class);
            killApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            killApp.putExtra("KILL_APP", true);
            startActivity(killApp);
        }
        else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        brainstormListView.bringToFront();
        relativeLayout.bringToFront();
        return true;
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context) {
            super(context, R.layout.memberview, titles);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitem, null, true);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.brainstormImage);
            TextView title = (TextView) rowView.findViewById(R.id.brainstormTitle);
            TextView description = (TextView) rowView.findViewById(R.id.brainstormDescription);
            imageView.setImageBitmap(images.get(+position));
            title.setText(titles.get(+position));
            description.setText(descriptions.get(+position));
            return rowView;
        }
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
            if(requestCode==REQUEST_PICTURE){
                /*photo_imageview.setImageBitmap(loadPicture());
                photo = loadPicture();
                images.add(photo);*/
            }
            if(requestCode==REQUEST_PHOTO_ALBUM) {
                photo_imageview.setImageURI(data.getData());
                try {
                    photo = MediaStore.Images.Media.getBitmap(getContentResolver(),data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Brainstorm {
        Brainstorm() {};
        Brainstorm(String Btitle, String Bdescription, String Bimage, String Bname) {
            title = Btitle;
            description = Bdescription;
            image = Bimage;
            writer = Bname;
        };

        private String title, description, image, writer;

        public String getTitle() {
            return title;
        }
        public String getDescription() {
            return description;
        }
        public String getImage() {
            return image;
        }
        public String getWriter() {
            return writer;
        }
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

    private void writeNewBrainstorm(String title, String description, String image) {
        Brainstorm brainstorm = new Brainstorm(title, description, image, currentUser.getDisplayName());

        brainstormRef.push().setValue(brainstorm);
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