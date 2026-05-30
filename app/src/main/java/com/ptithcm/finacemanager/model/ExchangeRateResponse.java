package com.ptithcm.finacemanager.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Model parse JSON response từ Open Exchange Rate API.
 *
 * <p>API endpoint: {@code https://open.er-api.com/v6/latest/VND}
 *
 * <p>Hỗ trợ quy đổi đa chiều bằng cross-rate:
 * chỉ cần 1 lần gọi API (base=VND), quy đổi được mọi cặp tiền tệ.
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
     * <p>API trả về: 1 VND = X ngoại tệ → đảo ngược: 1 ngoại tệ = 1/X VND.
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

    /**
     * Quy đổi đa chiều bằng cross-rate.
     *
     * <p>API base = VND → rates chứa: 1 VND = X [currency].
     * Để quy đổi bất kỳ cặp FROM → TO:
     * <pre>
     *   amount [FROM] → VND → [TO]
     *   result = amount × (rateTo / rateFrom)
     * </pre>
     *
     * @param amount Số tiền cần quy đổi
     * @param from   Mã tiền tệ nguồn (VD: "USD")
     * @param to     Mã tiền tệ đích (VD: "EUR")
     * @return Kết quả quy đổi, hoặc 0 nếu không tìm thấy tỷ giá
     */
    public double convert(double amount, String from, String to) {
        if (from.equals(to)) return amount;
        if (rates == null) return 0;

        Double rateFrom = rates.get(from); // 1 VND = rateFrom [FROM]
        Double rateTo = rates.get(to);     // 1 VND = rateTo [TO]

        if (rateFrom == null || rateTo == null || rateFrom == 0) return 0;

        // amount [FROM] → VND → [TO]
        return amount * (rateTo / rateFrom);
    }

    /**
     * Lấy tỷ giá trực tiếp: 1 đơn vị [from] = ? [to].
     *
     * @return Tỷ giá, hoặc 0 nếu không tìm thấy
     */
    public double getRate(String from, String to) {
        return convert(1.0, from, to);
    }
}
