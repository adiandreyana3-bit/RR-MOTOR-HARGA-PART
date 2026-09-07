package com.rrmotor.hargapart;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText namaPartInput;
    private EditText hargaPokokInput;
    private EditText kodePartInput;
    private EditText stokInput;
    private EditText supplierInput;
    private EditText catatanInput;

    private TextView hasilHargaText;
    private TextView hasilPencarianText;

    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            tampilkanLogin();
        } else {
            tampilkanMenuUtama();
        }
    }

    // ============================================================
    // LOGIN
    // ============================================================

    private void tampilkanLogin() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(35, 45, 35, 40);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);

        TextView judul = new TextView(this);
        judul.setText("🏍️ RR MOTOR");
        judul.setTextSize(30);
        judul.setGravity(Gravity.CENTER);
        judul.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(
                judul,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView subjudul = new TextView(this);
        subjudul.setText("CEK HARGA PART");
        subjudul.setTextSize(20);
        subjudul.setGravity(Gravity.CENTER);
        subjudul.setPadding(0, 10, 0, 35);

        root.addView(subjudul);

        EditText emailInput = buatInputLogin(
                "Email",
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        EditText passwordInput = buatInputLogin(
                "Password",
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        passwordInput.setTransformationMethod(
                PasswordTransformationMethod.getInstance()
        );

        root.addView(emailInput);
        root.addView(passwordInput);

        Button masuk = new Button(this);
        masuk.setText("🔐 MASUK");
        masuk.setTextSize(17);
        masuk.setAllCaps(false);

        root.addView(masuk);

        Button daftar = new Button(this);
        daftar.setText("📝 DAFTAR AKUN BARU");
        daftar.setTextSize(16);
        daftar.setAllCaps(false);

        root.addView(daftar);

        TextView info = new TextView(this);
        info.setText(
                "Gunakan akun Firebase RR MOTOR.\n" +
                "Akun yang sama dapat digunakan di HP lain."
        );
        info.setGravity(Gravity.CENTER);
        info.setTextSize(14);
        info.setPadding(10, 25, 10, 10);

        root.addView(info);

        masuk.setOnClickListener(v -> {

            String email = emailInput.getText()
                    .toString()
                    .trim();

            String password = passwordInput.getText()
                    .toString();

            if (email.isEmpty()) {
                emailInput.setError("Email wajib diisi");
                emailInput.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password wajib diisi");
                passwordInput.requestFocus();
                return;
            }

            masuk.setEnabled(false);
            masuk.setText("⏳ MASUK...");

            auth.signInWithEmailAndPassword(
                            email,
                            password
                    )
                    .addOnCompleteListener(task -> {

                        masuk.setEnabled(true);
                        masuk.setText("🔐 MASUK");

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    this,
                                    "Login berhasil 👍",
                                    Toast.LENGTH_SHORT
                            ).show();

                            tampilkanMenuUtama();

                        } else {

                            String pesan =
                                    "Login gagal";

                            if (task.getException() != null) {
                                pesan =
                                        task.getException()
                                                .getMessage();
                            }

                            Toast.makeText(
                                    this,
                                    pesan,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        daftar.setOnClickListener(
                v -> tampilkanDaftarAkun()
        );

        setContentView(scroll);
    }

    private EditText buatInputLogin(
            String hint,
            int inputType
    ) {

        EditText input = new EditText(this);

        input.setHint(hint);
        input.setTextSize(17);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setPadding(20, 15, 20, 15);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 10, 0, 10);

        input.setLayoutParams(params);

        return input;
    }

    // ============================================================
    // DAFTAR AKUN
    // ============================================================

    private void tampilkanDaftarAkun() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 45, 35, 40);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);

        TextView judul = new TextView(this);
        judul.setText("📝 DAFTAR AKUN RR MOTOR");
        judul.setTextSize(24);
        judul.setGravity(Gravity.CENTER);
        judul.setTypeface(null, android.graphics.Typeface.BOLD);
        judul.setPadding(0, 0, 0, 30);

        root.addView(judul);

        EditText emailInput = buatInputLogin(
                "Email"
                ,
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        EditText passwordInput = buatInputLogin(
                "Password minimal 6 karakter",
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        EditText ulangInput = buatInputLogin(
                "Ulangi Password",
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        passwordInput.setTransformationMethod(
                PasswordTransformationMethod.getInstance()
        );

        ulangInput.setTransformationMethod(
                PasswordTransformationMethod.getInstance()
        );

        root.addView(emailInput);
        root.addView(passwordInput);
        root.addView(ulangInput);

        Button daftar = new Button(this);
        daftar.setText("📝 DAFTAR");
        daftar.setAllCaps(false);
        daftar.setTextSize(17);

        root.addView(daftar);

        Button kembali = new Button(this);
        kembali.setText("⬅️ KEMBALI");
        kembali.setAllCaps(false);

        root.addView(kembali);

        daftar.setOnClickListener(v -> {

            String email =
                    emailInput.getText()
                            .toString()
                            .trim();

            String password =
                    passwordInput.getText()
                            .toString();

            String ulang =
                    ulangInput.getText()
                            .toString();

            if (email.isEmpty()) {
                emailInput.setError(
                        "Email wajib diisi"
                );
                emailInput.requestFocus();
                return;
            }

            if (password.length() < 6) {
                passwordInput.setError(
                        "Password minimal 6 karakter"
                );
                passwordInput.requestFocus();
                return;
            }

            if (!password.equals(ulang)) {
                ulangInput.setError(
                        "Password tidak sama"
                );
                ulangInput.requestFocus();
                return;
            }

            daftar.setEnabled(false);
            daftar.setText("⏳ MEMBUAT AKUN...");

            auth.createUserWithEmailAndPassword(
                            email,
                            password
                    )
                    .addOnCompleteListener(task -> {

                        daftar.setEnabled(true);
                        daftar.setText("📝 DAFTAR");

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    this,
                                    "Akun berhasil dibuat 👍",
                                    Toast.LENGTH_SHORT
                            ).show();

                            tampilkanMenuUtama();

                        } else {

                            String pesan =
                                    "Gagal membuat akun";

                            if (task.getException() != null) {
                                pesan =
                                        task.getException()
                                                .getMessage();
                            }

                            Toast.makeText(
                                    this,
                                    pesan,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        kembali.setOnClickListener(
                v -> tampilkanLogin()
        );

        setContentView(scroll);
    }

    // ============================================================
    // MENU UTAMA
    // ============================================================

    private void tampilkanMenuUtama() {

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                20,
                20,
                20,
                50
        );

        scrollView.addView(root);

        TextView judul =
                new TextView(this);

        judul.setText(
                "🏍️ RR MOTOR\nCEK HARGA PART"
        );

        judul.setTextSize(25);
        judul.setGravity(Gravity.CENTER);
        judul.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        judul.setPadding(
                0,
                10,
                0,
                25
        );

        root.addView(judul);

        // ========================================================
        // CARI PART
        // ========================================================

        TextView cariTitle =
                new TextView(this);

        cariTitle.setText(
                "🔎 CARI PART"
        );

        cariTitle.setTextSize(19);
        cariTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        cariTitle.setPadding(
                5,
                10,
                5,
                8
        );

        root.addView(cariTitle);

        EditText cariInput =
                buatInput(
                        "Nama atau Kode Part"
                );

        root.addView(cariInput);

        Button cariButton =
                new Button(this);

        cariButton.setText(
                "🔎 CARI"
        );

        cariButton.setAllCaps(false);

        root.addView(cariButton);

        hasilPencarianText =
                new TextView(this);

        hasilPencarianText.setTextSize(16);
        hasilPencarianText.setPadding(
                10,
                10,
                10,
                20
        );

        root.addView(
                hasilPencarianText
        );

        cariButton.setOnClickListener(
                v -> cariPart(
                        cariInput.getText()
                                .toString()
                                .trim()
                )
        );

        // ========================================================
        // MENU DAFTAR PART
        // ========================================================

        Button daftarPartButton =
                new Button(this);

        daftarPartButton.setText(
                "📋 DAFTAR PART TERSIMPAN"
        );

        daftarPartButton.setTextSize(17);
        daftarPartButton.setAllCaps(false);

        root.addView(
                daftarPartButton
        );

        daftarPartButton.setOnClickListener(
                v -> tampilkanDaftarPart()
        );

        // ========================================================
        // GARIS
        // ========================================================

        TextView garis =
                new TextView(this);

        garis.setText(
                "────────────────────────"
        );

        garis.setGravity(
                Gravity.CENTER
        );

        garis.setPadding(
                0,
                20,
                0,
                15
        );

        root.addView(garis);

        // ========================================================
        // TAMBAH PART
        // ========================================================

        TextView tambahTitle =
                new TextView(this);

        tambahTitle.setText(
                "➕ TAMBAH PART"
        );

        tambahTitle.setTextSize(19);
        tambahTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        tambahTitle.setPadding(
                5,
                5,
                5,
                10
        );

        root.addView(tambahTitle);

        namaPartInput =
                buatInput(
                        "Nama Part *"
                );

        hargaPokokInput =
                buatInput(
                        "Harga Pokok / Modal *"
                );

        kodePartInput =
                buatInput(
                        "Kode Part (opsional)"
                );

        stokInput =
                buatInput(
                        "Stok (opsional)"
                );

        supplierInput =
                buatInput(
                        "Supplier (opsional)"
                );

        catatanInput =
                buatInput(
                        "Catatan (opsional)"
                );

        hargaPokokInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        stokInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        catatanInput.setSingleLine(false);
        catatanInput.setMinLines(3);
        catatanInput.setGravity(
                Gravity.TOP | Gravity.START
        );

        root.addView(namaPartInput);
        root.addView(hargaPokokInput);
        root.addView(kodePartInput);
        root.addView(stokInput);
        root.addView(supplierInput);
        root.addView(catatanInput);

        // ========================================================
        // HITUNG HARGA
        // ========================================================

        Button hitungButton =
                new Button(this);

        hitungButton.setText(
                "💰 HITUNG HARGA JUAL"
        );

        hitungButton.setTextSize(17);
        hitungButton.setAllCaps(false);

        root.addView(hitungButton);

        hasilHargaText =
                new TextView(this);

        hasilHargaText.setTextSize(17);
        hasilHargaText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        hasilHargaText.setPadding(
                10,
                10,
                10,
                10
        );

        root.addView(
                hasilHargaText
        );

        hitungButton.setOnClickListener(
                v -> hitungHargaJual()
        );

        // ========================================================
        // SIMPAN
        // ========================================================

        Button simpanButton =
                new Button(this);

        simpanButton.setText(
                "💾 SIMPAN PART"
        );

        simpanButton.setTextSize(17);
        simpanButton.setAllCaps(false);

        root.addView(simpanButton);

        simpanButton.setOnClickListener(
                v -> simpanPart()
        );

        // ========================================================
        // SCAN NOTA
        // ========================================================

        Button scanButton =
                new Button(this);

        scanButton.setText(
                "📷 SCAN NOTA SUPPLIER"
        );

        scanButton.setTextSize(17);
        scanButton.setAllCaps(false);

        root.addView(scanButton);

        scanButton.setOnClickListener(
                v -> mulaiScanNota()
        );

        // ========================================================
        // LOGOUT
        // ========================================================

        TextView garis2 =
                new TextView(this);

        garis2.setText(
                "────────────────────────"
        );

        garis2.setGravity(
                Gravity.CENTER
        );

        garis2.setPadding(
                0,
                25,
                0,
                10
        );

        root.addView(garis2);

        Button keluarButton =
                new Button(this);

        keluarButton.setText(
                "🚪 KELUAR AKUN"
        );

        keluarButton.setAllCaps(false);

        root.addView(keluarButton);

        keluarButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle(
                            "Keluar Akun"
                    )
                    .setMessage(
                            "Apakah Anda ingin keluar dari akun?"
                    )
                    .setNegativeButton(
                            "BATAL",
                            null
                    )
                    .setPositiveButton(
                            "KELUAR",
                            (dialog, which) -> {

                                auth.signOut();

                                tampilkanLogin();
                            }
                    )
                    .show();
        });

        setContentView(scrollView);
    }

    // ============================================================
    // INPUT
    // ============================================================

    private EditText buatInput(
            String hint
    ) {

        EditText input =
                new EditText(this);

        input.setHint(hint);
        input.setTextSize(16);
        input.setSingleLine(true);

        input.setPadding(
                15,
                12,
                15,
                12
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                5,
                0,
                5
        );

        input.setLayoutParams(params);

        return input;
    }

    // ============================================================
    // HITUNG HARGA JUAL
    // ============================================================

    private void hitungHargaJual() {

        String nama =
                namaPartInput.getText()
                        .toString()
                        .trim();

        long modal =
                angka(hargaPokokInput);

        if (nama.isEmpty()) {

            namaPartInput.setError(
                    "Nama Part wajib diisi"
            );

            namaPartInput.requestFocus();
            return;
        }

        if (modal <= 0) {

            hargaPokokInput.setError(
                    "Harga Pokok wajib diisi"
            );

            hargaPokokInput.requestFocus();
            return;
        }

        double[] margin =
                hitungMargin(modal);

        long hargaMin =
                Math.round(
                        modal +
                                (modal *
                                        margin[0] /
                                        100.0)
                );

        long hargaMax =
                Math.round(
                        modal +
                                (modal *
                                        margin[1] /
                                        100.0)
                );

        hasilHargaText.setText(
                "Nama: " + nama +
                        "\nModal: " +
                        formatRupiah(modal) +
                        "\nMargin: " +
                        formatPersen(margin[0]) +
                        " - " +
                        formatPersen(margin[1]) +
                        "\nHarga Jual: " +
                        formatRupiah(hargaMin) +
                        " - " +
                        formatRupiah(hargaMax)
        );
    }

    // ============================================================
    // ATURAN MARGIN
    // ============================================================

    private double[] hitungMargin(
            long modal
    ) {

        String nama =
                namaPartInput == null
                        ? ""
                        : namaPartInput.getText()
                        .toString()
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        // OLI = FIX 10%
        if (nama.contains("oli")) {
            return new double[]{
                    10.0,
                    10.0
            };
        }

        if (modal < 10000) {
            return new double[]{
                    100.0,
                    120.0
            };
        }

        if (modal <= 15000) {
            return interpolasi(
                    modal,
                    10000,
                    15000,
                    100,
                    120,
                    60,
                    80
            );
        }

        if (modal <= 25000) {
            return interpolasi(
                    modal,
                    15000,
                    25000,
                    60,
                    80,
                    60,
                    80
            );
        }

        if (modal <= 30000) {
            return interpolasi(
                    modal,
                    25000,
                    30000,
                    60,
                    80,
                    35,
                    50
            );
        }

        if (modal <= 50000) {
            return interpolasi(
                    modal,
                    30000,
                    50000,
                    35,
                    50,
                    35,
                    50
            );
        }

        if (modal <= 60000) {
            return interpolasi(
                    modal,
                    50000,
                    60000,
                    35,
                    50,
                    20,
                    30
            );
        }

        if (modal <= 90000) {
            return interpolasi(
                    modal,
                    60000,
                    90000,
                    20,
                    30,
                    20,
                    30
            );
        }

        if (modal <= 100000) {
            return interpolasi(
                    modal,
                    90000,
                    100000,
                    20,
                    30,
                    10,
                    18
            );
        }

        if (modal <= 150000) {
            return interpolasi(
                    modal,
                    100000,
                    150000,
                    10,
                    18,
                    10,
                    18
            );
        }

        if (modal <= 160000) {
            return interpolasi(
                    modal,
                    150000,
                    160000,
                    10,
                    18,
                    10,
                    15
            );
        }

        if (modal <= 200000) {
            return interpolasi(
                    modal,
                    160000,
                    200000,
                    10,
                    15,
                    10,
                    15
            );
        }

        return new double[]{
                10.0,
                15.0
        };
    }

    private double[] interpolasi(
            long nilai,
            long batas1,
            long batas2,
            double min1,
            double max1,
            double min2,
            double max2
    ) {

        if (batas2 == batas1) {
            return new double[]{
                    min1,
                    max1
            };
        }

        double posisi =
                (double)
                        (nilai - batas1)
                        /
                        (double)
                                (batas2 - batas1);

        double min =
                min1 +
                        ((min2 - min1)
                                * posisi);

        double max =
                max1 +
                        ((max2 - max1)
                                * posisi);

        return new double[]{
                min,
                max
        };
    }

    // ============================================================
    // SIMPAN PART
    // ============================================================

    private void simpanPart() {

        String nama =
                namaPartInput.getText()
                        .toString()
                        .trim();

        long modal =
                angka(hargaPokokInput);

        String kode =
                kodePartInput.getText()
                        .toString()
                        .trim();

        long stok =
                angka(stokInput);

        String supplier =
                supplierInput.getText()
                        .toString()
                        .trim();

        String catatan =
                catatanInput.getText()
                        .toString()
                        .trim();

        if (nama.isEmpty()) {

            namaPartInput.setError(
                    "Nama Part wajib diisi"
            );

            namaPartInput.requestFocus();
            return;
        }

        if (modal <= 0) {

            hargaPokokInput.setError(
                    "Harga Pokok wajib diisi"
            );

            hargaPokokInput.requestFocus();
            return;
        }

        double[] margin =
                hitungMargin(modal);

        long hargaMin =
                Math.round(
                        modal +
                                modal *
                                        margin[0] /
                                        100.0
                );

        long hargaMax =
                Math.round(
                        modal +
                                modal *
                                        margin[1] /
                                        100.0
                );

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "namaPart",
                nama
        );

        data.put(
                "hargaPokok",
                modal
        );

        data.put(
                "marginMin",
                margin[0]
        );

        data.put(
                "marginMax",
                margin[1]
        );

        data.put(
                "hargaJualMin",
                hargaMin
        );

        data.put(
                "hargaJualMax",
                hargaMax
        );

        data.put(
                "kodePart",
                kode
        );

        data.put(
                "stok",
                stok
        );

        data.put(
                "supplier",
                supplier
        );

        data.put(
                "catatan",
                catatan
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
                                    this,
                                    "Part berhasil disimpan ☁️",
                                    Toast.LENGTH_SHORT
                            ).show();

                            bersihkanForm();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Gagal menyimpan: " +
                                        e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // ============================================================
    // DAFTAR PART TERSIMPAN
    // ============================================================

    private void tampilkanDaftarPart() {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .create();

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                20,
                15,
                20,
                15
        );

        TextView judul =
                new TextView(this);

        judul.setText(
                "📋 DAFTAR PART TERSIMPAN"
        );

        judul.setTextSize(21);
        judul.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        judul.setGravity(
                Gravity.CENTER
        );

        judul.setPadding(
                5,
                5,
                5,
                15
        );

        root.addView(judul);

        EditText cari =
                buatInput(
                        "🔎 Cari nama atau kode part"
                );

        root.addView(cari);

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout daftar =
                new LinearLayout(this);

        daftar.setOrientation(
                LinearLayout.VERTICAL
        );

        scroll.addView(daftar);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        Button tutup =
                new Button(this);

        tutup.setText(
                "TUTUP"
        );

        tutup.setAllCaps(false);

        root.addView(tutup);

        dialog.setView(root);

        tutup.setOnClickListener(
                v -> dialog.dismiss()
        );

        final List<DocumentSnapshot> semuaPart =
                new ArrayList<>();

        Runnable tampilkanData = () -> {

            daftar.removeAllViews();

            String kata =
                    cari.getText()
                            .toString()
                            .trim()
                            .toLowerCase(
                                    Locale.getDefault()
                            );

            int jumlah = 0;

            for (DocumentSnapshot doc :
                    semuaPart) {

                String nama =
                        getStringField(
                                doc,
                                "namaPart"
                        );

                String kode =
                        getStringField(
                                doc,
                                "kodePart"
                        );

                String gabungan =
                        (
                                nama + " " + kode
                        )
                                .toLowerCase(
                                        Locale.getDefault()
                                );

                if (!kata.isEmpty() &&
                        !gabungan.contains(kata)) {
                    continue;
                }

                daftar.addView(
                        buatCardPart(doc)
                );

                jumlah++;
            }

            if (jumlah == 0) {

                TextView kosong =
                        new TextView(this);

                kosong.setText(
                        "Tidak ada part ditemukan."
                );

                kosong.setTextSize(16);
                kosong.setGravity(
                        Gravity.CENTER
                );

                kosong.setPadding(
                        20,
                        40,
                        20,
                        40
                );

                daftar.addView(
                        kosong
                );
            }
        };

        cari.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        tampilkanData.run();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {
                    }
                }
        );

        db.collection("parts")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            semuaPart.clear();

                            semuaPart.addAll(
                                    queryDocumentSnapshots
                                            .getDocuments()
                            );

                            tampilkanData.run();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            TextView error =
                                    new TextView(this);

                            error.setText(
                                    "Gagal mengambil data:\n" +
                                            e.getMessage()
                            );

                            error.setTextSize(16);
                            error.setPadding(
                                    15,
                                    25,
                                    15,
                                    25
                            );

                            daftar.addView(
                                    error
                            );
                        }
                );

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow()
                    .setLayout(
                            (int)
                                    (
                                            getResources()
                                                    .getDisplayMetrics()
                                                    .widthPixels
                                                    * 0.95
                                    ),
                            (int)
                                    (
                                            getResources()
                                                    .getDisplayMetrics()
                                                    .heightPixels
                                                    * 0.85
                                    )
                    );
        }
    }

    // ============================================================
    // CARD PART
    // ============================================================

    private LinearLayout buatCardPart(
            DocumentSnapshot doc
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                15,
                15,
                15,
                15
        );

        TextView info =
                new TextView(this);

        String nama =
                getStringField(
                        doc,
                        "namaPart"
                );

        String kode =
                getStringField(
                        doc,
                        "kodePart"
                );

        String supplier =
                getStringField(
                        doc,
                        "supplier"
                );

        String catatan =
                getStringField(
                        doc,
                        "catatan"
                );

        long modal =
                getLongField(
                        doc,
                        "hargaPokok"
                );

        long hargaMin =
                getLongField(
                        doc,
                        "hargaJualMin"
                );

        long hargaMax =
                getLongField(
                        doc,
                        "hargaJualMax"
                );

        long stok =
                getLongField(
                        doc,
                        "stok"
                );

        StringBuilder teks =
                new StringBuilder();

        teks.append(
                "🔧 "
        )
                .append(nama)
                .append("\n");

        if (!kode.isEmpty()) {

            teks.append(
                    "Kode: "
            )
                    .append(kode)
                    .append("\n");
        }

        teks.append(
                "💰 Modal: "
        )
                .append(
                        formatRupiah(modal)
                )
                .append("\n");

        teks.append(
                "💵 Jual: "
        )
                .append(
                        formatRupiah(hargaMin)
                )
                .append(" - ")
                .append(
                        formatRupiah(hargaMax)
                )
                .append("\n");

        teks.append(
                "📦 STOK: "
        )
                .append(stok)
                .append("\n");

        if (!supplier.isEmpty()) {

            teks.append(
                    "🏪 Supplier: "
            )
                    .append(supplier)
                    .append("\n");
        }

        if (!catatan.isEmpty()) {

            teks.append(
                    "📝 Catatan: "
            )
                    .append(catatan)
                    .append("\n");
        }

        info.setText(
                teks.toString()
        );

        info.setTextSize(16);

        card.addView(info);

        TextView garis =
                new TextView(this);

        garis.setText(
                "────────────────────"
        );

        garis.setGravity(
                Gravity.CENTER
        );

        card.addView(garis);

        return card;
    }

    // ============================================================
    // CARI PART
    // ============================================================

    private void cariPart(
            String kata
    ) {

        if (kata.isEmpty()) {

            Toast.makeText(
                    this,
                    "Masukkan nama atau kode part",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("parts")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            StringBuilder hasil =
                                    new StringBuilder();

                            int jumlah = 0;

                            for (DocumentSnapshot doc :
                                    queryDocumentSnapshots
                                            .getDocuments()) {

                                String nama =
                                        getStringField(
                                                doc,
                                                "namaPart"
                                        );

                                String kode =
                                        getStringField(
                                                doc,
                                                "kodePart"
                                        );

                                String gabungan =
                                        (
                                                nama + " " +
                                                        kode
                                        )
                                                .toLowerCase(
                                                        Locale.getDefault()
                                                );

                                if (!gabungan.contains(
                                        kata.toLowerCase(
                                                Locale.getDefault()
                                        )
                                )) {
                                    continue;
                                }

                                long modal =
                                        getLongField(
                                                doc,
                                                "hargaPokok"
                                        );

                                long hargaMin =
                                        getLongField(
                                                doc,
                                                "hargaJualMin"
                                        );

                                long hargaMax =
                                        getLongField(
                                                doc,
                                                "hargaJualMax"
                                        );

                                long stok =
                                        getLongField(
                                                doc,
                                                "stok"
                                        );

                                hasil.append(
                                        "🔧 "
                                )
                                        .append(nama)
                                        .append("\n");

                                if (!kode.isEmpty()) {

                                    hasil.append(
                                            "Kode: "
                                    )
                                            .append(kode)
                                            .append("\n");
                                }

                                hasil.append(
                                        "Modal: "
                                )
                                        .append(
                                                formatRupiah(
                                                        modal
                                                )
                                        )
                                        .append("\n");

                                hasil.append(
                                        "Harga Jual: "
                                )
                                        .append(
                                                formatRupiah(
                                                        hargaMin
                                                )
                                        )
                                        .append(
                                                " - "
                                        )
                                        .append(
                                                formatRupiah(
                                                        hargaMax
                                                )
                                        )
                                        .append("\n");

                                hasil.append(
                                        "📦 Stok: "
                                )
                                        .append(stok)
                                        .append("\n");

                                hasil.append(
                                        "────────────────\n"
                                );

                                jumlah++;
                            }

                            if (jumlah == 0) {

                                hasilPencarianText.setText(
                                        "❌ Part tidak ditemukan."
                                );

                            } else {

                                hasilPencarianText.setText(
                                        hasil.toString()
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e ->
                                hasilPencarianText.setText(
                                        "Gagal mencari part:\n" +
                                                e.getMessage()
                                )
                );
    }

    // ============================================================
    // SCAN NOTA SUPPLIER
    // ============================================================

    private void mulaiScanNota() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    REQUEST_CAMERA
            );

            return;
        }

        bukaKamera();
    }

    private void bukaKamera() {

        try {

            Intent intent =
                    new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE
                    );

            startActivityForResult(
                    intent,
                    REQUEST_IMAGE_CAPTURE
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Kamera tidak dapat dibuka",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode ==
                REQUEST_IMAGE_CAPTURE &&
                resultCode ==
                        RESULT_OK &&
                data != null) {

            Bundle extras =
                    data.getExtras();

            if (extras == null) {
                return;
            }

            Object object =
                    extras.get("data");

            if (object instanceof Bitmap) {

                Bitmap bitmap =
                        (Bitmap) object;

                prosesOCR(bitmap);
            }
        }
    }

    // ============================================================
    // OCR
    // ============================================================

    private void prosesOCR(
            Bitmap bitmap
    ) {

        Toast.makeText(
                this,
                "⏳ Membaca nota...",
                Toast.LENGTH_SHORT
        ).show();

        try {

            InputImage image =
                    InputImage.fromBitmap(
                            bitmap,
                            0
                    );

            TextRecognizer recognizer =
                    TextRecognition
                            .getClient(
                                    TextRecognizerOptions
                                            .DEFAULT_OPTIONS
                            );

            recognizer.process(image)
                    .addOnSuccessListener(
                            result -> {

                                String teks =
                                        result.getText();

                                if (teks == null ||
                                        teks.trim().isEmpty()) {

                                    Toast.makeText(
                                            this,
                                            "Tulisan tidak terbaca",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                tampilkanHasilOCR(
                                        teks
                                );
                            }
                    )
                    .addOnFailureListener(
                            e ->
                                    Toast.makeText(
                                            this,
                                            "OCR gagal: " +
                                                    e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                    );
        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Gagal memproses gambar",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // HASIL OCR EDITABLE
    // ============================================================

    private void tampilkanHasilOCR(
            String teksOCR
    ) {

        String nama =
                deteksiNamaPart(
                        teksOCR
                );

        long harga =
                deteksiHarga(
                        teksOCR
                );

        long qty =
                deteksiQty(
                        teksOCR
                );

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                20,
                10,
                20,
                10
        );

        EditText namaInput =
                buatInput(
                        "Nama Part"
                );

        namaInput.setText(
                nama
        );

        EditText hargaInput =
                buatInput(
                        "Harga Pokok"
                );

        hargaInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        if (harga > 0) {

            hargaInput.setText(
                    String.valueOf(harga)
            );
        }

        EditText qtyInput =
                buatInput(
                        "Jumlah / Qty"
                );

        qtyInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        qtyInput.setText(
                String.valueOf(qty)
        );

        root.addView(namaInput);
        root.addView(hargaInput);
        root.addView(qtyInput);

        TextView raw =
                new TextView(this);

        raw.setText(
                "HASIL OCR:\n" +
                        teksOCR
        );

        raw.setTextSize(13);
        raw.setPadding(
                5,
                15,
                5,
                10
        );

        root.addView(raw);

        new AlertDialog.Builder(this)
                .setTitle(
                        "📷 HASIL SCAN NOTA"
                )
                .setView(root)
                .setNegativeButton(
                        "BATAL",
                        null
                )
                .setPositiveButton(
                        "GUNAKAN DATA",
                        (dialog, which) -> {

                            this.namaPartInput
                                    .setText(
                                            namaInput
                                                    .getText()
                                                    .toString()
                                    );

                            this.hargaPokokInput
                                    .setText(
                                            hargaInput
                                                    .getText()
                                                    .toString()
                                    );

                            this.stokInput
                                    .setText(
                                            qtyInput
                                                    .getText()
                                                    .toString()
                                    );

                            hitungHargaJual();

                            Toast.makeText(
                                    this,
                                    "Data hasil scan dimasukkan. Silakan periksa lalu SIMPAN.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .show();
    }

    // ============================================================
    // DETEKSI NAMA
    // ============================================================

    private String deteksiNamaPart(
            String teks
    ) {

        String[] baris =
                teks.split("\\r?\\n");

        for (String barisSatu :
                baris) {

            String b =
                    barisSatu.trim();

            if (b.isEmpty()) {
                continue;
            }

            if (b.matches(
                    ".*\\d{1,3}[.,]?\\d{3}.*"
            )) {

                String hasil =
                        b.replaceAll(
                                "(?i)Rp\\s*",
                                ""
                        );

                hasil =
                        hasil.replaceAll(
                                "\\d+[.,]?\\d*",
                                ""
                        );

                hasil =
                        hasil.replaceAll(
                                "[xX]\\s*\\d+",
                                ""
                        );

                hasil =
                        hasil.replaceAll(
                                "\\s+",
                                " "
                        )
                                .trim();

                if (!hasil.isEmpty()) {
                    return hasil;
                }
            }
        }

        if (baris.length > 0) {
            return baris[0].trim();
        }

        return "";
    }

    // ============================================================
    // DETEKSI HARGA
    // ============================================================

    private long deteksiHarga(
            String teks
    ) {

        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile(
                        "(?i)(?:Rp\\s*)?([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,})"
                );

        java.util.regex.Matcher matcher =
                pattern.matcher(teks);

        while (matcher.find()) {

            String angka =
                    matcher.group(1);

            if (angka == null) {
                continue;
            }

            angka =
                    angka.replace(
                            ".",
                            ""
                    )
                            .replace(
                                    ",",
                                    ""
                            );

            try {

                long nilai =
                        Long.parseLong(
                                angka
                        );

                if (nilai >= 1000) {
                    return nilai;
                }

            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    // ============================================================
    // DETEKSI QTY
    // ============================================================

    private long deteksiQty(
            String teks
    ) {

        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile(
                        "(?i)(?:qty|jumlah|pcs|pc|x)\\s*[:=]?\\s*(\\d+)"
                );

        java.util.regex.Matcher matcher =
                pattern.matcher(teks);

        if (matcher.find()) {

            try {

                return Long.parseLong(
                        matcher.group(1)
                );

            } catch (Exception ignored) {
            }
        }

        return 1;
    }

    // ============================================================
    // BANTUAN FIRESTORE
    // ============================================================

    private String getStringField(
            DocumentSnapshot doc,
            String key
    ) {

        Object value =
                doc.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }

    private long getLongField(
            DocumentSnapshot doc,
            String key
    ) {

        Object value =
                doc.get(key);

        if (value instanceof Number) {

            return (
                    (Number) value
            )
                    .longValue();
        }

        try {

            return Long.parseLong(
                    String.valueOf(value)
            );

        } catch (Exception e) {

            return 0;
        }
    }

    // ============================================================
    // ANGKA
    // ============================================================

    private long angka(
            EditText input
    ) {

        if (input == null ||
                input.getText() == null) {

            return 0;
        }

        String teks =
                input.getText()
                        .toString()
                        .replace(
                                ".",
                                ""
                        )
                        .replace(
                                ",",
                                ""
                        )
                        .trim();

        if (teks.isEmpty()) {
            return 0;
        }

        try {

            return Long.parseLong(
                    teks
            );

        } catch (Exception e) {

            return 0;
        }
    }

    // ============================================================
    // FORMAT RUPIAH
    // ============================================================

    private String formatRupiah(
            long angka
    ) {

        NumberFormat nf =
                NumberFormat
                        .getNumberInstance(
                                new Locale(
                                        "id",
                                        "ID"
                                )
                        );

        return "Rp " +
                nf.format(angka);
    }

    private String formatPersen(
            double angka
    ) {

        return String.format(
                Locale.US,
                "%.1f%%",
                angka
        );
    }

    // ============================================================
    // BERSIHKAN FORM
    // ============================================================

    private void bersihkanForm() {

        if (namaPartInput != null) {
            namaPartInput.setText("");
        }

        if (hargaPokokInput != null) {
            hargaPokokInput.setText("");
        }

        if (kodePartInput != null) {
            kodePartInput.setText("");
        }

        if (stokInput != null) {
            stokInput.setText("");
        }

        if (supplierInput != null) {
            supplierInput.setText("");
        }

        if (catatanInput != null) {
            catatanInput.setText("");
        }

        if (hasilHargaText != null) {
            hasilHargaText.setText("");
        }

        if (namaPartInput != null) {
            namaPartInput.requestFocus();
        }
    }
}
