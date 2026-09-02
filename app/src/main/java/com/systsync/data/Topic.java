package com.systsync.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Topic {
    private String id;
    private String title;
    private String description;
    private String createdAt;
    private List<CodeEntry> entries;

    public Topic(String title, String description) {
        this.id = "top_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        this.title = title;
        this.description = description;
        this.createdAt = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
        this.entries = new ArrayList<>();
    }

    public Topic(String id, String title, String description, String createdAt, List<CodeEntry> entries) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : "Sept 01, 2026";
        this.entries = entries != null ? entries : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public List<CodeEntry> getEntries() { return entries; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void addEntry(CodeEntry entry) { this.entries.add(0, entry); }
    public void deleteEntry(CodeEntry entry) { this.entries.remove(entry); }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("description", description);
        obj.put("createdAt", createdAt);
        JSONArray arr = new JSONArray();
        for (CodeEntry e : entries) {
            arr.put(e.toJson());
        }
        obj.put("entries", arr);
        return obj;
    }

    public static Topic fromJson(JSONObject obj) throws JSONException {
        String id = obj.optString("id", "top_" + System.currentTimeMillis());
        String title = obj.getString("title");
        String desc = obj.optString("description", "");
        String date = obj.optString("createdAt", "Sept 01, 2026");
        List<CodeEntry> list = new ArrayList<>();
        JSONArray arr = obj.optJSONArray("entries");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                list.add(CodeEntry.fromJson(arr.getJSONObject(i)));
            }
        }
        return new Topic(id, title, desc, date, list);
    }
}
