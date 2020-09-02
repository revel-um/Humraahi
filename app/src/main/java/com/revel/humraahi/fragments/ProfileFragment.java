package com.revel.humraahi.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.WriterException;
import com.revel.humraahi.R;
import com.revel.humraahi.activities.QrCodeScannerActivity;

import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    ImageView profileImage;
    TextView profileText;
    Button scanQR;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(getContext(), QrCodeScannerActivity.class));
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    SharedPreferences sp = getActivity().getSharedPreferences("ImageUri", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("ImageUri", uri.toString());
                    editor.apply();
                    uploadImage(uri);
                    Glide.with(getActivity()).load(uri).circleCrop().into(profileImage);
                }
            }
        }
    }

    private void uploadImage(Uri uri) {
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()).replace(".", "_"));
        StorageReference imageRef = mStorageRef.child("dp");

        imageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "uploading successful", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(getContext(), "uploading failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        QRGEncoder qrgEncoder = new QRGEncoder(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_")
                , null, QRGContents.Type.TEXT, 1000);
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = v.findViewById(R.id.profileImage);
        profileText = v.findViewById(R.id.profileText);
        scanQR = v.findViewById(R.id.scanqr);
        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    getActivity().startActivity(new Intent(getContext(), QrCodeScannerActivity.class));
                } else {
                    String[] permissions = new String[]{Manifest.permission.CAMERA};
                    requestPermissions(permissions, 0);
                }
            }
        });
        SharedPreferences sp = getActivity().getSharedPreferences("ImageUri", MODE_PRIVATE);
        String imageUri = sp.getString("ImageUri", null);
        SharedPreferences sp1 = getActivity().getSharedPreferences("username", MODE_PRIVATE);
        String un = sp1.getString("username", null);
        if (un != null) {
            profileText.setText(un);
        } else {
            profileText.setText("New user");
        }
        if (imageUri != null) {
            Glide.with(getActivity()).load(imageUri).circleCrop().into(profileImage);
        } else {
            Glide.with(getActivity()).load(R.drawable.dp).circleCrop().into(profileImage);
        }
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
        profileText.setOnClickListener(new View.OnClickListener() {
            String preName;

            @Override
            public void onClick(View view) {
                preName = profileText.getText().toString();
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                final EditText input = new EditText(getContext());
                input.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                input.setHint("Write your username here");
                dialog.setView(input);
                dialog.setTitle("Change your username");
                dialog.setMessage("Choose a username");
                dialog.setIcon(R.drawable.dialogimage);
                dialog.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (input.getText().toString().trim().isEmpty()) {
                            Toast.makeText(getContext(), "username can't be blank", Toast.LENGTH_SHORT).show();
                        } else {
                            String username = input.getText().toString();
                            SharedPreferences sp = getActivity().getSharedPreferences("username", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("username", username);
                            editor.apply();
                            profileText.setText(username);
                            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Username");
                            mRef.child(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_")).setValue(username);
                        }
                    }
                }).setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();

            }
        });
        ImageView qrImage = v.findViewById(R.id.qrcode);
        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return v;
    }
}