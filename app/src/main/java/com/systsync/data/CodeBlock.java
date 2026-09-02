package com.systsync.data;

import org.json.JSONException;
import org.json.JSONObject;

public class CodeBlock {
    private String id;
    private String code;
    private String language;

    public CodeBlock(String code, String language) {
        this.id = "blk_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        this.code = code;
        this.language = language;
    }

    public CodeBlock(String id, String code, String language) {
        this.id = id;
        this.code = code;
        this.language = language;
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getLanguage() { return language; }
    public void setCode(String code) { this.code = code; }
    public void setLanguage(String language) { this.language = language; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("code", code);
        obj.put("language", language);
        return obj;
    }

    public static CodeBlock fromJson(JSONObject obj) throws JSONException {
        return new CodeBlock(
            obj.optString("id", "blk_" + System.currentTimeMillis()),
            obj.getString("code"),
            obj.optString("language", "BASH")
        );
    }
}
