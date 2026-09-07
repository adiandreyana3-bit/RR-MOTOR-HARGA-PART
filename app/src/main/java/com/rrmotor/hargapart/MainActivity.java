package com.rrmotor.hargapart;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText namaPartInput;
    private EditText hargaPokokInput;
    private EditText kodePartInput;
    private EditText stokInput;
    private EditText supplierInput;
    private EditText catatanInput;

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

        Button cariButton = new Button(this);
        cariButton.setText("🔎 CARI PART");
        cariButton.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Menu pencarian akan kita tambahkan berikutnya.",
                        Toast.LENGTH_SHORT
                )
        );

        layout.addView(cariButton);

        Button scanButton = new Button(this);
        scanButton.setText("📷 SCAN NOTA SUPPLIER");
        scanButton.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "OCR akan kita tambahkan setelah penyimpanan part berhasil.",
                        Toast.LENGTH_SHORT
                )
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

    private void simpanPart() {

        String namaPart = namaPartInput.getText().toString().trim();
        String hargaText = hargaPokokInput.getText().toString().trim();

        if (namaPart.isEmpty()) {
            namaPartInput.setError("Nama Part wajib diisi");
            namaPartInput.requestFocus();
            return;
        }

        if (hargaText.isEmpty()) {
            hargaPokokInput.setError("Harga Pokok wajib diisi");
            hargaPokokInput.requestFocus();
            return;
        }

        long hargaPokok;

        try {
            hargaPokok = Long.parseLong(hargaText);
        } catch (NumberFormatException e) {
            hargaPokokInput.setError("Harga tidak valid");
            hargaPokokInput.requestFocus();
            return;
        }

        long stok = 0;

        String stokText = stokInput.getText().toString().trim();

        if (!stokText.isEmpty()) {
            try {
                stok = Long.parseLong(stokText);
            } catch (NumberFormatException e) {
                stokInput.setError("Stok tidak valid");
                stokInput.requestFocus();
                return;
            }
        }

        Map<String, Object> data = new HashMap<>();

        data.put("namaPart", namaPart);
        data.put("hargaPokok", hargaPokok);
        data.put("kodePart", kodePartInput.getText().toString().trim());
        data.put("stok", stok);
        data.put("supplier", supplierInput.getText().toString().trim());
        data.put("catatan", catatanInput.getText().toString().trim());
        data.put("updatedAt", System.currentTimeMillis());

        db.collection("parts")
                .add(data)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            MainActivity.this,
                            "✅ Part berhasil disimpan ke Firebase",
                            Toast.LENGTH_LONG
                    ).show();

                    bersihkanForm();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MainActivity.this,
                            "❌ Gagal menyimpan: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

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
