package com.systsync.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.systsync.R;
import com.systsync.bubble.BubbleActivity;
import com.systsync.data.DataManager;
import com.systsync.data.Topic;
import com.systsync.service.AppService;
import com.systsync.theme.ThemeManager;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends Activity {
    private static final int REQ_EXPORT = 101;
    private static final int REQ_IMPORT = 102;
    private static final int REQ_NOTIF = 201;

    private DataManager dataManager;
    private LinearLayout containerTopics;
    private EditText etSearch;
    private View drawerDim;
    private LinearLayout drawerCard;
    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = DataManager.getInstance(this);
        rootLayout = findViewById(R.id.main_root_layout);
        rootLayout.setBackgroundColor(ThemeManager.getBackgroundColor(this));

        containerTopics = findViewById(R.id.container_topic_cards);
        etSearch = findViewById(R.id.et_search_topics);
        drawerDim = findViewById(R.id.drawer_dim);
        drawerCard = findViewById(R.id.left_drawer_card);

        checkPermissions();

        findViewById(R.id.btn_open_drawer).setOnClickListener(v -> openDrawer());
        findViewById(R.id.btn_close_drawer).setOnClickListener(v -> closeDrawer());
        drawerDim.setOnClickListener(v -> closeDrawer());

        findViewById(R.id.btn_trigger_bubble).setOnClickListener(v -> 
            startActivity(new Intent(this, BubbleActivity.class))
        );

        findViewById(R.id.fab_add_topic).setOnClickListener(v -> showCreateTopicDialog(null));

        setupDrawerButtons();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rootLayout.setBackgroundColor(ThemeManager.getBackgroundColor(this));
        renderTopics(etSearch.getText().toString().trim());
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
            } else {
                AppService.start(this);
            }
        } else {
            AppService.start(this);
        }
    }

    private void openDrawer() {
        drawerDim.setVisibility(View.VISIBLE);
        drawerCard.setVisibility(View.VISIBLE);
        TranslateAnimation anim = new TranslateAnimation(-drawerCard.getWidth(), 0, 0, 0);
        anim.setDuration(220);
        drawerCard.startAnimation(anim);
    }

    private void closeDrawer() {
        drawerDim.setVisibility(View.GONE);
        drawerCard.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (drawerCard.getVisibility() == View.VISIBLE) {
            closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    private void setupDrawerButtons() {
        findViewById(R.id.drawer_btn_export).setOnClickListener(v -> {
            closeDrawer();
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            i.putExtra(Intent.EXTRA_TITLE, "benbook_vault.sync");
            startActivityForResult(i, REQ_EXPORT);
        });

        findViewById(R.id.drawer_btn_import).setOnClickListener(v -> {
            closeDrawer();
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, REQ_IMPORT);
        });

        findViewById(R.id.drawer_btn_about).setOnClickListener(v -> {
            closeDrawer();
            showAboutDialog();
        });

        findViewById(R.id.btn_theme_dark).setOnClickListener(v -> changeTheme(ThemeManager.THEME_DARK));
        findViewById(R.id.btn_theme_blue).setOnClickListener(v -> changeTheme(ThemeManager.THEME_BLUE));
        findViewById(R.id.btn_theme_green).setOnClickListener(v -> changeTheme(ThemeManager.THEME_GREEN));
    }

    private void changeTheme(String themeKey) {
        ThemeManager.setTheme(this, themeKey);
        closeDrawer();
        recreate();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("⚡ Benbook")
            .setMessage("Native Android Snippet Manager & Floating Code Companion.\n\n• Developer: Ben (@Benix-io)\n• Architecture: Pure Native Java (API 34–36)\n• Features: Multi-Card Snippets, .sync Vault Backup & Restore.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderTopics(s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void renderTopics(String query) {
        containerTopics.removeAllViews();
        List<Topic> list = dataManager.getTopics();
        int cardBg = ThemeManager.getCardColor(this);

        for (Topic t : list) {
            if (!query.isEmpty() && !t.getTitle().toLowerCase().contains(query.toLowerCase()) && !t.getDescription().toLowerCase().contains(query.toLowerCase())) {
                continue;
            }

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card_rounded);
            card.getBackground().setTint(cardBg);
            card.setPadding(24, 20, 24, 20);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(lp);

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(t.getTitle());
            tvTitle.setTextColor(Color.parseColor("#38BDF8"));
            tvTitle.setTextSize(17);
            tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView btnDel = new TextView(this);
            btnDel.setText("🗑");
            btnDel.setTextSize(16);
            btnDel.setOnClickListener(v -> {
                dataManager.deleteTopic(t);
                renderTopics(etSearch.getText().toString().trim());
            });

            top.addView(tvTitle);
            top.addView(btnDel);
            card.addView(top);

            if (!t.getDescription().isEmpty()) {
                TextView tvDesc = new TextView(this);
                tvDesc.setText(t.getDescription());
                tvDesc.setTextColor(Color.parseColor("#94A3B8"));
                tvDesc.setTextSize(13);
                tvDesc.setPadding(0, 4, 0, 8);
                card.addView(tvDesc);
            }

            LinearLayout bottom = new LinearLayout(this);
            bottom.setOrientation(LinearLayout.HORIZONTAL);
            bottom.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvDate = new TextView(this);
            tvDate.setText("Created: " + t.getCreatedAt());
            tvDate.setTextColor(Color.parseColor("#64748B"));
            tvDate.setTextSize(11);
            tvDate.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView badgeEdit = new TextView(this);
            badgeEdit.setText("Hold to Edit");
            badgeEdit.setTextSize(10);
            badgeEdit.setTextColor(Color.parseColor("#CBD5E1"));
            badgeEdit.setBackgroundResource(R.drawable.bg_badge);
            badgeEdit.setPadding(12, 4, 12, 4);

            bottom.addView(tvDate);
            bottom.addView(badgeEdit);
            card.addView(bottom);

            card.setOnClickListener(v -> {
                Intent i = new Intent(this, TopicDetailActivity.class);
                i.putExtra("topic_id", t.getId());
                startActivity(i);
            });

            card.setOnLongClickListener(v -> {
                showCreateTopicDialog(t);
                return true;
            });

            containerTopics.addView(card);
        }
    }

    private void showCreateTopicDialog(Topic editTopic) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(40, 24, 40, 12);

        final EditText etT = new EditText(this);
        etT.setHint("Topic Title (e.g. Termux, Docker, Kali)");
        etT.setBackgroundResource(R.drawable.bg_outlined_box);
        etT.setPadding(16, 12, 16, 12);
        etT.setTextColor(Color.WHITE);
        if (editTopic != null) etT.setText(editTopic.getTitle());

        final EditText etD = new EditText(this);
        etD.setHint("Description (Optional)");
        etD.setBackgroundResource(R.drawable.bg_outlined_box);
        etD.setPadding(16, 12, 16, 12);
        etD.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.setMargins(0, 14, 0, 0);
        etD.setLayoutParams(dlp);
        if (editTopic != null) etD.setText(editTopic.getDescription());

        l.addView(etT);
        l.addView(etD);

        b.setTitle(editTopic == null ? "Create New Topic" : "Edit Topic");
        b.setView(l);
        b.setPositiveButton(editTopic == null ? "Create" : "Save", (d, w) -> {
            String title = etT.getText().toString().trim();
            if (!title.isEmpty()) {
                if (editTopic == null) {
                    dataManager.addTopic(new Topic(title, etD.getText().toString().trim()));
                } else {
                    editTopic.setTitle(title);
                    editTopic.setDescription(etD.getText().toString().trim());
                    dataManager.saveToPrefs();
                }
                renderTopics("");
            }
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();

        if (requestCode == REQ_EXPORT) {
            try {
                OutputStream os = getContentResolver().openOutputStream(uri);
                if (dataManager.exportToStream(os)) {
                    Toast.makeText(this, "✅ .sync vault exported successfully!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Export Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQ_IMPORT) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                if (dataManager.importFromStream(is)) {
                    Toast.makeText(this, "✅ .sync vault restored successfully!", Toast.LENGTH_LONG).show();
                    renderTopics("");
                } else {
                    Toast.makeText(this, "❌ Invalid .sync file format", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Import Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
