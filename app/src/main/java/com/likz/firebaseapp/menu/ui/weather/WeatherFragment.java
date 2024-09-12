package com.likz.firebaseapp.menu.ui.weather;

import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.likz.firebaseapp.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class WeatherFragment extends Fragment {

    // private TextView cityTV, temperatureTV, weatherTypeTV;
    ImageButton getPosButton;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CityWCardFragment cityWCardFragment;

    public WeatherFragment() {
        super(R.layout.fragment_weather);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.weather_city_search).setOnClickListener(v -> {
            new Thread(this::updatingData).start();
        });

        getPosButton = view.findViewById(R.id.weather_nav_location);
        getPosButton.setOnClickListener(v -> {
            posButtonSwapF(false);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            requestCurrentLocation();
        });

        cityWCardFragment = (CityWCardFragment)
                this.getChildFragmentManager().findFragmentById(R.id.city_card_fragment);
        cityWCardFragment.getView().setVisibility(View.INVISIBLE);
    }

    void updatingData() {
        updatingData("");
    }

    @SuppressLint("SetTextI18n")
    void updatingData(@NonNull String districtName) {
        EditText city = getView().findViewById(R.id.weather_update_city);
        if (districtName.equals("")) {
            districtName = city.getText().toString();
        }
        WeatherConnect wc = new WeatherConnect();
        wc.getDataOfCity(districtName);

        switch (wc.getResponseCodee()) {
            case 200:
                if (cityWCardFragment != null) {
                    getActivity().runOnUiThread(() -> {
                        cityWCardFragment.initData(wc);
                        cityWCardFragment.getView().setVisibility(View.VISIBLE);
                    });
                }
                break;
            case 400:
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        city.setError("Search error - can't find city");
                    }
                });
                break;
        }

    }

    private void requestCurrentLocation() {

        // Request permission
        if (ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            System.out.println("*entered to get geopos*");
            fusedLocationProviderClient.getCurrentLocation(
                    PRIORITY_BALANCED_POWER_ACCURACY,
                    null).addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    try {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        String city = addresses.get(0).getLocality();
                        System.out.println("Geo-position city = " + city);
                        new Thread(() -> updatingData(city)).start();
                        posButtonSwapF(true);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            posButtonSwapF(true);
        }
    }

    private void posButtonSwapF(boolean toEnable) {
        if (toEnable) {
            getPosButton.setImageResource(R.drawable.local_navigation);
            getPosButton.clearAnimation();
            getPosButton.setEnabled(true);
        } else {
            getPosButton.setImageResource(R.drawable.baseline_refresh_24);
            Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
            getPosButton.startAnimation(rotateAnimation);
            getPosButton.setEnabled(false);
        }
    }

}