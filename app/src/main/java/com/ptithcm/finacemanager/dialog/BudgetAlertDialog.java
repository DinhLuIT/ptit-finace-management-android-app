package com.ptithcm.finacemanager.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;

/**
 * Dialog cảnh báo ngân sách siêu đẹp (Premium Budget Alert Dialog).
 *
 * <p>Hiển thị giao diện cảnh báo rực rỡ với gradient, animation bounce và progress bar
 * khi người dùng chi tiêu vượt ngưỡng 80% hoặc 100% ngân sách hũ.
 *
 * <p>Cách sử dụng (từ Activity):
 * <pre>
 *   BudgetAlertDialog dialog = BudgetAlertDialog.newInstance(
 *       potName, budgetLimit, currentBalance, spentPercentage, isDanger);
 *   dialog.setOnDismissListener(d -> { finish(); });
 *   dialog.show(getSupportFragmentManager(), "BudgetAlertDialog");
 * </pre>
 */
public class BudgetAlertDialog extends DialogFragment {

    private static final String ARG_POT_NAME = "arg_pot_name";
    private static final String ARG_BUDGET_LIMIT = "arg_budget_limit";
    private static final String ARG_CURRENT_BALANCE = "arg_current_balance";
    private static final String ARG_SPENT_PERCENTAGE = "arg_spent_percentage";
    private static final String ARG_IS_DANGER = "arg_is_danger";

    // Callback khi dialog bị dismiss (để Activity có thể finish())
    private DialogInterface.OnDismissListener onDismissListener;

    /**
     * Đặt listener được gọi khi dialog bị đóng.
     *
     * @param listener Listener sẽ được gọi khi dialog dismiss
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    /**
     * Factory method tạo instance mới của BudgetAlertDialog.
     *
     * @param potName         Tên hũ chi tiêu
     * @param budgetLimit     Ngân sách giới hạn
     * @param currentBalance  Số dư hiện tại (sau giao dịch)
     * @param spentPercentage Phần trăm đã chi tiêu (0.0 – 1.0+)
     * @param isDanger        true nếu vượt 100%, false nếu chỉ chạm 80%
     * @return Instance mới của BudgetAlertDialog
     */
    public static BudgetAlertDialog newInstance(String potName, double budgetLimit,
                                                double currentBalance, double spentPercentage,
                                                boolean isDanger) {
        BudgetAlertDialog dialog = new BudgetAlertDialog();
        Bundle args = new Bundle();
        args.putString(ARG_POT_NAME, potName);
        args.putDouble(ARG_BUDGET_LIMIT, budgetLimit);
        args.putDouble(ARG_CURRENT_BALANCE, currentBalance);
        args.putDouble(ARG_SPENT_PERCENTAGE, spentPercentage);
        args.putBoolean(ARG_IS_DANGER, isDanger);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng style trong suốt để thấy background bo tròn
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_budget_alert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null) {
            dismiss();
            return;
        }

        String potName = getArguments().getString(ARG_POT_NAME, "");
        double budgetLimit = getArguments().getDouble(ARG_BUDGET_LIMIT, 0);
        double currentBalance = getArguments().getDouble(ARG_CURRENT_BALANCE, 0);
        double spentPercentage = getArguments().getDouble(ARG_SPENT_PERCENTAGE, 0);
        boolean isDanger = getArguments().getBoolean(ARG_IS_DANGER, false);

        setupUI(view, potName, budgetLimit, currentBalance, spentPercentage, isDanger);
        playEntranceAnimation(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Padding cho dialog không dính mép
            dialog.getWindow().getDecorView().setPadding(48, 0, 48, 0);
        }
        setCancelable(true);
    }

    /**
     * Thiết lập giao diện dialog theo mức độ cảnh báo.
     */
    private void setupUI(View view, String potName, double budgetLimit,
                         double currentBalance, double spentPercentage, boolean isDanger) {

        View viewAlertCircle = view.findViewById(R.id.view_alert_circle);
        TextView tvAlertIcon = view.findViewById(R.id.tv_alert_icon);
        TextView tvAlertTitle = view.findViewById(R.id.tv_alert_title);
        TextView tvAlertPotName = view.findViewById(R.id.tv_alert_pot_name);
        ProgressBar pbAlertBudget = view.findViewById(R.id.pb_alert_budget);
        TextView tvAlertPercentage = view.findViewById(R.id.tv_alert_percentage);
        TextView tvAlertDetail = view.findViewById(R.id.tv_alert_detail);
        TextView tvAlertMessage = view.findViewById(R.id.tv_alert_message);
        MaterialButton btnDismiss = view.findViewById(R.id.btn_alert_dismiss);

        int percentage = (int) Math.min(spentPercentage * 100, 999);

        if (isDanger) {
            // === MỨC NGUY HIỂM (>= 100%) – Giao diện đỏ rực rỡ ===
            tvAlertIcon.setText("🚨");

            // Gradient đỏ cho vòng tròn icon
            GradientDrawable circleGradient = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            Color.parseColor("#FFEBEE"),  // Red 50
                            Color.parseColor("#FFCDD2")   // Red 100
                    }
            );
            circleGradient.setShape(GradientDrawable.OVAL);
            viewAlertCircle.setBackground(circleGradient);

            tvAlertTitle.setText(R.string.alert_budget_danger_title);
            tvAlertTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_expense));

            tvAlertPercentage.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_expense));

            // Progress bar đỏ
            pbAlertBudget.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.color_budget_danger),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            tvAlertMessage.setText(R.string.alert_budget_danger_message);

            // Nút dismiss đỏ
            btnDismiss.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_expense));

        } else {
            // === MỨC CẢNH BÁO (>= 80%) – Giao diện vàng/cam ấm áp ===
            tvAlertIcon.setText("⚠️");

            // Gradient vàng/cam cho vòng tròn icon
            GradientDrawable circleGradient = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            Color.parseColor("#FFF8E1"),  // Amber 50
                            Color.parseColor("#FFECB3")   // Amber 100
                    }
            );
            circleGradient.setShape(GradientDrawable.OVAL);
            viewAlertCircle.setBackground(circleGradient);

            tvAlertTitle.setText(R.string.alert_budget_warning_title);
            tvAlertTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_budget_warning));

            tvAlertPercentage.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_budget_warning));

            // Progress bar vàng
            pbAlertBudget.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.color_budget_warning),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            tvAlertMessage.setText(R.string.alert_budget_warning_message);

            // Nút dismiss vàng/cam
            btnDismiss.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_budget_warning));
        }

        // === Nội dung chung cho cả 2 mức ===
        tvAlertPotName.setText(getString(R.string.alert_budget_pot_label, potName));
        pbAlertBudget.setProgress(Math.min(percentage, 100));
        tvAlertPercentage.setText(percentage + "%");

        // Chi tiết: Đã chi / Ngân sách
        double spent = budgetLimit - currentBalance;
        if (spent < 0) spent = 0;
        String detail = CurrencyFormatter.format(spent) + " / " + CurrencyFormatter.format(budgetLimit);
        tvAlertDetail.setText(detail);

        // Nút đóng dialog
        btnDismiss.setOnClickListener(v -> dismiss());
    }

    /**
     * Chạy hiệu ứng bounce khi dialog xuất hiện (tạo cảm giác sống động).
     */
    private void playEntranceAnimation(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_bounce));
    }
}
