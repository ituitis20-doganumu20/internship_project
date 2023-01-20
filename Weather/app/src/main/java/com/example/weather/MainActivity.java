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

import androidx.viewpager.widget.ViewPager;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomnav;

    private ViewPager2 viewPager;

    public static final int REQUEST_LOCATION = 1;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bottomnav = findViewById(R.id.bottomNavigationView);
        //bottomnav.setSelectedItemId(R.id.home);
        //bottomnav.inflateMenu(R.menu.bottomnav_menu);
        //Log.i("TAG", String.valueOf(bottomnav.getMaxItemCount()));// handle failure
        //(new HomeFragment(),"home");
        viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment = null;
                switch (position) {
                    case 1:
                        fragment = getSupportFragmentManager().findFragmentByTag("home");
                        if (fragment == null) {
                            fragment = new HomeFragment();
                        }
                        break;
                    case 0:
                        fragment = getSupportFragmentManager().findFragmentByTag("weather");
                        if (fragment == null) {
                            fragment = new WeatherFragment();
                        }
                        break;
                    case 2:
                        fragment = getSupportFragmentManager().findFragmentByTag("clothing");
                        if (fragment == null) {
                            fragment = new ClothFragment();
                        }
                        break;
                }
                return fragment;
            }


            @Override
            public int getItemCount() {
                return 3;
            }
        });
        viewPager.setCurrentItem(1);
        bottomnav.setSelectedItemId(R.id.home);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomnav.setSelectedItemId(R.id.weather);
                        break;
                    case 1:
                        bottomnav.setSelectedItemId(R.id.home);
                        break;
                    case 2:
                        bottomnav.setSelectedItemId(R.id.clothing);
                        break;
                }
            }
        });

        bottomnav.setOnItemSelectedListener( item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.weather:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.clothing:
                    viewPager.setCurrentItem(2);
                    break;
            }
            return true;
        });

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