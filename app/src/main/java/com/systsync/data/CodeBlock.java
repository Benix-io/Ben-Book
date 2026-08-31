package com.systsync.data;

import org.json.JSONException;
import org.json.JSONObject;

public class CodeBlock {
    private String id;
    private String description;
    private String code;
    private String language;

    public CodeBlock(String id, String description, String code, String language) {
        this.id = id;
        this.description = description;
        this.code = code;
        this.language = language;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("description", description);
        json.put("code", code);
        json.put("language", language);
        return json;
    }

    public static CodeBlock fromJsonObject(JSONObject json) throws JSONException {
        return new CodeBlock(
            json.optString("id", String.valueOf(System.currentTimeMillis())),
            json.optString("description", ""),
            json.optString("code", ""),
            json.optString("language", "plaintext")
        );
    }
}
