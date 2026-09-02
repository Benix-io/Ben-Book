package com.systsync.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String PREF_NAME = "benbook_store_v3_clean";
    private static final String KEY_TOPICS = "topics_json_array";
    private static DataManager instance;
    private final SharedPreferences prefs;
    private final List<Topic> topics;

    private DataManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        topics = new ArrayList<>();
        loadFromPrefs();
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

    public Topic findTopicById(String id) {
        for (Topic t : topics) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    public void addTopic(Topic topic) {
        topics.add(0, topic);
        saveToPrefs();
    }

    public void deleteTopic(Topic topic) {
        topics.remove(topic);
        saveToPrefs();
    }

    public void saveToPrefs() {
        try {
            JSONArray arr = new JSONArray();
            for (Topic t : topics) {
                arr.put(t.toJson());
            }
            prefs.edit().putString(KEY_TOPICS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void loadFromPrefs() {
        try {
            String raw = prefs.getString(KEY_TOPICS, "[]");
            JSONArray arr = new JSONArray(raw);
            topics.clear();
            for (int i = 0; i < arr.length(); i++) {
                topics.add(Topic.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception ignored) {}
    }

    public boolean exportToStream(OutputStream os) {
        try {
            JSONObject root = new JSONObject();
            root.put("format", "benbook_sync_v2");
            root.put("exported_at", System.currentTimeMillis());
            JSONArray arr = new JSONArray();
            for (Topic t : topics) {
                arr.put(t.toJson());
            }
            root.put("topics", arr);
            os.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean importFromStream(InputStream is) {
        try {
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            String jsonStr = new String(buf, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonStr);
            JSONArray arr = root.getJSONArray("topics");
            topics.clear();
            for (int i = 0; i < arr.length(); i++) {
                topics.add(Topic.fromJson(arr.getJSONObject(i)));
            }
            saveToPrefs();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
