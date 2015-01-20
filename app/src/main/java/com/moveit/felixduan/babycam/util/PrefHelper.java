package com.moveit.felixduan.babycam.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Share camera settings among all component.
 */
public class PrefHelper {
    public static final String RESOLUTION = "resolution";
    public static final String LAPSE = "lapse";

    private final SharedPreferences mPref;

    public PrefHelper(Context context) {
        mPref  = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setResolution(int width, int height) {
        mPref.edit().putString(RESOLUTION,
                width + "," + height)
                .apply();
    }

    public void setLapse(int lapse) {
        mPref.edit().putInt(LAPSE, lapse).apply();
    }

    public int[] getResolution() {
        String raw = mPref.getString(RESOLUTION,null);
        if (raw == null) return null;
        String[] sAry = raw.split(",");
        int[] ary = new int[sAry.length];
        int i = 0;
        for (String s:sAry) {
            ary[i++] = Integer.parseInt(s);
        }
        return ary;
    }

    public int getLapse() {
        return mPref.getInt(LAPSE, 5);
    }
}
