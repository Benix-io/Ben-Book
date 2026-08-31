package com.systsync.bubble;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.systsync.R;
import com.systsync.data.CodeBlock;
import com.systsync.data.CodeEntry;
import com.systsync.data.DataManager;
import com.systsync.data.Topic;
import java.util.List;

public class BubbleActivity extends Activity {

    private EditText etDesc;
    private EditText etCode;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble);

        etDesc = findViewById(R.id.bubble_et_desc);
        etCode = findViewById(R.id.bubble_et_code);
        btnSave = findViewById(R.id.bubble_btn_save);

        btnSave.setOnClickListener(v -> saveSnippetAndClose());
    }

    private void saveSnippetAndClose() {
        String code = etCode.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(this, "Code content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        DataManager dm = DataManager.getInstance(this);
        List<Topic> topics = dm.getTopics();

        Topic quickTopic = null;
        for (Topic t : topics) {
            if ("Quick Notes".equalsIgnoreCase(t.getName())) {
                quickTopic = t;
                break;
            }
        }

        if (quickTopic == null) {
            quickTopic = new Topic(String.valueOf(System.currentTimeMillis()), "Quick Notes");
            topics.add(quickTopic);
        }

        CodeEntry entry = new CodeEntry(String.valueOf(System.currentTimeMillis()), 
            desc.isEmpty() ? "Snippet " + (quickTopic.getEntries().size() + 1) : desc);
        
        CodeBlock block = new CodeBlock(
            String.valueOf(System.currentTimeMillis()),
            desc,
            code,
            "plaintext"
        );
        entry.getCodeBlocks().add(block);
        quickTopic.getEntries().add(entry);

        dm.saveLocalData();
        Toast.makeText(this, "Saved to Quick Notes!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
