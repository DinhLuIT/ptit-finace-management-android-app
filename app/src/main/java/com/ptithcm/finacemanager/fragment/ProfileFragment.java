package com.ptithcm.finacemanager.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.ptithcm.finacemanager.R;
import com.ptithcm.finacemanager.activity.ExchangeRateActivity;
import com.ptithcm.finacemanager.activity.SavingsGoalsActivity;
import com.ptithcm.finacemanager.activity.StatisticsActivity;
import com.ptithcm.finacemanager.utils.CSVExportHelper;
import com.ptithcm.finacemanager.utils.Constants;
import com.ptithcm.finacemanager.utils.NotificationHelper;

public class ProfileFragment extends Fragment {

    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchNotification;
    private SwitchMaterial switchBiometric;
    private TextView tvBiometricDesc;

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
        switchBiometric = view.findViewById(R.id.switch_biometric);
        tvBiometricDesc = view.findViewById(R.id.tv_biometric_desc);

        // Đặt trạng thái ban đầu cho dark mode
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Đặt trạng thái ban đầu cho notification từ SharedPreferences
        switchNotification.setChecked(NotificationHelper.isNotificationEnabled(requireContext()));

        // Thiết lập trạng thái ban đầu cho Biometric
        initBiometricSwitch();
    }

    private void initListeners(View view) {
        // Toggle Dark Mode (lưu vào SharedPreferences để khôi phục khi khởi động lại)
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences(Constants.PREF_NAME, android.content.Context.MODE_PRIVATE);
            prefs.edit().putBoolean(Constants.PREF_DARK_MODE, isChecked).apply();

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

        view.findViewById(R.id.tv_export).setOnClickListener(v -> exportCSV());

        view.findViewById(R.id.tv_change_pin).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.tv_about).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Finance Manager v1.0\nPTIT HCM", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Xuất báo cáo CSV và mở Share Sheet.
     */
    private void exportCSV() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage(getString(R.string.msg_exporting));
        progressDialog.setCancelable(false);
        progressDialog.show();

        CSVExportHelper.exportAllTransactions(requireContext(), new CSVExportHelper.ExportCallback() {
            @Override
            public void onSuccess(Uri fileUri, String fileName) {
                progressDialog.dismiss();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/csv");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject));
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, getString(R.string.export_chooser_title)));
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Thiết lập switch Sinh trắc học:
     * - Kiểm tra phần cứng có hỗ trợ không (BiometricManager).
     * - Nếu không hỗ trợ hoặc chưa enroll vân tay: disable switch và thay text mô tả.
     * - Nếu hỗ trợ: đọc trạng thái từ SharedPreferences và lắng nghe thay đổi.
     */
    private void initBiometricSwitch() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        int canAuth = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // Thiết bị không hỗ trợ hoặc chưa cài vân tay
            switchBiometric.setEnabled(false);
            switchBiometric.setChecked(false);
            tvBiometricDesc.setText(R.string.msg_biometric_not_available);
        } else {
            // Thiết bị hỗ trợ → đọc trạng thái đã lưu
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences(Constants.PREF_NAME, android.content.Context.MODE_PRIVATE);
            switchBiometric.setChecked(prefs.getBoolean(Constants.PREF_BIOMETRIC_ENABLED, false));

            switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(Constants.PREF_BIOMETRIC_ENABLED, isChecked).apply();
            });
        }
    }
}
