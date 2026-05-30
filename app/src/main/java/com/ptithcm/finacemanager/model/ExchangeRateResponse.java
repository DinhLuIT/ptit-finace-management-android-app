package com.ptithcm.finacemanager.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Model parse JSON response từ Open Exchange Rate API.
 *
 * <p>API endpoint: {@code https://open.er-api.com/v6/latest/VND}
 *
 * <p>Chỉ extract các field cần thiết:
 * <ul>
 *     <li>{@code result} – "success" hoặc "error"</li>
 *     <li>{@code base_code} – mã tiền tệ gốc (VND)</li>
 *     <li>{@code rates} – Map chứa tỷ giá so với base</li>
 *     <li>{@code time_last_update_utc} – thời gian cập nhật</li>
 * </ul>
 */
public class ExchangeRateResponse {

    @SerializedName("result")
    private String result;

    @SerializedName("base_code")
    private String baseCode;

    @SerializedName("rates")
    private Map<String, Double> rates;

    @SerializedName("time_last_update_utc")
    private String timeLastUpdate;

    public boolean isSuccess() {
        return "success".equals(result);
    }

    public String getBaseCode() {
        return baseCode;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public String getTimeLastUpdate() {
        return timeLastUpdate;
    }

    /**
     * Tính tỷ giá: 1 đơn vị ngoại tệ = ? VND.
     *
     * <p>API trả về tỷ giá dạng: 1 VND = X ngoại tệ.
     * Cần đảo ngược: 1 ngoại tệ = 1/X VND.
     *
     * @param currencyCode Mã ngoại tệ (VD: "USD")
     * @return Tỷ giá 1 ngoại tệ → VND, hoặc 0 nếu không tìm thấy
     */
    public double getRateToVnd(String currencyCode) {
        if (rates == null || !rates.containsKey(currencyCode)) return 0;
        double rateFromVnd = rates.get(currencyCode);
        if (rateFromVnd == 0) return 0;
        return 1.0 / rateFromVnd;
    }
}
