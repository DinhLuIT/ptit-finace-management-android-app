package com.ptithcm.finacemanager.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.activity.PotDetailActivity;

/**
 * Helper class quản lý hệ thống thông báo cục bộ (Local Notifications).
 *
 * <p>Chịu trách nhiệm:
 * <ul>
 *   <li>Đăng ký Notification Channel (yêu cầu bắt buộc từ Android Oreo - SDK 26+)</li>
 *   <li>Gửi thông báo cảnh báo ngân sách (Budget Warning & Danger)</li>
 *   <li>Kiểm tra trạng thái bật/tắt thông báo từ SharedPreferences</li>
 * </ul>
 *
 * <p>Ngưỡng cảnh báo:
 * <ul>
 *   <li>>= 80%: Thông báo cảnh báo (Warning) – màu vàng</li>
 *   <li>>= 100%: Thông báo vượt mức (Danger) – màu đỏ</li>
 * </ul>
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    // ID của Notification Channel cho cảnh báo ngân sách
    private static final String CHANNEL_ID_BUDGET = "channel_budget_alert";

    // Ngưỡng phần trăm để gửi thông báo
    public static final double NOTIFICATION_WARNING_THRESHOLD = 0.80;
    public static final double NOTIFICATION_DANGER_THRESHOLD = 1.00;

    // Notification ID base – dùng potId để phân biệt từng hũ
    private static final int NOTIFICATION_ID_BASE = 2000;

    private NotificationHelper() {
        // Prevent instantiation – Utility class
    }

    /**
     * Đăng ký Notification Channel cho cảnh báo ngân sách.
     * Bắt buộc gọi 1 lần khi khởi động app (thường trong Application hoặc SplashActivity).
     * Gọi nhiều lần không gây lỗi (Android tự skip nếu channel đã tồn tại).
     *
     * @param context Context ứng dụng
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = context.getString(R.string.notification_channel_budget);
            String channelDesc = context.getString(R.string.notification_channel_budget_desc);

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_BUDGET,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(channelDesc);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Kiểm tra xem người dùng có bật thông báo trong cài đặt ứng dụng hay không.
     *
     * @param context Context hiện tại
     * @return true nếu thông báo được bật, false nếu bị tắt
     */
    public static boolean isNotificationEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.PREF_NOTIFICATION_ENABLED, true);
    }

    /**
     * Lưu trạng thái bật/tắt thông báo vào SharedPreferences.
     *
     * @param context Context hiện tại
     * @param enabled true để bật, false để tắt
     */
    public static void setNotificationEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(Constants.PREF_NOTIFICATION_ENABLED, enabled).apply();
    }

    /**
     * Kiểm tra ngân sách của hũ và gửi thông báo nếu vượt ngưỡng.
     *
     * <p>Logic tính phần trăm đã chi tiêu:
     * {@code spentPercentage = (budgetLimit - currentBalance) / budgetLimit}
     *
     * @param context       Context hiện tại
     * @param potId         ID của hũ chi tiêu
     * @param potName       Tên của hũ chi tiêu (hiển thị trên thông báo)
     * @param budgetLimit   Ngân sách giới hạn của hũ
     * @param currentBalance Số dư hiện tại của hũ (sau giao dịch)
     */
    public static void checkAndNotifyBudget(Context context, int potId, String potName,
                                            double budgetLimit, double currentBalance) {
        // Không gửi thông báo nếu người dùng đã tắt
        if (!isNotificationEnabled(context)) {
            return;
        }

        // Không tính nếu budgetLimit <= 0
        if (budgetLimit <= 0) {
            return;
        }

        // Tính phần trăm đã chi tiêu
        double spent = budgetLimit - currentBalance;
        if (spent < 0) spent = 0;
        double spentPercentage = spent / budgetLimit;

        if (spentPercentage >= NOTIFICATION_DANGER_THRESHOLD) {
            // >= 100%: Thông báo đỏ – VÃ†Æ¯á»¢T ngân sách
            sendBudgetNotification(context, potId, potName,
                    context.getString(R.string.notification_budget_danger_title),
                    context.getString(R.string.notification_budget_danger_body, potName),
                    true);
        } else if (spentPercentage >= NOTIFICATION_WARNING_THRESHOLD) {
            // >= 80%: Thông báo vàng – CẢNH BÁO gần hết ngân sách
            int percentage = (int) (spentPercentage * 100);
            sendBudgetNotification(context, potId, potName,
                    context.getString(R.string.notification_budget_warning_title),
                    context.getString(R.string.notification_budget_warning_body, potName, percentage),
                    false);
        }
    }

    /**
     * Gửi System Local Notification cho cảnh báo ngân sách.
     * Khi nhấn vào thông báo sẽ mở màn hình Chi tiết hũ tương ứng.
     *
     * @param context  Context hiện tại
     * @param potId    ID của hũ (dùng làm notification ID và intent extra)
     * @param potName  Tên hũ (để log / debug)
     * @param title    Tiêu đề thông báo
     * @param body     Nội dung thông báo
     * @param isDanger true nếu là mức nguy hiểm (đỏ), false nếu chỉ cảnh báo (vàng)
     */
    private static void sendBudgetNotification(Context context, int potId, String potName,
                                               String title, String body, boolean isDanger) {
        // Tạo Intent mở PotDetailActivity khi nhấn vào thông báo
        Intent intent = new Intent(context, PotDetailActivity.class);
        intent.putExtra(Constants.EXTRA_POT_ID, potId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, potId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Chọn icon nhỏ và màu sắc theo mức độ
        int smallIcon = isDanger ? R.drawable.ic_delete : R.drawable.ic_edit;
        int color = isDanger
                ? context.getColor(R.color.color_expense)
                : context.getColor(R.color.color_budget_warning);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_BUDGET)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(color)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Dùng NOTIFICATION_ID_BASE + potId để mỗi hũ có 1 thông báo riêng (không bị đè)
        try {
            notificationManager.notify(NOTIFICATION_ID_BASE + potId, builder.build());
        } catch (SecurityException e) {
            // Nếu người dùng chưa cấp quyền POST_NOTIFICATIONS (Android 13+)
            // Không crash app, chỉ bỏ qua
            android.util.Log.w(TAG, "Notification permission not granted", e);
        }
    }
}
