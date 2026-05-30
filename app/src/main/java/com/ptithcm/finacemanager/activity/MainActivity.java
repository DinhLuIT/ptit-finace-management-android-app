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

/**
 * Activity chính chứa Bottom Navigation và quản lý 4 Fragment tab.
 *
 * <p>Sử dụng show/hide thay vì replace để giữ state Fragment,
 * đặc biệt quan trọng cho TransactionsFragment (chứa ViewPager2 nested fragments).
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // Cache fragments để tránh recreate mỗi lần chuyển tab
    private Fragment homeFragment;
    private Fragment potsFragment;
    private Fragment transactionsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

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

        if (savedInstanceState == null) {
            initFragments();
        } else {
            // Khôi phục fragments sau config change (xoay màn hình)
            restoreFragments();
        }

        initListeners();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bnv_main);
    }

    /**
     * Khởi tạo 4 fragments lần đầu và add tất cả vào container.
     * Chỉ show Home, hide 3 còn lại.
     */
    private void initFragments() {
        homeFragment = new HomeFragment();
        potsFragment = new PotsFragment();
        transactionsFragment = new TransactionsFragment();
        profileFragment = new ProfileFragment();

        activeFragment = homeFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragment_container, transactionsFragment, "transactions").hide(transactionsFragment)
                .add(R.id.fragment_container, potsFragment, "pots").hide(potsFragment)
                .add(R.id.fragment_container, homeFragment, "home")
                .commit();
    }

    /**
     * Khôi phục fragment references sau config change.
     */
    private void restoreFragments() {
        homeFragment = getSupportFragmentManager().findFragmentByTag("home");
        potsFragment = getSupportFragmentManager().findFragmentByTag("pots");
        transactionsFragment = getSupportFragmentManager().findFragmentByTag("transactions");
        profileFragment = getSupportFragmentManager().findFragmentByTag("profile");

        // Tìm fragment đang visible
        if (homeFragment != null && !homeFragment.isHidden()) activeFragment = homeFragment;
        else if (potsFragment != null && !potsFragment.isHidden()) activeFragment = potsFragment;
        else if (transactionsFragment != null && !transactionsFragment.isHidden()) activeFragment = transactionsFragment;
        else if (profileFragment != null && !profileFragment.isHidden()) activeFragment = profileFragment;
        else activeFragment = homeFragment;
    }

    private void initListeners() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment target = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                target = homeFragment;
            } else if (itemId == R.id.nav_pots) {
                target = potsFragment;
            } else if (itemId == R.id.nav_transactions) {
                target = transactionsFragment;
            } else if (itemId == R.id.nav_profile) {
                target = profileFragment;
            }

            if (target != null && target != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(target)
                        .commit();
                activeFragment = target;
                return true;
            }
            return target != null;
        });
    }
}