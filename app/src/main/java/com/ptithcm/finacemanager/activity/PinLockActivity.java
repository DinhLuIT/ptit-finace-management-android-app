package com.ptithcm.finacemanager.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.utils.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PinLockActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 4;

    private TextView tvTitle, tvMessage, tvError;
    private View[] dots;
    private StringBuilder currentPin = new StringBuilder();

    private String mode; // "create", "confirm", "verify"
    private String firstPin; // Lưu PIN lần nhập đầu tiên khi tạo mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        initViews();

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "verify";

        updateUI();
        initListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_pin_title);
        tvMessage = findViewById(R.id.tv_pin_message);
        tvError = findViewById(R.id.tv_pin_error);

        dots = new View[]{
                findViewById(R.id.dot_1),
                findViewById(R.id.dot_2),
                findViewById(R.id.dot_3),
                findViewById(R.id.dot_4)
        };
    }

    private void initListeners() {
        int[] buttonIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(v -> {
                com.google.android.material.button.MaterialButton btn =
                        (com.google.android.material.button.MaterialButton) v;
                onNumberPressed(btn.getText().toString());
            });
        }

        findViewById(R.id.btn_backspace).setOnClickListener(v -> onBackspacePressed());
    }

    private void updateUI() {
        tvError.setVisibility(View.GONE);
        switch (mode) {
            case "create":
                tvTitle.setText(R.string.title_create_pin);
                tvMessage.setText(R.string.msg_create_pin);
                break;
            case "confirm":
                tvTitle.setText(R.string.title_confirm_pin);
                tvMessage.setText(R.string.msg_confirm_pin);
                break;
            case "verify":
                tvTitle.setText(R.string.title_enter_pin);
                tvMessage.setText(R.string.msg_enter_pin);
                break;
        }
    }

    private void onNumberPressed(String number) {
        if (currentPin.length() >= PIN_LENGTH) return;

        currentPin.append(number);
        updateDots();

        if (currentPin.length() == PIN_LENGTH) {
            handlePinComplete();
        }
    }

    private void onBackspacePressed() {
        if (currentPin.length() > 0) {
            currentPin.deleteCharAt(currentPin.length() - 1);
            updateDots();
        }
        tvError.setVisibility(View.GONE);
    }

    private void updateDots() {
        for (int i = 0; i < PIN_LENGTH; i++) {
            if (i < currentPin.length()) {
                dots[i].setBackgroundResource(R.drawable.bg_pin_dot_filled);
            } else {
                dots[i].setBackgroundResource(R.drawable.bg_pin_dot_empty);
            }
        }
    }

    private void handlePinComplete() {
        String pin = currentPin.toString();

        switch (mode) {
            case "create":
                // Lưu PIN lần đầu và chuyển sang confirm
                firstPin = pin;
                mode = "confirm";
                currentPin.setLength(0);
                updateDots();
                updateUI();
                break;

            case "confirm":
                if (pin.equals(firstPin)) {
                    // PIN khớp → lưu và vào app
                    savePin(pin);
                    Toast.makeText(this, R.string.msg_pin_created, Toast.LENGTH_SHORT).show();
                    goToMain();
                } else {
                    // PIN không khớp → quay lại tạo
                    tvError.setText(R.string.error_pin_mismatch);
                    tvError.setVisibility(View.VISIBLE);
                    mode = "create";
                    firstPin = null;
                    currentPin.setLength(0);
                    updateDots();
                    updateUI();
                }
                break;

            case "verify":
                if (verifyPin(pin)) {
                    goToMain();
                } else {
                    tvError.setText(R.string.error_pin_wrong);
                    tvError.setVisibility(View.VISIBLE);
                    currentPin.setLength(0);
                    updateDots();
                    // Animation lắc để báo sai
                    shakeAnimation();
                }
                break;
        }
    }

    private void savePin(String pin) {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(Constants.PREF_PIN_HASH, hashPin(pin))
                .putBoolean(Constants.PREF_IS_PIN_SET, true)
                .apply();
    }

    private boolean verifyPin(String pin) {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        String savedHash = prefs.getString(Constants.PREF_PIN_HASH, "");
        return savedHash.equals(hashPin(pin));
    }

    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return pin; // Fallback nếu SHA-256 không có
        }
    }

    private void shakeAnimation() {
        View dotsLayout = findViewById(R.id.ll_pin_dots);
        android.view.animation.TranslateAnimation shake =
                new android.view.animation.TranslateAnimation(-10, 10, 0, 0);
        shake.setDuration(50);
        shake.setRepeatCount(5);
        shake.setRepeatMode(android.view.animation.Animation.REVERSE);
        dotsLayout.startAnimation(shake);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Không cho back khi ở verify mode
        if ("verify".equals(mode)) {
            finishAffinity();
        } else {
            super.onBackPressed();
        }
    }
}
