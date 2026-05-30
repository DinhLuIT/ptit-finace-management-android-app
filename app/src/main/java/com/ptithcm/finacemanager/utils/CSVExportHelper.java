package com.ptithcm.finacemanager.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.FileProvider;

import com.ptithcm.finacemanager.database.DBManager;
import com.ptithcm.finacemanager.model.Transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tiện ích xuất lịch sử giao dịch ra file CSV.
 *
 * <p>Chạy hoàn toàn ở Background Thread (ExecutorService) để không block UI.
 * File CSV được lưu tạm vào thư mục cache nội bộ, sau đó trả về Uri
 * qua FileProvider để chia sẻ an toàn ra bên ngoài.
 *
 * <p>Chuẩn UTF-8 + BOM để Excel trên Windows hiển thị tiếng Việt không lỗi font.
 */
public class CSVExportHelper {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Callback trả kết quả về Main Thread. */
    public interface ExportCallback {
        /** Xuất thành công, trả về Uri của file CSV để share. */
        void onSuccess(Uri fileUri, String fileName);

        /** Xuất thất bại, trả về thông báo lỗi. */
        void onError(String errorMessage);
    }

    /**
     * Xuất toàn bộ giao dịch ra file CSV.
     *
     * @param context  Context của Activity/Fragment
     * @param callback Callback nhận kết quả (chạy trên Main Thread)
     */
    public static void exportAllTransactions(Context context, ExportCallback callback) {
        executor.execute(() -> {
            try {
                // ── 1. Query dữ liệu ──
                DBManager dbManager = DBManager.getInstance(context);
                List<Transaction> transactions = dbManager.getAllTransactions();

                if (transactions.isEmpty()) {
                    postError(callback, "Không có giao dịch nào để xuất");
                    return;
                }

                // ── 2. Tạo tên file chuyên nghiệp ──
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault())
                        .format(new Date());
                String fileName = "BaoCao_TaiChinh_" + timestamp + ".csv";

                // ── 3. Tạo thư mục cache/reports nếu chưa tồn tại ──
                File reportsDir = new File(context.getCacheDir(), "reports");
                if (!reportsDir.exists() && !reportsDir.mkdirs()) {
                    postError(callback, "Không thể tạo thư mục lưu báo cáo");
                    return;
                }

                File csvFile = new File(reportsDir, fileName);

                // ── 4. Ghi file CSV ──
                writeCSV(context, csvFile, transactions);

                // ── 5. Lấy Uri an toàn qua FileProvider ──
                Uri fileUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".fileprovider",
                        csvFile);

                // ── 6. Trả kết quả về Main Thread ──
                mainHandler.post(() -> callback.onSuccess(fileUri, fileName));

            } catch (Exception e) {
                e.printStackTrace();
                postError(callback, "Lỗi xuất báo cáo: " + e.getMessage());
            }
        });
    }

    /**
     * Ghi nội dung CSV ra file.
     *
     * <p>Format chuẩn:
     * <pre>STT,Ngày,Loại,Hủ quỹ,Danh mục,Số tiền,Ghi chú</pre>
     *
     * @param context      Context để resolve tên danh mục đa ngôn ngữ
     * @param file         File đích để ghi
     * @param transactions Danh sách giao dịch đã JOIN sẵn tên Pot + Category
     */
    private static void writeCSV(Context context, File file, List<Transaction> transactions) throws Exception {
        // DecimalFormat cho số tiền (không có dấu phẩy, giữ nguyên số thuần)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat amountFormat = new DecimalFormat("#.##", symbols);

        // Formatter cho ngày tháng đẹp hơn
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // ── BOM (Byte Order Mark) để Excel nhận diện UTF-8 ──
            writer.write('\uFEFF');

            // ── Header row ──
            writer.write("STT,Ngày,Loại,Hủ quỹ,Danh mục,Số tiền,Ghi chú");
            writer.newLine();

            // ── Data rows ──
            int stt = 1;
            for (Transaction trans : transactions) {
                StringBuilder row = new StringBuilder();

                // STT
                row.append(stt++);
                row.append(",");

                // Ngày (format đẹp: dd/MM/yyyy)
                String formattedDate = trans.getDate();
                try {
                    Date date = inputFormat.parse(trans.getDate());
                    if (date != null) {
                        formattedDate = outputFormat.format(date);
                    }
                } catch (Exception ignored) {}
                row.append(formattedDate);
                row.append(",");

                // Loại (Thu nhập / Chi tiêu)
                String type = Constants.TYPE_INCOME.equals(trans.getType())
                        ? "Thu nhập" : "Chi tiêu";
                row.append(type);
                row.append(",");

                // Hủ quỹ
                row.append(escapeCSV(trans.getPotName() != null ? trans.getPotName() : ""));
                row.append(",");

                // Danh mục (tên đã bản địa hóa)
                String category = trans.getLocalizedCategoryName(context);
                if (category == null || category.isEmpty()) {
                    category = trans.getCategoryName() != null ? trans.getCategoryName() : "";
                }
                // Gắn icon nếu có
                if (trans.getCategoryIcon() != null && !trans.getCategoryIcon().isEmpty()) {
                    category = trans.getCategoryIcon() + " " + category;
                }
                row.append(escapeCSV(category));
                row.append(",");

                // Số tiền
                row.append(amountFormat.format(trans.getAmount()));
                row.append(",");

                // Ghi chú
                row.append(escapeCSV(trans.getNote() != null ? trans.getNote() : ""));

                writer.write(row.toString());
                writer.newLine();
            }

            writer.flush();
        }
    }

    /**
     * Escape chuỗi CSV: nếu chứa dấu phẩy, xuống dòng hoặc ngoặc kép
     * thì bọc trong ngoặc kép và escape ngoặc kép thành cặp đôi.
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** Post lỗi về Main Thread. */
    private static void postError(ExportCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
