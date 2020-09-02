package com.revel.humraahi.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.revel.humraahi.R;
import com.revel.humraahi.fragments.HumraahiFragment;
import com.revel.humraahi.fragments.ProfileFragment;
import com.revel.humraahi.fragments.PublicAccountsFragment;

public class DrawerActivityJava extends AppCompatActivity {
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //get all the ids here
        navigationView = findViewById(R.id.navigation_drawer);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer);

        //setting drawer
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Humraahi");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.inSt, R.string.outSt);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setCheckedItem(R.id.humraahi);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new HumraahiFragment()).commit();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                setTitleAndCheck(item);
                openFragment(item);
                return false;
            }
        });
    }

    private void openFragment(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.humraahi:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, new HumraahiFragment()).commit();
                break;
            case R.id.open:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, new PublicAccountsFragment()).commit();
                break;
            case R.id.profile:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, new ProfileFragment()).commit();
                break;
            case R.id.settings:
                break;
        }
    }

    private void setTitleAndCheck(MenuItem item) {
        navigationView.setCheckedItem(item.getItemId());
        getSupportActionBar().setTitle(item.getTitle());
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}