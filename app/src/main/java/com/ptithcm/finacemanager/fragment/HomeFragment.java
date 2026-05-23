package com.ptithcm.finacemanager.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.activity.AddTransactionActivity;
import com.ptithcm.finacemanager.adapter.TransactionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView textViewTotalBalance, textViewMonthlyIncome, textViewMonthlyExpense;
    private TextView textViewEmptyTransactions, textViewViewAll;
    private RecyclerView recyclerViewRecentTransactions;
    private FloatingActionButton floatingActionButtonAddTransaction;

    private DBManager databaseManager;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> recentTransactions = new ArrayList<>();

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
        textViewEmptyTransactions = view.findViewById(R.id.tv_empty_transactions);
        textViewViewAll = view.findViewById(R.id.tv_view_all);
        recyclerViewRecentTransactions = view.findViewById(R.id.rv_recent_transactions);
        floatingActionButtonAddTransaction = view.findViewById(R.id.fab_add_transaction);
    }

    private void initListeners() {
        floatingActionButtonAddTransaction.setOnClickListener(buttonView -> {
            startActivity(new Intent(requireContext(), AddTransactionActivity.class));
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
    }

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
                        intent.putExtra(com.ptithcm.finacemanager.utils.Constants.EXTRA_TRANSACTION_ID, transaction.getId());
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
            textViewEmptyTransactions.setVisibility(View.VISIBLE);
            recyclerViewRecentTransactions.setVisibility(View.GONE);
        } else {
            textViewEmptyTransactions.setVisibility(View.GONE);
            recyclerViewRecentTransactions.setVisibility(View.VISIBLE);
        }
    }
}
