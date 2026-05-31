package com.ptithcm.finacemanager.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.utils.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

/**
 * Màn hình nhập mã PIN bảo mật.
 *
 * <p>Hỗ trợ 3 chế độ:
 * <ul>
 *     <li>{@code create} – Tạo mã PIN mới lần đầu</li>
 *     <li>{@code confirm} – Xác nhận mã PIN vừa tạo</li>
 *     <li>{@code verify} – Xác thực để vào app (hỗ trợ Biometric nếu đã bật)</li>
 * </ul>
 */
public class PinLockActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 4;

    private TextView tvTitle, tvMessage, tvError;
    private View[] dots;
    private ImageView ivFingerprint;
    private StringBuilder currentPin = new StringBuilder();

    private String mode; // "create", "confirm", "verify"
    private String firstPin; // Lưu PIN lần nhập đầu tiên khi tạo mới

    // Biometric
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private boolean isBiometricAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        initViews();

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "verify";

        updateUI();
        initListeners();

        // Khởi tạo Biometric nếu đang ở chế độ verify
        if ("verify".equals(mode)) {
            setupBiometric();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_pin_title);
        tvMessage = findViewById(R.id.tv_pin_message);
        tvError = findViewById(R.id.tv_pin_error);
        ivFingerprint = findViewById(R.id.iv_fingerprint);

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

        // Bấm icon vân tay để gọi lại BiometricPrompt
        ivFingerprint.setOnClickListener(v -> showBiometricPrompt());
    }

    /**
     * Thiết lập BiometricPrompt:
     * 1. Kiểm tra phần cứng hỗ trợ (BiometricManager).
     * 2. Kiểm tra người dùng đã bật trong Settings (SharedPreferences).
     * 3. Nếu cả 2 điều kiện đạt → hiện icon vân tay + tự động popup quét.
     */
    private void setupBiometric() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        boolean biometricEnabled = prefs.getBoolean(Constants.PREF_BIOMETRIC_ENABLED, false);

        if (!biometricEnabled) return;

        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuth = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) return;

        // ── Phần cứng OK + Settings đã bật → khởi tạo ──
        isBiometricAvailable = true;

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Người dùng bấm Hủy hoặc lỗi → không làm gì,
                        // giữ nguyên bàn phím PIN cho fallback
                    }

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        // Xác thực sinh trắc học thành công → vào app
                        goToMain();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // Vân tay không khớp → Android tự hiện thông báo,
                        // người dùng có thể thử lại hoặc dùng PIN
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setNegativeButtonText(getString(R.string.biometric_prompt_cancel))
                .build();

        // Hiện icon vân tay trên bàn phím số
        ivFingerprint.setVisibility(View.VISIBLE);

        // Tự động popup quét vân tay ngay khi mở app
        showBiometricPrompt();
    }

    /**
     * Hiển thị hộp thoại quét vân tay/khuôn mặt của hệ thống Android.
     */
    private void showBiometricPrompt() {
        if (isBiometricAvailable && biometricPrompt != null && promptInfo != null) {
            biometricPrompt.authenticate(promptInfo);
        }
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
