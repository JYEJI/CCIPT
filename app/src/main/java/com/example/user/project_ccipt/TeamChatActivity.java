package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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

public class TeamChatActivity extends Activity {

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
    ArrayList<Bitmap> photos = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teamchat);

        TextView teamNameTextView = (TextView) findViewById(R.id.TeamChatTeamName);
        teamNameTextView.setText(currentTeamName);

        brainstorming_button=(Button)findViewById(R.id.brainstorming_button);
        appointment_button=(Button)findViewById(R.id.appointment_button);
        teamchat_button=(Button)findViewById(R.id.teamchat_button);
        teammember_button=(Button)findViewById(R.id.teammember_button);
        send_button=(Button)findViewById(R.id.send_button);
        send_message=(EditText)findViewById(R.id.send_message);

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

                    Uri imageUri = Uri.parse(contact.child("chatPhoto").getValue().toString());

                    Bitmap bitmap = null;
                    //TODO: exception error
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    photos.add(bitmap);
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
            View rowView = inflater.inflate(R.layout.chatlistview_other, null, true);
            TextView chatName = (TextView) rowView.findViewById(R.id.chatName);
            TextView chatText = (TextView) rowView.findViewById(R.id.chatText);
            TextView chatTime = (TextView) rowView.findViewById(R.id.chatTime);
            ImageView chatImage = (ImageView) rowView.findViewById(R.id.chatImage);
            chatText.setText(messages.get(+position));
            chatTime.setText(times.get(+position));
            chatName.setText(names.get(+position));
            chatImage.setImageBitmap(photos.get(+position));
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
}
