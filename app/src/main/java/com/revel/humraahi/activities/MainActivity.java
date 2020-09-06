package com.revel.humraahi.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.revel.humraahi.R;
import com.revel.humraahi.services.MyService;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    DatabaseReference mRef;
    ImageView dp;
    StorageReference mStorageRef;
    LocationManager locationManager;
    private boolean gps_enabled = false;
    TextView username;
    String gender = null, preference = null, un = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    SharedPreferences sp = getSharedPreferences("ImageUri", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("ImageUri", uri.toString());
                    editor.apply();
                    uploadImage(uri);
                }
            }
        } else if (requestCode == 2) {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gps_enabled) {
                startService(new Intent(MainActivity.this, MyService.class));
                startActivity(new Intent(MainActivity.this, DrawerActivityJava.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }
        }
    }

    private void uploadImage(Uri uri) {
        StorageReference imageRef = mStorageRef.child("dp");

        imageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "uploading successful", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(MainActivity.this, "uploading failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

            } else {
                finishAffinity();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {

        SharedPreferences userGender = getSharedPreferences("gender", MODE_PRIVATE);
        gender = userGender.getString("gender", null);

        SharedPreferences userPref = getSharedPreferences("preference", MODE_PRIVATE);
        preference = userPref.getString("preference", null);

        SharedPreferences usernamePref = getSharedPreferences("usernamePref", MODE_PRIVATE);
        un = usernamePref.getString("usernamePref", null);

        SharedPreferences sp = getSharedPreferences("ImageUri", MODE_PRIVATE);
        String imageString = sp.getString("ImageUri", null);

        SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(this);
        String previouslyEncodedImage = shre.getString("image_data", "");
        if (un == null) {
            this.username.setText("New user");
            FirebaseDatabase.getInstance().getReference("Username")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_")).setValue("New user");
        } else {
            this.username.setText(un);
        }
        if (imageString == null) {
            if (!previouslyEncodedImage.equalsIgnoreCase("")) {
                byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                Glide.with(MainActivity.this).load(bitmap).circleCrop().into(dp);
            } else {
                Glide.with(MainActivity.this).load(R.drawable.dp).circleCrop().into(dp);
            }
        } else {
            Glide.with(MainActivity.this).load(imageString).circleCrop().into(dp);
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button startFinding = findViewById(R.id.startFinding);
        username = findViewById(R.id.username);
        Button stopFinding = findViewById(R.id.stopFinding);
        dp = findViewById(R.id.dp);
        mRef = FirebaseDatabase.getInstance().getReference("User");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mStorageRef = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()).replace(".", "_"));
        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
        startFinding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
       
                    gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (!gps_enabled) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 2);
                    } else {
                        startService(new Intent(MainActivity.this, MyService.class));
                        startActivity(new Intent(MainActivity.this, DrawerActivityJava.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    }
            }
        });

        stopFinding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Looks like its time for a break", Toast.LENGTH_SHORT).show();
                stopService(new Intent(MainActivity.this, MyService.class));
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, 2);
            }
        }
    }


}
