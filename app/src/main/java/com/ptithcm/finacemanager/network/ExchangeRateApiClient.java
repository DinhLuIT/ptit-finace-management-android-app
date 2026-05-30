package com.ptithcm.finacemanager.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.ptithcm.finacemanager.model.ExchangeRateResponse;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Singleton client quản lý call API tỷ giá và caching offline.
 *
 * <p>Sử dụng OkHttp để gọi {@code open.er-api.com} (miễn phí, không cần API key).
 * Kết quả được cache trong SharedPreferences với TTL 24 giờ.
 *
 * <p>Mọi callback đều được post về Main Thread để an toàn cập nhật UI.
 *
 * <h3>Flow xử lý:</h3>
 * <ol>
 *     <li>Kiểm tra cache: nếu còn hạn (< 24h) → trả cache ngay</li>
 *     <li>Nếu hết hạn/không có → gọi API</li>
 *     <li>Thành công → lưu cache + callback</li>
 *     <li>Thất bại + có cache cũ → trả cache cũ (stale)</li>
 *     <li>Thất bại + không cache → callback error</li>
 * </ol>
 */
public class ExchangeRateApiClient {

    private static final String API_URL = "https://open.er-api.com/v6/latest/VND";
    private static final String PREF_NAME = "exchange_rate_cache";
    private static final String KEY_RESPONSE_JSON = "cached_response";
    private static final String KEY_TIMESTAMP = "cached_timestamp";
    private static final long CACHE_TTL_MS = TimeUnit.HOURS.toMillis(24);

    private static ExchangeRateApiClient instance;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    /**
     * Callback interface – mọi method đều chạy trên Main Thread.
     */
    public interface RateCallback {
        /**
         * Gọi khi có dữ liệu (từ API, fresh cache, hoặc stale cache).
         *
         * @param response     Dữ liệu tỷ giá
         * @param isStaleCache true CHỈ KHI API lỗi và phải dùng cache cũ hết hạn.
         *                     false nếu data từ API mới hoặc cache còn hạn.
         */
        void onSuccess(ExchangeRateResponse response, boolean isStaleCache);

        /** Gọi khi không thể lấy dữ liệu (mất mạng + không có cache). */
        void onError(String errorMessage);
    }

    private ExchangeRateApiClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ExchangeRateApiClient getInstance() {
        if (instance == null) {
            instance = new ExchangeRateApiClient();
        }
        return instance;
    }

    /**
     * Lấy tỷ giá – ưu tiên cache nếu còn hạn, nếu không gọi API.
     *
     * @param context  Context để truy cập SharedPreferences
     * @param forceRefresh true để bỏ qua cache và gọi API mới
     * @param callback     Callback nhận kết quả trên Main Thread
     */
    public void fetchRates(@NonNull Context context, boolean forceRefresh,
                           @NonNull RateCallback callback) {
        executor.execute(() -> {
            // 1. Check fresh cache (còn hạn < 24h → trả ngay, KHÔNG đánh dấu stale)
            if (!forceRefresh) {
                ExchangeRateResponse cached = loadFromCache(context);
                if (cached != null) {
                    postSuccess(callback, cached, false); // fresh cache = not stale
                    return;
                }
            }

            // 2. Call API
            try {
                Request request = new Request.Builder()
                        .url(API_URL)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        handleApiError(context, callback, "HTTP " + response.code());
                        return;
                    }

                    String json = response.body().string();
                    ExchangeRateResponse rateResponse;
                    try {
                        rateResponse = gson.fromJson(json, ExchangeRateResponse.class);
                    } catch (Exception e) {
                        handleApiError(context, callback, "Dữ liệu trả về bị lỗi định dạng");
                        return;
                    }

                    if (rateResponse == null || !rateResponse.isSuccess()) {
                        handleApiError(context, callback, "API trả về lỗi");
                        return;
                    }

                    // 3. Lưu cache
                    saveToCache(context, json);
                    postSuccess(callback, rateResponse, false);
                }
            } catch (IOException e) {
                handleApiError(context, callback, e.getMessage());
            }
        });
    }

    /**
     * Khi API lỗi: fallback về cache cũ (nếu có), nếu không → error.
     */
    private void handleApiError(Context context, RateCallback callback, String errorMsg) {
        ExchangeRateResponse staleCache = loadFromCacheIgnoreTTL(context);
        if (staleCache != null) {
            postSuccess(callback, staleCache, true); // API lỗi + fallback = stale
        } else {
            mainHandler.post(() -> callback.onError(errorMsg));
        }
    }

    // ── Cache operations ──────────────────────────────────────────

    private void saveToCache(Context context, String json) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_RESPONSE_JSON, json)
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    /**
     * Load cache chỉ khi còn hạn (< 24h).
     */
    private ExchangeRateResponse loadFromCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long timestamp = prefs.getLong(KEY_TIMESTAMP, 0);

        if (System.currentTimeMillis() - timestamp > CACHE_TTL_MS) {
            return null; // Cache hết hạn
        }

        return parseCachedJson(prefs);
    }

    /**
     * Load cache bất kể thời gian (dùng khi API lỗi, có dữ liệu cũ vẫn tốt hơn không có gì).
     */
    private ExchangeRateResponse loadFromCacheIgnoreTTL(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return parseCachedJson(prefs);
    }

    private ExchangeRateResponse parseCachedJson(SharedPreferences prefs) {
        String json = prefs.getString(KEY_RESPONSE_JSON, null);
        if (json == null) return null;

        try {
            ExchangeRateResponse response = gson.fromJson(json, ExchangeRateResponse.class);
            return (response != null && response.isSuccess()) ? response : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ── Thread helpers ────────────────────────────────────────────

    private void postSuccess(RateCallback callback, ExchangeRateResponse response, boolean fromCache) {
        mainHandler.post(() -> callback.onSuccess(response, fromCache));
    }
}
