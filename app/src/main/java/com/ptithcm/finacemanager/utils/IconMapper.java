package com.ptithcm.finacemanager.utils;

import android.content.Context;
import com.ptithcm.finacemanager.R;

public class IconMapper {

    private static final String DEFAULT_ICON = "ic_other";

    public static int getIconResource(Context context, String iconKey) {
        if (iconKey == null || iconKey.isEmpty()) {
            iconKey = DEFAULT_ICON;
        }
        int resId = context.getResources().getIdentifier(iconKey, "drawable", context.getPackageName());
        if (resId == 0) {
            return R.drawable.ic_other;
        }
        return resId;
    }

    public static String toEmoji(String iconKey) {
        return ""; // Không dùng nữa, giữ lại để không báo lỗi biên dịch trước khi refactor adapter
    }

    private IconMapper() {
    }
}
