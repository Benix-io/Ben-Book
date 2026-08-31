package com.systsync.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String PREF_NAME = "benbook_storage";
    private static final String KEY_DATA = "topics_json";
    private static DataManager instance;
    private final SharedPreferences prefs;
    private List<Topic> topics;

    private DataManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadLocalData();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void saveLocalData() {
        try {
            JSONArray array = new JSONArray();
            for (Topic t : topics) {
                array.put(t.toJsonObject());
            }
            prefs.edit().putString(KEY_DATA, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLocalData() {
        topics = new ArrayList<>();
        String raw = prefs.getString(KEY_DATA, null);
        if (raw != null) {
            try {
                JSONArray array = new JSONArray(raw);
                for (int i = 0; i < array.length(); i++) {
                    topics.add(Topic.fromJsonObject(array.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String exportSyncData() throws Exception {
        JSONArray array = new JSONArray();
        for (Topic t : topics) {
            array.put(t.toJsonObject());
        }
        JSONObject syncObject = new JSONObject();
        syncObject.put("app", "Ben Book");
        syncObject.put("version", "1.0.0");
        syncObject.put("timestamp", System.currentTimeMillis());
        syncObject.put("payload", array);
        return syncObject.toString();
    }

    public void importSyncData(String jsonString) throws Exception {
        JSONObject syncObject = new JSONObject(jsonString);
        JSONArray array = syncObject.getJSONArray("payload");
        List<Topic> imported = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            imported.add(Topic.fromJsonObject(array.getJSONObject(i)));
        }
        this.topics = imported;
        saveLocalData();
    }
}
