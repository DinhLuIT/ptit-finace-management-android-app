package com.ptithcm.finacemanager.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.activity.AddTransactionActivity;
import com.ptithcm.finacemanager.activity.GoalDetailActivity;
import com.ptithcm.finacemanager.activity.SavingsGoalsActivity;
import com.ptithcm.finacemanager.adapter.HomeGoalAdapter;
import com.ptithcm.finacemanager.adapter.TransactionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.dialog.AddGoalDialog;
import com.ptithcm.finacemanager.dialog.TransferDialog;
import com.ptithcm.finacemanager.model.SavingsGoal;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView textViewTotalBalance, textViewMonthlyIncome, textViewMonthlyExpense;
    private TextView textViewViewAll;
    private RecyclerView recyclerViewRecentTransactions;

    // Empty State views
    private View emptyStateContainer;
    private TextView tvEmptyIcon, tvEmptyTitle, tvEmptySubtitle;

    // Speed Dial FAB
    private FloatingActionButton floatingActionButtonMain;
    private FloatingActionButton floatingActionButtonAddTransaction;
    private FloatingActionButton floatingActionButtonTransfer;
    private LinearLayout layoutFabAddTransaction;
    private LinearLayout layoutFabTransfer;
    private View viewFabOverlay;
    private boolean isFabMenuOpen = false;

    private DBManager databaseManager;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> recentTransactions = new ArrayList<>();

    // === Savings Goals Carousel ===
    private LinearLayout layoutGoalsSection;
    private RecyclerView recyclerViewHomeGoals;
    private HomeGoalAdapter homeGoalAdapter;
    private List<SavingsGoal> savingsGoals = new ArrayList<>();
    private TextView tvViewAllGoals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseManager = DBManager.getInstance(requireContext());
        initViews(view);
        initListeners();
        setupRecyclerView();
        setupGoalsCarousel();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews(View view) {
        textViewTotalBalance = view.findViewById(R.id.tv_total_balance);
        textViewMonthlyIncome = view.findViewById(R.id.tv_monthly_income);
        textViewMonthlyExpense = view.findViewById(R.id.tv_monthly_expense);
        textViewViewAll = view.findViewById(R.id.tv_view_all);
        recyclerViewRecentTransactions = view.findViewById(R.id.rv_recent_transactions);

        // Empty State – sử dụng layout tái sử dụng
        emptyStateContainer = view.findViewById(R.id.include_empty_state);
        tvEmptyIcon = emptyStateContainer.findViewById(R.id.tv_empty_icon);
        tvEmptyTitle = emptyStateContainer.findViewById(R.id.tv_empty_title);
        tvEmptySubtitle = emptyStateContainer.findViewById(R.id.tv_empty_subtitle);

        // Cấu hình nội dung Empty State cho phần giao dịch gần đây
        tvEmptyIcon.setText("✨");
        tvEmptyTitle.setText(R.string.empty_home_transactions_title);
        tvEmptySubtitle.setText(R.string.empty_home_transactions_subtitle);

        // Speed Dial FAB
        floatingActionButtonMain = view.findViewById(R.id.fab_main);
        floatingActionButtonAddTransaction = view.findViewById(R.id.fab_add_transaction);
        floatingActionButtonTransfer = view.findViewById(R.id.fab_transfer);
        layoutFabAddTransaction = view.findViewById(R.id.layout_fab_add_transaction);
        layoutFabTransfer = view.findViewById(R.id.layout_fab_transfer);
        viewFabOverlay = view.findViewById(R.id.view_fab_overlay);

        // Goals Carousel
        layoutGoalsSection = view.findViewById(R.id.layout_goals_section);
        recyclerViewHomeGoals = view.findViewById(R.id.rv_home_goals);
        tvViewAllGoals = view.findViewById(R.id.tv_view_all_goals);
    }

    private void initListeners() {
        // FAB chính: Bấm để mở/đóng Speed Dial menu
        floatingActionButtonMain.setOnClickListener(buttonView -> toggleFabMenu());

        // Bấm overlay để đóng menu
        viewFabOverlay.setOnClickListener(buttonView -> closeFabMenu());

        // Mini FAB: Thêm giao dịch
        floatingActionButtonAddTransaction.setOnClickListener(buttonView -> {
            closeFabMenu();
            startActivity(new Intent(requireContext(), AddTransactionActivity.class));
        });

        // Mini FAB: Chuyển tiền
        floatingActionButtonTransfer.setOnClickListener(buttonView -> {
            closeFabMenu();
            showTransferDialog();
        });

        textViewViewAll.setOnClickListener(buttonView -> {
            // Chuyển sang tab Transactions
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView =
                        getActivity().findViewById(R.id.bnv_main);
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_transactions);
                }
            }
        });

        // "Xem tất cả" mục tiêu tiết kiệm
        tvViewAllGoals.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SavingsGoalsActivity.class));
        });
    }

    // =============================================
    // ========= SPEED DIAL FAB LOGIC ==============
    // =============================================

    /**
     * Bật/tắt Speed Dial menu với animation xoay nút chính + hiệu ứng trượt các mini FAB.
     */
    private void toggleFabMenu() {
        if (isFabMenuOpen) {
            closeFabMenu();
        } else {
            openFabMenu();
        }
    }

    private void openFabMenu() {
        isFabMenuOpen = true;

        // Xoay nút chính 45° (dấu + → dấu x)
        floatingActionButtonMain.animate().rotation(45f).setDuration(200).start();

        // Hiện overlay mờ
        viewFabOverlay.setVisibility(View.VISIBLE);
        viewFabOverlay.setAlpha(0f);
        viewFabOverlay.animate().alpha(1f).setDuration(200).start();

        // Hiện mini FAB "Thêm giao dịch" với animation trượt lên
        layoutFabAddTransaction.setVisibility(View.VISIBLE);
        layoutFabAddTransaction.setAlpha(0f);
        layoutFabAddTransaction.setTranslationY(50f);
        layoutFabAddTransaction.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .start();

        // Hiện mini FAB "Chuyển tiền" với animation trượt lên (delay nhẹ)
        layoutFabTransfer.setVisibility(View.VISIBLE);
        layoutFabTransfer.setAlpha(0f);
        layoutFabTransfer.setTranslationY(50f);
        layoutFabTransfer.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(50)
                .setDuration(200)
                .start();
    }

    private void closeFabMenu() {
        isFabMenuOpen = false;

        // Xoay nút chính về 0° (dấu x → dấu +)
        floatingActionButtonMain.animate().rotation(0f).setDuration(200).start();

        // Ẩn overlay
        viewFabOverlay.animate().alpha(0f).setDuration(200).withEndAction(() ->
                viewFabOverlay.setVisibility(View.GONE)).start();

        // Ẩn mini FAB "Thêm giao dịch"
        layoutFabAddTransaction.animate()
                .alpha(0f)
                .translationY(50f)
                .setDuration(150)
                .withEndAction(() -> layoutFabAddTransaction.setVisibility(View.GONE))
                .start();

        // Ẩn mini FAB "Chuyển tiền"
        layoutFabTransfer.animate()
                .alpha(0f)
                .translationY(50f)
                .setDuration(150)
                .withEndAction(() -> layoutFabTransfer.setVisibility(View.GONE))
                .start();
    }

    // =============================================
    // ========= TRANSFER DIALOG ===================
    // =============================================

    private void showTransferDialog() {
        TransferDialog transferDialog = new TransferDialog();
        transferDialog.setOnTransferCompleteListener(this::loadData);
        transferDialog.show(getParentFragmentManager(), "TransferDialog");
    }

    // =============================================
    // ========= RECYCLERVIEW SETUP ================
    // =============================================

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(recentTransactions, requireContext());
        transactionAdapter.setOnTransactionClickListener(this::showTransactionOptionsDialog);
        recyclerViewRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewRecentTransactions.setAdapter(transactionAdapter);
    }

    private void showTransactionOptionsDialog(Transaction transaction) {
        String[] options = {getString(R.string.title_edit_transaction), getString(R.string.btn_delete)};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_transaction_options)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Sửa giao dịch
                        Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
                        intent.putExtra(Constants.EXTRA_TRANSACTION_ID, transaction.getId());
                        startActivity(intent);
                    } else if (which == 1) {
                        // Xóa giao dịch
                        confirmDeleteTransaction(transaction);
                    }
                })
                .show();
    }

    private void confirmDeleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.msg_confirm_delete)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    databaseManager.deleteTransaction(transaction.getId());
                    loadData();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    // =============================================
    // ========= DATA LOADING ======================
    // =============================================

    private void loadData() {
        // Tổng số dư
        double totalBalance = databaseManager.getTotalBalance();
        textViewTotalBalance.setText(CurrencyFormatter.format(totalBalance));

        // Thu/Chi tháng này
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();

        double income = databaseManager.getTotalIncomeByMonth(month, year);
        double expense = databaseManager.getTotalExpenseByMonth(month, year);

        textViewMonthlyIncome.setText(CurrencyFormatter.formatWithSign(income, true));
        textViewMonthlyExpense.setText(CurrencyFormatter.formatWithSign(expense, false));

        // 5 giao dịch gần đây
        recentTransactions = databaseManager.getRecentTransactions(5);
        transactionAdapter.updateData(recentTransactions);

        // Hiển thị empty state
        if (recentTransactions.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerViewRecentTransactions.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerViewRecentTransactions.setVisibility(View.VISIBLE);
        }

        // Load Savings Goals carousel
        loadGoals();
    }

    // =============================================
    // ========= SAVINGS GOALS CAROUSEL ============
    // =============================================

    /**
     * Cài đặt RecyclerView ngang cho carousel mục tiêu tiết kiệm.
     */
    private void setupGoalsCarousel() {
        homeGoalAdapter = new HomeGoalAdapter(savingsGoals, requireContext());
        homeGoalAdapter.setOnHomeGoalClickListener(new HomeGoalAdapter.OnHomeGoalClickListener() {
            @Override
            public void onGoalClick(SavingsGoal goal) {
                Intent intent = new Intent(requireContext(), GoalDetailActivity.class);
                intent.putExtra("extra_goal_id", goal.getId());
                startActivity(intent);
            }

            @Override
            public void onAddGoalClick() {
                showAddGoalDialog();
            }
        });

        recyclerViewHomeGoals.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewHomeGoals.setAdapter(homeGoalAdapter);
    }

    /**
     * Load danh sách mục tiêu và cập nhật carousel.
     * Hiện section nếu có ít nhất 1 goal, hoặc luôn hiện nút thêm.
     */
    private void loadGoals() {
        savingsGoals = databaseManager.getAllSavingsGoals();
        homeGoalAdapter.updateData(savingsGoals);

        // Luôn hiện section (có card "Thêm mục tiêu" dù không có goal nào)
        layoutGoalsSection.setVisibility(View.VISIBLE);
    }

    /**
     * Hiển thị dialog tạo mục tiêu mới từ Home.
     */
    private void showAddGoalDialog() {
        AddGoalDialog dialog = new AddGoalDialog();
        dialog.setOnGoalSavedListener(this::loadGoals);
        dialog.show(getParentFragmentManager(), "AddGoalDialog");
    }
}
