package com.rrmotor.hargapart;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private EditText cariInput;
    private TextView hasilCari;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            tampilkanLogin();
        } else {
            buatTampilan();
        }
    }

    // =====================================================
    // LOGIN
    // =====================================================

    private void tampilkanLogin() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 50, 40, 40);

        TextView judul = new TextView(this);
        judul.setText("🏍️ RR MOTOR\nCEK HARGA PART");
        judul.setTextSize(25);
        judul.setPadding(0, 0, 0, 30);
        layout.addView(judul);

        TextView keterangan = new TextView(this);
        keterangan.setText("🔐 LOGIN");
        keterangan.setTextSize(20);
        keterangan.setPadding(0, 10, 0, 20);
        layout.addView(keterangan);

        EditText emailInput = new EditText(this);
        emailInput.setHint("Email");
        emailInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );
        emailInput.setTextSize(17);
        layout.addView(emailInput);

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        passwordInput.setTextSize(17);
        layout.addView(passwordInput);

        Button loginButton = new Button(this);
        loginButton.setText("🔐 LOGIN");
        layout.addView(loginButton);

        Button daftarButton = new Button(this);
        daftarButton.setText("📝 DAFTAR AKUN BARU");
        layout.addView(daftarButton);

        loginButton.setOnClickListener(v -> {

            String email =
                    emailInput.getText().toString().trim();

            String password =
                    passwordInput.getText().toString();

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

            login(email, password);
        });

        daftarButton.setOnClickListener(
                v -> tampilkanDaftar()
        );

        setContentView(layout);
    }

    private void login(
            String email,
            String password) {

        Toast.makeText(
                this,
                "⏳ Login...",
                Toast.LENGTH_SHORT
        ).show();

        auth.signInWithEmailAndPassword(
                email,
                password
        ).addOnSuccessListener(
                result -> {

                    Toast.makeText(
                            this,
                            "✅ Login berhasil",
                            Toast.LENGTH_SHORT
                    ).show();

                    buatTampilan();
                }
        ).addOnFailureListener(
                e -> {

                    Toast.makeText(
                            this,
                            "❌ Login gagal: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }

    // =====================================================
    // DAFTAR AKUN
    // =====================================================

    private void tampilkanDaftar() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 50, 40, 40);

        TextView judul = new TextView(this);
        judul.setText("🏍️ RR MOTOR\nDAFTAR AKUN");
        judul.setTextSize(24);
        judul.setPadding(0, 0, 0, 25);
        layout.addView(judul);

        EditText emailInput = new EditText(this);
        emailInput.setHint("Email");
        emailInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );
        layout.addView(emailInput);

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password minimal 6 karakter");
        passwordInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        layout.addView(passwordInput);

        EditText ulangiPasswordInput = new EditText(this);
        ulangiPasswordInput.setHint("Ulangi Password");
        ulangiPasswordInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        layout.addView(ulangiPasswordInput);

        Button daftarButton = new Button(this);
        daftarButton.setText("📝 BUAT AKUN");
        layout.addView(daftarButton);

        Button kembaliButton = new Button(this);
        kembaliButton.setText("⬅️ KEMBALI LOGIN");
        layout.addView(kembaliButton);

        daftarButton.setOnClickListener(v -> {

            String email =
                    emailInput.getText().toString().trim();

            String password =
                    passwordInput.getText().toString();

            String ulangi =
                    ulangiPasswordInput.getText().toString();

            if (email.isEmpty()) {
                emailInput.setError("Email wajib diisi");
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

            if (!password.equals(ulangi)) {
                ulangiPasswordInput.setError(
                        "Password tidak sama"
                );
                ulangiPasswordInput.requestFocus();
                return;
            }

            buatAkun(email, password);
        });

        kembaliButton.setOnClickListener(
                v -> tampilkanLogin()
        );

        setContentView(layout);
    }

    private void buatAkun(
            String email,
            String password) {

        Toast.makeText(
                this,
                "⏳ Membuat akun...",
                Toast.LENGTH_SHORT
        ).show();

        auth.createUserWithEmailAndPassword(
                email,
                password
        ).addOnSuccessListener(
                result -> {

                    Toast.makeText(
                            this,
                            "✅ Akun berhasil dibuat",
                            Toast.LENGTH_LONG
                    ).show();

                    buatTampilan();
                }
        ).addOnFailureListener(
                e -> {

                    Toast.makeText(
                            this,
                            "❌ Gagal membuat akun: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }

    // =====================================================
    // TAMPILAN UTAMA
    // =====================================================

    private void buatTampilan() {

        ScrollView scrollView =
                new ScrollView(this);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30,
                30,
                30,
                30
        );

        TextView judul =
                new TextView(this);

        judul.setText(
                "🏍️ RR MOTOR\nCEK HARGA PART"
        );

        judul.setTextSize(24);

        judul.setPadding(
                0,
                0,
                0,
                20
        );

        layout.addView(judul);

        TextView akun =
                new TextView(this);

        FirebaseUser user =
                auth.getCurrentUser();

        String email =
                user != null
                        ? user.getEmail()
                        : "";

        akun.setText(
                "👤 Login: " + email
        );

        akun.setTextSize(14);

        layout.addView(akun);

        Button logoutButton =
                new Button(this);

        logoutButton.setText(
                "🚪 LOGOUT"
        );

        logoutButton.setOnClickListener(
                v -> logout()
        );

        layout.addView(
                logoutButton
        );

        TextView subjudul =
                new TextView(this);

        subjudul.setText(
                "Buku Harga & Stok Part"
        );

        subjudul.setTextSize(17);

        subjudul.setPadding(
                0,
                10,
                0,
                15
        );

        layout.addView(
                subjudul
        );

        // =========================
        // CARI PART
        // =========================

        TextView judulCari =
                new TextView(this);

        judulCari.setText(
                "🔎 CARI PART"
        );

        judulCari.setTextSize(19);

        layout.addView(
                judulCari
        );

        cariInput =
                buatInput(
                        "Nama atau Kode Part",
                        "Contoh: BUSI atau CPR6EA"
                );

        layout.addView(
                cariInput
        );

        Button cariButton =
                new Button(this);

        cariButton.setText(
                "🔎 CARI"
        );

        cariButton.setOnClickListener(
                v -> cariPart()
        );

        layout.addView(
                cariButton
        );

        hasilCari =
                new TextView(this);

        hasilCari.setTextSize(16);

        hasilCari.setPadding(
                0,
                15,
                0,
                20
        );

        layout.addView(
                hasilCari
        );

        // =========================
        // TAMBAH PART
        // =========================

        TextView judulTambah =
                new TextView(this);

        judulTambah.setText(
                "➕ TAMBAH PART"
        );

        judulTambah.setTextSize(19);

        layout.addView(
                judulTambah
        );

        namaPartInput =
                buatInput(
                        "Nama Part *",
                        "Contoh: BUSI NGK CPR6EA"
                );

        layout.addView(
                namaPartInput
        );

        hargaPokokInput =
                buatInput(
                        "Harga Pokok / Modal *",
                        "Contoh: 18000"
                );

        hargaPokokInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        layout.addView(
                hargaPokokInput
        );

        kodePartInput =
                buatInput(
                        "Kode Part",
                        "Contoh: CPR6EA"
                );

        layout.addView(
                kodePartInput
        );

        stokInput =
                buatInput(
                        "Stok",
                        "Contoh: 10"
                );

        stokInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        layout.addView(
                stokInput
        );

        supplierInput =
                buatInput(
                        "Supplier",
                        "Nama supplier"
                );

        layout.addView(
                supplierInput
        );

        catatanInput =
                buatInput(
                        "Catatan",
                        "Catatan tambahan"
                );

        layout.addView(
                catatanInput
        );

        Button hitungHargaButton =
                new Button(this);

        hitungHargaButton.setText(
                "💰 HITUNG HARGA JUAL"
        );

        hitungHargaButton.setOnClickListener(
                v -> hitungHargaJual()
        );

        layout.addView(
                hitungHargaButton
        );

        Button simpanButton =
                new Button(this);

        simpanButton.setText(
                "💾 SIMPAN PART"
        );

        simpanButton.setOnClickListener(
                v -> simpanPart()
        );

        layout.addView(
                simpanButton
        );

        // =========================
        // SCAN NOTA
        // =========================

        Button scanButton =
                new Button(this);

        scanButton.setText(
                "📷 SCAN NOTA SUPPLIER"
        );

        scanButton.setOnClickListener(
                v -> mulaiScanNota()
        );

        layout.addView(
                scanButton
        );

        scrollView.addView(
                layout
        );

        setContentView(
                scrollView
        );
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    private void logout() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage(
                        "Keluar dari akun RR MOTOR?"
                )
                .setNegativeButton(
                        "BATAL",
                        null
                )
                .setPositiveButton(
                        "LOGOUT",
                        (dialog, which) -> {

                            auth.signOut();

                            tampilkanLogin();
                        }
                )
                .show();
    }

    // =====================================================
    // INPUT
    // =====================================================

    private EditText buatInput(
            String hint,
            String contoh) {

        EditText input =
                new EditText(this);

        input.setHint(
                hint + "\n" + contoh
        );

        input.setTextSize(16);

        input.setPadding(
                15,
                15,
                15,
                15
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                5,
                0,
                10
        );

        input.setLayoutParams(
                params
        );

        return input;
    }

    // =====================================================
    // HITUNG HARGA
    // =====================================================

    private void hitungHargaJual() {

        String nama =
                namaPartInput.getText()
                        .toString()
                        .trim();

        String hargaText =
                hargaPokokInput.getText()
                        .toString()
                        .trim();

        if (nama.isEmpty()) {

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

        long modal;

        try {

            modal =
                    Long.parseLong(
                            hargaText
                    );

        } catch (NumberFormatException e) {

            hargaPokokInput.setError(
                    "Harga tidak valid"
            );

            return;
        }

        double[] margin =
                hitungMargin(
                        nama,
                        modal
                );

        long hargaMin =
                Math.round(
                        modal
                                * (1 + margin[0])
                );

        long hargaMax =
                Math.round(
                        modal
                                * (1 + margin[1])
                );

        hasilCari.setText(
                "💰 HARGA JUAL\n\n"
                        + "Modal: "
                        + formatRupiah(modal)
                        + "\n"
                        + "Margin: "
                        + formatPersen(margin[0])
                        + " - "
                        + formatPersen(margin[1])
                        + "\n\n"
                        + "Harga Jual Minimum:\n"
                        + formatRupiah(hargaMin)
                        + "\n\n"
                        + "Harga Jual Maksimum:\n"
                        + formatRupiah(hargaMax)
        );
    }

    // =====================================================
    // MARGIN
    // =====================================================

    private double[] hitungMargin(
            String nama,
            long modal) {

        String n =
                nama.toLowerCase(
                        Locale.ROOT
                );

        if (n.contains("oli")) {

            return new double[]{
                    0.10,
                    0.10
            };
        }

        if (modal < 10000) {
            return new double[]{
                    1.00,
                    1.20
            };
        }

        if (modal < 15000) {
            return interpolasi(
                    modal,
                    10000,
                    15000,
                    1.00,
                    0.60,
                    1.20,
                    0.80
            );
        }

        if (modal <= 25000) {
            return new double[]{
                    0.60,
                    0.80
            };
        }

        if (modal < 30000) {
            return interpolasi(
                    modal,
                    25000,
                    30000,
                    0.60,
                    0.35,
                    0.80,
                    0.50
            );
        }

        if (modal <= 50000) {
            return new double[]{
                    0.35,
                    0.50
            };
        }

        if (modal < 60000) {
            return interpolasi(
                    modal,
                    50000,
                    60000,
                    0.35,
                    0.20,
                    0.50,
                    0.30
            );
        }

        if (modal <= 90000) {
            return new double[]{
                    0.20,
                    0.30
            };
        }

        if (modal < 100000) {
            return interpolasi(
                    modal,
                    90000,
                    100000,
                    0.20,
                    0.10,
                    0.30,
                    0.18
            );
        }

        if (modal <= 150000) {
            return new double[]{
                    0.10,
                    0.18
            };
        }

        if (modal < 160000) {
            return interpolasi(
                    modal,
                    150000,
                    160000,
                    0.10,
                    0.10,
                    0.18,
                    0.15
            );
        }

        return new double[]{
                0.10,
                0.15
        };
    }

    private double[] interpolasi(
            long nilai,
            long bawah,
            long atas,
            double minBawah,
            double minAtas,
            double maxBawah,
            double maxAtas) {

        double posisi =
                (double)
                        (nilai - bawah)
                        / (double)
                        (atas - bawah);

        return new double[]{
                minBawah
                        + posisi
                        * (minAtas - minBawah),

                maxBawah
                        + posisi
                        * (maxAtas - maxBawah)
        };
    }

    // =====================================================
    // SIMPAN
    // =====================================================

    private void simpanPart() {

        String nama =
                namaPartInput.getText()
                        .toString()
                        .trim();

        String hargaText =
                hargaPokokInput.getText()
                        .toString()
                        .trim();

        if (nama.isEmpty()) {

            namaPartInput.setError(
                    "Nama Part wajib diisi"
            );

            return;
        }

        if (hargaText.isEmpty()) {

            hargaPokokInput.setError(
                    "Harga Pokok wajib diisi"
            );

            return;
        }

        long harga;

        try {

            harga =
                    Long.parseLong(
                            hargaText
                    );

        } catch (Exception e) {

            hargaPokokInput.setError(
                    "Harga tidak valid"
            );

            return;
        }

        long stok = 0;

        String stokText =
                stokInput.getText()
                        .toString()
                        .trim();

        if (!stokText.isEmpty()) {

            try {

                stok =
                        Long.parseLong(
                                stokText
                        );

            } catch (Exception e) {

                stokInput.setError(
                        "Stok tidak valid"
                );

                return;
            }
        }

        double[] margin =
                hitungMargin(
                        nama,
                        harga
                );

        long jualMin =
                Math.round(
                        harga
                                * (1 + margin[0])
                );

        long jualMax =
                Math.round(
                        harga
                                * (1 + margin[1])
                );

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "namaPart",
                nama
        );

        data.put(
                "hargaPokok",
                harga
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
                jualMin
        );

        data.put(
                "hargaJualMax",
                jualMax
        );

        data.put(
                "kodePart",
                kodePartInput.getText()
                        .toString()
                        .trim()
        );

        data.put(
                "stok",
                stok
        );

        data.put(
                "supplier",
                supplierInput.getText()
                        .toString()
                        .trim()
        );

        data.put(
                "catatan",
                catatanInput.getText()
                        .toString()
                        .trim()
        );

        data.put(
                "updatedAt",
                System.currentTimeMillis()
        );

        db.collection("parts")
                .add(data)
                .addOnSuccessListener(
                        result -> {

                            Toast.makeText(
                                    this,
                                    "✅ Part berhasil disimpan",
                                    Toast.LENGTH_LONG
                            ).show();

                            bersihkanForm();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "❌ Gagal menyimpan: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        )
                );
    }

    // =====================================================
    // CARI
    // =====================================================

    private void cariPart() {

        String kata =
                cariInput.getText()
                        .toString()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (kata.isEmpty()) {

            cariInput.setError(
                    "Masukkan nama atau kode"
            );

            return;
        }

        hasilCari.setText(
                "⏳ Mencari..."
        );

        db.collection("parts")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            StringBuilder hasil =
                                    new StringBuilder();

                            int jumlah = 0;

                            for (
                                    QueryDocumentSnapshot doc
                                    : snapshot
                            ) {

                                String nama =
                                        doc.getString(
                                                "namaPart"
                                        );

                                String kode =
                                        doc.getString(
                                                "kodePart"
                                        );

                                if (nama == null) {
                                    nama = "";
                                }

                                if (kode == null) {
                                    kode = "";
                                }

                                if (
                                        nama.toLowerCase(
                                                Locale.ROOT
                                        ).contains(kata)
                                                ||
                                        kode.toLowerCase(
                                                Locale.ROOT
                                        ).contains(kata)
                                ) {

                                    jumlah++;

                                    Long harga =
                                            doc.getLong(
                                                    "hargaPokok"
                                            );

                                    Long stok =
                                            doc.getLong(
                                                    "stok"
                                            );

                                    if (harga == null) {
                                        harga = 0L;
                                    }

                                    if (stok == null) {
                                        stok = 0L;
                                    }

                                    double[] m =
                                            hitungMargin(
                                                    nama,
                                                    harga
                                            );

                                    long jualMin =
                                            Math.round(
                                                    harga
                                                            * (1 + m[0])
                                            );

                                    long jualMax =
                                            Math.round(
                                                    harga
                                                            * (1 + m[1])
                                            );

                                    String supplier =
                                            doc.getString(
                                                    "supplier"
                                            );

                                    if (
                                            supplier == null
                                                    ||
                                            supplier.isEmpty()
                                    ) {
                                        supplier = "-";
                                    }

                                    String catatan =
                                            doc.getString(
                                                    "catatan"
                                            );

                                    if (
                                            catatan == null
                                                    ||
                                            catatan.isEmpty()
                                    ) {
                                        catatan = "-";
                                    }

                                    hasil.append(
                                            "━━━━━━━━━━━━━━━━━━\n"
                                    );

                                    hasil.append(
                                            "🏍️ "
                                                    + nama
                                                    + "\n"
                                    );

                                    if (!kode.isEmpty()) {

                                        hasil.append(
                                                "Kode: "
                                                        + kode
                                                        + "\n"
                                        );
                                    }

                                    hasil.append(
                                            "Modal: "
                                                    + formatRupiah(
                                                    harga
                                            )
                                                    + "\n"
                                    );

                                    hasil.append(
                                            "Harga Jual: "
                                                    + formatRupiah(
                                                    jualMin
                                            )
                                                    + " - "
                                                    + formatRupiah(
                                                    jualMax
                                            )
                                                    + "\n"
                                    );

                                    hasil.append(
                                            "Stok: "
                                                    + stok
                                                    + "\n"
                                    );

                                    hasil.append(
                                            "Supplier: "
                                                    + supplier
                                                    + "\n"
                                    );

                                    hasil.append(
                                            "Catatan: "
                                                    + catatan
                                                    + "\n"
                                    );
                                }
                            }

                            if (jumlah == 0) {

                                hasilCari.setText(
                                        "❌ Part tidak ditemukan."
                                );

                            } else {

                                hasil.insert(
                                        0,
                                        "Ditemukan "
                                                + jumlah
                                                + " part\n\n"
                                );

                                hasilCari.setText(
                                        hasil.toString()
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> hasilCari.setText(
                                "❌ Gagal mengambil data: "
                                        + e.getMessage()
                        )
                );
    }

    // =====================================================
    // KAMERA
    // =====================================================

    private void mulaiScanNota() {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                )
                        != PackageManager.PERMISSION_GRANTED
        ) {

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

        Intent intent =
                new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE
                );

        if (
                intent.resolveActivity(
                        getPackageManager()
                ) != null
        ) {

            startActivityForResult(
                    intent,
                    REQUEST_IMAGE_CAPTURE
            );

        } else {

            Toast.makeText(
                    this,
                    "❌ Kamera tidak tersedia",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (
                requestCode == REQUEST_CAMERA
                        &&
                grantResults.length > 0
                        &&
                grantResults[0]
                                == PackageManager.PERMISSION_GRANTED
        ) {

            bukaKamera();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == REQUEST_IMAGE_CAPTURE
                        &&
                resultCode == RESULT_OK
                        &&
                data != null
        ) {

            Bundle extras =
                    data.getExtras();

            if (extras != null) {

                Bitmap bitmap =
                        (Bitmap) extras.get(
                                "data"
                        );

                if (bitmap != null) {

                    jalankanOCR(bitmap);
                }
            }
        }
    }

    // =====================================================
    // OCR
    // =====================================================

    private void jalankanOCR(
            Bitmap bitmap) {

        Toast.makeText(
                this,
                "⏳ Membaca nota...",
                Toast.LENGTH_SHORT
        ).show();

        InputImage image =
                InputImage.fromBitmap(
                        bitmap,
                        0
                );

        TextRecognition
                .getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                )
                .process(image)
                .addOnSuccessListener(
                        text -> {

                            String hasil =
                                    text.getText();

                            if (
                                    hasil == null
                                            ||
                                    hasil.trim().isEmpty()
                            ) {

                                Toast.makeText(
                                        this,
                                        "❌ Tulisan tidak terbaca",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            tampilkanHasilOCR(
                                    hasil
                            );
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "❌ OCR gagal: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =====================================================
    // HASIL OCR
    // =====================================================

    private void tampilkanHasilOCR(
            String teksOCR) {

        EditText nama =
                buatInput(
                        "Nama Part *",
                        "Hasil OCR"
                );

        nama.setText(
                deteksiNamaPart(
                        teksOCR
                )
        );

        EditText harga =
                buatInput(
                        "Harga Pokok / Modal *",
                        "Hasil OCR"
                );

        harga.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        harga.setText(
                deteksiHarga(
                        teksOCR
                )
        );

        EditText qty =
                buatInput(
                        "Jumlah / Qty",
                        "Hasil OCR"
                );

        qty.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        qty.setText(
                deteksiQty(
                        teksOCR
                )
        );

        EditText mentah =
                buatInput(
                        "Hasil OCR",
                        "Bisa diedit"
                );

        mentah.setText(
                teksOCR
        );

        mentah.setMinLines(5);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                20,
                10,
                20,
                10
        );

        TextView info =
                new TextView(this);

        info.setText(
                "Periksa dan edit hasil scan sebelum dilanjutkan."
        );

        info.setTextSize(16);

        layout.addView(info);
        layout.addView(nama);
        layout.addView(harga);
        layout.addView(qty);

        TextView label =
                new TextView(this);

        label.setText(
                "📝 Teks OCR:"
        );

        label.setTextSize(16);

        layout.addView(label);
        layout.addView(mentah);

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(layout);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "📷 HASIL SCAN NOTA"
                        )
                        .setView(scroll)
                        .setNegativeButton(
                                "BATAL",
                                null
                        )
                        .setPositiveButton(
                                "LANJUT",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button lanjut =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    lanjut.setOnClickListener(
                            v -> {

                                String n =
                                        nama.getText()
                                                .toString()
                                                .trim();

                                String h =
                                        harga.getText()
                                                .toString()
                                                .trim();

                                String q =
                                        qty.getText()
                                                .toString()
                                                .trim();

                                if (n.isEmpty()) {

                                    nama.setError(
                                            "Nama part wajib"
                                    );

                                    return;
                                }

                                if (h.isEmpty()) {

                                    harga.setError(
                                            "Harga wajib"
                                    );

                                    return;
                                }

                                isiFormDariOCR(
                                        n,
                                        h,
                                        q
                                );

                                dialog.dismiss();
                            }
                    );
                }
        );

        dialog.show();
    }

    // =====================================================
    // DETEKSI OCR
    // =====================================================

    private String deteksiNamaPart(
            String teks) {

        String[] baris =
                teks.split(
                        "\\r?\\n"
                );

        for (String b : baris) {

            String bersih =
                    b.trim();

            if (bersih.isEmpty()) {
                continue;
            }

            if (
                    bersih.toLowerCase(
                            Locale.ROOT
                    ).contains("total")
            ) {
                continue;
            }

            Matcher matcher =
                    Pattern.compile(
                            "(?i)(rp\\.?\\s*)?"
                                    +
                                    "[0-9]{1,3}"
                                    +
                                    "(?:[.,][0-9]{3})+"
                    ).matcher(bersih);

            if (matcher.find()) {

                String nama =
                        matcher.replaceAll("")
                                .trim();

                if (!nama.isEmpty()) {
                    return nama;
                }
            }
        }

        return baris.length > 0
                ? baris[0].trim()
                : "";
    }

    private String deteksiHarga(
            String teks) {

        Pattern pattern =
                Pattern.compile(
                        "(?i)(?:rp\\.?\\s*)?"
                                +
                                "([0-9]{1,3}"
                                +
                                "(?:[.,][0-9]{3})+)"
                );

        Matcher matcher =
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
                    ).replace(
                            ",",
                            ""
                    );

            try {

                long nilai =
                        Long.parseLong(
                                angka
                        );

                if (nilai >= 1000) {

                    return String.valueOf(
                            nilai
                    );
                }

            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private String deteksiQty(
            String teks) {

        Pattern pattern =
                Pattern.compile(
                        "(?i)"
                                +
                                "(?:qty|jumlah|pcs|pc|x)"
                                +
                                "\\s*[:=]?\\s*"
                                +
                                "([0-9]+)"
                );

        Matcher matcher =
                pattern.matcher(teks);

        if (matcher.find()) {

            return matcher.group(1);
        }

        return "1";
    }

    private void isiFormDariOCR(
            String nama,
            String harga,
            String qty) {

        namaPartInput.setText(
                nama
        );

        hargaPokokInput.setText(
                harga
        );

        stokInput.setText(
                qty.isEmpty()
                        ? "1"
                        : qty
        );

        hasilCari.setText(
                "✅ Hasil OCR sudah masuk ke form.\n\n"
                        +
                "Periksa kembali data.\n"
                        +
                "Jika benar, tekan SIMPAN PART."
        );

        namaPartInput.requestFocus();
    }

    // =====================================================
    // FORMAT
    // =====================================================

    private String formatRupiah(
            long angka) {

        NumberFormat format =
                NumberFormat.getCurrencyInstance(
                        new Locale(
                                "id",
                                "ID"
                        )
                );

        return format.format(
                angka
        ).replace(
                ",00",
                ""
        );
    }

    private String formatPersen(
            double angka) {

        return String.format(
                Locale.US,
                "%.1f%%",
                angka * 100
        );
    }

    // =====================================================
    // BERSIHKAN
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
