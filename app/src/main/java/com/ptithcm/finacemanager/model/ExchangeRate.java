package com.ptithcm.finacemanager.model;

/**
 * Model đại diện cho 1 loại ngoại tệ và tỷ giá so với VND.
 *
 * <p>Dùng trong bảng tỷ giá của {@link com.ptithcm.finacemanager.activity.ExchangeRateActivity}.
 * Mỗi instance chứa đầy đủ thông tin hiển thị: mã tiền tệ, tên đầy đủ, cờ quốc gia,
 * và tỷ giá quy đổi sang VND.
 */
public class ExchangeRate {

    private final String currencyCode;   // VD: "USD"
    private final String fullName;       // VD: "Đô la Mỹ"
    private final String flag;           // VD: "🇺🇸"
    private final double rateToVnd;      // 1 đơn vị ngoại tệ = ? VND

    public ExchangeRate(String currencyCode, String fullName, String flag, double rateToVnd) {
        this.currencyCode = currencyCode;
        this.fullName = fullName;
        this.flag = flag;
        this.rateToVnd = rateToVnd;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFlag() {
        return flag;
    }

    public double getRateToVnd() {
        return rateToVnd;
    }
}
