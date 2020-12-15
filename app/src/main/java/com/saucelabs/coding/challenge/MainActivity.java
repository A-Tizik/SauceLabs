package com.saucelabs.coding.challenge;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Class serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method listServices = serviceManagerClass.getMethod("listServices");
            String[] services = (String[]) listServices.invoke(null);

            Log.d("Services", Arrays.toString(services));

            Method getService = serviceManagerClass.getMethod("getService",new Class[] {String.class});
            Object batteryStatsService = getService.invoke(null,"batterystats");

            Class batteryStatsStub = Class.forName("com.android.internal.app.IBatteryStats$Stub");
            Method asInterface = batteryStatsStub.getMethod("asInterface",new Class[] {IBinder.class});

            Object batteryStatsServiceInterface = asInterface.invoke(null,batteryStatsService);

            Class batteryStatsServiceInterfaceClass = Class.forName("com.android.internal.app.IBatteryStats");
            @SuppressLint("SoonBlockedPrivateApi") Method isCharging = batteryStatsServiceInterfaceClass.getDeclaredMethod("isCharging");
            isCharging.invoke(batteryStatsServiceInterface);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}