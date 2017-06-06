package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by user on 2017-04-26.
 */

public class TeamChatActivity extends Activity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String currentTeamName = TeamListActivity.currentTeamName;
    private static final String TAG = "TeamChatActivity";
    Team currentTeam = new Team(currentTeamName);

    Button brainstorming_button,appointment_button,teamchat_button,teammember_button,send_button;
    EditText send_message;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference chatRef = database.getReference("Teams").child(currentTeamName).child("messages");

    ListView chatList;
    ArrayList<String> messages = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> photos = new ArrayList<>();

    ImageButton setting_bt;
    NavigationView navigationView;
    View headerLayout;
    ImageView nav_userImage;
    TextView nav_userName,nav_userEmail;
    String userimage,username,useremail;
    boolean buttoncliked = false;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teamchat);

        chatList = (ListView)findViewById(R.id.chatListView);
        relativeLayout = (RelativeLayout)findViewById(R.id.relativeLayout);

        brainstorming_button=(Button)findViewById(R.id.brainstorming_button);
        appointment_button=(Button)findViewById(R.id.appointment_button);
        teamchat_button=(Button)findViewById(R.id.teamchat_button);
        teammember_button=(Button)findViewById(R.id.teammember_button);
        send_button=(Button)findViewById(R.id.send_button);
        send_message=(EditText)findViewById(R.id.send_message);

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
        Glide.with(TeamChatActivity.this).load(photoUri).into(nav_userImage);
        nav_userName.setText(username);
        nav_userEmail.setText(useremail);

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
                    chatList.bringToFront();
                    relativeLayout.bringToFront();
                    send_button.bringToFront();
                    send_message.bringToFront();
                    buttoncliked=false;
                }


            }
        });
        navigationView.setNavigationItemSelectedListener(this);

        TextView teamNameTextView = (TextView) findViewById(R.id.TeamChatTeamName);
        teamNameTextView.setText(currentTeamName);



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
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 현재시간을 msec 으로 구한다.
                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm:ss");
                // nowDate 변수에 값을 저장한다.
                String formatDate = sdfNow.format(date);

                //공백처리
                if(send_message.getText().toString().trim().length() != 0){
                    writeNewMember(send_message.getText().toString().trim(), formatDate);
                }

                send_message.setText("");
            }
        });

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                messages.clear();
                times.clear();
                names.clear();
                photos.clear();
                for (DataSnapshot contact : contactChildren) {
                    photos.add(contact.child("chatPhoto").getValue().toString());
                    names.add(contact.child("chatName").getValue().toString());
                    times.add(contact.child("chatTime").getValue().toString());
                    messages.add(contact.child("chatText").getValue().toString());

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
        CustomList adapter = new CustomList(this);
        chatList = (ListView)findViewById(R.id.chatListView);
        chatList.setAdapter(adapter);

        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), messages.get(+position), Toast.LENGTH_LONG).show();
            }
        });
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context) {
            super(context, R.layout.chatlistview_other, messages);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView;

            if(currentUser.getDisplayName().equals(names.get(+position))) {
                rowView = inflater.inflate(R.layout.chatlistview_me, null, true);
                TextView chatName = (TextView) rowView.findViewById(R.id.chatName_me);
                TextView chatText = (TextView) rowView.findViewById(R.id.chatText_me);
                TextView chatTime = (TextView) rowView.findViewById(R.id.chatTime_me);
                ImageView chatImage = (ImageView) rowView.findViewById(R.id.chatImage_me);

                Uri photoUri = Uri.parse(photos.get(+position));
                Glide.with(TeamChatActivity.this).load(photoUri).into(chatImage);
                chatText.setText(messages.get(+position));
                chatTime.setText(times.get(+position));
                chatName.setText(names.get(+position));
            } else {
                rowView = inflater.inflate(R.layout.chatlistview_other, null, true);
                TextView chatName = (TextView) rowView.findViewById(R.id.chatName_other);
                TextView chatText = (TextView) rowView.findViewById(R.id.chatText_other);
                TextView chatTime = (TextView) rowView.findViewById(R.id.chatTime_other);
                ImageView chatImage = (ImageView) rowView.findViewById(R.id.chatImage_other);

                Uri photoUri = Uri.parse(photos.get(+position));
                Glide.with(TeamChatActivity.this).load(photoUri).into(chatImage);
                chatText.setText(messages.get(+position));
                chatTime.setText(times.get(+position));
                chatName.setText(names.get(+position));
            }

            return rowView;
        }
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


    private static class Chat {
        Chat() {};
        Chat(String name, String photo, String text, String time) {
            chatName = name;
            chatPhoto = photo;
            chatText = text;
            chatTime = time;
        };

        //TODO : Developing userAddress
        private String chatName, chatPhoto, chatText, chatTime;

        public String getChatName() {
            return chatName;
        };
        public String getChatPhoto() {
            return chatPhoto;
        }
        public String getChatText() {
            return chatText;
        };
        public String getChatTime() {
            return chatTime;
        }
    }

    private void writeNewMember(String text, String time) {
        Chat chat = new Chat(currentUser.getDisplayName(), currentUser.getPhotoUrl().toString(), text, time);

        chatRef.push().setValue(chat);
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
        chatList.bringToFront();
        return true;
    }
}
