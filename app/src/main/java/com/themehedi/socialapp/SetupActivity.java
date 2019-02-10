package com.themehedi.socialapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, DistrictName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;

    private String currentUserID;
    private boolean isChanged = false;

    private Bitmap compressedImageFile;

    Uri download_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Profile");
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        mToolbar = findViewById(R.id.setup_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Account");
        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_full_name);
        DistrictName = findViewById(R.id.setup_district_name);
        SaveInformationButton = findViewById(R.id.setup_information_button);
        ProfileImage = findViewById(R.id.setup_profile_image);




        UserRef.addValueEventListener(new ValueEventListener() {
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

                            Picasso.with(SetupActivity.this)
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


                        SaveInformationButton.setEnabled(true);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {



                    }
                });



        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SaveAccountSetupInformation();

            }
        });


        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }

                } else {

                    BringImagePicker();

                }

            }

        });

    }



    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);

    }


    private void SaveAccountSetupInformation() {

        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String district = DistrictName.getText().toString();

        if(TextUtils.isEmpty(username)){

            Toast.makeText(this, "Please fill Username field!", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(fullname)){

            Toast.makeText(this, "Please fill Full Name field!", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(district)){

            Toast.makeText(this, "Please fill Country field!", Toast.LENGTH_SHORT).show();
        }

        else{

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("uid", currentUserID);
            userMap.put("fullname", fullname);
            userMap.put("district", district);
            userMap.put("status", "hey there!");
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationshipstatus", "none");

            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){

                        Toast.makeText(SetupActivity.this, "Profile Updated!", Toast.LENGTH_LONG).show();
                    }

                    else{


                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }

                }
            });


        }

        SendUserToMainActivity();


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){

            Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
            startActivity(mainIntent);


        }
        return super.onOptionsItemSelected(item);
    }



    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                download_uri = result.getUri();
                ProfileImage.setImageURI(download_uri);


                currentUserID = mAuth.getCurrentUser().getUid();

                File newImageFile = new File(download_uri.getPath());
                try {

                    compressedImageFile = new Compressor(SetupActivity.this)
                            .setMaxHeight(125)
                            .setMaxWidth(125)
                            .setQuality(50)
                            .compressToBitmap(newImageFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumbData = baos.toByteArray();

                StorageReference image_path = UserProfileImageRef.child(currentUserID + ".jpg");

                image_path.putBytes(thumbData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        Uri uri = urlTask.getResult();

                        Uri downloadUrl;

                        if(uri != null) {

                            downloadUrl = uri;

                        } else {

                            downloadUrl = download_uri;

                        }


                        UserRef.child("profileimages").setValue(downloadUrl.toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){



                                        }

                                        else{

                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                                        }

                                    }
                                });


                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Toast.makeText(SetupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                SendUserToMainActivity();

            }
        }

    }


}
