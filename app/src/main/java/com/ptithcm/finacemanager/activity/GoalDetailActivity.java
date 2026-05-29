package com.ptithcm.finacemanager.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.GoalContributionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.dialog.AddGoalDialog;
import com.ptithcm.finacemanager.model.GoalContribution;
import com.ptithcm.finacemanager.model.SavingsGoal;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.CustomToast;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Màn hình chi tiết mục tiêu tiết kiệm.
 *
 * <p>Hiển thị thông tin mục tiêu (tên, icon, tiến độ, số tiền hiện tại/mục tiêu),
 * cho phép đóng góp tiền, xem lịch sử đóng góp, sửa và xóa mục tiêu.
 *
 * <p>Nhận EXTRA_GOAL_ID qua Intent để load dữ liệu từ {@link DBManager}.
 */
public class GoalDetailActivity extends AppCompatActivity {

    // Key cho Intent Extra (chưa có trong Constants, dùng trực tiếp)
    private static final String EXTRA_GOAL_ID = "extra_goal_id";

    // === Views ===
    private TextView textViewGoalIcon, textViewGoalName;
    private TextView textViewPercentage, textViewCurrentAmount, textViewTargetAmount;
    private TextView textViewRemainingAmount, textViewDaysRemaining;
    private TextView textViewEmpty;
    private ProgressBar progressBarGoal;
    private MaterialCardView cardProgress;
    private MaterialButton buttonContribute;
    private RecyclerView recyclerViewContributions;

    // === Data ===
    private DBManager databaseManager;
    private GoalContributionAdapter contributionAdapter;
    private List<GoalContribution> contributions = new ArrayList<>();
    private int goalId;
    private SavingsGoal currentGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail);

        databaseManager = DBManager.getInstance(this);
        goalId = getIntent().getIntExtra(EXTRA_GOAL_ID, -1);

        // Kiểm tra goalId hợp lệ
        if (goalId == -1) {
            finish();
            return;
        }

        initViews();
        initListeners();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    /**
     * Khởi tạo các view từ layout.
     */
    private void initViews() {
        textViewGoalIcon = findViewById(R.id.tv_goal_icon);
        textViewGoalName = findViewById(R.id.tv_goal_name);
        textViewPercentage = findViewById(R.id.tv_percentage);
        textViewCurrentAmount = findViewById(R.id.tv_current_amount);
        textViewTargetAmount = findViewById(R.id.tv_target_amount);
        textViewRemainingAmount = findViewById(R.id.tv_remaining_amount);
        textViewDaysRemaining = findViewById(R.id.tv_days_remaining);
        textViewEmpty = findViewById(R.id.tv_empty);
        progressBarGoal = findViewById(R.id.pb_goal_progress);
        cardProgress = findViewById(R.id.card_progress);
        buttonContribute = findViewById(R.id.btn_contribute);
        recyclerViewContributions = findViewById(R.id.rv_contributions);

        // Nút back
        findViewById(R.id.iv_back).setOnClickListener(view -> finish());
    }

    /**
     * Gắn sự kiện cho các nút: Đóng góp, Sửa, Xóa.
     */
    private void initListeners() {
        // Nút đóng góp
        buttonContribute.setOnClickListener(view -> showContributeDialog());

        // Sửa mục tiêu
        findViewById(R.id.iv_edit).setOnClickListener(view -> showEditGoalDialog());

        // Xóa mục tiêu
        findViewById(R.id.iv_delete).setOnClickListener(view -> confirmDeleteGoal());
    }

    /**
     * Cài đặt RecyclerView cho danh sách đóng góp.
     */
    private void setupRecyclerView() {
        contributionAdapter = new GoalContributionAdapter(contributions, this);
        contributionAdapter.setOnItemClickListener(this::confirmDeleteContribution);
        recyclerViewContributions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContributions.setAdapter(contributionAdapter);
    }

    // ====================================================================
    // LOAD DATA
    // ====================================================================

    /**
     * Load dữ liệu mục tiêu và danh sách đóng góp từ database.
     * Cập nhật UI: progress, số tiền, ngày còn lại, danh sách contributions.
     */
    private void loadData() {
        currentGoal = databaseManager.getSavingsGoalById(goalId);
        if (currentGoal == null) {
            finish();
            return;
        }

        // === Toolbar ===
        textViewGoalIcon.setText(currentGoal.getIcon() != null ? currentGoal.getIcon() : "🎯");
        textViewGoalName.setText(currentGoal.getName());

        // === Progress Card ===
        int percentage = currentGoal.getProgressPercentage();

        // Hiển thị phần trăm
        textViewPercentage.setText(percentage + "%");

        // Animate progress bar
        animateProgressBar(percentage);

        // Số tiền hiện tại / mục tiêu
        textViewCurrentAmount.setText(CurrencyFormatter.format(currentGoal.getCurrentAmount()));
        textViewTargetAmount.setText(CurrencyFormatter.format(currentGoal.getTargetAmount()));

        // Số tiền còn thiếu
        double remaining = currentGoal.getRemainingAmount();
        textViewRemainingAmount.setText(getString(R.string.label_remaining_amount) + ": " + CurrencyFormatter.format(remaining));

        // Ngày còn lại
        updateDaysRemaining();

        // Gradient background cho card theo màu mục tiêu
        applyCardGradient();

        // === Danh sách đóng góp ===
        contributions = databaseManager.getContributionsByGoalId(goalId);
        contributionAdapter.updateData(contributions);

        // Hiển thị empty state nếu không có đóng góp
        if (contributions.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewContributions.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewContributions.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Animate progress bar từ 0 đến giá trị hiện tại.
     */
    private void animateProgressBar(int targetProgress) {
        ObjectAnimator animation = ObjectAnimator.ofInt(
                progressBarGoal, "progress", 0, targetProgress);
        animation.setDuration(800);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();
    }

    /**
     * Áp dụng gradient background cho card tiến độ dựa trên màu mục tiêu.
     * Tạo hiệu ứng gradient từ màu goal → màu tối hơn.
     */
    private void applyCardGradient() {
        String colorHex = currentGoal.getColor();
        if (colorHex == null || colorHex.isEmpty()) {
            colorHex = "#4CAF50"; // Mặc định xanh lá
        }

        try {
            int goalColor = Color.parseColor(colorHex);
            // Tạo màu tối hơn cho gradient
            int darkerColor = darkenColor(goalColor, 0.3f);

            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{goalColor, darkerColor}
            );
            gradient.setCornerRadius(getResources().getDimension(R.dimen.card_radius));

            // Áp dụng gradient cho container bên trong card
            View progressContainer = findViewById(R.id.ll_progress_container);
            progressContainer.setBackground(gradient);
        } catch (IllegalArgumentException e) {
            // Nếu parse color thất bại, giữ màu mặc định
            cardProgress.setCardBackgroundColor(getResources().getColor(R.color.color_primary));
        }
    }

    /**
     * Làm tối một màu theo tỷ lệ cho trước.
     *
     * @param color  Màu gốc
     * @param factor Tỷ lệ làm tối (0.0 = giữ nguyên, 1.0 = đen hoàn toàn)
     * @return Màu đã được làm tối
     */
    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * (1 - factor));
        int g = Math.round(Color.green(color) * (1 - factor));
        int b = Math.round(Color.blue(color) * (1 - factor));
        return Color.argb(a, Math.max(r, 0), Math.max(g, 0), Math.max(b, 0));
    }

    /**
     * Cập nhật hiển thị số ngày còn lại đến hạn mục tiêu.
     * Sử dụng string resources: label_days_remaining, label_due_today, label_overdue, label_no_deadline.
     */
    private void updateDaysRemaining() {
        String targetDate = currentGoal.getTargetDate();
        if (targetDate == null || targetDate.isEmpty()) {
            textViewDaysRemaining.setText(R.string.label_no_deadline);
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date target = sdf.parse(targetDate);
            Date today = new Date();

            if (target != null) {
                long diffMillis = target.getTime() - today.getTime();
                long daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis);

                if (daysLeft > 0) {
                    textViewDaysRemaining.setText(getString(R.string.label_days_remaining, (int) daysLeft));
                } else if (daysLeft == 0) {
                    textViewDaysRemaining.setText(R.string.label_due_today);
                } else {
                    textViewDaysRemaining.setText(R.string.label_overdue);
                }
            }
        } catch (Exception e) {
            textViewDaysRemaining.setText(R.string.label_no_deadline);
        }
    }

    // ====================================================================
    // ĐÓNG GÓP
    // ====================================================================

    /**
     * Hiển thị dialog đóng góp tiền vào mục tiêu.
     * Dialog sử dụng layout dialog_contribute_goal.xml.
     */
    private void showContributeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_contribute_goal);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Lấy các view trong dialog
        TextInputLayout tilAmount = dialog.findViewById(R.id.til_contribute_amount);
        TextInputEditText etAmount = dialog.findViewById(R.id.et_contribute_amount);
        TextInputEditText etNote = dialog.findViewById(R.id.et_contribute_note);
        TextView tvRemainingInfo = dialog.findViewById(R.id.tv_remaining_info);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnContribute = dialog.findViewById(R.id.btn_contribute);

        // Hiển thị số tiền còn thiếu trong dialog
        double remaining = currentGoal.getRemainingAmount();
        tvRemainingInfo.setText(getString(R.string.label_remaining_amount) + ": " + CurrencyFormatter.format(remaining));

        // Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Đóng góp
        btnContribute.setOnClickListener(v -> {
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";

            // Validate số tiền
            if (amountStr.isEmpty()) {
                tilAmount.setError(getString(R.string.error_contribute_amount_invalid));
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                tilAmount.setError(getString(R.string.error_contribute_amount_invalid));
                return;
            }
            tilAmount.setError(null);

            // Lấy ghi chú
            String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

            // Tạo GoalContribution và lưu vào database
            GoalContribution contribution = new GoalContribution(
                    goalId,
                    amount,
                    note.isEmpty() ? null : note,
                    DateUtils.getTodayDB()
            );
            databaseManager.addGoalContribution(contribution);

            CustomToast.showSuccess(this, getString(R.string.msg_contributed_success));

            dialog.dismiss();

            // Refresh data và animate bounce
            loadData();
            animateBounce();
        });

        dialog.show();
    }

    /**
     * Hiệu ứng bounce (nhảy) cho nút đóng góp sau khi đóng góp thành công.
     */
    private void animateBounce() {
        buttonContribute.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.scale_bounce));
    }

    // ====================================================================
    // SỬA MỤC TIÊU
    // ====================================================================

    /**
     * Hiển thị dialog sửa mục tiêu tiết kiệm.
     * Sử dụng lại {@link AddGoalDialog} ở chế độ edit.
     */
    private void showEditGoalDialog() {
        currentGoal = databaseManager.getSavingsGoalById(goalId);
        if (currentGoal == null) return;

        AddGoalDialog dialog = new AddGoalDialog();
        dialog.setEditGoal(currentGoal);
        dialog.setOnGoalSavedListener(() -> {
            loadData();
            CustomToast.showSuccess(this, getString(R.string.msg_goal_updated));
        });
        dialog.show(getSupportFragmentManager(), "EditGoalDialog");
    }

    // ====================================================================
    // XÓA MỤC TIÊU
    // ====================================================================

    /**
     * Hiển thị dialog xác nhận xóa mục tiêu.
     */
    private void confirmDeleteGoal() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_confirm_delete_goal)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    databaseManager.deleteSavingsGoal(goalId);
                    CustomToast.showSuccess(this, getString(R.string.msg_goal_deleted));
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    // ====================================================================
    // XÓA ĐÓNG GÓP
    // ====================================================================

    /**
     * Hiển thị dialog xác nhận xóa một bản ghi đóng góp.
     * Sau khi xóa, refresh dữ liệu để cập nhật currentAmount của goal.
     *
     * @param contribution Đóng góp cần xóa
     */
    private void confirmDeleteContribution(GoalContribution contribution) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.msg_confirm_delete_contribution)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    databaseManager.deleteGoalContribution(
                            contribution.getId(),
                            contribution.getGoalId(),
                            contribution.getAmount()
                    );
                    loadData(); // Refresh để cập nhật progress
                    CustomToast.showSuccess(this, getString(R.string.msg_contribution_deleted));
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    // ====================================================================
    // FINISH VỚI ANIMATION
    // ====================================================================

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
