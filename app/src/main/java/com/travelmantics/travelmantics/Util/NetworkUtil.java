package com.travelmantics.travelmantics.Util;

import android.content.Context;
import android.net.ConnectivityManager;

import android.os.Build;

import java.io.IOException;

public class NetworkUtil {

    public static boolean NetworkAvailable(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }
      return false;
    }

}
