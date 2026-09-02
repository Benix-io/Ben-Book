package com.systsync.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CodeEntry {
    private String id;
    private String title;
    private String description;
    private List<CodeBlock> blocks;

    public CodeEntry(String title, String description) {
        this.id = "ent_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        this.title = title;
        this.description = description;
        this.blocks = new ArrayList<>();
    }

    public CodeEntry(String id, String title, String description, List<CodeBlock> blocks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.blocks = blocks != null ? blocks : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<CodeBlock> getBlocks() { return blocks; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setBlocks(List<CodeBlock> blocks) { this.blocks = blocks; }
    public void addBlock(CodeBlock block) { this.blocks.add(block); }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("description", description);
        JSONArray arr = new JSONArray();
        for (CodeBlock b : blocks) {
            arr.put(b.toJson());
        }
        obj.put("blocks", arr);
        return obj;
    }

    public static CodeEntry fromJson(JSONObject obj) throws JSONException {
        String id = obj.optString("id", "ent_" + System.currentTimeMillis());
        String title = obj.getString("title");
        String desc = obj.optString("description", "");
        List<CodeBlock> list = new ArrayList<>();
        JSONArray arr = obj.optJSONArray("blocks");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                list.add(CodeBlock.fromJson(arr.getJSONObject(i)));
            }
        }
        return new CodeEntry(id, title, desc, list);
    }
}
