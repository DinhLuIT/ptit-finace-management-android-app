package com.ptithcm.finacemanager.fragment;

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

import com.google.android.material.chip.Chip;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.TransactionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionsFragment extends Fragment {

    private RecyclerView rvAllTransactions;
    private TextView tvEmpty;
    private Chip chipAll, chipIncome, chipExpense;

    private DBManager dbManager;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
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
        rvAllTransactions = view.findViewById(R.id.rv_all_transactions);
        tvEmpty = view.findViewById(R.id.tv_empty);
        chipAll = view.findViewById(R.id.chip_all);
        chipIncome = view.findViewById(R.id.chip_income);
        chipExpense = view.findViewById(R.id.chip_expense);
    }

    private void initListeners() {
        chipAll.setOnCheckedChangeListener((v, checked) -> {
            if (checked) filterTransactions(null);
        });
        chipIncome.setOnCheckedChangeListener((v, checked) -> {
            if (checked) filterTransactions(Constants.TYPE_INCOME);
        });
        chipExpense.setOnCheckedChangeListener((v, checked) -> {
            if (checked) filterTransactions(Constants.TYPE_EXPENSE);
        });
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(allTransactions, requireContext());
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAllTransactions.setAdapter(adapter);
    }

    private void loadData() {
        allTransactions = dbManager.getAllTransactions();
        adapter.updateData(allTransactions);
        updateEmptyState(allTransactions);
    }

    private void filterTransactions(String type) {
        List<Transaction> filtered;
        if (type == null) {
            filtered = allTransactions;
        } else {
            filtered = allTransactions.stream()
                    .filter(t -> type.equals(t.getType()))
                    .collect(Collectors.toList());
        }
        adapter.updateData(filtered);
        updateEmptyState(filtered);
    }

    private void updateEmptyState(List<Transaction> list) {
        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvAllTransactions.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvAllTransactions.setVisibility(View.VISIBLE);
        }
    }
}
