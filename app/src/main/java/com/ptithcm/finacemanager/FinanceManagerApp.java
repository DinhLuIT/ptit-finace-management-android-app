package com.ptithcm.finacemanager;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.worker.RecurringTransactionWorker;

import java.util.concurrent.TimeUnit;

/**
 * Application class toàn cục của Finance Manager.
 *
 * <p>Được khởi tạo trước tất cả Activity/Fragment. Đảm nhiệm:
 * <ul>
 *     <li>Khôi phục trạng thái Dark Mode từ SharedPreferences (tránh nháy sáng khi khởi động).</li>
 *     <li>Đăng ký WorkManager cho giao dịch định kỳ (chạy mỗi 24h).</li>
 * </ul>
 *
 * <p>Đăng ký trong AndroidManifest.xml: {@code android:name=".FinanceManagerApp"}
 */
public class FinanceManagerApp extends Application {

    private static final String WORK_TAG_RECURRING = "recurring_transactions_worker";

    @Override
    public void onCreate() {
        super.onCreate();

        // Khôi phục Dark Mode từ SharedPreferences ngay khi app khởi động
        restoreDarkMode();

        // Đăng ký Worker xử lý giao dịch định kỳ (chạy mỗi 24h)
        scheduleRecurringTransactionWorker();
    }

    /**
     * Đọc trạng thái Dark Mode đã lưu trong SharedPreferences và áp dụng ngay.
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

    /**
     * Đăng ký PeriodicWorkRequest cho RecurringTransactionWorker.
     * Chạy mỗi 24h, KEEP nếu đã tồn tại (không tạo trùng lặp).
     */
    private void scheduleRecurringTransactionWorker() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                RecurringTransactionWorker.class,
                24, TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_TAG_RECURRING,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
}
