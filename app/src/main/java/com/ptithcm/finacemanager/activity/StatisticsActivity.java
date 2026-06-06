package com.ptithcm.finacemanager.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.CategoryExpenseAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.CategoryExpense;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Thống Kê chi tiêu theo danh mục.
 * Hiển thị biểu đồ tròn (PieChart - Donut) và danh sách chi tiết bên dưới.
 * Cho phép người dùng chuyển đổi giữa các tháng bằng nút mũi tên.
 */
public class StatisticsActivity extends AppCompatActivity {

    private PieChart pieChartExpense;
    private RecyclerView recyclerViewCategoryExpenses;
    private TextView textViewCurrentMonth;
    private ImageButton buttonPreviousMonth;
    private ImageButton buttonNextMonth;
    private LinearLayout layoutEmptyState;
    private View cardCategoryList;

    private DBManager databaseManager;
    private CategoryExpenseAdapter categoryExpenseAdapter;

    private int currentMonth;
    private int currentYear;

    // Phạm vi tháng/năm có giao dịch (dùng để disable nút chuyển tháng)
    private int earliestMonth;
    private int earliestYear;
    private int latestMonth;
    private int latestYear;

    // Bảng màu Material Design cho biểu đồ (phong phú, dễ nhìn, hài hòa)
    private static final int[] CHART_COLORS = {
            Color.parseColor("#4CAF50"),  // Xanh lá
            Color.parseColor("#2196F3"),  // Xanh dương
            Color.parseColor("#FF9800"),  // Cam
            Color.parseColor("#9C27B0"),  // Tím
            Color.parseColor("#F44336"),  // Đỏ
            Color.parseColor("#009688"),  // Teal
            Color.parseColor("#E91E63"),  // Hồng
            Color.parseColor("#3F51B5"),  // Indigo
            Color.parseColor("#CDDC39"),  // Lime
            Color.parseColor("#795548"),  // Nâu
            Color.parseColor("#00BCD4"),  // Cyan
            Color.parseColor("#FF5722")   // Deep Orange
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        databaseManager = DBManager.getInstance(this);

        // Mặc định hiển thị tháng hiện tại
        currentMonth = DateUtils.getCurrentMonth();
        currentYear = DateUtils.getCurrentYear();

        initViews();
        initListeners();
        setupPieChart();
        loadTransactionDateRange();
        loadData();
    }

    private void initViews() {
        // Toolbar với nút Back
        MaterialToolbar toolbarStatistics = findViewById(R.id.toolbar_statistics);
        toolbarStatistics.setNavigationOnClickListener(view -> finish());

        pieChartExpense = findViewById(R.id.pie_chart_expense);
        recyclerViewCategoryExpenses = findViewById(R.id.rv_category_expenses);
        textViewCurrentMonth = findViewById(R.id.tv_current_month);
        buttonPreviousMonth = findViewById(R.id.btn_previous_month);
        buttonNextMonth = findViewById(R.id.btn_next_month);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        cardCategoryList = findViewById(R.id.card_category_list);

        // Cấu hình RecyclerView
        recyclerViewCategoryExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategoryExpenses.setHasFixedSize(false);

        // Đường phân cách giữa các item danh mục
        androidx.recyclerview.widget.DividerItemDecoration divider =
                new androidx.recyclerview.widget.DividerItemDecoration(
                        this, LinearLayoutManager.VERTICAL);
        recyclerViewCategoryExpenses.addItemDecoration(divider);
    }

    private void initListeners() {
        // Nút lùi tháng
        buttonPreviousMonth.setOnClickListener(view -> {
            currentMonth--;
            if (currentMonth < 1) {
                currentMonth = 12;
                currentYear--;
            }
            loadData();
        });

        // Nút tiến tháng
        buttonNextMonth.setOnClickListener(view -> {
            currentMonth++;
            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }
            loadData();
        });

        // Nhấn vào tháng → mở DatePicker chọn tháng/năm
        textViewCurrentMonth.setOnClickListener(view -> showMonthYearPicker());
    }

    /**
     * Hiển thị dialog chọn tháng. Chỉ hiện các tháng thực sự có giao dịch.
     */
    private void showMonthYearPicker() {
        List<String> availableMonths = databaseManager.getDistinctTransactionMonths();

        if (availableMonths.isEmpty()) {
            return;
        }

        // Chuyển "yyyy-MM" thành label hiển thị "Tháng MM/yyyy"
        String[] displayLabels = new String[availableMonths.size()];
        int checkedIndex = 0;
        String currentKey = String.format(Locale.getDefault(), "%04d-%02d", currentYear, currentMonth);

        for (int i = 0; i < availableMonths.size(); i++) {
            String ym = availableMonths.get(i); // "2026-06"
            String[] parts = ym.split("-");
            displayLabels[i] = String.format("Tháng %s/%s", parts[1], parts[0]);

            if (ym.equals(currentKey)) {
                checkedIndex = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn tháng")
                .setSingleChoiceItems(displayLabels, checkedIndex, (dialog, which) -> {
                    String selected = availableMonths.get(which);
                    String[] parts = selected.split("-");
                    currentYear = Integer.parseInt(parts[0]);
                    currentMonth = Integer.parseInt(parts[1]);
                    loadData();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Cấu hình giao diện cơ bản cho PieChart (chỉ gọi 1 lần lúc khởi tạo).
     */
    private void setupPieChart() {
        // Bật dạng Donut (có lỗ ở giữa)
        pieChartExpense.setDrawHoleEnabled(true);
        pieChartExpense.setHoleColor(Color.TRANSPARENT);
        pieChartExpense.setHoleRadius(55f);
        pieChartExpense.setTransparentCircleRadius(60f);
        pieChartExpense.setTransparentCircleColor(Color.WHITE);
        pieChartExpense.setTransparentCircleAlpha(80);

        // Tắt legend mặc định (ta dùng RecyclerView làm legend)
        pieChartExpense.getLegend().setEnabled(false);

        // Tắt description mặc định của thư viện
        pieChartExpense.getDescription().setEnabled(false);

        // Cho phép xoay biểu đồ bằng tay
        pieChartExpense.setRotationEnabled(true);
        pieChartExpense.setHighlightPerTapEnabled(true);

        // Bắt đầu từ góc trên cùng
        pieChartExpense.setRotationAngle(270f);

        // Sử dụng phần trăm
        pieChartExpense.setUsePercentValues(true);

        // TẮT chữ (tên danh mục) hiển thị trên các slice của đồ thị
        pieChartExpense.setDrawEntryLabels(false);

        // Khoảng đệm
        pieChartExpense.setExtraOffsets(8f, 8f, 8f, 8f);
    }

    /**
     * Tải dữ liệu chi tiêu theo tháng/năm hiện tại và cập nhật giao diện.
     */
    private void loadData() {
        // Cập nhật tiêu đề tháng (VD: "Tháng 05/2026")
        String monthLabel = String.format(Locale.getDefault(),
                getString(R.string.format_month_year), currentMonth, currentYear);
        textViewCurrentMonth.setText(monthLabel);

        // Truy vấn dữ liệu chi tiêu theo danh mục từ DB
        List<CategoryExpense> categoryExpenseList =
                databaseManager.getExpensesByCategory(currentMonth, currentYear);

        if (categoryExpenseList.isEmpty()) {
            // Hiển thị trạng thái trống
            pieChartExpense.setVisibility(View.GONE);
            cardCategoryList.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            pieChartExpense.clear();
        } else {
            // Hiển thị biểu đồ và danh sách
            pieChartExpense.setVisibility(View.VISIBLE);
            cardCategoryList.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);

            updatePieChart(categoryExpenseList);
            updateCategoryList(categoryExpenseList);
        }

        // Cập nhật trạng thái enable/disable của nút chuyển tháng
        updateNavigationButtons();
    }

    /**
     * Đổ dữ liệu vào PieChart.
     */
    private void updatePieChart(List<CategoryExpense> categoryExpenseList) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<Integer> colorList = new ArrayList<>();
        double totalExpense = 0;

        for (int index = 0; index < categoryExpenseList.size(); index++) {
            CategoryExpense categoryExpense = categoryExpenseList.get(index);

            // Chuyển tên danh mục (resource key) sang tên hiển thị theo I18N
            String categoryName = categoryExpense.getCategoryName();
            int stringResourceId = getResources().getIdentifier(
                    categoryName, "string", getPackageName());
            String displayName = stringResourceId != 0
                    ? getString(stringResourceId) : categoryName;

            pieEntries.add(new PieEntry(
                    (float) categoryExpense.getTotalAmount(), displayName));

            colorList.add(CHART_COLORS[index % CHART_COLORS.length]);
            totalExpense += categoryExpense.getTotalAmount();
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries,
                getString(R.string.label_expense_breakdown));

        // Gán bảng màu
        pieDataSet.setColors(colorList);

        // Khoảng cách giữa các phần (slice)
        pieDataSet.setSliceSpace(2f);

        // Kích thước hình vẽ chỉ dẫn bên ngoài
        pieDataSet.setSelectionShift(6f);

        // Hiển thị giá trị phần trăm bên trong từng slice
        pieDataSet.setValueTextSize(11f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

        // Đặt vị trí value bên trong slice
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChartExpense));

        pieChartExpense.setData(pieData);

        // Hiển thị tổng chi tiêu ở giữa lỗ Donut
        SpannableString centerText = createCenterText(totalExpense);
        pieChartExpense.setCenterText(centerText);
        pieChartExpense.setCenterTextSize(14f);

        // Animation mượt mà khi load dữ liệu
        pieChartExpense.animateY(800, Easing.EaseInOutQuad);
        pieChartExpense.invalidate();
    }

    /**
     * Tạo văn bản hiển thị ở chính giữa lỗ Donut.
     * Dòng 1: "Tổng chi" (nhỏ, màu xám)
     * Dòng 2: "x,xxx,xxx ₫" (lớn, đậm, màu đỏ)
     */
    private SpannableString createCenterText(double totalExpense) {
        String totalLabel = getString(R.string.label_total_expense);
        String totalFormatted = CurrencyFormatter.format(totalExpense);
        String fullText = totalLabel + "\n" + totalFormatted;

        SpannableString spannableString = new SpannableString(fullText);

        // Dòng 1 (label): nhỏ hơn, màu xám
        int labelEnd = totalLabel.length();
        spannableString.setSpan(new RelativeSizeSpan(0.75f),
                0, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.color_text_secondary)),
                0, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Dòng 2 (số tiền): đậm, màu đỏ
        int amountStart = labelEnd + 1;
        spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                amountStart, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.color_expense)),
                amountStart, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    /**
     * Cập nhật danh sách chi tiết theo danh mục (RecyclerView) bên dưới biểu đồ.
     */
    private void updateCategoryList(List<CategoryExpense> categoryExpenseList) {
        if (categoryExpenseAdapter == null) {
            categoryExpenseAdapter = new CategoryExpenseAdapter(
                    categoryExpenseList, this, CHART_COLORS);
            categoryExpenseAdapter.setOnCategoryClickListener(categoryExpense -> {
                Intent intent = new Intent(this, CategoryTransactionsActivity.class);
                intent.putExtra("EXTRA_CATEGORY_ID", categoryExpense.getCategoryId());
                intent.putExtra("EXTRA_CATEGORY_NAME", categoryExpense.getCategoryName());
                intent.putExtra("EXTRA_MONTH", currentMonth);
                intent.putExtra("EXTRA_YEAR", currentYear);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
            recyclerViewCategoryExpenses.setAdapter(categoryExpenseAdapter);
        } else {
            categoryExpenseAdapter.updateData(categoryExpenseList);
        }
    }

    /**
     * Tải phạm vi tháng/năm có giao dịch từ cơ sở dữ liệu.
     * Dùng để xác định giới hạn cho các nút chuyển tháng.
     */
    private void loadTransactionDateRange() {
        String earliestDate = databaseManager.getEarliestTransactionDate();
        String latestDate = databaseManager.getLatestTransactionDate();

        if (earliestDate != null && earliestDate.length() >= 7) {
            // Định dạng ngày: "yyyy-MM-dd" → tách lấy tháng và năm
            String[] earliestParts = earliestDate.split("-");
            earliestYear = Integer.parseInt(earliestParts[0]);
            earliestMonth = Integer.parseInt(earliestParts[1]);
        } else {
            // Nếu không có giao dịch nào, giới hạn = tháng hiện tại
            earliestYear = currentYear;
            earliestMonth = currentMonth;
        }

        if (latestDate != null && latestDate.length() >= 7) {
            String[] latestParts = latestDate.split("-");
            latestYear = Integer.parseInt(latestParts[0]);
            latestMonth = Integer.parseInt(latestParts[1]);
        } else {
            latestYear = currentYear;
            latestMonth = currentMonth;
        }
    }

    /**
     * Cập nhật trạng thái enable/disable và độ mờ của nút chuyển tháng.
     * - Nếu đang ở tháng sớm nhất có giao dịch → mờ nút Lùi, không cho bấm.
     * - Nếu đang ở tháng muộn nhất có giao dịch → mờ nút Tiến, không cho bấm.
     */
    private void updateNavigationButtons() {
        // Kiểm tra nút Lùi tháng
        boolean canGoPrevious = (currentYear > earliestYear)
                || (currentYear == earliestYear && currentMonth > earliestMonth);
        buttonPreviousMonth.setEnabled(canGoPrevious);
        buttonPreviousMonth.setAlpha(canGoPrevious ? 1.0f : 0.3f);

        // Kiểm tra nút Tiến tháng
        boolean canGoNext = (currentYear < latestYear)
                || (currentYear == latestYear && currentMonth < latestMonth);
        buttonNextMonth.setEnabled(canGoNext);
        buttonNextMonth.setAlpha(canGoNext ? 1.0f : 0.3f);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
