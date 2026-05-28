package com.ptithcm.finacemanager.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ptithcm.finacemanager.R;

/**
 * Helper class để hiển thị Toast tùy chỉnh (Custom Floating Toast).
 * Thay thế Toast mặc định của Android bằng giao diện bo tròn, có icon trạng thái
 * và bóng đổ (elevation) tạo cảm giác cao cấp.
 *
 * Cách sử dụng:
 * <pre>
 *   CustomToast.showSuccess(context, "Giao dịch đã được lưu!");
 *   CustomToast.showError(context, "Lỗi khi lưu giao dịch");
 *   CustomToast.showWarning(context, "Ngân sách gần hết!");
 * </pre>
 */
public class CustomToast {

    // Các loại Toast tương ứng với 3 trạng thái chính
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_WARNING = 2;

    // Icon emoji cho từng loại trạng thái
    private static final String ICON_SUCCESS = "✅";
    private static final String ICON_ERROR = "❌";
    private static final String ICON_WARNING = "⚠️";

    // Background resource tương ứng cho từng loại trạng thái
    private static final int[] BACKGROUND_RES = {
            R.drawable.bg_toast_success,
            R.drawable.bg_toast_error,
            R.drawable.bg_toast_warning
    };

    private CustomToast() {
        // Prevent instantiation – Utility class
    }

    /**
     * Hiển thị Toast thành công (màu xanh lá).
     *
     * @param context Context hiện tại
     * @param message Nội dung thông báo
     */
    public static void showSuccess(Context context, String message) {
        show(context, message, TYPE_SUCCESS, Toast.LENGTH_SHORT);
    }

    /**
     * Hiển thị Toast lỗi (màu đỏ).
     *
     * @param context Context hiện tại
     * @param message Nội dung thông báo
     */
    public static void showError(Context context, String message) {
        show(context, message, TYPE_ERROR, Toast.LENGTH_SHORT);
    }

    /**
     * Hiển thị Toast cảnh báo (màu vàng/cam).
     *
     * @param context Context hiện tại
     * @param message Nội dung thông báo
     */
    public static void showWarning(Context context, String message) {
        show(context, message, TYPE_WARNING, Toast.LENGTH_SHORT);
    }

    /**
     * Hiển thị Toast thành công (màu xanh lá) từ string resource.
     *
     * @param context  Context hiện tại
     * @param stringId Resource ID của chuỗi thông báo
     */
    public static void showSuccess(Context context, int stringId) {
        showSuccess(context, context.getString(stringId));
    }

    /**
     * Hiển thị Toast lỗi (màu đỏ) từ string resource.
     *
     * @param context  Context hiện tại
     * @param stringId Resource ID của chuỗi thông báo
     */
    public static void showError(Context context, int stringId) {
        showError(context, context.getString(stringId));
    }

    /**
     * Hiển thị Toast cảnh báo (màu vàng/cam) từ string resource.
     *
     * @param context  Context hiện tại
     * @param stringId Resource ID của chuỗi thông báo
     */
    public static void showWarning(Context context, int stringId) {
        showWarning(context, context.getString(stringId));
    }

    /**
     * Phương thức chung để inflate layout custom và hiển thị Toast.
     *
     * @param context  Context hiện tại
     * @param message  Nội dung thông báo
     * @param type     Loại toast (TYPE_SUCCESS, TYPE_ERROR, TYPE_WARNING)
     * @param duration Thời gian hiển thị (Toast.LENGTH_SHORT hoặc Toast.LENGTH_LONG)
     */
    private static void show(Context context, String message, int type, int duration) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.layout_custom_toast, null);

        // Thiết lập icon theo loại trạng thái
        TextView tvIcon = layout.findViewById(R.id.tv_toast_icon);
        TextView tvMessage = layout.findViewById(R.id.tv_toast_message);
        View container = layout.findViewById(R.id.ll_toast_container);

        switch (type) {
            case TYPE_SUCCESS:
                tvIcon.setText(ICON_SUCCESS);
                container.setBackgroundResource(R.drawable.bg_toast_success);
                break;
            case TYPE_ERROR:
                tvIcon.setText(ICON_ERROR);
                container.setBackgroundResource(R.drawable.bg_toast_error);
                break;
            case TYPE_WARNING:
                tvIcon.setText(ICON_WARNING);
                container.setBackgroundResource(R.drawable.bg_toast_warning);
                break;
            default:
                tvIcon.setText(ICON_SUCCESS);
                container.setBackgroundResource(R.drawable.bg_toast_success);
                break;
        }

        tvMessage.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 120);
        toast.show();
    }
}
