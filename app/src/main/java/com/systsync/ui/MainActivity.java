package com.systsync.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.systsync.R;
import com.systsync.bubble.BubbleHelper;
import com.systsync.data.CodeBlock;
import com.systsync.data.CodeEntry;
import com.systsync.data.DataManager;
import com.systsync.data.Topic;
import com.systsync.theme.ThemeManager;
import java.util.List;

public class MainActivity extends Activity {

    private DataManager dataManager;
    private LinearLayout containerContent;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = DataManager.getInstance(this);
        containerContent = findViewById(R.id.container_content);
        etSearch = findViewById(R.id.et_search);

        Button btnBubble = findViewById(R.id.btn_bubble_toggle);
        Button btnSettings = findViewById(R.id.btn_settings);
        Button btnAdd = findViewById(R.id.btn_add_action);

        btnBubble.setOnClickListener(v -> BubbleHelper.displayBubble(this));
        btnSettings.setOnClickListener(v -> SettingsDialog.show(this, this::renderUI));
        btnAdd.setOnClickListener(v -> showAddTopicDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContent(s.toString().trim().toLowerCase());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        renderUI();
    }

    private void renderUI() {
        containerContent.removeAllViews();
        List<Topic> topics = dataManager.getTopics();

        if (topics.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No topics found. Tap '+ Add Topic / Entry' below.");
            emptyView.setTextColor(Color.GRAY);
            emptyView.setPadding(0, 40, 0, 0);
            containerContent.addView(emptyView);
            return;
        }

        for (Topic topic : topics) {
            renderTopicCard(topic);
        }
    }

    private void renderTopicCard(Topic topic) {
        LinearLayout topicLayout = new LinearLayout(this);
        topicLayout.setOrientation(LinearLayout.VERTICAL);
        topicLayout.setPadding(0, 16, 0, 16);

        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("📂 " + topic.getName());
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(Color.parseColor("#FFB74D"));
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));

        Button btnAddEntry = new Button(this);
        btnAddEntry.setText("+ Entry");
        btnAddEntry.setTextSize(10);
        btnAddEntry.setOnClickListener(v -> showAddEntryDialog(topic));

        Button btnDeleteTopic = new Button(this);
        btnDeleteTopic.setText("Del");
        btnDeleteTopic.setTextSize(10);
        btnDeleteTopic.setOnClickListener(v -> {
            dataManager.getTopics().remove(topic);
            dataManager.saveLocalData();
            renderUI();
        });

        headerLayout.addView(tvTitle);
        headerLayout.addView(btnAddEntry);
        headerLayout.addView(btnDeleteTopic);
        topicLayout.addView(headerLayout);

        for (CodeEntry entry : topic.getEntries()) {
            renderEntryCard(topicLayout, topic, entry);
        }

        containerContent.addView(topicLayout);
    }

    private void renderEntryCard(LinearLayout parent, Topic topic, CodeEntry entry) {
        TextView entryTitle = new TextView(this);
        entryTitle.setText(" • " + entry.getTitle());
        entryTitle.setTextSize(15);
        entryTitle.setTextColor(Color.WHITE);
        entryTitle.setPadding(12, 10, 0, 6);
        parent.addView(entryTitle);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CodeBlock block : entry.getCodeBlocks()) {
            View blockView = inflater.inflate(R.layout.item_code_block, parent, false);

            TextView tvDesc = blockView.findViewById(R.id.tv_block_desc);
            TextView tvCode = blockView.findViewById(R.id.tv_code_content);
            Button btnCopy = blockView.findViewById(R.id.btn_copy_code);
            Button btnDel = blockView.findViewById(R.id.btn_delete_code);

            tvDesc.setText(block.getDescription().isEmpty() ? "Snippet" : block.getDescription());
            tvCode.setText(SyntaxHighlighter.highlight(block.getCode()));

            btnCopy.setOnClickListener(v -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Code Block", block.getCode());
                cm.setPrimaryClip(clip);
                Toast.makeText(this, "Copied code to clipboard!", Toast.LENGTH_SHORT).show();
            });

            btnDel.setOnClickListener(v -> {
                entry.getCodeBlocks().remove(block);
                dataManager.saveLocalData();
                renderUI();
            });

            parent.addView(blockView);
        }
    }

    private void showAddTopicDialog() {
        EditText input = new EditText(this);
        input.setHint("Topic name (e.g. Android Java, SQL)");
        new AlertDialog.Builder(this)
            .setTitle("Create New Topic")
            .setView(input)
            .setPositiveButton("Create", (d, w) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    Topic t = new Topic(String.valueOf(System.currentTimeMillis()), name);
                    dataManager.getTopics().add(t);
                    dataManager.saveLocalData();
                    renderUI();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAddEntryDialog(Topic topic) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 10);

        EditText etTitle = new EditText(this);
        etTitle.setHint("Entry Title (e.g. RecyclerView Adapter)");
        EditText etCode = new EditText(this);
        etCode.setHint("Code snippet body...");

        layout.addView(etTitle);
        layout.addView(etCode);

        new AlertDialog.Builder(this)
            .setTitle("Add Code to " + topic.getName())
            .setView(layout)
            .setPositiveButton("Save", (d, w) -> {
                String title = etTitle.getText().toString().trim();
                String code = etCode.getText().toString().trim();
                if (!title.isEmpty() && !code.isEmpty()) {
                    CodeEntry entry = new CodeEntry(String.valueOf(System.currentTimeMillis()), title);
                    entry.getCodeBlocks().add(new CodeBlock(String.valueOf(System.currentTimeMillis()), title, code, "java"));
                    topic.getEntries().add(entry);
                    dataManager.saveLocalData();
                    renderUI();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void filterContent(String query) {
        if (query.isEmpty()) {
            renderUI();
            return;
        }
        containerContent.removeAllViews();
        for (Topic t : dataManager.getTopics()) {
            if (t.getName().toLowerCase().contains(query)) {
                renderTopicCard(t);
            }
        }
    }
}
