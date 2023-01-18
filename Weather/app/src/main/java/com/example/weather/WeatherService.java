package com.example.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


interface WeatherService {
    @GET("weather?")
    Call<WeatherData> getWeather(@Query("q") String city, @Query("appid") String apiKey);
}

