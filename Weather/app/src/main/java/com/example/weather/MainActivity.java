package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import android.os.Bundle;

import android.os.AsyncTask;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {

    private TextView temperatureTextView;
    private TextView clothingRecommendationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperatureTextView = findViewById(R.id.temperature_text_view);
        clothingRecommendationTextView = findViewById(R.id.clothingRecommendationTextView);
    }

    public void getData(View view) {
        new GetWeatherDataTask().execute();
    }

    private class GetWeatherDataTask extends AsyncTask<Void, Void, WeatherData> {
        @Override
        protected WeatherData doInBackground(Void... voids) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(WeatherData.API_URL)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();
                JSONObject json = new JSONObject(jsonData);
                JSONObject main = json.getJSONObject("main");
                double temperature = main.getDouble("temp");
                String weatherDescription = json.getJSONArray("weather").getJSONObject(0).getString("description");
                return new WeatherData(temperature, weatherDescription);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(WeatherData weatherData) {
            if (weatherData != null) {
                temperatureTextView.setText("Temperature: " + weatherData.temperature + " C " + weatherData.description);
                clothingRecommendationTextView.setText(getClothingRecommendation(weatherData.temperature));
            } else {
                // show error message
            }
        }
    }

    private static String getClothingRecommendation(double temperature) {
        if (temperature < 40) {
            return "Wear a coat, hat, and gloves.";
        } else if (temperature < 60) {
            return "Wear a sweater and a light jacket.";
        } else if (temperature < 80) {
            return "Wear a t-shirt and jeans.";
        }
        else {
            return "Wear a t-shirt and shorts.";
        }
    }
    private static class WeatherData {
        double temperature;
        String description;
        private static final String LOCATION = "Istanbul";
        private static final String UNIT = "metric";
        private static final String API_KEY = "89f5066ac34b5258a4aa571d8738f92f";

        private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=" + LOCATION + "&units=" + UNIT + "&appid=" + API_KEY;

        WeatherData(double temperature, String description) {
            this.temperature = temperature;
            this.description = description;
        }
    }
}