package com.systsync.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Topic {
    private String id;
    private String name;
    private List<CodeEntry> entries;

    public Topic(String id, String name) {
        this.id = id;
        this.name = name;
        this.entries = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<CodeEntry> getEntries() { return entries; }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        JSONArray entriesArray = new JSONArray();
        for (CodeEntry entry : entries) {
            entriesArray.put(entry.toJsonObject());
        }
        json.put("entries", entriesArray);
        return json;
    }

    public static Topic fromJsonObject(JSONObject json) throws JSONException {
        Topic topic = new Topic(
            json.optString("id", String.valueOf(System.currentTimeMillis())),
            json.optString("name", "Untitled Topic")
        );
        JSONArray entriesArray = json.optJSONArray("entries");
        if (entriesArray != null) {
            for (int i = 0; i < entriesArray.length(); i++) {
                topic.entries.add(CodeEntry.fromJsonObject(entriesArray.getJSONObject(i)));
            }
        }
        return topic;
    }
}
