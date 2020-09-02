package com.revel.humraahi.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.revel.humraahi.R;
import com.revel.humraahi.classes.GetStates;

import java.io.ByteArrayOutputStream;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ImageView logo;
    LinearLayout up, down;
    EditText email, password;
    Button login;
    AutoCompleteTextView states;
    TextView signUpTxt;
    DatabaseReference mRef;
    String usersState;

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        }

        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        logo = findViewById(R.id.logo);
        mRef = FirebaseDatabase.getInstance().getReference("User Uid");

        states = findViewById(R.id.states);
        states.setAdapter(GetStates.getAdapter(this));

        mAuth = FirebaseAuth.getInstance();

        Glide.with(this).load(R.drawable.logo).circleCrop().into(logo);
        up.animate().setDuration(1000).translationY(0);
        down.animate().setDuration(500).translationY(0);

        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);
        login = findViewById(R.id.btnLogin);
        signUpTxt = findViewById(R.id.signUpTxt);

        signUpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersState = states.getText().toString();
                if (!email.getText().toString().trim().isEmpty() && !password.getText().toString().trim().isEmpty() && !usersState.isEmpty() && GetStates.getList().contains(usersState)) {
                    final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
                    dialog.setTitle("Authenticating...");
                    dialog.setCancelable(false);
                    dialog.create();
                    dialog.show();
                    mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        SharedPreferences sp = getSharedPreferences("usersState", MODE_PRIVATE);
                                        final SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("usersState", usersState);
                                        editor.apply();
                                        mRef.child(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_")).setValue(usersState);
                                        dialog.cancel();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                        StorageReference ref = FirebaseStorage.getInstance()
                                                .getReference("" + FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_") + "/")
                                                .child("dp");
                                        ref.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                byte[] b = baos.toByteArray();

                                                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                                                SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                                SharedPreferences.Editor edit = shre.edit();
                                                edit.putString("image_data", encodedImage);
                                                edit.apply();
                                            }
                                        });
                                        FirebaseDatabase.getInstance().getReference("Username")
                                                .child("" + FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_"))
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            String username = (String) snapshot.getValue();
                                                            SharedPreferences sp = getSharedPreferences("username", MODE_PRIVATE);
                                                            SharedPreferences.Editor editor1 = sp.edit();
                                                            editor1.putString("username", username);
                                                            editor1.apply();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "" + task.getException(), Toast.LENGTH_LONG).show();
                                        dialog.cancel();
                                        // If sign in fails, display a message to the user.
                                    }

                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}