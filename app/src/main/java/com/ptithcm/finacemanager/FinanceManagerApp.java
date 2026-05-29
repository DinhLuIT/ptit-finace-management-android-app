package com.ptithcm.finacemanager;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.ptithcm.finacemanager.utils.Constants;

/**
 * Application class toàn cục của Finance Manager.
 *
 * <p>Được khởi tạo trước tất cả Activity/Fragment. Đảm nhiệm:
 * <ul>
 *     <li>Khôi phục trạng thái Dark Mode từ SharedPreferences (tránh nháy sáng khi khởi động).</li>
 *     <li>Điểm mở rộng cho WorkManager, Analytics, hoặc các SDK khác trong tương lai.</li>
 * </ul>
 *
 * <p>Đăng ký trong AndroidManifest.xml: {@code android:name=".FinanceManagerApp"}
 */
public class FinanceManagerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Khôi phục Dark Mode từ SharedPreferences ngay khi app khởi động
        // để tránh hiện tượng "nháy" giao diện sáng → tối
        restoreDarkMode();
    }

    /**
     * Đọc trạng thái Dark Mode đã lưu trong SharedPreferences và áp dụng ngay.
     * Nếu chưa từng thiết lập, mặc định theo hệ thống (MODE_NIGHT_FOLLOW_SYSTEM).
     */
    private void restoreDarkMode() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(Constants.PREF_DARK_MODE, false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
