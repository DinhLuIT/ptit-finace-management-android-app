package com.ptithcm.finacemanager.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.activity.ExchangeRateActivity;
import com.ptithcm.finacemanager.activity.SavingsGoalsActivity;
import com.ptithcm.finacemanager.activity.StatisticsActivity;
import com.ptithcm.finacemanager.utils.NotificationHelper;

public class ProfileFragment extends Fragment {

    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchNotification;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initListeners(view);
    }

    private void initViews(View view) {
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchNotification = view.findViewById(R.id.switch_notification);

        // Đặt trạng thái ban đầu cho dark mode
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Đặt trạng thái ban đầu cho notification từ SharedPreferences
        switchNotification.setChecked(NotificationHelper.isNotificationEnabled(requireContext()));
    }

    private void initListeners(View view) {
        // Toggle Dark Mode
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Toggle Notification Budget Alert
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationHelper.setNotificationEnabled(requireContext(), isChecked);
        });

        // Mở màn hình Thống kê chi tiêu
        view.findViewById(R.id.tv_statistics).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StatisticsActivity.class);
            startActivity(intent);
        });

        // Mở màn hình Mục tiêu tiết kiệm
        view.findViewById(R.id.tv_savings_goals).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SavingsGoalsActivity.class);
            startActivity(intent);
        });

        // Chuyển sang tab "Giao dịch" → sub-tab "Định kỳ"
        view.findViewById(R.id.tv_recurring_transactions).setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bnv_main);
                if (bottomNav != null) {
                    // Chuyển sang tab Transactions
                    bottomNav.setSelectedItemId(R.id.nav_transactions);

                    // Tìm TransactionsFragment đã cache và chuyển sang sub-tab Định kỳ
                    Fragment transFragment = getActivity().getSupportFragmentManager()
                            .findFragmentByTag("transactions");
                    if (transFragment instanceof TransactionsFragment) {
                        ((TransactionsFragment) transFragment).selectRecurringTab();
                    }
                }
            }
        });

        view.findViewById(R.id.tv_exchange_rate).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ExchangeRateActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.tv_export).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming in Phase 3", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.tv_change_pin).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.tv_about).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Finance Manager v1.0\nPTIT HCM", Toast.LENGTH_LONG).show();
        });
    }
}
