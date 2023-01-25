package com.example.weather;

import static com.example.weather.MainActivity.REQUEST_LOCATION;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import android.widget.Toast;
public class WeatherFragment extends Fragment {

    private TextView temperatureTextView;
    //private TextView clothingRecommendationTextView;

    private TextView CityNameView;
    private TextView progressText;
    private ProgressBar mProgressBar;

    private Button button;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_weather, container, false);
        button = view.findViewById(R.id.getDataButton);

        CityNameView = view.findViewById(R.id.CityNameView);
        temperatureTextView = view.findViewById(R.id.temperature_text_view);
        //clothingRecommendationTextView = view.findViewById(R.id.clothingRecommendationTextView);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        progressText = view.findViewById(R.id.progress_text);
        progressText.setVisibility(View.GONE);

        return view;
    }

    void getWeatherData() {

        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            progressText.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            //permission granted, proceed with location services
            LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

            //List<String> providers = locationManager.getProviders(true);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, false);
            locationManager.requestLocationUpdates(bestProvider, 5000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Do something with the updated location
                    try {
                        Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        String city = addresses.get(0).getAdminArea();
                        Retrofit retrofit = WeatherClient.getClient();

                        WeatherService service = retrofit.create(WeatherService.class);

                        //loading dialoge

                        Call<WeatherData> call = service.getWeather(city, "89f5066ac34b5258a4aa571d8738f92f");
                        call.enqueue(new Callback<WeatherData>() {// enqueue for async, execute for sync
                            @Override
                            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                                if (response.isSuccessful()) {

                                    CityNameView.setText("City Name:" + city);
                                    WeatherData weatherData = response.body();
                                    temperatureTextView.setText("Temperature: " + String.format("%.2f", weatherData.getTemperature()-273) + " C " + weatherData.getWeatherDescription());
                                    //ClothFragment clothFragment = (ClothFragment) getParentFragmentManager().findFragmentById(R.id.cloth_fragment);
                                    // Set the temperature data as an argument
                                    ClothFragment clothFragment = (ClothFragment) MainActivity.mAdapter.getItem(2);
                                    Bundle bundle = new Bundle();
                                    bundle.putDouble("temperature", weatherData.getTemperature());
                                    clothFragment.setArguments(bundle);
                                } else {
                                    System.out.println("respond is not successful");// handle error
                                }
                                //end loading
                                mProgressBar.setVisibility(View.GONE);
                                progressText.setVisibility(View.GONE);
                                button.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Call<WeatherData> call, Throwable t) {
                                Log.i("TAG", "onFailure: failed");// handle failure
                                //end loading
                                mProgressBar.setVisibility(View.GONE);
                                progressText.setVisibility(View.GONE);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });

        }


    }
    private static String getClothingRecommendation(double temperature) {
        if (temperature < 10) {
            return "Wear a coat, hat, and gloves.";
        } else if (temperature < 20) {
            return "Wear a sweater and a light jacket.";
        } else if (temperature < 30) {
            return "Wear a t-shirt and jeans.";
        }
        else {
            return "Wear a t-shirt and shorts.";
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                else
                    getWeatherData();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_LOCATION){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getWeatherData();
            }else{
                Toast.makeText(getContext(),"cannot use the app without location permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }



}