package com.example.user.project_ccipt;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by user on 2017-04-26.
 */

public class AppointmentActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String currentTeamName = TeamListActivity.currentTeamName;

    Button brainstorming_button,appointment_button,teamchat_button,teammember_button, plus_btn;

    int PLACE_PICKER_REQUEST = 1;
    final String TAG = "AppointmentActivity";

    ListView appointmentListView;

    private ArrayList<String> namelist = new ArrayList<>();
    private ArrayList<String> addresslist = new ArrayList<>();
    private ArrayList<String> datetimelist = new ArrayList<>();
    private ArrayList<String> key = new ArrayList<>();
    String name = "", address = "", datetime = "";
    int year=100, month, day, hour=100, minute;

    private Double teamlat = 0.0D, teamlng = 0.0D;
    private ArrayList<String> memberUids = new ArrayList();
    private ArrayList<Double> lng_ary = new ArrayList<>();
    private ArrayList<Double> lat_ary = new ArrayList<>();

    int index;


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference appointmentRef = database.getReference("Teams").child(currentTeamName).child("Appointments");
    DatabaseReference memberRef = database.getReference("Teams").child(currentTeamName).child("members");
    DatabaseReference userRef = database.getReference("Users");

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    ImageButton dateButton, timeButton, placeButton;
    EditText dateEditText, timeEditText, placeEditText;
    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;

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
        setContentView(R.layout.appointment);

        relativeLayout=(RelativeLayout)findViewById(R.id.relativeLayout);

        setting_bt = (ImageButton) findViewById(R.id.setting_bt);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        headerLayout = navigationView.getHeaderView(0);
        nav_userImage=(ImageView) headerLayout.findViewById(R.id.UserImage);
        nav_userName=(TextView)headerLayout.findViewById(R.id.userName);
        nav_userEmail=(TextView)headerLayout.findViewById(R.id.userEmail);
        appointmentListView = (ListView)findViewById(R.id.appointmentlistview);

        userimage = currentUser.getPhotoUrl().toString();
        username = currentUser.getDisplayName().toString();
        useremail = currentUser.getEmail();

        Uri photoUri = Uri.parse(userimage);
        Glide.with(AppointmentActivity.this).load(photoUri).into(nav_userImage);
        nav_userName.setText(username);
        if(useremail==null)
            nav_userEmail.setText(" ");
        else
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
                    appointmentListView.bringToFront();
                    relativeLayout.bringToFront();
                    buttoncliked=false;
                }


            }
        });
        navigationView.setNavigationItemSelectedListener(this);


        TextView teamNameTextView = (TextView) findViewById(R.id.AppointmentTeamName);
        teamNameTextView.setText(currentTeamName);

        brainstorming_button = (Button) findViewById(R.id.brainstorming_button);
        appointment_button = (Button) findViewById(R.id.appointment_button);
        teamchat_button = (Button) findViewById(R.id.teamchat_button);
        teammember_button = (Button) findViewById(R.id.teammember_button);
        plus_btn = (Button) findViewById(R.id.plus_button);

        getLatLng();

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

        plus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AppointmentActivity.this);

                View customLayout=View.inflate(AppointmentActivity.this,R.layout.appointment_dialog,null);
                dialog.setView(customLayout);

                placeEditText = (EditText)customLayout.findViewById(R.id.placeEditText);
                dateEditText = (EditText)customLayout.findViewById(R.id.dateEditText);
                timeEditText = (EditText)customLayout.findViewById(R.id.timeEditText);

                placeButton = (ImageButton)customLayout.findViewById(R.id.placeButton);
                dateButton = (ImageButton)customLayout.findViewById(R.id.dateButton);
                timeButton = (ImageButton)customLayout.findViewById(R.id.timeButton);

                dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yearofYear, int monthOfYear, int dayOfMonth) {

                        // TODO Auto-generated method stub
                        String msg = String.format("%d / %d / %d", yearofYear,monthOfYear+1, dayOfMonth);
                        Toast.makeText(AppointmentActivity.this, msg, Toast.LENGTH_SHORT).show();

                        year = yearofYear;
                        month = monthOfYear + 1;
                        day = dayOfMonth;

                        if(year != 100) {
                            dateEditText.setText(year + "/" + month + "/" + day);
                        }
                    }
                };
                timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {

                        // TODO Auto-generated method stub
                        String msg = String.format("%d / %d", hourOfDay, minuteOfDay);
                        Toast.makeText(AppointmentActivity.this, msg, Toast.LENGTH_SHORT).show();

                        hour = hourOfDay;
                        minute = minuteOfDay;

                        if(hour != 100) {
                            timeEditText.setText(hour + ":" + minute);
                        }
                    }
                };

                placeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        //37.282944 127.046250
                        Log.d("teamlatlng2", teamlat + ", " + teamlng);

                        LatLng boundSW = new LatLng(teamlat - 0.0025D,teamlng - 0.0025D);
                        LatLng boundNE = new LatLng(teamlat + 0.0025D,teamlng + 0.0025D);
                        LatLngBounds latLngBounds = new LatLngBounds(boundSW, boundNE);
                        builder.setLatLngBounds(latLngBounds);
                        try {
                            startActivityForResult(builder.build(AppointmentActivity.this), PLACE_PICKER_REQUEST);


                        } catch (GooglePlayServicesRepairableException e) {
                            e.printStackTrace();
                        } catch (GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }

                    }
                });
                dateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GregorianCalendar calendar = new GregorianCalendar();
                        new DatePickerDialog(AppointmentActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });
                timeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GregorianCalendar calendar = new GregorianCalendar();
                        new TimePickerDialog(AppointmentActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
                    }
                });


                        dialog.setPositiveButton("Appoint",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                datetime = year + "/" + month + "/" + day + " " + hour + ":" + minute;

                                if(name.equals("")) {
                                    Toast.makeText(AppointmentActivity.this, "You should input place information.", Toast.LENGTH_LONG).show();
                                } else if(year == 100 || hour == 100) {
                                    Toast.makeText(AppointmentActivity.this, "You should input date or time information.", Toast.LENGTH_LONG).show();
                                }else {
                                    writeNewBrainstorm(name, datetime, address);
                                }

                            }});

                // Setting Negative "NO" Button
                dialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog
                                dialog.cancel();
                            }
                        });

                dialog.create();
                dialog.show();

            }
        });

        appointmentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                namelist.clear();
                addresslist.clear();
                datetimelist.clear();
                for (DataSnapshot contact : contactChildren) {
                    namelist.add(contact.child("name").getValue().toString());
                    addresslist.add(contact.child("address").getValue().toString());
                    datetimelist.add(contact.child("datetime").getValue().toString());
                }
                makeCustomList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
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
        appointmentListView.bringToFront();
        relativeLayout.bringToFront();
        buttoncliked=false;
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                name = place.getName().toString().trim();
                address = place.getAddress().toString().trim();

                String msg = name + " : " + address;
                Toast.makeText(AppointmentActivity.this, msg, Toast.LENGTH_SHORT).show();

                placeEditText.setText(name);

            }
        }
    }

    public void makeCustomList() {
        AppointmentActivity.CustomList adapter = new AppointmentActivity.CustomList(this);
        appointmentListView = (ListView)findViewById(R.id.appointmentlistview);
        appointmentListView.setAdapter(adapter);

        appointmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AppointmentActivity.this);

                index = position;

                View customLayout=View.inflate(AppointmentActivity.this,R.layout.appointment_dialog,null);
                dialog.setView(customLayout);

                placeEditText = (EditText)customLayout.findViewById(R.id.placeEditText);
                dateEditText = (EditText)customLayout.findViewById(R.id.dateEditText);
                timeEditText = (EditText)customLayout.findViewById(R.id.timeEditText);

                String date = datetimelist.get(+position).split(" ")[0];
                String time = datetimelist.get(+position).split(" ")[1];

                placeEditText.setText(namelist.get(+position));
                dateEditText.setText(date);
                timeEditText.setText(time);

                placeButton = (ImageButton)customLayout.findViewById(R.id.placeButton);
                dateButton = (ImageButton)customLayout.findViewById(R.id.dateButton);
                timeButton = (ImageButton)customLayout.findViewById(R.id.timeButton);

                dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yearofYear, int monthOfYear, int dayOfMonth) {

                        // TODO Auto-generated method stub
                        String msg = String.format("%d / %d / %d", yearofYear,monthOfYear+1, dayOfMonth);
                        Toast.makeText(AppointmentActivity.this, msg, Toast.LENGTH_SHORT).show();

                        year = yearofYear;
                        month = monthOfYear + 1;
                        day = dayOfMonth;

                        if(year != 100) {
                            dateEditText.setText(year + "/" + month + "/" + day);
                        }
                    }
                };
                timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {

                        // TODO Auto-generated method stub
                        String msg = String.format("%d / %d", hourOfDay, minuteOfDay);
                        Toast.makeText(AppointmentActivity.this, msg, Toast.LENGTH_SHORT).show();

                        hour = hourOfDay;
                        minute = minuteOfDay;

                        if(hour != 100) {
                            timeEditText.setText(hour + ":" + minute);
                        }
                    }
                };

                placeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        //37.282944 127.046250
                        Log.d("teamlatlng2", teamlat + ", " + teamlng);

                        LatLng boundSW = new LatLng(teamlat - 0.0025D,teamlng - 0.0025D);
                        LatLng boundNE = new LatLng(teamlat + 0.0025D,teamlng + 0.0025D);
                        LatLngBounds latLngBounds = new LatLngBounds(boundSW, boundNE);
                        builder.setLatLngBounds(latLngBounds);
                        try {
                            startActivityForResult(builder.build(AppointmentActivity.this), PLACE_PICKER_REQUEST);


                        } catch (GooglePlayServicesRepairableException e) {
                            e.printStackTrace();
                        } catch (GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }

                    }
                });
                dateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DatePickerDialog(AppointmentActivity.this, dateSetListener, year, month, day).show();
                    }
                });
                timeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new TimePickerDialog(AppointmentActivity.this, timeSetListener, hour, minute, false).show();
                    }
                });


                dialog.setPositiveButton("Modify",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {

                                year = Integer.parseInt(dateEditText.getText().toString().split("/")[0]);
                                month = Integer.parseInt(dateEditText.getText().toString().split("/")[1]);
                                day = Integer.parseInt(dateEditText.getText().toString().split("/")[2]);

                                hour = Integer.parseInt(timeEditText.getText().toString().split(":")[0]);
                                minute = Integer.parseInt(timeEditText.getText().toString().split(":")[1]);


                                datetime = year + "/" + month + "/" + day + " " + hour + ":" + minute;

                                if(name.equals("")) {
                                    Toast.makeText(AppointmentActivity.this, "You should input place information.", Toast.LENGTH_LONG).show();
                                } else if(year == 100 || hour == 100) {
                                    Toast.makeText(AppointmentActivity.this, "You should input date or time information.", Toast.LENGTH_LONG).show();
                                } else {
                                    //writeNewBrainstorm(name, datetime, address);
                                    appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                                            key.clear();
                                            for (DataSnapshot contact : contactChildren) {
                                                key.add(contact.getKey());
                                            }
                                            //makeCustomList();

                                            Appointment appointment = new Appointment(name, datetime, address);

                                            appointmentRef.child(key.get(index)).setValue(appointment);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                        }
                                    });

                                }

                            }});

                // Setting Negative "NO" Button
                dialog.setNegativeButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog

                                appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                                        key.clear();
                                        for (DataSnapshot contact : contactChildren) {
                                            key.add(contact.getKey());
                                        }
                                        Log.d("index :: ", "" + index);
                                        appointmentRef.child(key.get(index)).setValue(null);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                    }
                                });
                            }
                        });

                dialog.create();
                dialog.show();
            }
        });

        registerForContextMenu(appointmentListView);
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context) {
            super(context, R.layout.memberview, namelist);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.appointmentlistview, null, true);
            TextView name = (TextView) rowView.findViewById(R.id.appointmentName);
            TextView address = (TextView) rowView.findViewById(R.id.appointmentAddress);
            TextView datetime = (TextView) rowView.findViewById(R.id.appointmentDateTime);
            name.setText(namelist.get(+position));
            address.setText(addresslist.get(+position));
            datetime.setText(datetimelist.get(+position).toString());
            return rowView;
        }
    }

    private static class Appointment {
        Appointment() {};
        Appointment(String Aname, String Adatetime, String Aaddress) {
            name = Aname;
            datetime = Adatetime;
            address = Aaddress;
        };

        private String name, datetime,  address;


        public String getName() {
            return name;
        }
        public String getAddress() {
            return address;
        }
        public String getDatetime() {
            return datetime;
        }
    }

    private void writeNewBrainstorm(String name, String datetime, String address) {
        Appointment appointment = new Appointment(name, datetime, address);

        appointmentRef.push().setValue(appointment);
    }

    public void getLatLng() {
        lat_ary.clear();
        lng_ary.clear();
        memberRef.addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                for (DataSnapshot contact : contactChildren) {
                    memberUids.add(contact.child("memberUid").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> contactChildren = dataSnapshot.getChildren();

                for (DataSnapshot contact : contactChildren) {
                    for(int i = 0; i < memberUids.size(); i ++) {
                        if(contact.getKey().equals(memberUids.get(i))) {
                            lat_ary.add((Double)contact.child("latitude").getValue());
                            lng_ary.add((Double)contact.child("longitude").getValue());
                        }
                    }
                }

                if(!lat_ary.isEmpty() && !lng_ary.isEmpty()) {
                    for(int i = 0; i < lat_ary.size(); i++) {
                        teamlat += lat_ary.get(i);
                        teamlng += lng_ary.get(i);
                    }
                    teamlat = teamlat / (float)lat_ary.size();
                    teamlng = teamlng / (float)lng_ary.size();

                    Log.d("teamlatlng1", teamlat + ", " + teamlng);
                }
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

    }
}
