package com.systsync.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.systsync.R;
import com.systsync.data.CodeBlock;
import com.systsync.data.CodeEntry;
import com.systsync.data.DataManager;
import com.systsync.data.Topic;
import com.systsync.theme.ThemeManager;

public class TopicDetailActivity extends Activity {
    private String topicId;
    private Topic topic;
    private LinearLayout containerTocChips;
    private LinearLayout containerAccordion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        topicId = getIntent().getStringExtra("topic_id");
        topic = DataManager.getInstance(this).findTopicById(topicId);

        if (topic == null) {
            finish();
            return;
        }

        findViewById(R.id.btn_back_topic).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tv_detail_topic_title)).setText(topic.getTitle());
        ((TextView) findViewById(R.id.tv_detail_topic_desc)).setText(topic.getDescription());

        containerTocChips = findViewById(R.id.container_toc_chips);
        containerAccordion = findViewById(R.id.container_accordion_entries);

        findViewById(R.id.fab_add_entry).setOnClickListener(v -> {
            Intent i = new Intent(this, EntryEditorActivity.class);
            i.putExtra("topic_id", topic.getId());
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        topic = DataManager.getInstance(this).findTopicById(topicId);
        renderAccordion();
    }

    private void renderAccordion() {
        containerTocChips.removeAllViews();
        containerAccordion.removeAllViews();
        int cardBg = ThemeManager.getCardColor(this);

        if (topic.getEntries().isEmpty()) {
            findViewById(R.id.card_toc).setVisibility(View.GONE);
            TextView empty = new TextView(this);
            empty.setText("No entries in this topic yet.\nTap + to save a command or code snippet.");
            empty.setTextColor(Color.parseColor("#64748B"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 80, 0, 0);
            containerAccordion.addView(empty);
            return;
        }

        findViewById(R.id.card_toc).setVisibility(View.VISIBLE);

        for (int i = 0; i < topic.getEntries().size(); i++) {
            CodeEntry entry = topic.getEntries().get(i);

            TextView chip = new TextView(this);
            chip.setText(entry.getTitle());
            chip.setTextColor(Color.parseColor("#E2E8F0"));
            chip.setTextSize(12);
            chip.setBackgroundResource(R.drawable.bg_chip);
            chip.setPadding(20, 8, 20, 8);
            LinearLayout.LayoutParams chlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chlp.setMargins(0, 0, 12, 0);
            chip.setLayoutParams(chlp);
            containerTocChips.addView(chip);

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setBackgroundResource(R.drawable.bg_card_rounded);
            item.getBackground().setTint(cardBg);
            item.setPadding(20, 16, 20, 16);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ilp.setMargins(0, 0, 0, 16);
            item.setLayoutParams(ilp);

            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(entry.getTitle());
            tvTitle.setTextColor(Color.parseColor("#38BDF8"));
            tvTitle.setTextSize(16);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            ImageView ivExpandIcon = new ImageView(this);
            ivExpandIcon.setImageResource(R.drawable.ic_expand);
            ivExpandIcon.setLayoutParams(new LinearLayout.LayoutParams(48, 48));

            header.addView(tvTitle);
            header.addView(ivExpandIcon);
            item.addView(header);

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setVisibility(View.GONE);

            if (!entry.getDescription().isEmpty()) {
                TextView tvDesc = new TextView(this);
                tvDesc.setText(entry.getDescription());
                tvDesc.setTextColor(Color.parseColor("#CBD5E1"));
                tvDesc.setTextSize(13);
                tvDesc.setPadding(0, 10, 0, 10);
                body.addView(tvDesc);
            }

            for (CodeBlock block : entry.getBlocks()) {
                LinearLayout codeBox = new LinearLayout(this);
                codeBox.setOrientation(LinearLayout.VERTICAL);
                codeBox.setBackgroundResource(R.drawable.bg_code_card);
                codeBox.setPadding(16, 12, 16, 12);
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                clp.setMargins(0, 8, 0, 0);
                codeBox.setLayoutParams(clp);

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
                btnCopy.setTextSize(18);
                btnCopy.setPadding(8, 4, 8, 4);
                btnCopy.setOnClickListener(copyClick -> copyToClipboard(block.getCode()));

                bar.addView(tvLang);
                bar.addView(btnCopy);
                codeBox.addView(bar);

                TextView tvCode = new TextView(this);
                tvCode.setText(block.getCode());
                tvCode.setTextColor(Color.parseColor("#A7F3D0"));
                tvCode.setTextSize(13);
                tvCode.setTypeface(Typeface.MONOSPACE);
                tvCode.setPadding(0, 8, 0, 0);
                codeBox.addView(tvCode);

                body.addView(codeBox);
            }

            TextView btnManage = new TextView(this);
            btnManage.setText("Manage Detail");
            btnManage.setTextColor(Color.parseColor("#38BDF8"));
            btnManage.setTextSize(12);
            btnManage.setGravity(Gravity.END);
            btnManage.setPadding(0, 14, 0, 4);
            btnManage.setOnClickListener(v -> {
                Intent mi = new Intent(this, EntryDetailActivity.class);
                mi.putExtra("topic_id", topic.getId());
                mi.putExtra("entry_id", entry.getId());
                startActivity(mi);
            });
            body.addView(btnManage);

            item.addView(body);

            header.setOnClickListener(v -> {
                boolean isOpen = body.getVisibility() == View.VISIBLE;
                body.setVisibility(isOpen ? View.GONE : View.VISIBLE);
                ivExpandIcon.setImageResource(isOpen ? R.drawable.ic_expand : R.drawable.ic_collapse);
            });

            chip.setOnClickListener(v -> {
                body.setVisibility(View.VISIBLE);
                ivExpandIcon.setImageResource(R.drawable.ic_collapse);
            });

            containerAccordion.addView(item);
        }
    }

    private void copyToClipboard(String code) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Benbook Code Snippet", code));
        Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}
