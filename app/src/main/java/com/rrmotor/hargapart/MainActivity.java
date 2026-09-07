package com.rrmotor.hargapart;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText namaPartInput;
    private EditText hargaPokokInput;
    private EditText kodePartInput;
    private EditText stokInput;
    private EditText supplierInput;
    private EditText catatanInput;

    private EditText cariInput;
    private TextView hasilCari;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        buatTampilan();
    }

    private void buatTampilan() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView judul = new TextView(this);
        judul.setText("🏍️ RR MOTOR\nCEK HARGA PART");
        judul.setTextSize(24);
        judul.setPadding(0, 0, 0, 25);

        layout.addView(judul);

        TextView subjudul = new TextView(this);
        subjudul.setText("Buku Harga & Stok Part");
        subjudul.setTextSize(17);
        subjudul.setPadding(0, 0, 0, 15);

        layout.addView(subjudul);

        // =========================
        // PENCARIAN
        // =========================

        TextView judulCari = new TextView(this);
        judulCari.setText("🔎 CARI PART");
        judulCari.setTextSize(19);
        judulCari.setPadding(0, 15, 0, 5);

        layout.addView(judulCari);

        cariInput = buatInput(
                "Nama atau Kode Part",
                "Contoh: BUSI atau CPR6EA"
        );

        layout.addView(cariInput);

        Button cariButton = new Button(this);
        cariButton.setText("🔎 CARI");
        cariButton.setOnClickListener(v -> cariPart());

        layout.addView(cariButton);

        hasilCari = new TextView(this);
        hasilCari.setTextSize(16);
        hasilCari.setPadding(0, 15, 0, 20);

        layout.addView(hasilCari);

        // =========================
        // TAMBAH PART
        // =========================

        TextView judulTambah = new TextView(this);
        judulTambah.setText("➕ TAMBAH PART");
        judulTambah.setTextSize(19);
        judulTambah.setPadding(0, 15, 0, 5);

        layout.addView(judulTambah);

        namaPartInput = buatInput(
                "Nama Part *",
                "Contoh: BUSI NGK CPR6EA"
        );

        layout.addView(namaPartInput);

        hargaPokokInput = buatInput(
                "Harga Pokok / Modal *",
                "Contoh: 18000"
        );

        hargaPokokInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(hargaPokokInput);

        kodePartInput = buatInput(
                "Kode Part",
                "Contoh: CPR6EA"
        );

        layout.addView(kodePartInput);

        stokInput = buatInput(
                "Stok",
                "Contoh: 10"
        );

        stokInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(stokInput);

        supplierInput = buatInput(
                "Supplier",
                "Nama supplier"
        );

        layout.addView(supplierInput);

        catatanInput = buatInput(
                "Catatan",
                "Catatan tambahan"
        );

        layout.addView(catatanInput);

        Button simpanButton = new Button(this);
        simpanButton.setText("💾 SIMPAN PART");

        simpanButton.setOnClickListener(v -> simpanPart());

        layout.addView(simpanButton);

        // =========================
        // SCAN
        // =========================

        Button scanButton = new Button(this);
        scanButton.setText("📷 SCAN NOTA SUPPLIER");

        scanButton.setOnClickListener(v ->
                Toast.makeText(
                        MainActivity.this,
                        "Fitur OCR akan kita pasang setelah pencarian selesai.",
                        Toast.LENGTH_LONG
                ).show()
        );

        layout.addView(scanButton);

        scrollView.addView(layout);

        setContentView(scrollView);
    }

    private EditText buatInput(String hint, String contoh) {

        EditText input = new EditText(this);

        input.setHint(hint + "\n" + contoh);
        input.setTextSize(16);
        input.setPadding(15, 15, 15, 15);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 5, 0, 10);

        input.setLayoutParams(params);

        return input;
    }

    // =====================================================
    // SIMPAN PART
    // =====================================================

    private void simpanPart() {

        String namaPart =
                namaPartInput.getText().toString().trim();

        String hargaText =
                hargaPokokInput.getText().toString().trim();

        if (namaPart.isEmpty()) {

            namaPartInput.setError(
                    "Nama Part wajib diisi"
            );

            namaPartInput.requestFocus();

            return;
        }

        if (hargaText.isEmpty()) {

            hargaPokokInput.setError(
                    "Harga Pokok wajib diisi"
            );

            hargaPokokInput.requestFocus();

            return;
        }

        long hargaPokok;

        try {

            hargaPokok =
                    Long.parseLong(hargaText);

        } catch (NumberFormatException e) {

            hargaPokokInput.setError(
                    "Harga tidak valid"
            );

            hargaPokokInput.requestFocus();

            return;
        }

        long stok = 0;

        String stokText =
                stokInput.getText().toString().trim();

        if (!stokText.isEmpty()) {

            try {

                stok =
                        Long.parseLong(stokText);

            } catch (NumberFormatException e) {

                stokInput.setError(
                        "Stok tidak valid"
                );

                stokInput.requestFocus();

                return;
            }
        }

        Map<String, Object> data =
                new java.util.HashMap<>();

        data.put(
                "namaPart",
                namaPart
        );

        data.put(
                "hargaPokok",
                hargaPokok
        );

        data.put(
                "kodePart",
                kodePartInput.getText().toString().trim()
        );

        data.put(
                "stok",
                stok
        );

        data.put(
                "supplier",
                supplierInput.getText().toString().trim()
        );

        data.put(
                "catatan",
                catatanInput.getText().toString().trim()
        );

        data.put(
                "updatedAt",
                System.currentTimeMillis()
        );

        db.collection("parts")
                .add(data)
                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    MainActivity.this,
                                    "✅ Part berhasil disimpan",
                                    Toast.LENGTH_LONG
                            ).show();

                            bersihkanForm();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    MainActivity.this,
                                    "❌ Gagal menyimpan: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =====================================================
    // CARI PART
    // =====================================================

    private void cariPart() {

        String kataKunci =
                cariInput.getText()
                        .toString()
                        .trim()
                        .toLowerCase();

        if (kataKunci.isEmpty()) {

            cariInput.setError(
                    "Masukkan nama atau kode part"
            );

            cariInput.requestFocus();

            return;
        }

        hasilCari.setText(
                "⏳ Mencari data..."
        );

        db.collection("parts")
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            StringBuilder hasil =
                                    new StringBuilder();

                            int jumlah = 0;

                            for (
                                    QueryDocumentSnapshot document
                                    : querySnapshot
                            ) {

                                String nama =
                                        document.getString(
                                                "namaPart"
                                        );

                                String kode =
                                        document.getString(
                                                "kodePart"
                                        );

                                if (nama == null) {
                                    nama = "";
                                }

                                if (kode == null) {
                                    kode = "";
                                }

                                String namaKecil =
                                        nama.toLowerCase();

                                String kodeKecil =
                                        kode.toLowerCase();

                                if (
                                        namaKecil.contains(
                                                kataKunci
                                        )
                                        ||
                                        kodeKecil.contains(
                                                kataKunci
                                        )
                                ) {

                                    jumlah++;

                                    Long harga =
                                            document.getLong(
                                                    "hargaPokok"
                                            );

                                    Long stok =
                                            document.getLong(
                                                    "stok"
                                            );

                                    String supplier =
                                            document.getString(
                                                    "supplier"
                                            );

                                    String catatan =
                                            document.getString(
                                                    "catatan"
                                            );

                                    if (harga == null) {
                                        harga = 0L;
                                    }

                                    if (stok == null) {
                                        stok = 0L;
                                    }

                                    if (supplier == null
                                            || supplier.isEmpty()) {
                                        supplier = "-";
                                    }

                                    if (catatan == null
                                            || catatan.isEmpty()) {
                                        catatan = "-";
                                    }

                                    hasil.append(
                                            "━━━━━━━━━━━━━━━━━━\n"
                                    );

                                    hasil.append(
                                            "🏍️ "
                                    );

                                    hasil.append(
                                            nama
                                    );

                                    hasil.append(
                                            "\n"
                                    );

                                    if (!kode.isEmpty()) {

                                        hasil.append(
                                                "Kode: "
                                        );

                                        hasil.append(
                                                kode
                                        );

                                        hasil.append(
                                                "\n"
                                        );
                                    }

                                    hasil.append(
                                            "Modal: "
                                    );

                                    hasil.append(
                                            formatRupiah(
                                                    harga
                                            )
                                    );

                                    hasil.append(
                                            "\n"
                                    );

                                    hasil.append(
                                            "Stok: "
                                    );

                                    hasil.append(
                                            stok
                                    );

                                    hasil.append(
                                            "\n"
                                    );

                                    hasil.append(
                                            "Supplier: "
                                    );

                                    hasil.append(
                                            supplier
                                    );

                                    hasil.append(
                                            "\n"
                                    );

                                    hasil.append(
                                            "Catatan: "
                                    );

                                    hasil.append(
                                            catatan
                                    );

                                    hasil.append(
                                            "\n"
                                    );
                                }
                            }

                            hasil.append(
                                    "━━━━━━━━━━━━━━━━━━\n"
                            );

                            hasil.insert(
                                    0,
                                    "Ditemukan "
                                            + jumlah
                                            + " part\n\n"
                            );

                            if (jumlah == 0) {

                                hasilCari.setText(
                                        "❌ Part tidak ditemukan."
                                );

                            } else {

                                hasilCari.setText(
                                        hasil.toString()
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            hasilCari.setText(
                                    "❌ Gagal mengambil data."
                            );

                            Toast.makeText(
                                    MainActivity.this,
                                    "Error: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =====================================================
    // FORMAT RUPIAH
    // =====================================================

    private String formatRupiah(long angka) {

        NumberFormat format =
                NumberFormat.getCurrencyInstance(
                        new Locale("id", "ID")
                );

        return format.format(angka)
                .replace(",00", "");
    }

    // =====================================================
    // BERSIHKAN FORM
    // =====================================================

    private void bersihkanForm() {

        namaPartInput.setText("");
        hargaPokokInput.setText("");
        kodePartInput.setText("");
        stokInput.setText("");
        supplierInput.setText("");
        catatanInput.setText("");

        namaPartInput.requestFocus();
    }
}
