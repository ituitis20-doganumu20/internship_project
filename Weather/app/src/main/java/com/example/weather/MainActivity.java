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
import java.util.ArrayList;
import java.util.HashMap;
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

    public static final int REQUEST_LOCATION = 1;

    static MyAdapter mAdapter;
    ViewPager mPager;

    public static class MyAdapter extends FragmentPagerAdapter  {
        private List<Fragment> fragments;
        public MyAdapter(FragmentManager fm,List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;

        }
        @Override
        public int getCount() {
            return 3;
        }


        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

    }

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
        List<Fragment> fragments = new ArrayList<>();
        mPager = findViewById(R.id.view_pager);
        fragments.add(new WeatherFragment());
        fragments.add(new HomeFragment());
        fragments.add(new ClothFragment());
        mAdapter = new MyAdapter(getSupportFragmentManager(),fragments);
        mPager.setOffscreenPageLimit(3);
        mPager.setAdapter(mAdapter);


        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

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


            @Override
            public void onPageScrollStateChanged(int state) {

            }
            // implementation here
        });

        bottomnav.setOnItemSelectedListener( item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    mPager.setCurrentItem(1);
                    break;
                case R.id.weather:
                    mPager.setCurrentItem(0);
                    break;
                case R.id.clothing:
                    mPager.setCurrentItem(2);

                    break;
            }
            return true;
        });
        mPager.setCurrentItem(1);

    }


}