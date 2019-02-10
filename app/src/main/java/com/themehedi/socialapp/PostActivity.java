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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private TextView progressMessage;
    private ProgressBar progressBar;

    private Uri ImageUri;
    private String Description;
    private String currentUserID;
    private String saveCurrentDate, saveCurrentTime, postRandomName, record;


    private StorageReference PostsImagesRef;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, PostsRef, UserPostRef;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Spinner mySpinner = findViewById(R.id.spinner1);
        final ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(PostActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                record = myAdapter.getItem(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                record = "Others";

            }
        });





        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PostsImagesRef = FirebaseStorage.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Profile");
        UserPostRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("UserPost");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        progressMessage = findViewById(R.id.progress_message);
        progressBar = findViewById(R.id.progressBar);
        SelectPostImage = findViewById(R.id.select_post_image);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.post_description);


        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Post");


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OpenGallery();

            }
        });


        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatePostInfo();

            }
        });


    }




    private void ValidatePostInfo() {

        Description = PostDescription.getText().toString();

        if(ImageUri == null){

            Toast.makeText(this, "Please select an image..", Toast.LENGTH_SHORT).show();

        }

        else if(TextUtils.isEmpty(Description)){

            Toast.makeText(this, "Say something about your post!", Toast.LENGTH_SHORT).show();
        }

        else{
            progressMessage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            currentUserID = mAuth.getCurrentUser().getUid();

            File newImageFile = new File(ImageUri.getPath());
            try {

                compressedImageFile = new Compressor(PostActivity.this)
                        .setMaxHeight(400)
                        .setMaxWidth(400)
                        .setQuality(50)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumbData = baos.toByteArray();


            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(callForDate.getTime());


            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(callForTime.getTime());

            postRandomName = saveCurrentDate + saveCurrentTime;


            final StorageReference image_path = PostsImagesRef.child("Post Images").child( postRandomName + currentUserID + ".jpg");

            image_path.putBytes(thumbData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){

                        image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();

                                PostsRef.child("All").child(postRandomName + currentUserID).child("postimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                        }else{
                                            Toast.makeText(PostActivity.this,"Problem occurred while saving post image!",Toast.LENGTH_SHORT) .show();
                                        }
                                    }
                                });

                                PostsRef.child(record).child(postRandomName + currentUserID).child("postimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                        }else{
                                            Toast.makeText(PostActivity.this,"Problem occurred while saving post image!",Toast.LENGTH_SHORT) .show();
                                        }
                                    }
                                });


                                UserPostRef.child("All").child(postRandomName + currentUserID).child("postimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                        }else{
                                            Toast.makeText(PostActivity.this,"Problem occurred while saving post image!",Toast.LENGTH_SHORT) .show();
                                        }
                                    }
                                });


                                UserPostRef.child(record).child(postRandomName + currentUserID).child("postimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                        }else{
                                            Toast.makeText(PostActivity.this,"Problem occurred while saving post image!",Toast.LENGTH_SHORT) .show();
                                        }
                                    }
                                });

                            }
                        });





                    }

                    else{

                        Toast.makeText(PostActivity.this,"Your picture did NOT saved",Toast.LENGTH_SHORT) .show();

                    }
                }
            });


            //uploading into database

            UserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if((dataSnapshot.exists()) && (dataSnapshot.hasChild("fullname")) && (dataSnapshot.hasChild("profileimages"))){

                        String profileimages = dataSnapshot.child("profileimages").getValue().toString();
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        HashMap postsMap = new HashMap();
                        postsMap.put("uid",currentUserID);
                        postsMap.put("date",saveCurrentDate);
                        postsMap.put("time",saveCurrentTime);
                        postsMap.put("description",Description);
                        postsMap.put("profileimage",profileimages);
                        postsMap.put("fullname",fullname);


                        PostsRef.child("All").child(postRandomName + currentUserID).setValue(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Post Updated!", Toast.LENGTH_SHORT).show();

                                }

                                else{

                                    String message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });


                        PostsRef.child(record).child(postRandomName + currentUserID).setValue(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Post Updated!", Toast.LENGTH_SHORT).show();

                                }

                                else{

                                    String message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });


                        UserPostRef.child("All").child(postRandomName + currentUserID).setValue(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Post Updated!", Toast.LENGTH_SHORT).show();

                                }

                                else{

                                    String message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });


                        UserPostRef.child(record).child(postRandomName + currentUserID).setValue(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Post Updated!", Toast.LENGTH_SHORT).show();

                                }

                                else{

                                    String message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });



                    }

                    else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("fullname"))){

                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        HashMap postsMap = new HashMap();
                        postsMap.put("uid",currentUserID);
                        postsMap.put("date",saveCurrentDate);
                        postsMap.put("time",saveCurrentTime);
                        postsMap.put("description",Description);
                        postsMap.put("profileimage","none");
                        postsMap.put("fullname",fullname);


                        PostsRef.child("All").child(postRandomName + currentUserID).setValue(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Post Updated!", Toast.LENGTH_SHORT).show();

                                }

                                else{

                                    String message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });


                        PostsRef.child(record).child(postRandomName + currentUserID).setValue(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Post Updated!", Toast.LENGTH_SHORT).show();

                                }

                                else{

                                    String message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });



                        UserPostRef.child("All").child(postRandomName + currentUserID).setValue(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Post Updated!", Toast.LENGTH_SHORT).show();

                                }

                                else{

                                    String message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });



                        UserPostRef.child(record).child(postRandomName + currentUserID).setValue(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Post Updated!", Toast.LENGTH_SHORT).show();

                                }

                                else{

                                    String message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });


                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {



                }
            });


            SendUserToMainActivity();

        }

    }

    private void OpenGallery() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(PostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            } else {

                BringImagePicker();

            }

        } else {

            BringImagePicker();

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){

            SendUserToMainActivity();

        }
        return super.onOptionsItemSelected(item);
    }


    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(PostActivity.this);

    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(PostActivity.this,MainActivity.class);
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

                ImageUri = result.getUri();
                SelectPostImage.setImageURI(ImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Toast.makeText(PostActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                SendUserToMainActivity();

            }
        }

    }

}
