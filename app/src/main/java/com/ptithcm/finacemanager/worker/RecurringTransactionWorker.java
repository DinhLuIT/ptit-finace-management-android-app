package com.ptithcm.finacemanager.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.RecurringTransaction;
import com.ptithcm.finacemanager.model.Transaction;
import com.ptithcm.finacemanager.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Worker chạy nền hàng ngày để kiểm tra và tự động tạo giao dịch từ bảng RECURRING_TRANSACTIONS.
 *
 * <p>Logic hoạt động:
 * <ol>
 *   <li>Lấy tất cả recurring transactions có {@code NEXT_DATE <= today} và {@code IS_ACTIVE = 1}</li>
 *   <li>Với mỗi recurring, tạo một Transaction thực tế vào bảng TRANSACTIONS</li>
 *   <li>Tính toán {@code NEXT_DATE} mới dựa trên {@code FREQUENCY} (DAILY/WEEKLY/MONTHLY/YEARLY)</li>
 *   <li>Cập nhật {@code NEXT_DATE} trong bảng RECURRING_TRANSACTIONS</li>
 * </ol>
 *
 * <p>Worker được đăng ký với PeriodicWorkRequest (24h) trong {@code FinanceManagerApp.onCreate()}.
 */
public class RecurringTransactionWorker extends Worker {

    private static final String TAG = "RecurringWorker";

    public RecurringTransactionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Bắt đầu kiểm tra giao dịch định kỳ...");

        DBManager dbManager = DBManager.getInstance(getApplicationContext());
        String today = DateUtils.getTodayDB();

        // Lấy tất cả recurring transactions đến hạn
        List<RecurringTransaction> dueList = dbManager.getDueRecurringTransactions(today);

        if (dueList.isEmpty()) {
            Log.d(TAG, "Không có giao dịch định kỳ nào đến hạn.");
            return Result.success();
        }

        Log.d(TAG, "Tìm thấy " + dueList.size() + " giao dịch định kỳ đến hạn.");

        for (RecurringTransaction recurring : dueList) {
            try {
                // Tạo Transaction thực tế từ recurring
                Transaction transaction = new Transaction(
                        recurring.getPotId(),
                        recurring.getCategoryId(),
                        recurring.getAmount(),
                        recurring.getType(),
                        today,
                        recurring.getNote()
                );
                dbManager.addTransaction(transaction);

                // Tính NEXT_DATE mới
                String nextDate = calculateNextDate(recurring.getNextDate(), recurring.getFrequency());
                dbManager.updateRecurringNextDate(recurring.getId(), nextDate);

                Log.d(TAG, "Đã tạo giao dịch từ recurring #" + recurring.getId()
                        + " | Next: " + nextDate);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xử lý recurring #" + recurring.getId(), e);
            }
        }

        return Result.success();
    }

    /**
     * Tính ngày lặp tiếp theo dựa trên tần suất.
     *
     * @param currentNextDate Ngày lặp hiện tại (yyyy-MM-dd)
     * @param frequency       Tần suất: DAILY, WEEKLY, MONTHLY, YEARLY
     * @return Ngày lặp tiếp theo (yyyy-MM-dd)
     */
    private String calculateNextDate(String currentNextDate, String frequency) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(currentNextDate);
            if (date == null) return currentNextDate;

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            switch (frequency) {
                case "DAILY":
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case "WEEKLY":
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "MONTHLY":
                    cal.add(Calendar.MONTH, 1);
                    break;
                case "YEARLY":
                    cal.add(Calendar.YEAR, 1);
                    break;
                default:
                    cal.add(Calendar.MONTH, 1); // Fallback
                    break;
            }

            return sdf.format(cal.getTime());
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tính NEXT_DATE", e);
            return currentNextDate;
        }
    }
}
