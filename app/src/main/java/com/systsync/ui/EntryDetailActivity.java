package com.systsync.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.systsync.R;
import com.systsync.data.CodeBlock;
import com.systsync.data.CodeEntry;
import com.systsync.data.DataManager;
import com.systsync.data.Topic;
import com.systsync.theme.ThemeManager;

public class EntryDetailActivity extends Activity {
    private String topicId;
    private String entryId;
    private Topic topic;
    private CodeEntry entry;
    private LinearLayout containerCodeBlocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);

        topicId = getIntent().getStringExtra("topic_id");
        entryId = getIntent().getStringExtra("entry_id");

        findViewById(R.id.btn_back_entry).setOnClickListener(v -> finish());
        containerCodeBlocks = findViewById(R.id.container_detail_code_blocks);

        findViewById(R.id.btn_edit_entry).setOnClickListener(v -> {
            Intent i = new Intent(this, EntryEditorActivity.class);
            i.putExtra("topic_id", topicId);
            i.putExtra("entry_id", entryId);
            startActivity(i);
        });

        findViewById(R.id.btn_delete_entry).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this snippet?")
                .setPositiveButton("Delete", (d, w) -> {
                    topic.deleteEntry(entry);
                    DataManager.getInstance(this).saveToPrefs();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        topic = DataManager.getInstance(this).findTopicById(topicId);
        if (topic == null) { finish(); return; }

        for (CodeEntry e : topic.getEntries()) {
            if (e.getId().equals(entryId)) {
                entry = e;
                break;
            }
        }
        if (entry == null) { finish(); return; }

        ((TextView) findViewById(R.id.tv_manage_entry_header)).setText(entry.getTitle());
        ((TextView) findViewById(R.id.tv_entry_main_title)).setText(entry.getTitle());
        ((TextView) findViewById(R.id.tv_entry_full_desc)).setText(entry.getDescription());

        renderCodeBlocks();
    }

    private void renderCodeBlocks() {
        containerCodeBlocks.removeAllViews();
        for (CodeBlock block : entry.getBlocks()) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setBackgroundResource(R.drawable.bg_code_card);
            box.setPadding(16, 14, 16, 14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 14);
            box.setLayoutParams(lp);

            LinearLayout bar = new LinearLayout(this);
            bar.setOrientation(LinearLayout.HORIZONTAL);
            bar.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvLang = new TextView(this);
            tvLang.setText(block.getLanguage());
            tvLang.setTextColor(Color.parseColor("#38BDF8"));
            tvLang.setTextSize(11);
            tvLang.setBackgroundResource(R.drawable.bg_badge);
            tvLang.setPadding(8, 2, 8, 2);
            tvLang.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView btnCopy = new TextView(this);
            btnCopy.setText("📋");
            btnCopy.setTextSize(20);
            btnCopy.setPadding(8, 4, 8, 4);
            btnCopy.setOnClickListener(v -> copyToClipboard(block.getCode()));

            bar.addView(tvLang);
            bar.addView(btnCopy);
            box.addView(bar);

            TextView tvCode = new TextView(this);
            tvCode.setText(block.getCode());
            tvCode.setTextColor(Color.parseColor("#A7F3D0"));
            tvCode.setTextSize(13);
            tvCode.setTypeface(Typeface.MONOSPACE);
            tvCode.setPadding(0, 10, 0, 0);
            box.addView(tvCode);

            containerCodeBlocks.addView(box);
        }
    }

    private void copyToClipboard(String code) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Benbook Code Snippet", code));
        Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}
