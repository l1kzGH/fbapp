package com.likz.firebaseapp.settings;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;

public class Params {

    public static void startSettings(Activity activity) {

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

}
