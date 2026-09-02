package com.systsync.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.systsync.R;
import com.systsync.service.AppService;

public class OnboardingActivity extends Activity {
    private static final String PREFS_NAME = "benbook_prefs";
    private static final String KEY_FIRST = "first_launch_done";

    private ImageView ivLogo;
    private TextView tvTitle, tvDesc;
    private TextView dot1, dot2, dot3;
    private Button btnNext;
    private View bottomControls;
    private int currentStep = 0;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean launched = prefs.getBoolean(KEY_FIRST, false);

        ivLogo = findViewById(R.id.iv_onboard_logo);
        tvTitle = findViewById(R.id.tv_onboard_title);
        tvDesc = findViewById(R.id.tv_onboard_desc);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        btnNext = findViewById(R.id.btn_onboard_next);
        bottomControls = findViewById(R.id.bottom_controls_bar);

        ivLogo.setScaleX(0.2f);
        ivLogo.setScaleY(0.2f);
        ivLogo.setAlpha(0f);
        ivLogo.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(500).setInterpolator(new OvershootInterpolator()).start();

        if (launched) {
            // Returning user: show animated splash only
            bottomControls.setVisibility(View.GONE);
            tvTitle.setText("Benbook");
            tvDesc.setText("Loading synced vaults...");
            AppService.start(this);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ivLogo.animate().scaleX(0.8f).scaleY(0.8f).alpha(0f).setDuration(250).withEndAction(() -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }).start();
            }, 750);
            return;
        }

        updateStep(0);

        btnNext.setOnClickListener(v -> {
            if (currentStep < 2) updateStep(currentStep + 1);
            else finishOnboarding();
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
                if (e1 == null || e2 == null) return false;
                if (e1.getX() - e2.getX() > 80 && currentStep < 2) {
                    updateStep(currentStep + 1);
                    return true;
                } else if (e2.getX() - e1.getX() > 80 && currentStep > 0) {
                    updateStep(currentStep - 1);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector != null && gestureDetector.onTouchEvent(event)) return true;
        return super.onTouchEvent(event);
    }

    private void updateStep(int step) {
        currentStep = step;
        int active = Color.parseColor("#38BDF8");
        int inactive = Color.parseColor("#334155");

        dot1.setTextColor(step == 0 ? active : inactive);
        dot2.setTextColor(step == 1 ? active : inactive);
        dot3.setTextColor(step == 2 ? active : inactive);

        switch (step) {
            case 0:
                tvTitle.setText("⚡ Welcome to Benbook");
                tvDesc.setText("Lightning-fast native code organizer and developer snippet notebook.");
                btnNext.setText("Next →");
                break;
            case 1:
                tvTitle.setText("🚀 Seamless .sync Engine");
                tvDesc.setText("Import, export, and transfer your snippet vaults anytime with single-tap JSON sync.");
                btnNext.setText("Next →");
                break;
            case 2:
                tvTitle.setText("🫧 Floating Bubble Capture");
                tvDesc.setText("Save terminal commands and notes over any running app in real-time.");
                btnNext.setText("Finish 🚀");
                break;
        }
    }

    private void finishOnboarding() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_FIRST, true).apply();
        AppService.start(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
