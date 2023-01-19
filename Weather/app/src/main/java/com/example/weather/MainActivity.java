package com.example.weather;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.location.LocationListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.os.Bundle;

import java.io.IOException;
import java.util.Locale;
import android.location.Address;
import java.util.List;
import android.location.Geocoder;


import android.widget.ProgressBar;

import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;




public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomnav;


    public static final int REQUEST_LOCATION = 1;


    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment != null) {
            fragmentTransaction.show(existingFragment);
        } else {
            fragmentTransaction.add(R.id.frame_layout, fragment, tag);
        }

        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment f : fragments) {
            if (!f.equals(existingFragment)) {
                fragmentTransaction.hide(f);
            }
        }
        fragmentTransaction.commit();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bottomnav = findViewById(R.id.bottomNavigationView);
        bottomnav.setSelectedItemId(R.id.home);
        //bottomnav.inflateMenu(R.menu.bottomnav_menu);
        //Log.i("TAG", String.valueOf(bottomnav.getMaxItemCount()));// handle failure
        replaceFragment(new HomeFragment(),"home");

        bottomnav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        // Handle home item selected
                        replaceFragment(new HomeFragment(),"home");
                        break;
                    case R.id.weather:
                        // Handle get weather item selected
                        replaceFragment(new WeatherFragment(), "weather");
                        break;
                    case R.id.clothing:
                        // Handle get recommendation item selected
                        replaceFragment(new ClothFragment(),"clothing");
                        break;

            }
                return true;
        }});
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_LOCATION){
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"cannot use the app without permission.", Toast.LENGTH_SHORT).show();
                this.finishAffinity();
            }
        }
    }



}