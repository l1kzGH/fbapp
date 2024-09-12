package com.likz.firebaseapp.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HourForecast {

    private int day; // 0, 1, 2, 3 etc
    private String time; // 10:00 etc
    private double tempC; // 10.2 etc
    private String iconUrl; // icon

    public HourForecast(int day, String time, double tempC, String iconUrl) {
        this.day = day;
        this.tempC = tempC;
        this.iconUrl = iconUrl;

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
            Date date = inputFormat.parse(time);
            String newTime = outputFormat.format(date);
            this.time = newTime;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public int getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public double getTempC() {
        return tempC;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
