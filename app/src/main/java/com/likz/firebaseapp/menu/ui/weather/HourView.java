package com.likz.firebaseapp.menu.ui.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HourView {

    private Context context;
    private LinearLayout mainLayout;
    private String time;
    private double tempC;
    private String iconUrl;

    public HourView(Context context, String time, double tempC, String iconUrl) {
        this.context = context;
        mainLayout = createMainLayout();
        this.time = time;
        this.tempC = tempC;
        this.iconUrl = iconUrl;

        mainLayout.addView(createTempLayout());
        mainLayout.addView(createImageLayout());
        mainLayout.addView(createTimeLayout());
    }

    public LinearLayout getMainLayout() {
        return mainLayout;
    }

    private LinearLayout createMainLayout() {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(
                convertPixelsToDp(10, context),
                0,
                convertPixelsToDp(10, context),
                0);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        return linearLayout;
    }

    @SuppressLint("SetTextI18n")
    private TextView createTempLayout() {

        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);

        textView.setText(tempC + "°");
        textView.setTextColor(Color.parseColor("#E6000000"));
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextSize(20);
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        return textView;
    }

    private ImageView createImageLayout() {
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                convertPixelsToDp(50, context),
                convertPixelsToDp(50, context)
        );
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);

        final Bitmap[] imageBitmap = {null};
        Thread t = new Thread(() -> {
            try {
                InputStream in = new URL("https:" + iconUrl).openStream();
                imageBitmap[0] = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        try {
            t.join();
            imageView.setImageBitmap(imageBitmap[0]);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        return imageView;
    }

    private TextView createTimeLayout() {

        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);

        textView.setText(time);
        textView.setTextColor(Color.parseColor("#E6000000"));
        textView.setTextSize(18);
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        return textView;
    }

    public static int convertPixelsToDp(int pixels, Context context) {
        return (int) (pixels * context.getResources().getDisplayMetrics().density);
    }

}
