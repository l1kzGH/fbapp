package com.likz.firebaseapp.entity;

public class DayForecast {

    final private String cityName, countryName, localTime, tempC;
    final private String windMph, windDir, humidity, pressure, uv;
    final private String conditionText, conditionIcon;
    final private int isDay;

    public DayForecast(String cityName, String countryName, String localTime,
                       String tempC, String windMph, String windDir,
                       String humidity, String pressure, String uv,
                       String conditionText, String conditionIcon, int isDay) {
        this.cityName = cityName;
        this.countryName = countryName;
        this.localTime = localTime;
        this.tempC = tempC;
        this.windMph = windMph;
        this.windDir = windDir;
        this.humidity = humidity;
        this.pressure = pressure;
        this.uv = uv;
        this.conditionText = conditionText;
        this.conditionIcon = conditionIcon;
        this.isDay = isDay;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getLocalTime() {
        return localTime;
    }

    public String getTempC() {
        return tempC;
    }

    public String getWindMph() {
        return windMph;
    }

    public String getWindDir() {
        return windDir;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public String getUv() {
        return uv;
    }

    public String getConditionText() {
        return conditionText;
    }

    public String getConditionIcon() {
        return conditionIcon;
    }

    public int getIsDay() {
        return isDay;
    }
}
