package com.ptithcm.finacemanager.utils;

public class Constants {
    // Database
    public static final String DATABASE_NAME = "finance_manager.sqlite";
    public static final int DATABASE_VERSION = 3;

    // Tables
    public static final String TABLE_POTS = "POTS";
    public static final String TABLE_TRANSACTIONS = "TRANSACTIONS";
    public static final String TABLE_CATEGORIES = "CATEGORIES";
    public static final String TABLE_USER_SETTINGS = "USER_SETTINGS";

    // Transaction Types
    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    // Category Types
    public static final String CAT_TYPE_INCOME = "INCOME";
    public static final String CAT_TYPE_EXPENSE = "EXPENSE";
    public static final String CAT_TYPE_BOTH = "BOTH";

    // SharedPreferences
    public static final String PREF_NAME = "finance_manager_prefs";
    public static final String PREF_PIN_HASH = "pin_hash";
    public static final String PREF_IS_PIN_SET = "is_pin_set";
    public static final String PREF_DARK_MODE = "dark_mode";
    public static final String PREF_LANGUAGE = "language";

    // Intent Extras
    public static final String EXTRA_POT_ID = "extra_pot_id";
    public static final String EXTRA_TRANSACTION_ID = "extra_transaction_id";
    public static final String EXTRA_TRANSACTION_TYPE = "extra_transaction_type";

    // Request Codes
    public static final int REQUEST_ADD_TRANSACTION = 100;
    public static final int REQUEST_ADD_POT = 101;
    public static final int REQUEST_EDIT_POT = 102;

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

    // Pot Icons (Emoji)
    public static final String[] POT_ICONS = {
            "💰", "🍜", "🏠", "🚗", "🎮", "📚", "💊", "🛍",
            "✈️", "☕", "🎬", "👕", "💡", "🐱", "🎵", "💼"
    };

    public static final String DEFAULT_POT_ICON = "💰";

    // Budget Thresholds
    public static final double BUDGET_WARNING_THRESHOLD = 0.6;
    public static final double BUDGET_DANGER_THRESHOLD = 0.85;

    private Constants() {
        // Prevent instantiation
    }
}
