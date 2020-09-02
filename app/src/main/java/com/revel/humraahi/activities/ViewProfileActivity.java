package com.revel.humraahi.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.revel.humraahi.R;

public class ViewProfileActivity extends AppCompatActivity {
    ImageView mainImage;
    GridView imageGrid;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String accountId = bundle.getString("accountId", null);
            if (accountId != null) {
                mainImage = findViewById(R.id.mainImage);
                imageGrid = findViewById(R.id.imageGrid);
                pb = findViewById(R.id.pb);
                StorageReference mRef = FirebaseStorage.getInstance().getReference(accountId + "/");
                mRef.child("dp").getBytes(1024 * 1024)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Toast.makeText(ViewProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                setImage(bitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ViewProfileActivity.this, "No such account", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "No such account", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setImage(Bitmap bitmap) {
        Glide.with(ViewProfileActivity.this).load(bitmap).fitCenter().into(mainImage);
        pb.setVisibility(View.GONE);
    }
}