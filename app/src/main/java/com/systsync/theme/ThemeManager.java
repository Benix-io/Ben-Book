package com.systsync.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import com.systsync.R;

public class ThemeManager {
    public static final String THEME_DARK = "dark";
    public static final String THEME_BLUE = "blue_dark";
    public static final String THEME_GREEN = "green_dark";

    private static final String PREF_NAME = "benbook_theme_pref";
    private static final String KEY_THEME = "current_theme";

    public static void setTheme(Context context, String themeKey) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_THEME, themeKey).apply();
    }

    public static String getTheme(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_THEME, THEME_DARK);
    }

    public static void applyTheme(Activity activity) {
        String t = getTheme(activity);
        if (THEME_BLUE.equals(t)) {
            activity.setTheme(R.style.Theme_BenBook_BlueDark);
        } else if (THEME_GREEN.equals(t)) {
            activity.setTheme(R.style.Theme_BenBook_GreenDark);
        } else {
            activity.setTheme(R.style.Theme_BenBook_Dark);
        }
    }

    public static int getCardColor(Context context) {
        String t = getTheme(context);
        if (THEME_BLUE.equals(t)) return Color.parseColor("#0C2340");
        if (THEME_GREEN.equals(t)) return Color.parseColor("#0A291B");
        return Color.parseColor("#131B2E");
    }

    public static int getBackgroundColor(Context context) {
        String t = getTheme(context);
        if (THEME_BLUE.equals(t)) return Color.parseColor("#061324");
        if (THEME_GREEN.equals(t)) return Color.parseColor("#05160E");
        return Color.parseColor("#0B0F19");
    }
}
