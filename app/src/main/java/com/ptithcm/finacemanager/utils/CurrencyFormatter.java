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
