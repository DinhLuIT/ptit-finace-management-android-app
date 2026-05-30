package com.ptithcm.finacemanager.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * TextWatcher chuyên biệt để format tiền tệ on-time (ngay khi đang gõ).
 * <p>
 * Tự động phân loại tiền tệ:
 * - Nhóm Zero-Decimal (VND, JPY, KRW...): Không thập phân, ngăn cách hàng nghìn bằng dấu chấm. VD: 1.000.000
 * - Nhóm Decimal (USD, EUR...): Cho phép 2 thập phân, ngăn cách hàng nghìn bằng dấu phẩy. VD: 1,000.50
 */
public class CurrencyTextWatcher implements TextWatcher {
    private final EditText editText;
    private String currentCurrencyCode = "VND";
    private final Runnable onChangeCallback;

    // Các ngoại tệ không dùng số thập phân (hoặc rất hiếm khi dùng)
    private static final List<String> ZERO_DECIMAL_CURRENCIES = Arrays.asList("VND", "JPY", "KRW");

    public CurrencyTextWatcher(EditText editText, Runnable onChangeCallback) {
        this.editText = editText;
        this.onChangeCallback = onChangeCallback;
    }

    /**
     * Activity gọi hàm này khi người dùng đổi Spinner tiền tệ.
     * Hàm sẽ tự động re-format lại số đang có trong ô nhập.
     */
    public void setCurrencyCode(String currencyCode) {
        if (this.currentCurrencyCode.equals(currencyCode)) return;
        
        this.currentCurrencyCode = currencyCode;
        // Kích hoạt re-format ngay lập tức nếu ô nhập đang có chữ
        String currentText = editText.getText().toString();
        if (!currentText.isEmpty()) {
            editText.setText(currentText);
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        editText.removeTextChangedListener(this);

        try {
            String input = s.toString();
            if (input.isEmpty()) {
                if (onChangeCallback != null) onChangeCallback.run();
                editText.addTextChangedListener(this);
                return;
            }

            boolean isZeroDecimal = ZERO_DECIMAL_CURRENCIES.contains(currentCurrencyCode);
            String formattedString;

            if (isZeroDecimal) {
                // Xóa mọi thứ trừ chữ số
                String cleanString = input.replaceAll("[^\\d]", "");
                if (cleanString.isEmpty()) {
                    formattedString = "";
                } else {
                    double parsed = Double.parseDouble(cleanString);
                    // Dùng format kiểu Đức/Việt: dấu chấm ngăn cách hàng nghìn
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
                    symbols.setGroupingSeparator('.');
                    DecimalFormat df = new DecimalFormat("#,###", symbols);
                    formattedString = df.format(parsed);
                }
            } else {
                // Giữ lại số và dấu chấm thập phân đầu tiên
                String cleanString = input.replaceAll("[^\\d.]", "");
                
                // Đảm bảo chỉ có 1 dấu chấm (loại bỏ các dấu chấm vô tình gõ dư)
                int dotIndex = cleanString.indexOf(".");
                if (dotIndex != -1) {
                    String beforeDot = cleanString.substring(0, dotIndex);
                    String afterDot = cleanString.substring(dotIndex + 1).replaceAll("\\.", "");
                    // Giới hạn 2 số thập phân
                    if (afterDot.length() > 2) {
                        afterDot = afterDot.substring(0, 2);
                    }
                    cleanString = beforeDot + "." + afterDot;
                }

                if (cleanString.isEmpty() || cleanString.equals(".")) {
                    formattedString = cleanString.equals(".") ? "0." : "";
                } else {
                    // Tách phần nguyên và phần thập phân để format phần nguyên
                    String[] parts = cleanString.split("\\.");
                    double parsed = Double.parseDouble(parts[0]);
                    
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                    symbols.setGroupingSeparator(',');
                    DecimalFormat df = new DecimalFormat("#,###", symbols);
                    
                    formattedString = df.format(parsed);
                    if (cleanString.contains(".")) {
                        if (parts.length > 1) {
                            formattedString += "." + parts[1]; // Gắn lại phần thập phân
                        } else {
                            formattedString += "."; // Gắn lại dấu chấm nếu đang gõ dở
                        }
                    }
                }
            }

            editText.setText(formattedString);
            editText.setSelection(formattedString.length()); // Đưa cursor về cuối
        } catch (Exception e) {
            e.printStackTrace();
        }

        editText.addTextChangedListener(this);
        
        // Báo cho Activity biết để tính toán lại tỷ giá
        if (onChangeCallback != null) {
            onChangeCallback.run();
        }
    }
    
    /**
     * Hàm tiện ích để parse chuỗi đã format ra số nguyên thủy (Double) để tính toán.
     */
    public static double parseFormattedValue(String formattedValue, String currencyCode) {
        if (formattedValue == null || formattedValue.isEmpty()) return 0;
        try {
            boolean isZeroDecimal = ZERO_DECIMAL_CURRENCIES.contains(currencyCode);
            if (isZeroDecimal) {
                String cleanString = formattedValue.replaceAll("[^\\d]", "");
                return cleanString.isEmpty() ? 0 : Double.parseDouble(cleanString);
            } else {
                String cleanString = formattedValue.replaceAll("[^\\d.]", "");
                return cleanString.isEmpty() ? 0 : Double.parseDouble(cleanString);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
