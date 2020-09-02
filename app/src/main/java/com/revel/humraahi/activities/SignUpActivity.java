package com.revel.humraahi.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.revel.humraahi.R;
import com.revel.humraahi.classes.GetStates;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ImageView logo;
    LinearLayout up, down;
    EditText email, password;
    Button login;
    AutoCompleteTextView states;
    TextView loginTxt;
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("User Uid");
    String usersState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        }

        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        logo = findViewById(R.id.logo);

        mAuth = FirebaseAuth.getInstance();

        Glide.with(this).load(R.drawable.logo).circleCrop().into(logo);
        up.animate().setDuration(1000).translationY(0);
        down.animate().setDuration(500).translationY(0);

        states = findViewById(R.id.states);
        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);
        login = findViewById(R.id.btnLogin);
        loginTxt = findViewById(R.id.loginTxt);

        states.setAdapter(GetStates.getAdapter(this));


        loginTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersState = states.getText().toString();
                if (email.getText().toString().trim().isEmpty()) {
                    email.setError("Please fill this field");
                    password.requestFocus();
                } else if (password.getText().toString().trim().isEmpty()) {
                    password.setError("Please fill this field");
                    password.requestFocus();
                } else if (usersState.trim().isEmpty() || !GetStates.getList().contains(usersState)) {
                    states.setError("Please choose one of given");
                    states.requestFocus();
                } else {
                    final ProgressDialog dialog = new ProgressDialog(SignUpActivity.this);
                    dialog.setTitle("Authenticating...");
                    dialog.setCancelable(false);
                    dialog.create();
                    dialog.show();
                    mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        SharedPreferences sp = getSharedPreferences("usersState", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("usersState", usersState);
                                        editor.apply();
                                        // Sign in success, update UI with the signed-in user's information
                                        mRef.child(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_")).setValue(usersState);
                                        dialog.cancel();
                                        startActivity(new Intent(SignUpActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                        finish();
                                    } else {
                                        dialog.cancel();
                                        Toast.makeText(SignUpActivity.this, "" + task.getException(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}