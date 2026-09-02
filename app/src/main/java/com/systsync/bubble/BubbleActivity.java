package com.systsync.bubble;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.systsync.R;
import com.systsync.data.CodeBlock;
import com.systsync.data.CodeEntry;
import com.systsync.data.DataManager;
import com.systsync.data.Topic;
import java.util.List;

public class BubbleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble);

        DataManager dm = DataManager.getInstance(this);
        List<Topic> topics = dm.getTopics();

        Spinner spTopic = findViewById(R.id.spinner_bubble_topic);
        Spinner spLang = findViewById(R.id.spinner_bubble_lang);
        EditText etTitle = findViewById(R.id.et_bubble_title);
        EditText etCode = findViewById(R.id.et_bubble_code);
        Button btnSave = findViewById(R.id.btn_bubble_save);
        Button btnClose = findViewById(R.id.btn_bubble_close);

        String[] names = new String[Math.max(1, topics.size())];
        if (topics.isEmpty()) {
            Topic t = new Topic("General", "Default notes");
            dm.addTopic(t);
            topics = dm.getTopics();
        }
        for (int i = 0; i < topics.size(); i++) {
            names[i] = topics.get(i).getTitle();
        }

        ArrayAdapter<String> tAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spTopic.setAdapter(tAdapter);

        String[] langs = new String[]{"BASH", "PYTHON", "JAVA", "JAVASCRIPT", "KOTLIN", "YAML", "JSON", "SQL"};
        ArrayAdapter<String> lAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, langs);
        spLang.setAdapter(lAdapter);

        List<Topic> finalTopics = topics;
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String code = etCode.getText().toString().trim();
            if (title.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Title & Code required", Toast.LENGTH_SHORT).show();
                return;
            }

            int idx = spTopic.getSelectedItemPosition();
            Topic target = (idx >= 0 && idx < finalTopics.size()) ? finalTopics.get(idx) : finalTopics.get(0);

            CodeEntry entry = new CodeEntry(title, "Quick captured via floating companion");
            entry.addBlock(new CodeBlock(code, spLang.getSelectedItem().toString()));
            target.addEntry(entry);
            dm.saveToPrefs();

            Toast.makeText(this, "⚡ Snippet saved to " + target.getTitle(), Toast.LENGTH_SHORT).show();
            finish();
        });

        btnClose.setOnClickListener(v -> finish());
    }
}
