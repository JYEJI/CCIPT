package com.example.user.project_ccipt;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
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

public class BrainstormingActivity extends Activity {

    private static final String currentTeamName = TeamListActivity.currentTeamName;
    Team currentTeam = new Team(currentTeamName);

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    //사진으로 전송시 되돌려 받을 번호
    static int REQUEST_PICTURE=1;
    //앨범으로 전송시 돌려받을 번호
    static int REQUEST_PHOTO_ALBUM=2;
    //첫번째 이미지 아이콘 샘플 이다.
    static String SAMPLEIMG="ic_launcher.png";

    private Uri imageUri;

    Button brainstorming_button, appointment_button, teamchat_button, teammember_button, plus_button;

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> descriptions = new ArrayList<>();
    ArrayList<Bitmap> images = new ArrayList<>();

    ImageView photo_imageview;
    Bitmap photo;

    boolean photo_in = false;

    private ListView                m_ListView;
    private ListViewAdapter   m_Adapter;
    private String absolutePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brainstorming);

        TextView teamNameTextView = (TextView) findViewById(R.id.BrainStromTeamName);
        teamNameTextView.setText(currentTeam.getTitle());

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
                        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                doTakePhotoAction();
                            }
                        };
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
                        new AlertDialog.Builder(BrainstormingActivity.this)
                                .setTitle("Select Image")
                                //.setPositiveButton("Take a Photo",cameraListener)
                                .setNeutralButton("Select Album",albumListener)
                                .setNegativeButton("Cancel",cancelListener)
                                .show();
                    }
                });

                builder.setPositiveButton("Input",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                titles.add(titleBox.getText().toString());
                                descriptions.add(descriptionBox.getText().toString());

                                m_Adapter = new ListViewAdapter();
                                m_ListView = (ListView) findViewById(R.id.listview);
                                m_ListView.setAdapter(m_Adapter);

                                for (int i =0;i<titles.size();i++){
                                    m_Adapter.addItem(images.get(i),titles.get(i),descriptions.get(i));
                                }

                                m_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                                        // get item
                                        ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

                                        String titleStr = item.getTitle() ;
                                        String descStr = item.getDesc() ;
                                        Bitmap icon = item.getIcon() ;

                                        AlertDialog.Builder builder = new AlertDialog.Builder(BrainstormingActivity.this);

                                        View customLayout=View.inflate(BrainstormingActivity.this,R.layout.view_brainstorming_item,null);

                                        TextView title = (TextView)customLayout.findViewById(R.id.input_title);
                                        title.setText(titleStr);

                                        TextView description = (TextView)customLayout.findViewById(R.id.input_description);
                                        title.setText(descStr);

                                        ImageView idea_img = (ImageView) customLayout.findViewById(R.id.photo_imageview);
                                        idea_img.setImageBitmap(icon);

                                        builder.setView(customLayout);



                                    }
                                }) ;

                            }
                        });

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

    }




    public void doTakePhotoAction(){
        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, REQUEST_PICTURE);*/
    }

    public void doTakeAlbumAction(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,REQUEST_PHOTO_ALBUM);

    }
    Bitmap loadPicture(){
        File file=new File(Environment.getExternalStorageDirectory(),SAMPLEIMG);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize = 16;
        return BitmapFactory.decodeFile(file.getAbsolutePath(),options);
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
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(),data.getData());
                    images.add(bm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

}