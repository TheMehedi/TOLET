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

public class MainActivity extends AppCompatActivity {

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
    private DatabaseReference UserRef, PostsRef;

    private String currentUserID;
    private String record;
    Uri download_uri = null;
    private int backButtonCount;
    private ProgressBar homeProgressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner mySpinner = findViewById(R.id.spinner1);
        final ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
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
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child("All");
        homeProgressbar = findViewById(R.id.home_progressbar);

        SearchBtn = findViewById(R.id.search_btn);
        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(record);
                DisplayAllUsersPost(PostsRef);

            }
        });


        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = findViewById(R.id.add_new_post_button);
        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        int myColor = getResources().getColor(R.color.colorPrimary);
        navigationView.setBackgroundColor(myColor);
        NavigationMenuView navMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        navMenuView.addItemDecoration(new DividerItemDecoration(MainActivity.this,DividerItemDecoration.VERTICAL));


        postList = findViewById(R.id.all_users_post_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        int headerColor = getResources().getColor(R.color.headerColor);
        View navView = (navigationView).inflateHeaderView(R.layout.navigation_header);
        navView.setBackgroundColor(headerColor);
        NavProfileImage = navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = navView.findViewById(R.id.nav_user_full_name);


        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("fullname")) && (dataSnapshot.hasChild("profileimages"))){

                    String navprofileimage = dataSnapshot.child("profileimages").getValue().toString();
                    String navprofileusername = dataSnapshot.child("fullname").getValue().toString();


                    download_uri = Uri.parse(navprofileimage);

                    NavProfileUserName.setText(navprofileusername);

                    Picasso.with(MainActivity.this)
                            .load(download_uri)
                            .resize(200, 200)
                            .placeholder(R.drawable.profile)
                            .into(NavProfileImage);


                }

                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("fullname"))){

                    String navprofileusername = dataSnapshot.child("fullname").getValue().toString();

                    NavProfileUserName.setText(navprofileusername);

                }

                else{

                    Toast.makeText(MainActivity.this, "Please update your profile!", Toast.LENGTH_SHORT).show();
                    SendUserToSetupActivity();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToPostActivity();

            }
        });



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                UserMenuSelector(item);
                return false;
            }
        });




        DisplayAllUsersPost(PostsRef);


    }

    private void DisplayAllUsersPost(final DatabaseReference postsRef) {

        homeProgressbar.setVisibility(View.VISIBLE);

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postsRef,Posts.class)
                .build();





        final FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull Posts model) {

                Drawable postBackground = getResources().getDrawable(R.drawable.post_background);
                holder.itemView.setBackground(postBackground);

                final String PostKey = getRef(position).getKey();
                final String CategoryKey = record;

                holder.username.setText(model.getFullname());
                Picasso.with(MainActivity.this).load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.image);
                holder.PostTime.setText(model.getTime());
                holder.PostDate.setText(model.getDate());
                holder.PostDescription.setText(model.getDescription());
                Picasso.with(MainActivity.this).load(model.getPostimage()).into(holder.PostImage);

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

                        Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("PostKey", PostKey);
                        profileIntent.putExtra("CategoryKey", CategoryKey);
                        startActivity(profileIntent);

                    }
                });


            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout, viewGroup,false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;

            }
        };


        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

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


    @Override
    protected void onStart() {

        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){


            CheckUserExistence();

        }

        else{

            SendUserToLoginActivity();
        }

    }


    private void SendUserToPostActivity() {

        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postIntent);

    }



    private void CheckUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.child(current_user_id).exists()){

                    SendUserToSetupActivity();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetupActivity() {

        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        startActivity(setupIntent);
    }



    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void SendUserToMainActivity() {

        recreate();
        this.drawerLayout.closeDrawer(GravityCompat.START);
    }


    /**
     * Back button listener.
     * Will close the application if the back button pressed twice.
     * if I set this activity , welcome screen doesn't load while app re open
     */

    @Override
    public void onBackPressed()
    {

        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {

            this.drawerLayout.closeDrawer(GravityCompat.START);

        }
        else {

            if(backButtonCount >= 1)
            {
                backButtonCount = 0;
                finish();
            }
            else
            {
                Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show();
                backButtonCount++;
            }

        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId()){


            case R.id.nav_home:
                SendUserToMainActivity();
                break;

            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                Intent profileIntent = new Intent(MainActivity.this,UserProfileActivity.class);
                startActivity(profileIntent);
                break;

            case R.id.nav_my_post:
                Toast.makeText(this, "My Posts", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_saved_post:
                Toast.makeText(MainActivity.this, "Saved Post", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_Logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }

    }
}
