package com.ptithcm.finacemanager.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for formatting currency values.
 * Display format: 1,500,000 ₫
 * Storage format: double (no formatting)
 */
public class CurrencyFormatter {

    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        formatter = new DecimalFormat("#,###", symbols);
    }

    /**
     * Format amount to display string with currency symbol.
     * Example: 1500000.0 → "1,500,000 ₫"
     */
    public static String format(double amount) {
        return formatter.format(amount) + " ₫";
    }

    /**
     * Format amount without currency symbol.
     * Example: 1500000.0 → "1,500,000"
     */
    public static String formatNoCurrency(double amount) {
        return formatter.format(amount);
    }

    /**
     * Format amount with sign indicator.
     * Example: 1500000.0, true → "+1,500,000 ₫"
     * Example: 1500000.0, false → "-1,500,000 ₫"
     */
    public static String formatWithSign(double amount, boolean isIncome) {
        String sign = isIncome ? "+" : "-";
        return sign + formatter.format(Math.abs(amount)) + " ₫";
    }

    /**
     * Format số tiền gọn cho card nhỏ.
     * Ví dụ: 1,500,000 → "1.5M ₫", 500,000 → "500K ₫", 1,200 → "1,200 ₫"
     */
    public static String formatCompact(double amount) {
        double abs = Math.abs(amount);
        if (abs >= 1_000_000_000) {
            return String.format(Locale.US, "%.1fB ₫", amount / 1_000_000_000);
        } else if (abs >= 1_000_000) {
            double m = amount / 1_000_000;
            // Bỏ .0 nếu là số tròn
            if (m == Math.floor(m)) {
                return String.format(Locale.US, "%.0fM ₫", m);
            }
            return String.format(Locale.US, "%.1fM ₫", m);
        } else if (abs >= 1_000) {
            double k = amount / 1_000;
            if (k == Math.floor(k)) {
                return String.format(Locale.US, "%.0fK ₫", k);
            }
            return String.format(Locale.US, "%.1fK ₫", k);
        } else {
            return format(amount);
        }
    }

    /**
     * Parse formatted string back to double.
     * Example: "1,500,000" → 1500000.0
     */
    public static double parse(String formattedAmount) {
        try {
            String cleaned = formattedAmount.replaceAll("[^\\d.]", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private CurrencyFormatter() {
        // Prevent instantiation
    }
}
