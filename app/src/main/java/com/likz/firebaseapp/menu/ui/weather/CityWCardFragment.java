package com.likz.firebaseapp.menu.ui.weather;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.likz.firebaseapp.R;
import com.likz.firebaseapp.entity.HourForecast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CityWCardFragment extends Fragment {

    WeatherConnect wc;

    public CityWCardFragment() {
        super(R.layout.fragment_city_w_card);
    }
    TextView ccard_name_and_country, ccard_localtime, ccard_tempc, ccard_condition,
            ccard_daytype, ccard_wind, ccard_humidity, ccard_pressure, ccard_uv;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ccard_name_and_country = view.findViewById(R.id.ccard_name_and_country);
        ccard_localtime = view.findViewById(R.id.ccard_localtime);
        ccard_tempc = view.findViewById(R.id.ccard_tempc);
        ccard_condition = view.findViewById(R.id.ccard_condition);
        ccard_daytype = view.findViewById(R.id.ccard_daytype);
        ccard_wind = view.findViewById(R.id.ccard_wind);
        ccard_humidity = view.findViewById(R.id.ccard_humidity);
        ccard_pressure = view.findViewById(R.id.ccard_pressure);
        ccard_uv = view.findViewById(R.id.ccard_uv);
    }

    @SuppressLint("SetTextI18n")
    public void initData(WeatherConnect wc){
        this.wc = wc;
        ccard_name_and_country.setText(wc.getCityName() + ", " + wc.getCountryName());

        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = inputDateFormat.parse(wc.getLocalTime());
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("d MMMM yyyy, 'it''s' HH:mm 'now'");
            String outputDate = outputDateFormat.format(date);
            ccard_localtime.setText(outputDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        ccard_tempc.setText(wc.getTempC() + "℃");
        ccard_condition.setText(wc.getConditionText());
        ccard_daytype.setText(wc.getIsDay() == 1 ? "Day" : "Night");
        ccard_wind.setText(wc.getWindMph() + " mph (" + wc.getWindDir() + ")");
        ccard_humidity.setText(wc.getHumidity() + "%");

        String mmhgPressure = (int) (Double.parseDouble(wc.getPressure()) * 0.75) + "";
        ccard_pressure.setText(mmhgPressure + " mm Hg.");

        ccard_uv.setText(wc.getUv() + " UV-index");

        // hours forecast
        initHoursForecast();

    }

    @SuppressLint({"NewApi", "LocalSuppress"})
    private void initHoursForecast(){
        LinearLayout hoursForecast = getView().findViewById(R.id.ccard_hours_forecast);
        hoursForecast.removeAllViews();
        int i = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
        LocalDateTime dateTime = LocalDateTime.parse(wc.getLocalTime(), formatter);
        int currHour = dateTime.getHour();

        for(; i < wc.getHourForecastList().size(); i++){
            HourForecast hourForecast = wc.getHourForecastList().get(i);
            LocalTime time = LocalTime.parse(hourForecast.getTime());
            int hour = time.getHour();

            if(hour + 1 > currHour){
                HourView hourView = new HourView(
                        getContext(),
                        hourForecast.getTime(),
                        hourForecast.getTempC(),
                        hourForecast.getIconUrl());
                hoursForecast.addView(hourView.getMainLayout());

                HourView hourView2 = new HourView(
                        getContext(),
                        dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        Double.parseDouble(wc.getTempC()),
                        wc.getConditionIcon());
                hoursForecast.addView(hourView2.getMainLayout());

                i++;
                break;
            }
        }

        for(; i < wc.getHourForecastList().size(); i++){
            HourForecast hourForecast = wc.getHourForecastList().get(i);
            HourView hourView = new HourView(
                    getContext(),
                    hourForecast.getTime(),
                    hourForecast.getTempC(),
                    hourForecast.getIconUrl());
            hoursForecast.addView(hourView.getMainLayout());
        }
    }
}