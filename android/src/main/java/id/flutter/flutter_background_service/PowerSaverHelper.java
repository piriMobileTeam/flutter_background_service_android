package id.flutter.flutter_background_service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.content.ContextCompat;
import androidx.annotation.RequiresApi;

public class PowerSaverHelper {
    public enum WhiteListedInBatteryOptimizationsState {
        WHITE_LISTED, NOT_WHITE_LISTED, ERROR_GETTING_STATE, IRRELEVANT_OLD_ANDROID_API
    }

    public static void openSystemPowerSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        context.startActivity(intent);
    }

    public static WhiteListedInBatteryOptimizationsState getWhiteListedInBatteryOptimizationsState(Context context, String packageName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return WhiteListedInBatteryOptimizationsState.IRRELEVANT_OLD_ANDROID_API;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null)
            return WhiteListedInBatteryOptimizationsState.ERROR_GETTING_STATE;
        if (pm.isIgnoringBatteryOptimizations(packageName))
            return WhiteListedInBatteryOptimizationsState.WHITE_LISTED;
        else
            return WhiteListedInBatteryOptimizationsState.NOT_WHITE_LISTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"BatteryLife", "InlinedApi"})
    public static Intent prepareIntentForWhiteListingOfBatteryOptimization(Context context, String packageName, boolean alsoWhenWhiteListed) {
        switch (getWhiteListedInBatteryOptimizationsState(context, packageName)) {
            case WHITE_LISTED:
                if (alsoWhenWhiteListed)
                    return new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                break;
            case NOT_WHITE_LISTED:
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_DENIED)
                    return new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                return new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        .setData(Uri.parse("package:" + packageName));
            case ERROR_GETTING_STATE:
                return new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            case IRRELEVANT_OLD_ANDROID_API:
                break;
        }
        return null;
    }

    public static Boolean isWhiteListedFromBatteryOptimizations(Context context) {
        switch (getWhiteListedInBatteryOptimizationsState(context, context.getPackageName())) {
            case IRRELEVANT_OLD_ANDROID_API:
            case WHITE_LISTED:
                return true;
            case NOT_WHITE_LISTED:
                return false;
            case ERROR_GETTING_STATE:
                return null;
        }
        return null;
    }
}

