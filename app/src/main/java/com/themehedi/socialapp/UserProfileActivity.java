package com.themehedi.socialapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private TextView UserName, FullName, DistrictName;
    private Button EditInformationButton;
    private CircleImageView ProfileImage;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, PostRef;

    private String currentUserID, PostKey;


    Uri download_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");


        mToolbar = findViewById(R.id.setup_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account");

        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_full_name);
        DistrictName = findViewById(R.id.setup_district_name);
        EditInformationButton = findViewById(R.id.edit_information_button);
        ProfileImage = findViewById(R.id.setup_profile_image);


        EditInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent setupIntent = new Intent(UserProfileActivity.this,SetupActivity.class);
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(setupIntent);


            }
        });


    }


    @Override
    protected void onStart() {

        super.onStart();

        if(currentUserID != null){

            ProfileInfo();

        }


    }




    private void ProfileInfo() {

        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("fullname")) && (dataSnapshot.hasChild("username")) && (dataSnapshot.hasChild("district"))
                        && (dataSnapshot.hasChild("profileimages"))){

                    String username = dataSnapshot.child("username").getValue().toString();
                    String profileimages = dataSnapshot.child("profileimages").getValue().toString();
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    String country = dataSnapshot.child("district").getValue().toString();


                    download_uri = Uri.parse(profileimages);

                    UserName.setText(username);
                    FullName.setText(fullname);
                    DistrictName.setText(country);

                    Picasso.with(UserProfileActivity.this)
                            .load(download_uri)
                            .resize(200, 200)
                            .placeholder(R.drawable.default_image)
                            .into(ProfileImage);


                }

                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("fullname")) && (dataSnapshot.hasChild("username")) && (dataSnapshot.hasChild("district"))){

                    String username = dataSnapshot.child("username").getValue().toString();
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    String country = dataSnapshot.child("district").getValue().toString();

                    UserName.setText(username);
                    FullName.setText(fullname);
                    DistrictName.setText(country);

                }


                EditInformationButton.setEnabled(true);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){

            Intent mainIntent = new Intent(UserProfileActivity.this,MainActivity.class);
            startActivity(mainIntent);


        }
        return super.onOptionsItemSelected(item);
    }


}
