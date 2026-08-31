package com.systsync.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.systsync.R;

public class ThemeManager {
    private static final String PREF_THEME = "app_theme_pref";
    private static final String KEY_THEME = "selected_theme";
    private static final String KEY_FONT = "selected_font";

    public static final String THEME_DARK = "dark";
    public static final String THEME_GREEN = "green_dark";
    public static final String THEME_BLUE = "blue_dark";
    public static final String THEME_LIGHT = "light";

    public static void applyTheme(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREF_THEME, Context.MODE_PRIVATE);
        String theme = sp.getString(KEY_THEME, THEME_DARK);

        switch (theme) {
            case THEME_GREEN:
                activity.setTheme(R.style.Theme_BenBook_GreenDark);
                break;
            case THEME_BLUE:
                activity.setTheme(R.style.Theme_BenBook_BlueDark);
                break;
            case THEME_LIGHT:
                activity.setTheme(R.style.Theme_BenBook_Light);
                break;
            case THEME_DARK:
            default:
                activity.setTheme(R.style.Theme_BenBook_Dark);
                break;
        }
    }

    public static void setThemeChoice(Context context, String themeKey) {
        context.getSharedPreferences(PREF_THEME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, themeKey)
                .apply();
    }

    public static String getFontChoice(Context context) {
        return context.getSharedPreferences(PREF_THEME, Context.MODE_PRIVATE)
                .getString(KEY_FONT, "monospace");
    }

    public static void setFontChoice(Context context, String fontKey) {
        context.getSharedPreferences(PREF_THEME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FONT, fontKey)
                .apply();
    }
}
