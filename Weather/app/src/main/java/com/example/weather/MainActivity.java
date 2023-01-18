package com.example.weather;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.location.LocationListener;
import android.util.Log;
import android.view.View;

import android.os.Bundle;

import java.io.IOException;
import java.util.Locale;
import android.location.Address;
import java.util.List;
import android.location.Geocoder;




import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;




public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOCATION = 1;
    private TextView temperatureTextView;
    private TextView clothingRecommendationTextView;

    private TextView CityNameView;

    private Button button;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.getDataButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWeatherData();
            }
        });
        CityNameView = findViewById(R.id.CityNameView);
        temperatureTextView = findViewById(R.id.temperature_text_view);
        clothingRecommendationTextView = findViewById(R.id.clothingRecommendationTextView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_LOCATION){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
                getWeatherData();
            else
                Toast.makeText(this,"access not permitted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getWeatherData() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else{

            //permission granted, proceed with location services
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //List<String> providers = locationManager.getProviders(true);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, false);
            locationManager.requestLocationUpdates(bestProvider, 5000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Do something with the updated location
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
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
                                    clothingRecommendationTextView.setText(getClothingRecommendation(weatherData.getTemperature()-273));
                                } else {
                                    System.out.println("respond is not successful");// handle error
                                }
                                //end loading
                            }

                            @Override
                            public void onFailure(Call<WeatherData> call, Throwable t) {
                                Log.i("TAG", "onFailure: failed");// handle failure
                                //end loading
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

}