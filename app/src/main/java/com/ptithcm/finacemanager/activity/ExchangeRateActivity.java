package com.ptithcm.finacemanager.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.ExchangeRateAdapter;
import com.ptithcm.finacemanager.model.ExchangeRate;
import com.ptithcm.finacemanager.model.ExchangeRateResponse;
import com.ptithcm.finacemanager.network.ExchangeRateApiClient;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Màn hình xem & quy đổi tỷ giá ngoại tệ.
 *
 * <p>Gồm 2 phần chính:
 * <ol>
 *     <li><b>Converter Card</b>: Nhập VNĐ → chọn ngoại tệ → hiện kết quả quy đổi</li>
 *     <li><b>Rate Table</b>: Bảng tỷ giá 9 ngoại tệ phổ biến với người Việt</li>
 * </ol>
 *
 * <p>Dùng {@link ExchangeRateApiClient} (OkHttp + Gson) để fetch API miễn phí,
 * cache 24h trong SharedPreferences, fallback khi mất mạng.
 */
public class ExchangeRateActivity extends AppCompatActivity {

    // ── 9 ngoại tệ phổ biến, thứ tự: USD, EUR, GBP, JPY, KRW, CNY, AUD, SGD, THB ──
    private static final Map<String, String[]> SUPPORTED_CURRENCIES = new LinkedHashMap<>();

    static {
        // { mã tiền, tên tiếng Việt, cờ emoji }
        SUPPORTED_CURRENCIES.put("USD", new String[]{"Đô la Mỹ", "🇺🇸"});
        SUPPORTED_CURRENCIES.put("EUR", new String[]{"Euro", "🇪🇺"});
        SUPPORTED_CURRENCIES.put("GBP", new String[]{"Bảng Anh", "🇬🇧"});
        SUPPORTED_CURRENCIES.put("JPY", new String[]{"Yên Nhật", "🇯🇵"});
        SUPPORTED_CURRENCIES.put("KRW", new String[]{"Won Hàn Quốc", "🇰🇷"});
        SUPPORTED_CURRENCIES.put("CNY", new String[]{"Nhân dân tệ", "🇨🇳"});
        SUPPORTED_CURRENCIES.put("AUD", new String[]{"Đô la Úc", "🇦🇺"});
        SUPPORTED_CURRENCIES.put("SGD", new String[]{"Đô la Singapore", "🇸🇬"});
        SUPPORTED_CURRENCIES.put("THB", new String[]{"Baht Thái", "🇹🇭"});
    }

    // ── Views ──
    private EditText etAmountVnd;
    private TextView tvConvertedAmount, tvLastUpdate, tvCacheBanner, tvRefresh;
    private Spinner spinnerCurrency;
    private RecyclerView rvRates;
    private View layoutLoading, layoutError;

    // ── Data ──
    private ExchangeRateAdapter adapter;
    private ExchangeRateResponse currentResponse;
    private final List<String> currencyCodes = new ArrayList<>(SUPPORTED_CURRENCIES.keySet());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);

        initViews();
        initListeners();
        setupSpinner();
        setupRecyclerView();
        fetchRates(false);
    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        etAmountVnd = findViewById(R.id.et_amount_vnd);
        tvConvertedAmount = findViewById(R.id.tv_converted_amount);
        tvLastUpdate = findViewById(R.id.tv_last_update);
        tvCacheBanner = findViewById(R.id.tv_cache_banner);
        tvRefresh = findViewById(R.id.tv_refresh);
        spinnerCurrency = findViewById(R.id.spinner_currency);
        rvRates = findViewById(R.id.rv_rates);
        layoutLoading = findViewById(R.id.layout_loading);
        layoutError = findViewById(R.id.layout_error);
    }

    private void initListeners() {
        // Khi nhập số VND → tính toán quy đổi realtime
        etAmountVnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateConversion();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút làm mới tỷ giá
        tvRefresh.setOnClickListener(v -> fetchRates(true));

        // Nút thử lại (trong error state)
        findViewById(R.id.btn_retry).setOnClickListener(v -> fetchRates(true));
    }

    private void setupSpinner() {
        // Hiện mã tiền tệ trong Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, currencyCodes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(spinnerAdapter);

        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new ExchangeRateAdapter(new ArrayList<>());
        adapter.setOnRateClickListener(rate -> {
            // Tap vào ngoại tệ → tự chọn loại tiền đó trong Spinner
            int index = currencyCodes.indexOf(rate.getCurrencyCode());
            if (index >= 0) {
                spinnerCurrency.setSelection(index);
                // Focus vào ô nhập nếu chưa có số
                if (etAmountVnd.getText().toString().isEmpty()) {
                    etAmountVnd.requestFocus();
                }
            }
        });
        rvRates.setLayoutManager(new LinearLayoutManager(this));
        rvRates.setAdapter(adapter);
    }

    // ── API Call ─────────────────────────────────────────────────

    private void fetchRates(boolean forceRefresh) {
        showLoading();

        ExchangeRateApiClient.getInstance().fetchRates(this, forceRefresh,
                new ExchangeRateApiClient.RateCallback() {
                    @Override
                    public void onSuccess(ExchangeRateResponse response, boolean isFromCache) {
                        currentResponse = response;
                        showContent(isFromCache);
                        populateRateTable(response);
                        calculateConversion();
                        updateLastUpdateText(response.getTimeLastUpdate());
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showError();
                    }
                });
    }

    // ── UI State Management ─────────────────────────────────────

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        rvRates.setVisibility(View.GONE);
    }

    private void showContent(boolean fromCache) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        rvRates.setVisibility(View.VISIBLE);

        // Hiện banner "Dữ liệu offline" nếu dùng cache
        tvCacheBanner.setVisibility(fromCache ? View.VISIBLE : View.GONE);
    }

    private void showError() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        rvRates.setVisibility(View.GONE);
    }

    // ── Business Logic ──────────────────────────────────────────

    /**
     * Build danh sách 9 ExchangeRate từ API response và đẩy vào adapter.
     */
    private void populateRateTable(ExchangeRateResponse response) {
        List<ExchangeRate> rates = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : SUPPORTED_CURRENCIES.entrySet()) {
            String code = entry.getKey();
            String[] info = entry.getValue();
            double rateToVnd = response.getRateToVnd(code);

            if (rateToVnd > 0) {
                rates.add(new ExchangeRate(code, info[0], info[1], rateToVnd));
            }
        }

        adapter.updateData(rates);
    }

    /**
     * Tính toán quy đổi VND → ngoại tệ dựa trên input hiện tại.
     */
    private void calculateConversion() {
        if (currentResponse == null) return;

        String inputText = etAmountVnd.getText().toString().trim();
        if (inputText.isEmpty()) {
            tvConvertedAmount.setText("0.00");
            return;
        }

        try {
            double vndAmount = Double.parseDouble(inputText);
            String selectedCurrency = currencyCodes.get(spinnerCurrency.getSelectedItemPosition());
            double rateToVnd = currentResponse.getRateToVnd(selectedCurrency);

            if (rateToVnd > 0) {
                double convertedAmount = vndAmount / rateToVnd;
                tvConvertedAmount.setText(formatConvertedAmount(convertedAmount));
            } else {
                tvConvertedAmount.setText("N/A");
            }
        } catch (NumberFormatException e) {
            tvConvertedAmount.setText("0.00");
        }
    }

    /**
     * Format kết quả quy đổi: thông minh dựa trên độ lớn.
     */
    private String formatConvertedAmount(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat formatter;

        if (amount >= 1000) {
            formatter = new DecimalFormat("#,##0.00", symbols);
        } else if (amount >= 1) {
            formatter = new DecimalFormat("#,##0.0000", symbols);
        } else {
            formatter = new DecimalFormat("0.000000", symbols);
        }

        return formatter.format(amount);
    }

    /**
     * Hiện thời gian cập nhật tỷ giá lần cuối.
     */
    private void updateLastUpdateText(String timeUtc) {
        if (timeUtc != null && !timeUtc.isEmpty()) {
            tvLastUpdate.setText(getString(R.string.label_last_update, timeUtc));
            tvLastUpdate.setVisibility(View.VISIBLE);
        } else {
            tvLastUpdate.setVisibility(View.GONE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
