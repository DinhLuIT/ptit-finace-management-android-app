package com.ptithcm.finacemanager.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class map tên icon drawable (lưu trong DB) sang emoji hiển thị.
 *
 * <p>DB lưu icon dạng String key (VD: "ic_food"), nhưng UI hiển thị emoji
 * trên TextView thay vì dùng ImageView. Class này cung cấp mapping tập trung,
 * tái sử dụng bởi tất cả Adapter (TransactionAdapter, RecurringTransactionAdapter, v.v.)
 */
public class IconMapper {

    private static final Map<String, String> ICON_MAP = new HashMap<>();

    static {
        // Expense categories
        ICON_MAP.put("ic_food", "🍜");
        ICON_MAP.put("ic_housing", "🏠");
        ICON_MAP.put("ic_transport", "🚗");
        ICON_MAP.put("ic_entertainment", "🎮");
        ICON_MAP.put("ic_education", "📚");
        ICON_MAP.put("ic_health", "💊");
        ICON_MAP.put("ic_shopping", "🛍");
        ICON_MAP.put("ic_savings", "💰");
        ICON_MAP.put("ic_other", "📦");

        // Income categories
        ICON_MAP.put("ic_salary", "💵");
        ICON_MAP.put("ic_gift", "🎁");

        // Special
        ICON_MAP.put("ic_transfer", "🔄");
    }

    private static final String DEFAULT_EMOJI = "📦";

    /**
     * Map icon key (từ DB) sang emoji hiển thị.
     *
     * @param iconKey Tên icon lưu trong DB (VD: "ic_food")
     * @return Emoji tương ứng (VD: "🍜"), hoặc default "📦" nếu không tìm thấy
     */
    public static String toEmoji(String iconKey) {
        if (iconKey == null || iconKey.isEmpty()) return DEFAULT_EMOJI;
        return ICON_MAP.getOrDefault(iconKey, DEFAULT_EMOJI);
    }

    private IconMapper() {
        // Prevent instantiation
    }
}
