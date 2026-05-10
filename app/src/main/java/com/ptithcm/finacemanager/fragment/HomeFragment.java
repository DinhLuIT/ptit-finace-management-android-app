package com.ptithcm.finacemanager.fragment;

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

    private TextView tvTotalBalance, tvMonthlyIncome, tvMonthlyExpense;
    private TextView tvEmptyTransactions, tvViewAll;
    private RecyclerView rvRecentTransactions;
    private FloatingActionButton fabAddTransaction;

    private DBManager dbManager;
    private TransactionAdapter adapter;
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
        dbManager = DBManager.getInstance(requireContext());
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
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvMonthlyIncome = view.findViewById(R.id.tv_monthly_income);
        tvMonthlyExpense = view.findViewById(R.id.tv_monthly_expense);
        tvEmptyTransactions = view.findViewById(R.id.tv_empty_transactions);
        tvViewAll = view.findViewById(R.id.tv_view_all);
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);
    }

    private void initListeners() {
        fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddTransactionActivity.class));
        });

        tvViewAll.setOnClickListener(v -> {
            // Chuyển sang tab Transactions
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bnv =
                        getActivity().findViewById(R.id.bnv_main);
                if (bnv != null) {
                    bnv.setSelectedItemId(R.id.nav_transactions);
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(recentTransactions, requireContext());
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentTransactions.setAdapter(adapter);
    }

    private void loadData() {
        // Tổng số dư
        double totalBalance = dbManager.getTotalBalance();
        tvTotalBalance.setText(CurrencyFormatter.format(totalBalance));

        // Thu/Chi tháng này
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();

        double income = dbManager.getTotalIncomeByMonth(month, year);
        double expense = dbManager.getTotalExpenseByMonth(month, year);

        tvMonthlyIncome.setText(CurrencyFormatter.formatWithSign(income, true));
        tvMonthlyExpense.setText(CurrencyFormatter.formatWithSign(expense, false));

        // 5 giao dịch gần đây
        recentTransactions = dbManager.getRecentTransactions(5);
        adapter.updateData(recentTransactions);

        // Hiển thị empty state
        if (recentTransactions.isEmpty()) {
            tvEmptyTransactions.setVisibility(View.VISIBLE);
            rvRecentTransactions.setVisibility(View.GONE);
        } else {
            tvEmptyTransactions.setVisibility(View.GONE);
            rvRecentTransactions.setVisibility(View.VISIBLE);
        }
    }
}
