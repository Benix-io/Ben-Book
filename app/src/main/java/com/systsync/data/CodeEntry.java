package com.systsync.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CodeEntry {
    private String id;
    private String title;
    private List<CodeBlock> codeBlocks;

    public CodeEntry(String id, String title) {
        this.id = id;
        this.title = title;
        this.codeBlocks = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<CodeBlock> getCodeBlocks() { return codeBlocks; }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("title", title);
        JSONArray blocksArray = new JSONArray();
        for (CodeBlock block : codeBlocks) {
            blocksArray.put(block.toJsonObject());
        }
        json.put("codeBlocks", blocksArray);
        return json;
    }

    public static CodeEntry fromJsonObject(JSONObject json) throws JSONException {
        CodeEntry entry = new CodeEntry(
            json.optString("id", String.valueOf(System.currentTimeMillis())),
            json.optString("title", "Untitled Entry")
        );
        JSONArray blocksArray = json.optJSONArray("codeBlocks");
        if (blocksArray != null) {
            for (int i = 0; i < blocksArray.length(); i++) {
                entry.codeBlocks.add(CodeBlock.fromJsonObject(blocksArray.getJSONObject(i)));
            }
        }
        return entry;
    }
}
