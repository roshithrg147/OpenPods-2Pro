package com.dosse.airpods;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class OpenPodsApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Applies Material 3 Wallpaper-based colors universally
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
