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

/**
 * Màn hình chi tiết hủ chi tiêu.
 *
 * <p>Hiển thị thông tin hủ (tên, số dư, progress ngân sách)
 * và danh sách giao dịch thuộc hủ đó.
 */
public class PotDetailActivity extends AppCompatActivity {

    private TextView textViewPotName, textViewBalance, textViewBudgetInfo, textViewEmpty;
    private ProgressBar progressBarBudget;
    private RecyclerView recyclerViewTransactions;
    private FloatingActionButton floatingActionButtonAddTransaction;

    private DBManager databaseManager;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactions = new ArrayList<>();
    private int potId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pot_detail);

        databaseManager = DBManager.getInstance(this);
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
        textViewPotName = findViewById(R.id.tv_pot_name);
        textViewBalance = findViewById(R.id.tv_balance);
        textViewBudgetInfo = findViewById(R.id.tv_budget_info);
        textViewEmpty = findViewById(R.id.tv_empty);
        progressBarBudget = findViewById(R.id.pb_budget);
        recyclerViewTransactions = findViewById(R.id.rv_transactions);
        floatingActionButtonAddTransaction = findViewById(R.id.fab_add_transaction);

        findViewById(R.id.iv_back).setOnClickListener(view -> finish());
    }

    private void initListeners() {
        floatingActionButtonAddTransaction.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra(Constants.EXTRA_POT_ID, potId);
            startActivity(intent);
        });

        // Edit pot
        findViewById(R.id.iv_edit).setOnClickListener(view -> showEditPotDialog());

        // Delete pot
        findViewById(R.id.iv_delete).setOnClickListener(view -> confirmDeletePot());
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(transactions, this);
        transactionAdapter.setOnTransactionClickListener(this::showTransactionOptionsDialog);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void showTransactionOptionsDialog(Transaction transaction) {
        String[] options = {getString(R.string.title_edit_transaction), getString(R.string.btn_delete)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_transaction_options)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Sửa giao dịch
                        Intent intent = new Intent(this, AddTransactionActivity.class);
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
        new AlertDialog.Builder(this)
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
        Pot pot = databaseManager.getPotById(potId);
        if (pot == null) {
            finish();
            return;
        }

        textViewPotName.setText(pot.getName());
        textViewBalance.setText(CurrencyFormatter.format(pot.getBalance()));

        int percentage = pot.getBudgetPercentage();
        progressBarBudget.setProgress(percentage);

        String budgetText = percentage + "% " +
                getString(R.string.label_spent) + " · " +
                getString(R.string.label_budget) + ": " +
                CurrencyFormatter.format(pot.getBudgetLimit());
        textViewBudgetInfo.setText(budgetText);

        // Load transactions
        transactions = databaseManager.getTransactionsByPotId(potId);
        transactionAdapter.updateData(transactions);

        if (transactions.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewTransactions.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void showEditPotDialog() {
        Pot pot = databaseManager.getPotById(potId);
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
                    databaseManager.deletePot(potId);
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
