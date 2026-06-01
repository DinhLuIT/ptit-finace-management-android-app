package com.ptithcm.finacemanager.utils;

public class Constants {
    // Database
    public static final String DATABASE_NAME = "finance_manager.sqlite";
    public static final int DATABASE_VERSION = 6;

    // Tables
    public static final String TABLE_POTS = "POTS";
    public static final String TABLE_TRANSACTIONS = "TRANSACTIONS";
    public static final String TABLE_CATEGORIES = "CATEGORIES";
    public static final String TABLE_USER_SETTINGS = "USER_SETTINGS";
    public static final String TABLE_SAVINGS_GOALS = "SAVINGS_GOALS";
    public static final String TABLE_GOAL_CONTRIBUTIONS = "GOAL_CONTRIBUTIONS";
    public static final String TABLE_RECURRING_TRANSACTIONS = "RECURRING_TRANSACTIONS";

    // Transaction Types
    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";
    public static final String TYPE_TRANSFER = "TRANSFER";

    // Recurring Frequencies (Tần suất lặp lại)
    public static final String FREQ_DAILY = "DAILY";
    public static final String FREQ_WEEKLY = "WEEKLY";
    public static final String FREQ_MONTHLY = "MONTHLY";
    public static final String FREQ_YEARLY = "YEARLY";

    // Category Types
    public static final String CAT_TYPE_INCOME = "INCOME";
    public static final String CAT_TYPE_EXPENSE = "EXPENSE";
    public static final String CAT_TYPE_BOTH = "BOTH";

    // Danh mục hệ thống cho chuyển tiền (lưu dưới dạng resource key)
    public static final String CAT_TRANSFER_OUT = "cat_transfer_out";
    public static final String CAT_TRANSFER_IN = "cat_transfer_in";

    // SharedPreferences
    public static final String PREF_NAME = "finance_manager_prefs";
    public static final String PREF_PIN_HASH = "pin_hash";
    public static final String PREF_IS_PIN_SET = "is_pin_set";
    public static final String PREF_DARK_MODE = "dark_mode";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_NOTIFICATION_ENABLED = "notification_enabled";
    public static final String PREF_BIOMETRIC_ENABLED = "biometric_enabled";
    public static final String PREF_EXCHANGE_RATE_CACHE = "exchange_rate_cache";
    public static final String PREF_EXCHANGE_RATE_TIMESTAMP = "exchange_rate_timestamp";

    // Intent Extras
    public static final String EXTRA_POT_ID = "extra_pot_id";
    public static final String EXTRA_TRANSACTION_ID = "extra_transaction_id";
    public static final String EXTRA_TRANSACTION_TYPE = "extra_transaction_type";

    // Request Codes
    public static final int REQUEST_ADD_TRANSACTION = 100;
    public static final int REQUEST_ADD_POT = 101;
    public static final int REQUEST_EDIT_POT = 102;
    public static final int REQUEST_TRANSFER = 103;

    // Date Formats
    public static final String DATE_FORMAT_DB = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy";
    public static final String DATE_FORMAT_FULL = "dd MMMM yyyy";
    public static final String DATETIME_FORMAT_DB = "yyyy-MM-dd HH:mm:ss";

    // Pot Colors Array
    public static final String[] POT_COLORS = {
            "#4CAF50", "#2196F3", "#FF9800", "#9C27B0",
            "#F44336", "#009688", "#E91E63", "#3F51B5"
    };

    // Pot Icons (Vector)
    public static final String[] POT_ICONS = {
            "EMPTY",
            "ic_savings", "ic_food", "ic_housing", "ic_transport", 
            "ic_entertainment", "ic_education", "ic_health", "ic_shopping",
            "ic_gift", "ic_profile", "ic_calendar", "ic_transaction",
            "ic_swap", "ic_history"
    };

    public static final String DEFAULT_POT_ICON = "ic_pot";

    // Budget Thresholds
    public static final double BUDGET_WARNING_THRESHOLD = 0.6;
    public static final double BUDGET_DANGER_THRESHOLD = 0.85;

    // Goal Icons (Emoji)
    public static final String[] GOAL_ICONS = {
            "🎯", "🏠", "🚗", "✈️", "📱", "💻", "🎓", "💍",
            "👶", "🏖️", "🎸", "🐶", "🏋️", "📷", "🎮", "💎"
    };

    // Goal Colors
    public static final String[] GOAL_COLORS = {
            "#4CAF50", "#2196F3", "#FF9800", "#9C27B0",
            "#E91E63", "#00BCD4", "#FF5722", "#607D8B"
    };

    public static final String DEFAULT_GOAL_ICON = "🎯";
    public static final String DEFAULT_GOAL_COLOR = "#4CAF50";

    private Constants() {
        // Prevent instantiation
    }
}
