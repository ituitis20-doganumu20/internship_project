package com.example.weather;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class WeatherData {
    @SerializedName("main")
    private MainData mainData;

    @SerializedName("weather")
    private List<Weather> weather;

    public double getTemperature() {
        return mainData.getTemperature();
    }

    public String getWeatherDescription() {
        return weather.get(0).getDescription();
    }
}

class MainData {
    @SerializedName("temp")
    private double temperature;

    public double getTemperature() {
        return temperature;
    }
}

class Weather {
    @SerializedName("description")
    private String description;

    public String getDescription() {
        return description;
    }
}
