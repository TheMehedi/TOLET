package com.themehedi.socialapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {



    private static int SPLASH_TIME_OUT = 1000;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {

        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    SendUserToMainActivity();

                }
            }, SPLASH_TIME_OUT);

        }

        else{

            SendUserToLoginActivity();


        }

    }




    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(WelcomeActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }


    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(WelcomeActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
