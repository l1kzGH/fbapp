package com.likz.firebaseapp.menu.ui.weather;

import com.likz.firebaseapp.entity.DayForecast;
import com.likz.firebaseapp.entity.HourForecast;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class WeatherConnect {

    static final private String API_URL = WeatherData.WEATHER_API_URL;
    private JSONObject jsonObj;
    private int responseCodee;
    private DayForecast dayForecast;
    private List<HourForecast> hourForecastList;

    public WeatherConnect() {
    }

    public void getDataOfCity(String city) {
        final int days = 2;
        HttpResponse<JsonNode> result = null;

        try {
            // get request to api
            Future<HttpResponse<JsonNode>> httpResponse = Unirest.get(API_URL)
                    .field("q", city)
                    .field("days", days)
                    .field("aqi", "no")
                    .field("alerts", "no")
                    .asJsonAsync();
            result = httpResponse.get();
        } catch (Exception e) {
            System.err.println("Something going wrong - " + e);
        }

        this.jsonObj = result.getBody().getObject();
        this.responseCodee = result.getCode();
        if (responseCodee == 200)
            getAllData();
    }

    private void getAllData() {
        try {
            dayForecast = new DayForecast(
                    jsonObj.getJSONObject("location").getString("name"),
                    jsonObj.getJSONObject("location").getString("country"),
                    jsonObj.getJSONObject("location").getString("localtime"),
                    jsonObj.getJSONObject("current").getString("temp_c"),
                    jsonObj.getJSONObject("current").getString("wind_mph"),
                    jsonObj.getJSONObject("current").getString("wind_dir"),
                    jsonObj.getJSONObject("current").getString("humidity"),
                    jsonObj.getJSONObject("current").getString("pressure_mb"),
                    jsonObj.getJSONObject("current").getString("uv"),
                    jsonObj.getJSONObject("current").getJSONObject("condition")
                            .getString("text"),
                    jsonObj.getJSONObject("current").getJSONObject("condition")
                            .getString("icon"),
                    jsonObj.getJSONObject("current").getInt("is_day")
            );

            hourForecastList = new ArrayList<>();
            JSONArray forecast = jsonObj.getJSONObject("forecast").getJSONArray("forecastday");
            for(int i = 0; i < forecast.length() * 24; i++){
                int day = i / 24;
                JSONObject hourObj = forecast.getJSONObject(day).getJSONArray("hour").getJSONObject(i - day * 24);

                HourForecast hour = new HourForecast(
                        day,
                        hourObj.getString("time"),
                        hourObj.getDouble("temp_c"),
                        hourObj.getJSONObject("condition").getString("icon")
                );
                hourForecastList.add(hour);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public List<HourForecast> getHourForecastList() {
        return hourForecastList;
    }

    public int getIsDay() {
        return dayForecast.getIsDay();
    }

    public String getUv() {
        return dayForecast.getUv();
    }

    public String getPressure() {
        return dayForecast.getPressure();
    }

    public String getCityName() {
        return dayForecast.getCityName();
    }

    public String getCountryName() {
        return dayForecast.getCountryName();
    }

    public String getLocalTime() {
        return dayForecast.getLocalTime();
    }

    public String getTempC() {
        return dayForecast.getTempC();
    }

    public String getWindMph() {
        return dayForecast.getWindMph();
    }

    public String getWindDir() {
        return dayForecast.getWindDir();
    }

    public String getHumidity() {
        return dayForecast.getHumidity();
    }

    public String getConditionText() {
        return dayForecast.getConditionText();
    }

    public String getConditionIcon() {
        return dayForecast.getConditionIcon();
    }

    public int getResponseCodee() {
        return responseCodee;
    }
}
