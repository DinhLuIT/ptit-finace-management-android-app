package com.ptithcm.finacemanager.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.NotificationHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Đăng ký Notification Channel khi khởi động app (idempotent – gọi nhiều lần không lỗi)
        NotificationHelper.createNotificationChannel(this);

        // Fade-in animation
        ImageView ivLogo = findViewById(R.id.iv_logo);
        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvTagline = findViewById(R.id.tv_tagline);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setFillAfter(true);

        ivLogo.startAnimation(fadeIn);

        AlphaAnimation fadeInDelayed = new AlphaAnimation(0f, 1f);
        fadeInDelayed.setDuration(800);
        fadeInDelayed.setStartOffset(400);
        fadeInDelayed.setFillAfter(true);

        tvAppName.startAnimation(fadeInDelayed);
        tvTagline.startAnimation(fadeInDelayed);

        // Chuyển màn hình sau 2 giây
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, 2000);
    }

    private void navigateNext() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        boolean isPinSet = prefs.getBoolean(Constants.PREF_IS_PIN_SET, false);

        Intent intent;
        if (isPinSet) {
            // Đã có PIN → vào màn hình nhập PIN
            intent = new Intent(this, PinLockActivity.class);
            intent.putExtra("mode", "verify");
        } else {
            // Chưa có PIN → vào màn hình tạo PIN
            intent = new Intent(this, PinLockActivity.class);
            intent.putExtra("mode", "create");
        }

        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
