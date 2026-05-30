package com.ptithcm.finacemanager.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
 * Màn hình quy đổi tỷ giá đa chiều.
 *
 * <p>Hỗ trợ quy đổi bất kỳ cặp tiền tệ nào (VND↔USD, USD↔EUR, JPY↔KRW…)
 * bằng cross-rate từ 1 lần gọi API duy nhất (base=VND).
 *
 * <p>Gồm 2 phần:
 * <ol>
 *     <li><b>Converter Card</b>: 2 Spinner (FROM/TO) + nút Swap + kết quả realtime</li>
 *     <li><b>Rate Table</b>: Bảng tỷ giá 10 loại tiền, cập nhật theo FROM đang chọn</li>
 * </ol>
 */
public class ExchangeRateActivity extends AppCompatActivity {

    // ── 10 loại tiền tệ hỗ trợ (bao gồm VND) ──
    private static final Map<String, String[]> CURRENCIES = new LinkedHashMap<>();

    static {
        // { mã tiền, tên tiếng Việt, cờ emoji }
        CURRENCIES.put("VND", new String[]{"Việt Nam Đồng", "🇻🇳"});
        CURRENCIES.put("USD", new String[]{"Đô la Mỹ", "🇺🇸"});
        CURRENCIES.put("EUR", new String[]{"Euro", "🇪🇺"});
        CURRENCIES.put("GBP", new String[]{"Bảng Anh", "🇬🇧"});
        CURRENCIES.put("JPY", new String[]{"Yên Nhật", "🇯🇵"});
        CURRENCIES.put("KRW", new String[]{"Won Hàn Quốc", "🇰🇷"});
        CURRENCIES.put("CNY", new String[]{"Nhân dân tệ", "🇨🇳"});
        CURRENCIES.put("AUD", new String[]{"Đô la Úc", "🇦🇺"});
        CURRENCIES.put("SGD", new String[]{"Đô la Singapore", "🇸🇬"});
        CURRENCIES.put("THB", new String[]{"Baht Thái", "🇹🇭"});
    }

    // ── Views ──
    private EditText etAmountFrom;
    private TextView tvConvertedAmount, tvDirectRate, tvLastUpdate;
    private TextView tvCacheBanner, tvRefresh, tvRateTableTitle;
    private Spinner spinnerFrom, spinnerTo;
    private ImageView btnSwap;
    private RecyclerView rvRates;
    private View layoutLoading, layoutError, layoutRateHeader;

    // ── Data ──
    private ExchangeRateAdapter adapter;
    private ExchangeRateResponse currentResponse;
    private final List<String> currencyCodes = new ArrayList<>(CURRENCIES.keySet());

    // ── Spinner display items: "🇻🇳 VND", "🇺🇸 USD" … ──
    private final List<String> spinnerDisplayItems = new ArrayList<>();

    // Suppress spinner re-trigger khi swap
    private boolean suppressSpinnerEvents = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);

        buildSpinnerDisplayItems();
        initViews();
        initListeners();
        setupSpinners();
        setupRecyclerView();
        fetchRates(false);
    }

    private void buildSpinnerDisplayItems() {
        for (Map.Entry<String, String[]> entry : CURRENCIES.entrySet()) {
            String flag = entry.getValue()[1];
            spinnerDisplayItems.add(flag + " " + entry.getKey());
        }
    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        etAmountFrom = findViewById(R.id.et_amount_from);
        tvConvertedAmount = findViewById(R.id.tv_converted_amount);
        tvDirectRate = findViewById(R.id.tv_direct_rate);
        tvLastUpdate = findViewById(R.id.tv_last_update);
        tvCacheBanner = findViewById(R.id.tv_cache_banner);
        tvRefresh = findViewById(R.id.tv_refresh);
        tvRateTableTitle = findViewById(R.id.tv_rate_table_title);
        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        btnSwap = findViewById(R.id.btn_swap);
        rvRates = findViewById(R.id.rv_rates);
        layoutLoading = findViewById(R.id.layout_loading);
        layoutError = findViewById(R.id.layout_error);
        layoutRateHeader = findViewById(R.id.layout_rate_header);
    }

    private void initListeners() {
        // Realtime conversion khi nhập số
        etAmountFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateConversion();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Swap FROM ↔ TO
        btnSwap.setOnClickListener(v -> swapCurrencies());

        // Refresh
        tvRefresh.setOnClickListener(v -> fetchRates(true));
        findViewById(R.id.btn_retry).setOnClickListener(v -> fetchRates(true));
    }

    private void setupSpinners() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_currency_item, spinnerDisplayItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(spinnerAdapter);
        spinnerTo.setAdapter(spinnerAdapter);

        // Mặc định: FROM = VND (index 0), TO = USD (index 1)
        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(1);

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressSpinnerEvents) return;

                // Validate: không cho chọn 2 loại tiền giống nhau
                if (spinnerFrom.getSelectedItemPosition() == spinnerTo.getSelectedItemPosition()) {
                    Toast.makeText(ExchangeRateActivity.this,
                            R.string.msg_same_currency, Toast.LENGTH_SHORT).show();

                    // Auto-sửa: đổi spinner kia sang loại tiền khác
                    suppressSpinnerEvents = true;
                    if (parent.getId() == R.id.spinner_from) {
                        // FROM vừa đổi trùng TO → sửa TO
                        spinnerTo.setSelection((position + 1) % currencyCodes.size());
                    } else {
                        // TO vừa đổi trùng FROM → sửa FROM
                        spinnerFrom.setSelection((position + 1) % currencyCodes.size());
                    }
                    suppressSpinnerEvents = false;
                }

                onCurrencySelectionChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerFrom.setOnItemSelectedListener(spinnerListener);
        spinnerTo.setOnItemSelectedListener(spinnerListener);
    }

    private void setupRecyclerView() {
        adapter = new ExchangeRateAdapter(new ArrayList<>());
        adapter.setOnRateClickListener(rate -> {
            // Tap vào tiền tệ → chọn làm TO currency
            int index = currencyCodes.indexOf(rate.getCurrencyCode());
            if (index >= 0) {
                spinnerTo.setSelection(index);
                if (etAmountFrom.getText().toString().isEmpty()) {
                    etAmountFrom.requestFocus();
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
                    public void onSuccess(ExchangeRateResponse response, boolean isStaleCache) {
                        currentResponse = response;
                        showContent(isStaleCache);
                        onCurrencySelectionChanged();
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
        layoutRateHeader.setVisibility(View.GONE);
    }

    private void showContent(boolean isStaleCache) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        rvRates.setVisibility(View.VISIBLE);
        layoutRateHeader.setVisibility(View.VISIBLE);

        // Banner chỉ hiện khi dùng cache CŨ HẾT HẠN (API thật sự lỗi)
        tvCacheBanner.setVisibility(isStaleCache ? View.VISIBLE : View.GONE);
    }

    private void showError() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        rvRates.setVisibility(View.GONE);
        layoutRateHeader.setVisibility(View.GONE);
    }

    // ── Business Logic ──────────────────────────────────────────

    /**
     * Gọi khi FROM hoặc TO thay đổi: cập nhật converter + bảng tỷ giá.
     */
    private void onCurrencySelectionChanged() {
        if (currentResponse == null) return;

        String fromCode = getSelectedFromCode();
        String toCode = getSelectedToCode();

        // Cập nhật direct rate label
        updateDirectRateLabel(fromCode, toCode);

        // Cập nhật bảng tỷ giá (theo FROM)
        populateRateTable(fromCode);

        // Cập nhật section title
        tvRateTableTitle.setText(getString(R.string.title_rate_table_for, fromCode));

        // Tính lại conversion
        calculateConversion();
    }

    /**
     * Đảo FROM ↔ TO với animation.
     */
    private void swapCurrencies() {
        int fromIndex = spinnerFrom.getSelectedItemPosition();
        int toIndex = spinnerTo.getSelectedItemPosition();

        // Suppress events trong khi swap
        suppressSpinnerEvents = true;
        spinnerFrom.setSelection(toIndex);
        spinnerTo.setSelection(fromIndex);
        suppressSpinnerEvents = false;

        // Đảo luôn số tiền input ↔ result
        String currentInput = etAmountFrom.getText().toString();
        String currentResult = tvConvertedAmount.getText().toString();

        if (!currentResult.isEmpty() && !"0.00".equals(currentResult) && !"N/A".equals(currentResult)) {
            etAmountFrom.setText(currentResult);
            etAmountFrom.setSelection(etAmountFrom.getText().length());
        }

        // Animate swap button
        btnSwap.animate()
                .rotationBy(180f)
                .setDuration(300)
                .start();

        // Trigger update
        onCurrencySelectionChanged();
    }

    /**
     * Tính quy đổi FROM → TO realtime.
     */
    private void calculateConversion() {
        if (currentResponse == null) return;

        String inputText = etAmountFrom.getText().toString().trim();
        if (inputText.isEmpty()) {
            tvConvertedAmount.setText("0.00");
            return;
        }

        try {
            double amount = Double.parseDouble(inputText);
            String fromCode = getSelectedFromCode();
            String toCode = getSelectedToCode();

            double result = currentResponse.convert(amount, fromCode, toCode);
            tvConvertedAmount.setText(result > 0 ? formatAmount(result) : "N/A");
        } catch (NumberFormatException e) {
            tvConvertedAmount.setText("0.00");
        }
    }

    /**
     * Build bảng tỷ giá: 1 [currency] = ? [fromCode].
     * Loại trừ chính fromCode khỏi danh sách.
     */
    private void populateRateTable(String fromCode) {
        List<ExchangeRate> rates = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : CURRENCIES.entrySet()) {
            String code = entry.getKey();
            if (code.equals(fromCode)) continue; // Bỏ chính nó

            String[] info = entry.getValue();
            double rateInFromCurrency = currentResponse.getRate(code, fromCode);

            if (rateInFromCurrency > 0) {
                rates.add(new ExchangeRate(code, info[0], info[1], rateInFromCurrency));
            }
        }

        adapter.updateData(rates, fromCode);
    }

    /**
     * Hiện label: "1 USD = 25,100 VNĐ".
     */
    private void updateDirectRateLabel(String fromCode, String toCode) {
        double rate = currentResponse.getRate(fromCode, toCode);
        if (rate > 0) {
            tvDirectRate.setText(String.format("1 %s = %s %s", fromCode, formatAmount(rate), toCode));
            tvDirectRate.setVisibility(View.VISIBLE);
        } else {
            tvDirectRate.setVisibility(View.GONE);
        }
    }

    private void updateLastUpdateText(String timeUtc) {
        if (timeUtc != null && !timeUtc.isEmpty()) {
            tvLastUpdate.setText(getString(R.string.label_last_update, timeUtc));
            tvLastUpdate.setVisibility(View.VISIBLE);
        } else {
            tvLastUpdate.setVisibility(View.GONE);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private String getSelectedFromCode() {
        return currencyCodes.get(spinnerFrom.getSelectedItemPosition());
    }

    private String getSelectedToCode() {
        return currencyCodes.get(spinnerTo.getSelectedItemPosition());
    }

    /**
     * Format số tiền thông minh dựa trên độ lớn.
     */
    private String formatAmount(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat formatter;

        if (amount >= 1000) {
            formatter = new DecimalFormat("#,##0.00", symbols);
        } else if (amount >= 1) {
            formatter = new DecimalFormat("#,##0.0000", symbols);
        } else if (amount >= 0.01) {
            formatter = new DecimalFormat("0.000000", symbols);
        } else {
            formatter = new DecimalFormat("0.00000000", symbols);
        }

        return formatter.format(amount);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
