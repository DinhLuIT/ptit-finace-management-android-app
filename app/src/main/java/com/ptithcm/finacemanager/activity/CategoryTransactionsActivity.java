package com.ptithcm.finacemanager.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.adapter.TransactionAdapter;
import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Transaction;

import java.util.List;
import java.util.Locale;

public class CategoryTransactionsActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME = "EXTRA_CATEGORY_NAME";
    public static final String EXTRA_CATEGORY_ID = "EXTRA_CATEGORY_ID";
    public static final String EXTRA_MONTH = "EXTRA_MONTH";
    public static final String EXTRA_YEAR = "EXTRA_YEAR";

    private RecyclerView rvTransactions;
    private LinearLayout layoutEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_transactions);

        // Get intent extras
        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        int categoryId = getIntent().getIntExtra(EXTRA_CATEGORY_ID, -1);
        int month = getIntent().getIntExtra(EXTRA_MONTH, 1);
        int year = getIntent().getIntExtra(EXTRA_YEAR, 2026);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        int resId = getResources().getIdentifier(categoryName, "string", getPackageName());
        String displayName = resId != 0 ? getString(resId) : categoryName;
        toolbar.setTitle(displayName + " - " + String.format(Locale.getDefault(), "Tháng %02d/%d", month, year));
        toolbar.setNavigationOnClickListener(v -> finish());

        // Init views
        rvTransactions = findViewById(R.id.rv_transactions);
        layoutEmptyState = findViewById(R.id.layout_empty_state);

        // Setup RecyclerView
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        // Load transactions
        DBManager databaseManager = DBManager.getInstance(this);
        List<Transaction> transactions = databaseManager.getTransactionsByCategoryAndMonth(categoryId, month, year);

        if (transactions.isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvTransactions.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);

            TransactionAdapter adapter = new TransactionAdapter(transactions, this);
            rvTransactions.setAdapter(adapter);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
