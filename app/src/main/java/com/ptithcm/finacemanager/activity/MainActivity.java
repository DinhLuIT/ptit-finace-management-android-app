package com.ptithcm.finacemanager.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.fragment.HomeFragment;
import com.ptithcm.finacemanager.fragment.PotsFragment;
import com.ptithcm.finacemanager.fragment.ProfileFragment;
import com.ptithcm.finacemanager.fragment.TransactionsFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        initListeners();

        // Hiển thị HomeFragment mặc định
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bnv_main);
    }

    private void initListeners() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_pots) {
                fragment = new PotsFragment();
            } else if (itemId == R.id.nav_transactions) {
                fragment = new TransactionsFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}