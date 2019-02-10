package com.themehedi.socialapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private FloatingActionButton AddNewPostButton;
    private Button SearchBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, UserPostRef, SavedPostRef;

    private String currentUserID;
    private String record;
    private TextView Message;
    Uri download_uri = null;
    private int backButtonCount;
    private ProgressBar homeProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);



        Spinner mySpinner = findViewById(R.id.spinner1);
        final ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MyPostActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category_names));

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                record = myAdapter.getItem(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                record = "All";

            }
        });




        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserPostRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("UserPost").child("All");
        SavedPostRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("SavedPost");
        homeProgressbar = findViewById(R.id.home_progressbar);
        Message = findViewById(R.id.message);

        SearchBtn = findViewById(R.id.search_btn);
        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserPostRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("UserPost").child(record);
                DisplayAllUsersPost(UserPostRef);

            }
        });


        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Posts");

        AddNewPostButton = findViewById(R.id.add_new_post_button);

        postList = findViewById(R.id.all_users_post_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);







        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToPostActivity();

            }
        });






        DisplayAllUsersPost(UserPostRef);

    }


    private void DisplayAllUsersPost(final DatabaseReference postsRef) {


        UserPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    homeProgressbar.setVisibility(View.VISIBLE);

                    FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                            .setQuery(postsRef,Posts.class)
                            .build();





                    final FirebaseRecyclerAdapter<Posts, MyPostActivity.PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostActivity.PostsViewHolder>(options) {
                        @Override
                        public void onBindViewHolder(@NonNull final MyPostActivity.PostsViewHolder holder, int position, @NonNull Posts model) {

                            Drawable postBackground = getResources().getDrawable(R.drawable.post_background);
                            holder.itemView.setBackground(postBackground);

                            final String PostKey = getRef(position).getKey();
                            final String CategoryKey = record;

                            holder.username.setText(model.getFullname());
                            Picasso.with(MyPostActivity.this).load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.image);
                            holder.PostTime.setText(model.getTime());
                            holder.PostDate.setText(model.getDate());
                            holder.PostDescription.setText(model.getDescription());
                            Picasso.with(MyPostActivity.this).load(model.getPostimage()).into(holder.PostImage);

                            holder.SavePost.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {


                                    int saveColor = getResources().getColor(R.color.colorPrimary);

                                    holder.SavePost.setText("Saved");
                                    holder.SavePost.setTextColor(saveColor);

                                }
                            });

                            homeProgressbar.setVisibility(View.INVISIBLE);

                            holder.image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent profileIntent = new Intent(MyPostActivity.this,ProfileActivity.class);
                                    profileIntent.putExtra("PostKey", PostKey);
                                    profileIntent.putExtra("CategoryKey", CategoryKey);
                                    startActivity(profileIntent);

                                }
                            });


                        }

                        @NonNull
                        @Override
                        public MyPostActivity.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout, viewGroup,false);
                            MyPostActivity.PostsViewHolder viewHolder = new MyPostActivity.PostsViewHolder(view);
                            return viewHolder;

                        }
                    };


                    postList.setAdapter(firebaseRecyclerAdapter);
                    firebaseRecyclerAdapter.startListening();

                }


                else {

                    Message.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder{


        TextView username, PostTime, PostDate, PostDescription,SavePost;
        CircleImageView image;
        ImageView PostImage;
        View mView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            username = mView.findViewById(R.id.post_user_name);



            image = mView.findViewById(R.id.post_profile_image);



            PostTime = mView.findViewById(R.id.post_time);



            PostDate = mView.findViewById(R.id.post_date);



            PostDescription = mView.findViewById(R.id.post_description);



            PostImage = mView.findViewById(R.id.post_image);


            SavePost = mView.findViewById(R.id.save_post);

        }




    }


    private void SendUserToPostActivity() {

        Intent postIntent = new Intent(MyPostActivity.this, PostActivity.class);
        startActivity(postIntent);

    }




}
