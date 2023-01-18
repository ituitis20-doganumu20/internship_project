package com.example.weather;


import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import android.os.Bundle;


import android.widget.Button;
import android.widget.TextView;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;




public class MainActivity extends AppCompatActivity {

    private TextView temperatureTextView;
    private TextView clothingRecommendationTextView;

    private Button button;
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
        temperatureTextView = findViewById(R.id.temperature_text_view);
        clothingRecommendationTextView = findViewById(R.id.clothingRecommendationTextView);
    }


    private void getWeatherData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);

        Call<WeatherData> call = service.getWeather("Istanbul", "89f5066ac34b5258a4aa571d8738f92f");
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful()) {
                    WeatherData weatherData = response.body();
                    double celcius= weatherData.getTemperature()-273;
                    temperatureTextView.setText("Temperature: " + String.format("%.2f", celcius) + " C " + weatherData.getWeatherDescription());
                    clothingRecommendationTextView.setText(getClothingRecommendation(celcius));
                } else {
                    System.out.println("respond is not successful");// handle error
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Log.i("TAG", "onFailure: fdsfdfs");// handle failure
            }
        });
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