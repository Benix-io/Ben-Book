package com.systsync.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import com.systsync.data.DataManager;
import com.systsync.theme.ThemeManager;

public class SettingsDialog {

    public static void show(Activity activity, Runnable onThemeChanged) {
        String[] options = {
            "Theme: Dark Pitch",
            "Theme: Green Dark",
            "Theme: Blue Dark",
            "Theme: Light",
            "Export .sync Backup",
            "Import .sync Backup"
        };

        new AlertDialog.Builder(activity)
            .setTitle("Settings & Preferences")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        ThemeManager.setThemeChoice(activity, ThemeManager.THEME_DARK);
                        activity.recreate();
                        break;
                    case 1:
                        ThemeManager.setThemeChoice(activity, ThemeManager.THEME_GREEN);
                        activity.recreate();
                        break;
                    case 2:
                        ThemeManager.setThemeChoice(activity, ThemeManager.THEME_BLUE);
                        activity.recreate();
                        break;
                    case 3:
                        ThemeManager.setThemeChoice(activity, ThemeManager.THEME_LIGHT);
                        activity.recreate();
                        break;
                    case 4:
                        exportSync(activity);
                        break;
                    case 5:
                        importSync(activity, onThemeChanged);
                        break;
                }
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private static void exportSync(Activity activity) {
        try {
            String json = DataManager.getInstance(activity).exportSyncData();
            android.content.ClipboardManager clipboard = 
                (android.content.ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(".sync Backup", json);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(activity, ".sync JSON payload copied to clipboard!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void importSync(Activity activity, Runnable onComplete) {
        EditText input = new EditText(activity);
        input.setHint("Paste .sync JSON string here");
        new AlertDialog.Builder(activity)
            .setTitle("Import .sync Backup")
            .setView(input)
            .setPositiveButton("Restore", (dialog, which) -> {
                try {
                    String raw = input.getText().toString().trim();
                    DataManager.getInstance(activity).importSyncData(raw);
                    Toast.makeText(activity, "Data imported successfully!", Toast.LENGTH_SHORT).show();
                    if (onComplete != null) onComplete.run();
                } catch (Exception e) {
                    Toast.makeText(activity, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
