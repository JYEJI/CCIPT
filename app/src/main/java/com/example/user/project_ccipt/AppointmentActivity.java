package com.example.user.project_ccipt;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by user on 2017-04-26.
 */

public class AppointmentActivity extends FragmentActivity{

    private static final String currentTeamName = TeamListActivity.currentTeamName;

    Button brainstorming_button,appointment_button,teamchat_button,teammember_button, plus_btn;

    int PLACE_PICKER_REQUEST = 1;
    final String TAG = "AppointmentActivity";

    ListView appointmentListView;

    private ArrayList<String> namelist = new ArrayList<>();
    private ArrayList<String> addresslist = new ArrayList<>();
    private ArrayList<String> datetimelist = new ArrayList<>();

    private ArrayList<Float> lng_ary = new ArrayList<>();
    private ArrayList<Float> lat_ary = new ArrayList<>();


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference appointmentRef = database.getReference("Teams").child(currentTeamName).child("Appointments");

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment);

        TextView teamNameTextView = (TextView) findViewById(R.id.AppointmentTeamName);
        teamNameTextView.setText(currentTeamName);

        brainstorming_button = (Button) findViewById(R.id.brainstorming_button);
        appointment_button = (Button) findViewById(R.id.appointment_button);
        teamchat_button = (Button) findViewById(R.id.teamchat_button);
        teammember_button = (Button) findViewById(R.id.teammember_button);
        plus_btn = (Button) findViewById(R.id.plus_button);

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
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                //37.282944 127.046250
                LatLng boundSW = new LatLng(37.280944,127.044250);
                LatLng boundNE = new LatLng(37.284944,127.048250);
                LatLngBounds latLngBounds = new LatLngBounds(boundSW, boundNE);
                builder.setLatLngBounds(latLngBounds);
                try {
                    startActivityForResult(builder.build(AppointmentActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }


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
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                writeNewBrainstorm(place.getName().toString(), "2018-06-03 10:00:00", place.getAddress().toString());
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
                Toast.makeText(getBaseContext(), namelist.get(+position), Toast.LENGTH_LONG).show();
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
}
