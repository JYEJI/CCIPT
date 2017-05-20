package com.example.user.project_ccipt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
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
    ArrayList<Bitmap> images = new ArrayList<>();

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference memberRef = database.getReference("Teams").child(currentTeamName).child("members");

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
                //TODO
                /*
                open dialog -> get user name.

                find user in database through the user name and insert the user name to database team member.
                if there isn't the user name in database, toast "no exist that user"
                */

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
                        findMember(input.getText().toString());
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

                //titles.add(title);
                Log.d("avalue::", dataSnapshot.getKey());

                //DataSnapshot contactSnapshot = dataSnapshot.child("");
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                Log.d("childrenValue::", contactChildren.toString());

                members.clear();
                for (DataSnapshot contact : contactChildren) {

                    Uri imageUri = Uri.parse(contact.child("memberPhoto").getValue().toString());
                    Log.d("zxcv::", imageUri.toString());
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        Log.d("asdf::", "asdf");

                    } catch (IOException e) {
                        Log.d("qwer::", "qwer");
                        e.printStackTrace();
                    }
                    members.add(contact.child("memberName").getValue().toString());
                    images.add(bitmap);

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
        DatabaseReference userRef = database.getReference().child("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                Log.d("childrenValue::", contactChildren.toString());

                for (DataSnapshot contact : contactChildren) {

                    Log.d("uservalue:: ", contact.child("name").getValue().toString());

                    if(contact.child("name").getValue().toString().equals(plusMember)) {
                        getMemberUid = contact.child("uid").getValue().toString();
                        getMemberName = contact.child("name").getValue().toString();
                        getMemberPhoto = contact.child("photoUrl").getValue().toString();
                    }
                }

                if(getMemberName.equals("") && getMemberUid.equals("") && getMemberPhoto.equals("")) {
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
        DatabaseReference teamRef = database.getReference().child("Teams").child(currentTeamName).child("members");
        teamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();
                Log.d("childrenValue::", contactChildren.toString());

                for (DataSnapshot contact : contactChildren) {

                    //Log.d("uservalue:: ", contact.child("name").getValue().toString());

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
            member.setText(members.get(+position));
            image.setImageBitmap(images.get(+position));
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
        }
    }


    private void writeNewMember() {
        Member member = new Member(getMemberName, getMemberUid, getMemberPhoto);

        memberRef.push().setValue(member);
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

}
