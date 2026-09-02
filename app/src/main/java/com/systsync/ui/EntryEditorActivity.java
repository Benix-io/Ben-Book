package com.systsync.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.systsync.R;
import com.systsync.data.CodeBlock;
import com.systsync.data.CodeEntry;
import com.systsync.data.DataManager;
import com.systsync.data.Topic;
import com.systsync.theme.ThemeManager;
import java.util.ArrayList;
import java.util.List;

public class EntryEditorActivity extends Activity {
    private String topicId;
    private String entryId;
    private Topic topic;
    private CodeEntry existingEntry;
    private EditText etTitle, etDesc;
    private LinearLayout containerBlocks;
    private final String[] languages = new String[]{"BASH", "PYTHON", "JAVA", "JAVASCRIPT", "KOTLIN", "YAML", "JSON", "SQL", "C/C++"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_editor);

        topicId = getIntent().getStringExtra("topic_id");
        entryId = getIntent().getStringExtra("entry_id");
        topic = DataManager.getInstance(this).findTopicById(topicId);

        if (topic == null) { finish(); return; }

        findViewById(R.id.btn_back_editor).setOnClickListener(v -> finish());
        etTitle = findViewById(R.id.et_edit_title);
        etDesc = findViewById(R.id.et_edit_desc);
        containerBlocks = findViewById(R.id.container_editable_code_blocks);

        if (entryId != null) {
            for (CodeEntry e : topic.getEntries()) {
                if (e.getId().equals(entryId)) {
                    existingEntry = e;
                    break;
                }
            }
        }

        if (existingEntry != null) {
            ((TextView) findViewById(R.id.tv_editor_header_title)).setText("Edit Code Entry");
            etTitle.setText(existingEntry.getTitle());
            etDesc.setText(existingEntry.getDescription());
            for (CodeBlock b : existingEntry.getBlocks()) {
                addCodeBlockCard(b.getCode(), b.getLanguage());
            }
        } else {
            ((TextView) findViewById(R.id.tv_editor_header_title)).setText("New Code Entry");
            addCodeBlockCard("", "BASH");
        }

        findViewById(R.id.btn_add_block_card).setOnClickListener(v -> addCodeBlockCard("", "BASH"));
        findViewById(R.id.btn_save_entry_changes).setOnClickListener(v -> saveEntry());
    }

    private void addCodeBlockCard(String initialCode, String initialLang) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_rounded);
        card.setPadding(16, 14, 16, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        // Top bar (Language spinner + Delete card button)
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        Spinner spinner = new Spinner(this);
        spinner.setBackgroundResource(R.drawable.bg_outlined_box);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        spinner.setAdapter(adapter);
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equalsIgnoreCase(initialLang)) {
                spinner.setSelection(i);
                break;
            }
        }
        spinner.setLayoutParams(new LinearLayout.LayoutParams(0, 80, 1f));

        TextView btnDel = new TextView(this);
        btnDel.setText("✕");
        btnDel.setTextColor(Color.parseColor("#EF4444"));
        btnDel.setTextSize(18);
        btnDel.setPadding(16, 8, 8, 8);
        btnDel.setOnClickListener(v -> containerBlocks.removeView(card));

        top.addView(spinner);
        top.addView(btnDel);
        card.addView(top);

        EditText etCode = new EditText(this);
        etCode.setHint("Code Block / Command Script...");
        etCode.setText(initialCode);
        etCode.setTextColor(Color.parseColor("#A7F3D0"));
        etCode.setHintTextColor(Color.parseColor("#475569"));
        etCode.setBackgroundResource(R.drawable.bg_code_card);
        etCode.setTypeface(android.graphics.Typeface.MONOSPACE);
        etCode.setTextSize(13);
        etCode.setGravity(Gravity.TOP | Gravity.START);
        etCode.setMinLines(3);
        etCode.setPadding(14, 12, 14, 12);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.setMargins(0, 8, 0, 0);
        etCode.setLayoutParams(clp);
        card.addView(etCode);

        containerBlocks.addView(card);
    }

    private void saveEntry() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CodeBlock> blocks = new ArrayList<>();
        for (int i = 0; i < containerBlocks.getChildCount(); i++) {
            LinearLayout card = (LinearLayout) containerBlocks.getChildAt(i);
            LinearLayout top = (LinearLayout) card.getChildAt(0);
            Spinner sp = (Spinner) top.getChildAt(0);
            EditText et = (EditText) card.getChildAt(1);

            String code = et.getText().toString();
            if (!code.trim().isEmpty()) {
                blocks.add(new CodeBlock(code, sp.getSelectedItem().toString()));
            }
        }

        if (blocks.isEmpty()) {
            Toast.makeText(this, "Please enter at least one code snippet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingEntry == null) {
            CodeEntry newEntry = new CodeEntry(title, etDesc.getText().toString().trim());
            newEntry.setBlocks(blocks);
            topic.addEntry(newEntry);
        } else {
            existingEntry.setTitle(title);
            existingEntry.setDescription(etDesc.getText().toString().trim());
            existingEntry.setBlocks(blocks);
        }

        DataManager.getInstance(this).saveToPrefs();
        finish();
    }
}
