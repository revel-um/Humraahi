package com.revel.humraahi.fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.revel.humraahi.R;
import com.revel.humraahi.adapters.AdapterClass;
import com.revel.humraahi.classes.Model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class HumraahiFragment extends Fragment {
    ViewPager viewPager;
    AdapterClass adapterClass;
    List<Model> models;
    Bitmap bitmap;
    public static ArrayList<String> nearbyMails;
    StorageReference mStorageRef;
    ArrayList<String> username;
    TextView textHumraahi;
    Button viewProfile;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_humraahi, container, false);
        textHumraahi = v.findViewById(R.id.textHumraahi);
        viewPager = v.findViewById(R.id.viewPager);
        viewProfile = v.findViewById(R.id.viewProfile);
        username = new ArrayList<>();
        models = new ArrayList<>();
        nearbyMails = getArrayList("nearbyMails");
        adapterClass = new AdapterClass(models, getContext());
        if (nearbyMails == null) {
            nearbyMails = new ArrayList<>();
        }

        for (int i = 0; i < nearbyMails.size(); i++) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Username").child("" + nearbyMails.get(i));

            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        username.add((String) snapshot.getValue());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        for (int i = 0; i < nearbyMails.size(); i++) {
            mStorageRef = FirebaseStorage.getInstance().getReference(nearbyMails.get(i) + "/");
            downloadImage(mStorageRef, i);
        }
        if (models.size() == 0) {
            viewPager.setOffscreenPageLimit(nearbyMails.size());
        } else {
            viewPager.setOffscreenPageLimit(models.size());
        }
        viewPager.setPadding(130, 130, 130, 130);
        viewPager.setAdapter(adapterClass);
        if (nearbyMails.size() != 0) {
            textHumraahi.setVisibility(View.GONE);
        }
        if (models.size() > 0) {
            viewProfile.setVisibility(View.VISIBLE);
        }
        return v;
    }

    private void downloadImage(StorageReference mStorageRef, final int i) {

        if (mStorageRef != null) {

            mStorageRef.child("dp").getBytes(1024 * 1024)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (username.size() - 1 >= i)
                        models.add(new Model(bitmap, username.get(i)));
                    else
                        models.add(new Model(bitmap, "null"));
                    adapterClass.notifyDataSetChanged();
                    viewPager.setAdapter(adapterClass);
                    viewPager.setOffscreenPageLimit(models.size());
                    if (models.size() > 0) {
                        viewProfile.setVisibility(View.VISIBLE);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
                    if (username.size() - 1 >= i)
                        models.add(new Model(bitmap, username.get(i)));
                    else
                        models.add(new Model(bitmap, "null"));
                    adapterClass.notifyDataSetChanged();
                    viewPager.setAdapter(adapterClass);
                    viewPager.setOffscreenPageLimit(models.size());

                    if (models.size() > 0) {
                        viewProfile.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    public ArrayList<String> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}