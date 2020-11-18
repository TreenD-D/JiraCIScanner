package com.example.myapplication;

import android.app.Application;


import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

public class CIScannerApp extends Application implements CameraXConfig.Provider {

    public static String AUTH_KEY = "authKey";

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

}


