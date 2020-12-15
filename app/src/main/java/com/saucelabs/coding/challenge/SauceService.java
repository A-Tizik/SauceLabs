package com.saucelabs.coding.challenge;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class SauceService extends IntentService {
    private static final String TAG = "SauceService";

    public SauceService() {
        super(TAG);
    }

    /**
     * ServiceManager sources: https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/os/ServiceManager.java?q=ServiceManager
     * IBatterStats sources: https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/com/android/internal/app/IBatteryStats.aidl?q=IBatteryStats
     * Battery service as interface example: https://android.googlesource.com/platform/packages/apps/Settings/+/ics-plus-aosp/src/com/android/settings/BatteryInfo.java#162
     */
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            //First we create a Class<ServiceManager> through reflection
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method listServices = serviceManagerClass.getMethod("listServices");
            //We don't need the ServiceManager instance as the method is static, so we pass null
            String[] services = (String[]) listServices.invoke(null);

            Log.i(TAG, "List of services: " + Arrays.toString(services));

            //Technically getService is also available through other means,
            // but to call it directly from ServiceManager we still need reflection
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder batteryStatsService = (IBinder) getService.invoke(null, "batterystats");

            Class<?> batteryStatsStub = Class.forName("com.android.internal.app.IBatteryStats$Stub");
            //This method is also static, we need it to access service as interface
            Method asInterface = batteryStatsStub.getMethod("asInterface", IBinder.class);
            Object batteryStatsServiceInterface = asInterface.invoke(null, batteryStatsService);

            //Now we have our interface, and we just call isCharging method on it
            Class<?> batteryStatsServiceInterfaceClass = Class.forName("com.android.internal.app.IBatteryStats");
            Method isChargingMethod = batteryStatsServiceInterfaceClass.getDeclaredMethod("isCharging");
            Boolean isCharging = (Boolean) isChargingMethod.invoke(batteryStatsServiceInterface);

            Log.i(TAG, "Charging: " + isCharging);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
