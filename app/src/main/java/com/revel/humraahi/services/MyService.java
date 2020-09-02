package com.revel.humraahi.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.revel.humraahi.R;
import com.revel.humraahi.activities.MainActivity;
import com.revel.humraahi.receivers.CancelReceiver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class MyService extends Service {
    DatabaseReference mRef;
    NotificationManager manager;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    String preLongitude, preLatitude;
    ArrayList<Double> arrayLongitude;
    ArrayList<Double> arrayLatitude;
    ArrayList<String> arrayUid;
    ArrayList<String> nearbyMails;
    double userLongitude = 0, userLatitude = 0;
    float[] results;
    boolean uploadSuccess = false;
    boolean stopUploading = false;
    String childRef = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_");

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    Map<String, String> map = (Map<String, String>) snapshot.getValue();
                    if (map != null) {
                        final Set set = map.keySet();
                        Iterator iterator = set.iterator();
                        while (iterator.hasNext()) {
                            String child = (String) iterator.next();
                            String value = map.get(child);
                            String lon = value.substring(0, value.indexOf("B")).replace("_", ".");
                            String lat = value.substring(value.indexOf("B") + 1).replace("_", ".");
                            arrayUid.add(child);
                            arrayLongitude.add(Double.valueOf(lon));
                            arrayLatitude.add(Double.valueOf(lat));

                            final Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (!stopUploading) {
                                        if (uploadSuccess)
                                            downloadLocation();
                                        handler.postDelayed(this, 10000);
                                    }
                                }
                            };
                            if (!stopUploading)
                                handler.postDelayed(runnable, 10000);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("ID", "VISTAS", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        Intent intent1 = new Intent(MyService.this, MainActivity.class);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(MyService.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent2 = new Intent(MyService.this, CancelReceiver.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(MyService.this, 1, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyService.this, "ID")
                .setSmallIcon(R.drawable.ic_baseline_location_searching_24)
                .setContentTitle("Humraahi")
                .setContentText("We are finding your humraahi")
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#8B4513"))
                .setColorized(true)
                .addAction(R.drawable.ic_baseline_cancel_24, "Stop searching", pendingIntent2)
                .setContentIntent(pendingIntent1);
        startForeground(1, builder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    private void downloadLocation() {
        mRef.child(childRef).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    String value = (String) snapshot.getValue();
                    String lon = value.substring(0, value.indexOf("B")).replace("_", ".");
                    String lat = value.substring(value.indexOf("B") + 1).replace("_", ".");
                    userLongitude = Double.parseDouble(lon);
                    userLatitude = Double.parseDouble(lat);
                    for (int i = 0; i < arrayUid.size(); i++) {
                        results = new float[arrayUid.size()];
                        Location.distanceBetween(userLatitude, userLongitude, arrayLatitude.get(i), arrayLongitude.get(i), results);
                        if (results[i] < 1000) {
                            nearbyMails.add(arrayUid.get(i));
                            String selfMail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_");
                            if (nearbyMails.contains(selfMail)) {
                                nearbyMails.remove(selfMail);
                            }
                            Set<String> set1 = new HashSet<>(nearbyMails);
                            nearbyMails = new ArrayList<>(set1);
                            if (nearbyMails != null) {
                                saveArrayList(nearbyMails, "nearbyMails");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {

        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        arrayLongitude = new ArrayList<>();
        arrayLatitude = new ArrayList<>();
        arrayUid = new ArrayList<>();
        nearbyMails = new ArrayList<>();

        String usersState = getSharedPreferences("usersState", MODE_PRIVATE).getString("usersState", "Unknown");
        mRef = FirebaseDatabase.getInstance().getReference(usersState);
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                uploadLocation();
                handler.postDelayed(this, 10000);
            }
        };
        handler.postDelayed(runnable, 10000);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUploading = true;
        unregisterReceiver(gpsReceiver);
        stopSelf();
    }

    private void uploadLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MyService.this);
        if (ActivityCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currentLocation = location;

                        if (currentLocation != null) {
                            String longitude = String.valueOf(currentLocation.getLongitude());
                            String latitude = String.valueOf(currentLocation.getLatitude());
                            if (longitude.contains(".")) {
                                longitude = longitude.replace(".", "_");
                            }
                            if (latitude.contains(".")) {
                                latitude = latitude.replace(".", "_");
                            }
                            if (longitude != preLongitude || latitude != preLatitude) {
                                mRef.child(childRef).setValue(longitude + "B" + latitude);
                                uploadSuccess = true;
                                preLongitude = longitude;
                                preLongitude = latitude;
                            }
                        }
                    }
                });
    }

    public void saveArrayList(ArrayList<String> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public ArrayList<String> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
