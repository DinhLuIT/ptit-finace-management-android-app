package com.ptithcm.finacemanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.TransactionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.dialog.AddPotDialog;
import com.ptithcm.finacemanager.model.Pot;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class PotDetailActivity extends AppCompatActivity {

    private TextView tvPotName, tvBalance, tvBudgetInfo, tvEmpty;
    private ProgressBar pbBudget;
    private RecyclerView rvTransactions;
    private FloatingActionButton fabAddTransaction;

    private DBManager dbManager;
    private TransactionAdapter adapter;
    private List<Transaction> transactions = new ArrayList<>();
    private int potId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pot_detail);

        dbManager = DBManager.getInstance(this);
        potId = getIntent().getIntExtra(Constants.EXTRA_POT_ID, -1);

        if (potId == -1) {
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

    private void initViews() {
        tvPotName = findViewById(R.id.tv_pot_name);
        tvBalance = findViewById(R.id.tv_balance);
        tvBudgetInfo = findViewById(R.id.tv_budget_info);
        tvEmpty = findViewById(R.id.tv_empty);
        pbBudget = findViewById(R.id.pb_budget);
        rvTransactions = findViewById(R.id.rv_transactions);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void initListeners() {
        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra(Constants.EXTRA_POT_ID, potId);
            startActivity(intent);
        });

        // Edit pot
        findViewById(R.id.iv_edit).setOnClickListener(v -> showEditPotDialog());

        // Delete pot
        findViewById(R.id.iv_delete).setOnClickListener(v -> confirmDeletePot());
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactions, this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void loadData() {
        Pot pot = dbManager.getPotById(potId);
        if (pot == null) {
            finish();
            return;
        }

        tvPotName.setText(pot.getName());
        tvBalance.setText(CurrencyFormatter.format(pot.getBalance()));

        int percentage = pot.getBudgetPercentage();
        pbBudget.setProgress(percentage);

        String budgetText = percentage + "% " +
                getString(R.string.label_spent) + " · " +
                getString(R.string.label_budget) + ": " +
                CurrencyFormatter.format(pot.getBudgetLimit());
        tvBudgetInfo.setText(budgetText);

        // Load transactions
        transactions = dbManager.getTransactionsByPotId(potId);
        adapter.updateData(transactions);

        if (transactions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void showEditPotDialog() {
        Pot pot = dbManager.getPotById(potId);
        if (pot == null) return;

        AddPotDialog dialog = new AddPotDialog();
        dialog.setEditPot(pot);
        dialog.setOnPotSavedListener(() -> loadData()); // Refresh data sau khi sửa
        dialog.show(getSupportFragmentManager(), "EditPotDialog");
    }

    private void confirmDeletePot() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_confirm_delete_pot)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    dbManager.deletePot(potId);
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
