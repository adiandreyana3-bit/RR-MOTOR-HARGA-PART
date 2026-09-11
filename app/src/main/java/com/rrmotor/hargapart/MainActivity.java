package com.rrmotor.hargapart;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1001;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout rootLayout;
    private ScrollView scrollView;

    private EditText namaPartInput;
    private EditText hargaPokokInput;
    private EditText kodePartInput;
    private EditText stokInput;
    private EditText supplierInput;
    private EditText catatanInput;

    private TextView hasilHargaText;
    private TextView hasilPencarianText;

    private final Locale localeIndonesia = new Locale("id", "ID");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cekLoginFirebase();
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void cekLoginFirebase() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            tampilkanLogin();
        } else {
            tampilkanMenuUtama();
        }
    }

    private void tampilkanLogin() {
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(35, 35, 35, 35);
        rootLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = buatJudul("🏍️ RR MOTOR\nCEK HARGA PART");
        rootLayout.addView(title);

        TextView info = new TextView(this);
        info.setText(
                "Login untuk menyimpan data part,\n" +
                "katalog motor dan data substitusi di Firebase."
        );
        info.setGravity(Gravity.CENTER);
        info.setTextSize(16);
        info.setPadding(0, 20, 0, 30);
        rootLayout.addView(info);

        EditText email = new EditText(this);
        email.setHint("Email");
        email.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        rootLayout.addView(email, params());

        EditText password = new EditText(this);
        password.setHint("Password");
        password.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        rootLayout.addView(password, params());

        Button login = tombol("🔐 LOGIN");
        rootLayout.addView(login, params());

        Button daftar = tombol("➕ DAFTAR AKUN");
        rootLayout.addView(daftar, params());

        setContentView(rootLayout);

        login.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();

            if (e.isEmpty() || p.isEmpty()) {
                toast("Email dan password wajib diisi.");
                return;
            }

            login.setEnabled(false);

            auth.signInWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {
                        login.setEnabled(true);

                        if (task.isSuccessful()) {
                            tampilkanMenuUtama();
                        } else {
                            toast("Login gagal: " +
                                    (task.getException() != null
                                            ? task.getException().getMessage()
                                            : "periksa email/password"));
                        }
                    });
        });

        daftar.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();

            if (e.isEmpty() || p.isEmpty()) {
                toast("Isi email dan password terlebih dahulu.");
                return;
            }

            if (p.length() < 6) {
                toast("Password minimal 6 karakter.");
                return;
            }

            daftar.setEnabled(false);

            auth.createUserWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {
                        daftar.setEnabled(true);

                        if (task.isSuccessful()) {
                            toast("Akun berhasil dibuat.");
                            tampilkanMenuUtama();
                        } else {
                            toast("Pendaftaran gagal: " +
                                    (task.getException() != null
                                            ? task.getException().getMessage()
                                            : ""));
                        }
                    });
        });
    }

    // =========================================================
    // MENU UTAMA
    // =========================================================

    private void tampilkanMenuUtama() {

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(25, 25, 25, 25);

        TextView title = buatJudul("🏍️ RR MOTOR\nCEK HARGA PART");
        rootLayout.addView(title);

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            TextView loginInfo = new TextView(this);
            loginInfo.setText("Login: " + user.getEmail());
            loginInfo.setGravity(Gravity.CENTER);
            loginInfo.setPadding(0, 5, 0, 20);
            rootLayout.addView(loginInfo);
        }

        Button cari = tombol("🔎 CARI PART");
        rootLayout.addView(cari, params());

        Button katalog = tombol("📚 KATALOG PART MOTOR");
        rootLayout.addView(katalog, params());

        Button substitusi = tombol("🔄 SUBSTITUSI PART");
        rootLayout.addView(substitusi, params());

        Button tersimpan = tombol("📋 DAFTAR PART TERSIMPAN");
        rootLayout.addView(tersimpan, params());

        Button tambah = tombol("➕ TAMBAH PART");
        rootLayout.addView(tambah, params());

        Button scan = tombol("📷 SCAN NOTA SUPPLIER");
        rootLayout.addView(scan, params());

        Button keluar = tombol("🚪 KELUAR AKUN");
        rootLayout.addView(keluar, params());

        setContentView(rootLayout);

        cari.setOnClickListener(v -> tampilkanCariPart());
        katalog.setOnClickListener(v -> tampilkanKatalogPart());
        substitusi.setOnClickListener(v -> tampilkanSubstitusiPart());
        tersimpan.setOnClickListener(v -> tampilkanDaftarPart());
        tambah.setOnClickListener(v -> tampilkanFormTambahPart());
        scan.setOnClickListener(v -> scanNotaSupplier());

        keluar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Keluar Akun")
                    .setMessage("Yakin ingin keluar dari akun?")
                    .setNegativeButton("BATAL", null)
                    .setPositiveButton("KELUAR", (d, w) -> {
                        auth.signOut();
                        tampilkanLogin();
                    })
                    .show();
        });
    }

    // =========================================================
    // TAMBAH PART
    // =========================================================

    private void tampilkanFormTambahPart() {

        LinearLayout layout = formLayout();

        TextView title = buatJudul("➕ TAMBAH PART");
        layout.addView(title);

        namaPartInput = input("Nama part *");
        layout.addView(namaPartInput, params());

        hargaPokokInput = input("Harga pokok / modal *");
        hargaPokokInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(hargaPokokInput, params());

        kodePartInput = input("Kode part");
        layout.addView(kodePartInput, params());

        stokInput = input("Stok");
        stokInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(stokInput, params());

        supplierInput = input("Supplier");
        layout.addView(supplierInput, params());

        catatanInput = input("Catatan");
        catatanInput.setMinLines(3);
        catatanInput.setGravity(Gravity.TOP);
        layout.addView(catatanInput, params());

        hasilHargaText = new TextView(this);
        hasilHargaText.setTextSize(16);
        hasilHargaText.setPadding(0, 15, 0, 15);
        layout.addView(hasilHargaText);

        Button hitung = tombol("💰 HITUNG HARGA JUAL");
        layout.addView(hitung, params());

        Button simpan = tombol("💾 SIMPAN PART");
        layout.addView(simpan, params());

        Button scan = tombol("📷 SCAN NOTA SUPPLIER");
        layout.addView(scan, params());

        Button kembali = tombol("⬅️ KEMBALI");
        layout.addView(kembali, params());

        setContentView(layout);

        hitung.setOnClickListener(v -> hitungHarga());

        simpan.setOnClickListener(v -> simpanPart());

        scan.setOnClickListener(v -> scanNotaSupplier());

        kembali.setOnClickListener(v -> tampilkanMenuUtama());
    }

    private void hitungHarga() {

        String nama = namaPartInput.getText().toString().trim();
        double modal = parseDouble(hargaPokokInput.getText().toString());

        if (nama.isEmpty()) {
            toast("Nama part wajib diisi.");
            return;
        }

        if (modal <= 0) {
            toast("Harga pokok wajib diisi.");
            return;
        }

        double[] margin = hitungMargin(nama, modal);

        double hargaMin = modal + (modal * margin[0] / 100.0);
        double hargaMax = modal + (modal * margin[1] / 100.0);

        hasilHargaText.setText(
                "Harga Pokok : " + rupiah(modal) + "\n" +
                "Margin      : " +
                formatAngka(margin[0]) + "% - " +
                formatAngka(margin[1]) + "%\n" +
                "Harga Jual  : " +
                rupiah(hargaMin) + " - " +
                rupiah(hargaMax)
        );
    }

    private double[] hitungMargin(String nama, double modal) {

        String n = nama.toLowerCase(Locale.ROOT);

        if (n.contains("oli")) {
            return new double[]{10, 10};
        }

        if (modal < 10000) {
            return new double[]{100, 120};
        }

        if (modal < 15000) {
            return interpolasi(
                    modal,
                    10000, 15000,
                    100, 120,
                    60, 80
            );
        }

        if (modal < 25000) {
            return new double[]{60, 80};
        }

        if (modal < 30000) {
            return interpolasi(
                    modal,
                    25000, 30000,
                    60, 80,
                    35, 50
            );
        }

        if (modal < 50000) {
            return new double[]{35, 50};
        }

        if (modal < 60000) {
            return interpolasi(
                    modal,
                    50000, 60000,
                    35, 50,
                    20, 30
            );
        }

        if (modal < 90000) {
            return new double[]{20, 30};
        }

        if (modal < 100000) {
            return interpolasi(
                    modal,
                    90000, 100000,
                    20, 30,
                    10, 18
            );
        }

        if (modal < 150000) {
            return new double[]{10, 18};
        }

        if (modal < 160000) {
            return interpolasi(
                    modal,
                    150000, 160000,
                    10, 18,
                    10, 15
            );
        }

        return new double[]{10, 15};
    }

    private double[] interpolasi(
            double nilai,
            double awal,
            double akhir,
            double minAwal,
            double maxAwal,
            double minAkhir,
            double maxAkhir) {

        double rasio = (nilai - awal) / (akhir - awal);

        double min = minAwal +
                (minAkhir - minAwal) * rasio;

        double max = maxAwal +
                (maxAkhir - maxAwal) * rasio;

        return new double[]{min, max};
    }

    private void simpanPart() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            toast("Silakan login terlebih dahulu.");
            tampilkanLogin();
            return;
        }

        String nama = namaPartInput.getText().toString().trim();
        double modal = parseDouble(hargaPokokInput.getText().toString());

        if (nama.isEmpty()) {
            toast("Nama part wajib diisi.");
            return;
        }

        if (modal <= 0) {
            toast("Harga pokok wajib diisi.");
            return;
        }

        double[] margin = hitungMargin(nama, modal);

        double hargaMin =
                modal + modal * margin[0] / 100.0;

        double hargaMax =
                modal + modal * margin[1] / 100.0;

        Map<String, Object> data = new HashMap<>();

        data.put("namaPart", nama);
        data.put("hargaPokok", modal);
        data.put("marginMin", margin[0]);
        data.put("marginMax", margin[1]);
        data.put("hargaJualMin", hargaMin);
        data.put("hargaJualMax", hargaMax);
        data.put("kodePart",
                kodePartInput.getText().toString().trim());
        data.put("stok",
                parseDouble(stokInput.getText().toString()));
        data.put("supplier",
                supplierInput.getText().toString().trim());
        data.put("catatan",
                catatanInput.getText().toString().trim());
        data.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("parts")
                .add(data)
                .addOnSuccessListener(documentReference -> {

                    toast("Part berhasil disimpan.");

                    new AlertDialog.Builder(this)
                            .setTitle("PART TERSIMPAN")
                            .setMessage(
                                    "Nama: " + nama + "\n\n" +
                                    "Harga jual:\n" +
                                    rupiah(hargaMin) + " - " +
                                    rupiah(hargaMax)
                            )
                            .setPositiveButton("OK", null)
                            .show();

                    tampilkanMenuUtama();
                })
                .addOnFailureListener(e ->
                        toast("Gagal menyimpan: " + e.getMessage()));
    }

    // =========================================================
    // CARI PART
    // =========================================================

    private void tampilkanCariPart() {

        LinearLayout layout = formLayout();

        TextView title = buatJudul("🔎 CARI PART");
        layout.addView(title);

        EditText pencarian = input(
                "Nama / kode part / supplier"
        );
        layout.addView(pencarian, params());

        Button cari = tombol("🔎 CARI");
        layout.addView(cari, params());

        hasilPencarianText = new TextView(this);
        hasilPencarianText.setTextSize(15);
        hasilPencarianText.setPadding(0, 20, 0, 20);
        layout.addView(hasilPencarianText);

        Button kembali = tombol("⬅️ KEMBALI");
        layout.addView(kembali, params());

        setContentView(layout);

        cari.setOnClickListener(v ->
                cariPartFirestore(
                        pencarian.getText().toString().trim()
                )
        );

        kembali.setOnClickListener(v ->
                tampilkanMenuUtama()
        );

        pencarian.setOnEditorActionListener((v, actionId, event) -> {
            cariPartFirestore(
                    pencarian.getText().toString().trim()
            );
            return true;
        });
    }

    private void cariPartFirestore(String kata) {

        db.collection("parts")
                .get()
                .addOnSuccessListener(snapshot -> {

                    StringBuilder hasil = new StringBuilder();

                    int jumlah = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        String nama = stringValue(
                                doc.get("namaPart")
                        );

                        String kode = stringValue(
                                doc.get("kodePart")
                        );

                        String supplier = stringValue(
                                doc.get("supplier")
                        );

                        String cari = kata.toLowerCase(
                                Locale.ROOT
                        );

                        boolean cocok = kata.isEmpty()
                                || nama.toLowerCase(Locale.ROOT)
                                .contains(cari)
                                || kode.toLowerCase(Locale.ROOT)
                                .contains(cari)
                                || supplier.toLowerCase(Locale.ROOT)
                                .contains(cari);

                        if (!cocok) {
                            continue;
                        }

                        jumlah++;

                        double modal = numberValue(
                                doc.get("hargaPokok")
                        );

                        double hargaMin = numberValue(
                                doc.get("hargaJualMin")
                        );

                        double hargaMax = numberValue(
                                doc.get("hargaJualMax")
                        );

                        double stok = numberValue(
                                doc.get("stok")
                        );

                        hasil.append(
                                "━━━━━━━━━━━━━━━━━━\n"
                        );

                        hasil.append("🔧 ")
                                .append(nama)
                                .append("\n");

                        if (!kode.isEmpty()) {
                            hasil.append("Kode: ")
                                    .append(kode)
                                    .append("\n");
                        }

                        if (!supplier.isEmpty()) {
                            hasil.append("Supplier: ")
                                    .append(supplier)
                                    .append("\n");
                        }

                        hasil.append("Modal: ")
                                .append(rupiah(modal))
                                .append("\n");

                        hasil.append("Jual: ")
                                .append(rupiah(hargaMin))
                                .append(" - ")
                                .append(rupiah(hargaMax))
                                .append("\n");

                        hasil.append("Stok: ")
                                .append(formatAngka(stok))
                                .append("\n");

                        String catatan = stringValue(
                                doc.get("catatan")
                        );

                        if (!catatan.isEmpty()) {
                            hasil.append("Catatan: ")
                                    .append(catatan)
                                    .append("\n");
                        }
                    }

                    if (jumlah == 0) {
                        hasil.append(
                                "Part tidak ditemukan."
                        );
                    } else {
                        hasil.insert(
                                0,
                                "Ditemukan " +
                                        jumlah +
                                        " part.\n"
                        );
                    }

                    hasilPencarianText.setText(
                            hasil.toString()
                    );
                })
                .addOnFailureListener(e ->
                        toast("Gagal mencari: " +
                                e.getMessage()));
    }

    // =========================================================
    // DAFTAR PART
    // =========================================================

    private void tampilkanDaftarPart() {

        db.collection("parts")
                .get()
                .addOnSuccessListener(snapshot -> {

                    LinearLayout layout = formLayout();

                    TextView title =
                            buatJudul("📋 DAFTAR PART TERSIMPAN");

                    layout.addView(title);

                    if (snapshot.isEmpty()) {

                        TextView kosong =
                                new TextView(this);

                        kosong.setText(
                                "Belum ada part tersimpan."
                        );

                        kosong.setTextSize(16);
                        kosong.setPadding(0, 20, 0, 20);

                        layout.addView(kosong);

                    } else {

                        for (DocumentSnapshot doc :
                                snapshot.getDocuments()) {

                            layout.addView(
                                    buatCardPart(doc)
                            );
                        }
                    }

                    Button kembali =
                            tombol("⬅️ KEMBALI");

                    layout.addView(
                            kembali,
                            params()
                    );

                    setContentView(layout);

                    kembali.setOnClickListener(
                            v -> tampilkanMenuUtama()
                    );
                })
                .addOnFailureListener(e ->
                        toast("Gagal mengambil data: " +
                                e.getMessage()));
    }

    private View buatCardPart(DocumentSnapshot doc) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                20, 20, 20, 20
        );

        String nama =
                stringValue(doc.get("namaPart"));

        String kode =
                stringValue(doc.get("kodePart"));

        double modal =
                numberValue(doc.get("hargaPokok"));

        double min =
                numberValue(doc.get("hargaJualMin"));

        double max =
                numberValue(doc.get("hargaJualMax"));

        double stok =
                numberValue(doc.get("stok"));

        String supplier =
                stringValue(doc.get("supplier"));

        String catatan =
                stringValue(doc.get("catatan"));

        TextView text =
                new TextView(this);

        StringBuilder s =
                new StringBuilder();

        s.append("🔧 ")
                .append(nama)
                .append("\n");

        if (!kode.isEmpty()) {
            s.append("Kode: ")
                    .append(kode)
                    .append("\n");
        }

        s.append("Modal: ")
                .append(rupiah(modal))
                .append("\n");

        s.append("Harga jual: ")
                .append(rupiah(min))
                .append(" - ")
                .append(rupiah(max))
                .append("\n");

        s.append("Stok: ")
                .append(formatAngka(stok))
                .append("\n");

        if (!supplier.isEmpty()) {
            s.append("Supplier: ")
                    .append(supplier)
                    .append("\n");
        }

        if (!catatan.isEmpty()) {
            s.append("Catatan: ")
                    .append(catatan);
        }

        text.setText(s.toString());
        text.setTextSize(15);

        card.addView(text);

        Button hapus =
                tombol("🗑️ HAPUS");

        card.addView(
                hapus,
                params()
        );

        hapus.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Hapus Part")
                    .setMessage(
                            "Hapus " + nama + "?"
                    )
                    .setNegativeButton(
                            "BATAL",
                            null
                    )
                    .setPositiveButton(
                            "HAPUS",
                            (dialog, which) ->
                                    doc.getReference()
                                            .delete()
                                            .addOnSuccessListener(
                                                    x -> {
                                                        toast(
                                                                "Part dihapus."
                                                        );
                                                        tampilkanDaftarPart();
                                                    }
                                            )
                    )
                    .show();
        });

        return card;
    }

    // =========================================================
    // KATALOG PART MOTOR
    // =========================================================

    private void tampilkanKatalogPart() {

        LinearLayout layout = formLayout();

        TextView title =
                buatJudul("📚 KATALOG PART MOTOR");

        layout.addView(title);

        EditText cari =
                input(
                        "Cari merk / model / kategori / part / kode"
                );

        layout.addView(
                cari,
                params()
        );

        Button tombolCari =
                tombol("🔎 CARI KATALOG");

        layout.addView(
                tombolCari,
                params()
        );

        Button tambah =
                tombol("➕ TAMBAH KATALOG");

        layout.addView(
                tambah,
                params()
        );

        TextView hasil =
                new TextView(this);

        hasil.setTextSize(14);
        hasil.setPadding(
                0, 20, 0, 20
        );

        layout.addView(hasil);

        Button kembali =
                tombol("⬅️ KEMBALI");

        layout.addView(
                kembali,
                params()
        );

        setContentView(layout);

        Runnable cariData = () ->
                cariKatalog(
                        cari.getText().toString().trim(),
                        hasil
                );

        tombolCari.setOnClickListener(
                v -> cariData.run()
        );

        cariData.run();

        tambah.setOnClickListener(
                v -> tampilkanFormKatalogPart()
        );

        kembali.setOnClickListener(
                v -> tampilkanMenuUtama()
        );
    }

    private void cariKatalog(
            String kata,
            TextView hasil) {

        db.collection("partCatalog")
                .whereEqualTo("aktif", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    StringBuilder s =
                            new StringBuilder();

                    int jumlah = 0;

                    String cari =
                            kata.toLowerCase(
                                    Locale.ROOT
                            );

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        String merk =
                                stringValue(
                                        doc.get("merk")
                                );

                        String model =
                                stringValue(
                                        doc.get("model")
                                );

                        String kategori =
                                stringValue(
                                        doc.get("kategoriPart")
                                );

                        String nama =
                                stringValue(
                                        doc.get("namaPart")
                                );

                        String oem =
                                stringValue(
                                        doc.get("kodeOEM")
                                );

                        String aftermarket =
                                stringValue(
                                        doc.get("kodeAftermarket")
                                );

                        boolean cocok =
                                kata.isEmpty()
                                        || mengandung(
                                        merk, cari)
                                        || mengandung(
                                        model, cari)
                                        || mengandung(
                                        kategori, cari)
                                        || mengandung(
                                        nama, cari)
                                        || mengandung(
                                        oem, cari)
                                        || mengandung(
                                        aftermarket, cari);

                        if (!cocok) {
                            continue;
                        }

                        jumlah++;

                        s.append(
                                "━━━━━━━━━━━━━━━━━━\n"
                        );

                        s.append("🏍️ ")
                                .append(merk)
                                .append(" ")
                                .append(model)
                                .append("\n");

                        s.append("🔧 ")
                                .append(nama)
                                .append("\n");

                        if (!kategori.isEmpty()) {
                            s.append("Kategori: ")
                                    .append(kategori)
                                    .append("\n");
                        }

                        int tahunMulai =
                                intValue(
                                        doc.get("tahunMulai")
                                );

                        int tahunSampai =
                                intValue(
                                        doc.get("tahunSampai")
                                );

                        if (tahunMulai > 0 ||
                                tahunSampai > 0) {

                            s.append("Tahun: ")
                                    .append(tahunMulai)
                                    .append(" - ")
                                    .append(tahunSampai)
                                    .append("\n");
                        }

                        if (!oem.isEmpty()) {
                            s.append("OEM: ")
                                    .append(oem)
                                    .append("\n");
                        }

                        if (!aftermarket.isEmpty()) {
                            s.append("Aftermarket: ")
                                    .append(aftermarket)
                                    .append("\n");
                        }

                        String ukuran =
                                stringValue(
                                        doc.get("ukuran")
                                );

                        if (!ukuran.isEmpty()) {
                            s.append("Ukuran: ")
                                    .append(ukuran)
                                    .append("\n");
                        } else {

                            s.append(
                                    "Ukuran: "
                            );

                            s.append(
                                    ukuranText(doc)
                            );

                            s.append("\n");
                        }

                        String satuan =
                                stringValue(
                                        doc.get("satuan")
                                );

                        if (!satuan.isEmpty()) {
                            s.append("Satuan: ")
                                    .append(satuan)
                                    .append("\n");
                        }

                        String catatan =
                                stringValue(
                                        doc.get("catatan")
                                );

                        if (!catatan.isEmpty()) {
                            s.append("Catatan: ")
                                    .append(catatan)
                                    .append("\n");
                        }
                    }

                    if (jumlah == 0) {
                        s.append(
                                "Katalog tidak ditemukan."
                        );
                    } else {
                        s.insert(
                                0,
                                "Ditemukan " +
                                        jumlah +
                                        " katalog.\n"
                        );
                    }

                    hasil.setText(
                            s.toString()
                    );
                })
                .addOnFailureListener(e ->
                        hasil.setText(
                                "Gagal mengambil katalog:\n" +
                                        e.getMessage()
                        ));
    }

    // =========================================================
    // FORM TAMBAH KATALOG
    // =========================================================

    private void tampilkanFormKatalogPart() {

        LinearLayout layout =
                formLayout();

        TextView title =
                buatJudul(
                        "➕ TAMBAH KATALOG PART"
                );

        layout.addView(title);

        EditText merk =
                input("Merk motor");

        EditText model =
                input("Model / tipe motor");

        EditText tahunMulai =
                input("Tahun mulai");

        EditText tahunSampai =
                input("Tahun sampai");

        EditText kategori =
                input("Kategori part");

        EditText nama =
                input("Nama part");

        EditText kodeOEM =
                input("Kode OEM");

        EditText kodeAftermarket =
                input("Kode aftermarket");

        EditText satuan =
                input("Satuan");

        EditText diameterDalam =
                input("Diameter dalam (mm)");

        EditText diameterLuar =
                input("Diameter luar (mm)");

        EditText panjang =
                input("Panjang (mm)");

        EditText lebar =
                input("Lebar (mm)");

        EditText tebal =
                input("Tebal (mm)");

        EditText ukuran =
                input("Ukuran lengkap, contoh 12 x 32 x 10");

        EditText catatan =
                input("Catatan");

        catatan.setMinLines(3);
        catatan.setGravity(Gravity.TOP);

        layout.addView(
                merk, params()
        );

        layout.addView(
                model, params()
        );

        tahunMulai.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        tahunSampai.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        layout.addView(
                tahunMulai, params()
        );

        layout.addView(
                tahunSampai, params()
        );

        layout.addView(
                kategori, params()
        );

        layout.addView(
                nama, params()
        );

        layout.addView(
                kodeOEM, params()
        );

        layout.addView(
                kodeAftermarket, params()
        );

        layout.addView(
                satuan, params()
        );

        setNumeric(diameterDalam);
        setNumeric(diameterLuar);
        setNumeric(panjang);
        setNumeric(lebar);
        setNumeric(tebal);

        layout.addView(
                diameterDalam, params()
        );

        layout.addView(
                diameterLuar, params()
        );

        layout.addView(
                panjang, params()
        );

        layout.addView(
                lebar, params()
        );

        layout.addView(
                tebal, params()
        );

        layout.addView(
                ukuran, params()
        );

        layout.addView(
                catatan, params()
        );

        Button simpan =
                tombol("💾 SIMPAN KATALOG");

        layout.addView(
                simpan,
                params()
        );

        Button kembali =
                tombol("⬅️ KEMBALI");

        layout.addView(
                kembali,
                params()
        );

        setContentView(layout);

        simpan.setOnClickListener(v -> {

            if (nama.getText().toString()
                    .trim().isEmpty()) {

                toast("Nama part wajib diisi.");
                return;
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "merk",
                    merk.getText().toString().trim()
            );

            data.put(
                    "model",
                    model.getText().toString().trim()
            );

            data.put(
                    "tahunMulai",
                    intValue(tahunMulai.getText().toString())
            );

            data.put(
                    "tahunSampai",
                    intValue(tahunSampai.getText().toString())
            );

            data.put(
                    "kategoriPart",
                    kategori.getText().toString().trim()
            );

            data.put(
                    "namaPart",
                    nama.getText().toString().trim()
            );

            data.put(
                    "kodeOEM",
                    kodeOEM.getText().toString().trim()
            );

            data.put(
                    "kodeAftermarket",
                    kodeAftermarket.getText().toString().trim()
            );

            data.put(
                    "satuan",
                    satuan.getText().toString().trim()
            );

            data.put(
                    "diameterDalam",
                    getDoubleField(diameterDalam)
            );

            data.put(
                    "diameterLuar",
                    getDoubleField(diameterLuar)
            );

            data.put(
                    "panjang",
                    getDoubleField(panjang)
            );

            data.put(
                    "lebar",
                    getDoubleField(lebar)
            );

            data.put(
                    "tebal",
                    getDoubleField(tebal)
            );

            data.put(
                    "ukuran",
                    ukuran.getText().toString().trim()
            );

            data.put(
                    "catatan",
                    catatan.getText().toString().trim()
            );

            data.put(
                    "aktif",
                    true
            );

            data.put(
                    "updatedAt",
                    FieldValue.serverTimestamp()
            );

            simpan.setEnabled(false);

            db.collection("partCatalog")
                    .add(data)
                    .addOnSuccessListener(x -> {
                        toast(
                                "Katalog berhasil disimpan."
                        );
                        tampilkanKatalogPart();
                    })
                    .addOnFailureListener(e -> {
                        simpan.setEnabled(true);
                        toast(
                                "Gagal menyimpan katalog: " +
                                        e.getMessage()
                        );
                    });
        });

        kembali.setOnClickListener(
                v -> tampilkanKatalogPart()
        );
    }

    // =========================================================
    // SUBSTITUSI PART
    // =========================================================

    private void tampilkanSubstitusiPart() {

        LinearLayout layout =
                formLayout();

        TextView title =
                buatJudul(
                        "🔄 SUBSTITUSI PART"
                );

        layout.addView(title);

        EditText cari =
                input(
                        "Cari part / kode / merk"
                );

        layout.addView(
                cari,
                params()
        );

        Button tombolCari =
                tombol("🔎 CARI SUBSTITUSI");

        layout.addView(
                tombolCari,
                params()
        );

        Button tambah =
                tombol("➕ TAMBAH SUBSTITUSI");

        layout.addView(
                tambah,
                params()
        );

        TextView hasil =
                new TextView(this);

        hasil.setTextSize(14);
        hasil.setPadding(
                0, 20, 0, 20
        );

        layout.addView(hasil);

        Button kembali =
                tombol("⬅️ KEMBALI");

        layout.addView(
                kembali,
                params()
        );

        setContentView(layout);

        tombolCari.setOnClickListener(
                v -> cariSubstitusi(
                        cari.getText().toString().trim(),
                        hasil
                )
        );

        cariSubstitusi("", hasil);

        tambah.setOnClickListener(
                v -> tampilkanFormSubstitusi()
        );

        kembali.setOnClickListener(
                v -> tampilkanMenuUtama()
        );
    }

    private void cariSubstitusi(
            String kata,
            TextView hasil) {

        db.collection("substitutions")
                .whereEqualTo("aktif", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    StringBuilder s =
                            new StringBuilder();

                    int jumlah = 0;

                    String cari =
                            kata.toLowerCase(
                                    Locale.ROOT
                            );

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        String nama =
                                stringValue(
                                        doc.get("namaPart")
                                );

                        String kodeAsal =
                                stringValue(
                                        doc.get("kodeAsal")
                                );

                        String kodePengganti =
                                stringValue(
                                        doc.get("kodePengganti")
                                );

                        String merk =
                                stringValue(
                                        doc.get("merkPengganti")
                                );

                        boolean cocok =
                                kata.isEmpty()
                                        || mengandung(
                                        nama, cari)
                                        || mengandung(
                                        kodeAsal, cari)
                                        || mengandung(
                                        kodePengganti, cari)
                                        || mengandung(
                                        merk, cari);

                        if (!cocok) {
                            continue;
                        }

                        jumlah++;

                        String tingkat =
                                stringValue(
                                        doc.get(
                                                "tingkatKecocokan"
                                        )
                                );

                        s.append(
                                "━━━━━━━━━━━━━━━━━━\n"
                        );

                        s.append("🔧 ")
                                .append(nama)
                                .append("\n");

                        s.append("Kode asal: ")
                                .append(kodeAsal)
                                .append("\n");

                        s.append("Kode pengganti: ")
                                .append(kodePengganti)
                                .append("\n");

                        if (!merk.isEmpty()) {
                            s.append("Merk pengganti: ")
                                    .append(merk)
                                    .append("\n");
                        }

                        s.append("Kecocokan: ")
                                .append(
                                        iconKecocokan(
                                                tingkat
                                        )
                                )
                                .append(" ")
                                .append(tingkat)
                                .append("\n");

                        s.append(
                                "Ukuran: "
                        );

                        s.append(
                                ukuranText(doc)
                        );

                        s.append("\n");

                        String catatan =
                                stringValue(
                                        doc.get("catatan")
                                );

                        if (!catatan.isEmpty()) {
                            s.append("Catatan: ")
                                    .append(catatan)
                                    .append("\n");
                        }
                    }

                    if (jumlah == 0) {
                        s.append(
                                "Substitusi belum ditemukan."
                        );
                    } else {
                        s.insert(
                                0,
                                "Ditemukan " +
                                        jumlah +
                                        " substitusi.\n"
                        );
                    }

                    hasil.setText(
                            s.toString()
                    );
                })
                .addOnFailureListener(e ->
                        hasil.setText(
                                "Gagal mengambil substitusi:\n" +
                                        e.getMessage()
                        ));
    }

    // =========================================================
    // FORM SUBSTITUSI
    // =========================================================

    private void tampilkanFormSubstitusi() {

        db.collection("partCatalog")
                .whereEqualTo("aktif", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        toast(
                                "Isi Katalog Part Motor terlebih dahulu."
                        );
                        return;
                    }

                    List<DocumentSnapshot> katalog =
                            new ArrayList<>(
                                    snapshot.getDocuments()
                            );

                    pilihPartAsal(
                            katalog
                    );
                })
                .addOnFailureListener(e ->
                        toast(
                                "Gagal mengambil katalog: " +
                                        e.getMessage()
                        ));
    }

    private void pilihPartAsal(
            List<DocumentSnapshot> katalog) {

        String[] daftar =
                new String[katalog.size()];

        for (int i = 0; i < katalog.size(); i++) {

            DocumentSnapshot d =
                    katalog.get(i);

            daftar[i] =
                    katalogLabel(d);
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Pilih PART ASAL"
                )
                .setItems(
                        daftar,
                        (dialog, which) -> {

                            DocumentSnapshot asal =
                                    katalog.get(which);

                            pilihPartPengganti(
                                    katalog,
                                    asal
                            );
                        }
                )
                .setNegativeButton(
                        "BATAL",
                        null
                )
                .show();
    }

    private void pilihPartPengganti(
            List<DocumentSnapshot> katalog,
            DocumentSnapshot asal) {

        String[] daftar =
                new String[katalog.size()];

        for (int i = 0; i < katalog.size(); i++) {

            DocumentSnapshot d =
                    katalog.get(i);

            daftar[i] =
                    katalogLabel(d);
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Pilih PART PENGGANTI"
                )
                .setItems(
                        daftar,
                        (dialog, which) -> {

                            DocumentSnapshot pengganti =
                                    katalog.get(which);

                            if (asal.getId().equals(
                                    pengganti.getId())) {

                                toast(
                                        "Part asal dan pengganti tidak boleh sama."
                                );

                                return;
                            }

                            tampilkanKonfirmasiSubstitusi(
                                    asal,
                                    pengganti
                            );
                        }
                )
                .setNegativeButton(
                        "BATAL",
                        null
                )
                .show();
    }

    private void tampilkanKonfirmasiSubstitusi(
            DocumentSnapshot asal,
            DocumentSnapshot pengganti) {

        String kecocokan =
                hitungKecocokanUkuran(
                        asal,
                        pengganti
                );

        StringBuilder pesan =
                new StringBuilder();

        pesan.append(
                "PART ASAL\n"
        );

        pesan.append(
                katalogLabel(asal)
        );

        pesan.append(
                "\n\nUKURAN ASAL:\n"
        );

        pesan.append(
                ukuranText(asal)
        );

        pesan.append(
                "\n\nPART PENGGANTI\n"
        );

        pesan.append(
                katalogLabel(pengganti)
        );

        pesan.append(
                "\n\nUKURAN PENGGANTI:\n"
        );

        pesan.append(
                ukuranText(pengganti)
        );

        pesan.append(
                "\n\nHASIL:\n"
        );

        pesan.append(
                iconKecocokan(kecocokan)
        );

        pesan.append(" ")
                .append(kecocokan);

        new AlertDialog.Builder(this)
                .setTitle(
                        "Cek Substitusi"
                )
                .setMessage(
                        pesan.toString()
                )
                .setNegativeButton(
                        "BATAL",
                        null
                )
                .setPositiveButton(
                        "SIMPAN",
                        (dialog, which) ->
                                simpanSubstitusi(
                                        asal,
                                        pengganti,
                                        kecocokan
                                )
                )
                .show();
    }

    private void simpanSubstitusi(
            DocumentSnapshot asal,
            DocumentSnapshot pengganti,
            String kecocokan) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "catalogIdAsal",
                asal.getId()
        );

        data.put(
                "catalogIdPengganti",
                pengganti.getId()
        );

        data.put(
                "namaPart",
                stringValue(
                        asal.get("namaPart")
                )
        );

        data.put(
                "kategoriPart",
                stringValue(
                        asal.get("kategoriPart")
                )
        );

        data.put(
                "kodeAsal",
                kodeKatalog(asal)
        );

        data.put(
                "kodePengganti",
                kodeKatalog(pengganti)
        );

        data.put(
                "merkPengganti",
                stringValue(
                        pengganti.get("merk")
                )
        );

        data.put(
                "diameterDalam",
                numberValue(
                        pengganti.get(
                                "diameterDalam"
                        )
                )
        );

        data.put(
                "diameterLuar",
                numberValue(
                        pengganti.get(
                                "diameterLuar"
                        )
                )
        );

        data.put(
                "panjang",
                numberValue(
                        pengganti.get(
                                "panjang"
                        )
                )
        );

        data.put(
                "lebar",
                numberValue(
                        pengganti.get(
                                "lebar"
                        )
                )
        );

        data.put(
                "tebal",
                numberValue(
                        pengganti.get(
                                "tebal"
                        )
                )
        );

        data.put(
                "tingkatKecocokan",
                kecocokan
        );

        data.put(
                "catatan",
                "Ukuran dibandingkan berdasarkan data katalog."
        );

        data.put(
                "aktif",
                true
        );

        data.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        db.collection("substitutions")
                .add(data)
                .addOnSuccessListener(x ->
                        toast(
                                "Substitusi berhasil disimpan."
                        ))
                .addOnFailureListener(e ->
                        toast(
                                "Gagal menyimpan substitusi: " +
                                        e.getMessage()
                        ));
    }

    // =========================================================
    // PERBANDINGAN UKURAN
    // =========================================================

    private String hitungKecocokanUkuran(
            DocumentSnapshot asal,
            DocumentSnapshot pengganti) {

        String[] fields = {
                "diameterDalam",
                "diameterLuar",
                "panjang",
                "lebar",
                "tebal"
        };

        int tersedia = 0;
        int sama = 0;
        int berbeda = 0;

        for (String field : fields) {

            Double a =
                    getDimension(
                            asal,
                            field
                    );

            Double b =
                    getDimension(
                            pengganti,
                            field
                    );

            // Ukuran yang kosong tidak dihitung
            if (a == null || b == null) {
                continue;
            }

            tersedia++;

            if (Math.abs(a - b) < 0.001) {
                sama++;
            } else {
                berbeda++;
            }
        }

        if (tersedia == 0) {
            return "PERLU CEK UKURAN";
        }

        if (sama == tersedia) {
            return "SAMA UKURAN";
        }

        if (berbeda > 0 && sama > 0) {
            return "PERLU CEK";
        }

        return "UKURAN BERBEDA";
    }

    private Double getDimension(
            DocumentSnapshot doc,
            String field) {

        Object value =
                doc.get(field);

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            double n =
                    ((Number) value).doubleValue();

            if (n <= 0) {
                return null;
            }

            return n;
        }

        try {

            String s =
                    String.valueOf(value)
                            .trim()
                            .replace(",", ".");

            if (s.isEmpty()) {
                return null;
            }

            double n =
                    Double.parseDouble(s);

            if (n <= 0) {
                return null;
            }

            return n;

        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================
    // SCAN NOTA SUPPLIER
    // =========================================================

    private void scanNotaSupplier() {

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

        Intent intent =
                new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE
                );

        if (intent.resolveActivity(
                getPackageManager()
        ) != null) {

            startActivityForResult(
                    intent,
                    REQUEST_CAMERA
            );

        } else {
            toast(
                    "Kamera tidak tersedia."
            );
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

        if (requestCode == REQUEST_CAMERA) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                bukaKamera();

            } else {

                toast(
                        "Izin kamera diperlukan untuk scan nota."
                );
            }
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

        if (requestCode == REQUEST_CAMERA &&
                resultCode == RESULT_OK &&
                data != null) {

            Bundle extras =
                    data.getExtras();

            if (extras == null) {
                return;
            }

            Bitmap bitmap =
                    (Bitmap) extras.get(
                            "data"
                    );

            if (bitmap != null) {
                prosesOCR(bitmap);
            }
        }
    }

    private void prosesOCR(Bitmap bitmap) {

        InputImage image =
                InputImage.fromBitmap(
                        bitmap,
                        0
                );

        TextRecognizer recognizer =
                TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                );

        recognizer.process(image)
                .addOnSuccessListener(
                        text -> tampilkanHasilOCR(
                                text
                        )
                )
                .addOnFailureListener(
                        e -> toast(
                                "OCR gagal: " +
                                        e.getMessage()
                        )
                );
    }

    private void tampilkanHasilOCR(
            Text text) {

        String isi =
                text.getText();

        if (isi == null ||
                isi.trim().isEmpty()) {

            toast(
                    "Tulisan pada nota tidak terbaca."
            );

            return;
        }

        String nama =
                ambilNamaPartOCR(isi);

        double harga =
                ambilHargaOCR(isi);

        int qty =
                ambilQtyOCR(isi);

        StringBuilder hasil =
                new StringBuilder();

        hasil.append(
                "HASIL SCAN NOTA\n\n"
        );

        hasil.append(
                isi
        );

        hasil.append(
                "\n\n--------------------\n"
        );

        hasil.append(
                "Perkiraan nama part: "
        )
                .append(
                        nama.isEmpty()
                                ? "-"
                                : nama
                )
                .append("\n");

        hasil.append(
                "Perkiraan harga: "
        )
                .append(
                        harga > 0
                                ? rupiah(harga)
                                : "-"
                )
                .append("\n");

        hasil.append(
                "Perkiraan jumlah: "
        )
                .append(qty)
                .append("\n\n");

        new AlertDialog.Builder(this)
                .setTitle(
                        "📷 HASIL SCAN"
                )
                .setMessage(
                        hasil.toString()
                )
                .setNegativeButton(
                        "TUTUP",
                        null
                )
                .setPositiveButton(
                        "PAKAI DATA",
                        (dialog, which) -> {

                            tampilkanFormTambahPart();

                            if (!nama.isEmpty()) {
                                namaPartInput.setText(
                                        nama
                                );
                            }

                            if (harga > 0) {
                                hargaPokokInput.setText(
                                        String.valueOf(
                                                (long) harga
                                        )
                                );
                            }

                            if (qty > 0) {
                                stokInput.setText(
                                        String.valueOf(qty)
                                );
                            }
                        })
                .show();
    }

    private String ambilNamaPartOCR(
            String text) {

        String[] baris =
                text.split("\\r?\\n");

        for (String barisSatu :
                baris) {

            String s =
                    barisSatu.trim();

            if (s.length() < 3) {
                continue;
            }

            String lower =
                    s.toLowerCase(
                            Locale.ROOT
                    );

            if (lower.contains("total")
                    || lower.contains("subtotal")
                    || lower.contains("harga")
                    || lower.contains("qty")
                    || lower.contains("jumlah")
                    || lower.contains("rp")
                    || lower.matches(
                    ".*\\d{3,}.*"
            )) {
                continue;
            }

            return s;
        }

        return "";
    }

    private double ambilHargaOCR(
            String text) {

        Pattern p =
                Pattern.compile(
                        "(?:rp\\.?\\s*)?([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,})",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher m =
                p.matcher(text);

        double terbesar = 0;

        while (m.find()) {

            String angka =
                    m.group(1);

            double nilai =
                    parseOCRNumber(angka);

            if (nilai > terbesar) {
                terbesar = nilai;
            }
        }

        return terbesar;
    }

    private int ambilQtyOCR(
            String text) {

        Pattern p =
                Pattern.compile(
                        "(?:qty|jumlah|pcs|pc|x)\\s*[:=]?\\s*(\\d+)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher m =
                p.matcher(text);

        if (m.find()) {

            try {
                return Integer.parseInt(
                        m.group(1)
                );
            } catch (Exception ignored) {
            }
        }

        return 1;
    }

    private double parseOCRNumber(
            String value) {

        try {

            String s =
                    value
                            .replace(".", "")
                            .replace(",", "");

            return Double.parseDouble(s);

        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================
    // HELPER UI
    // =========================================================

    private LinearLayout formLayout() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                25, 25, 25, 25
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(layout);

        setContentView(scroll);

        return layout;
    }

    private TextView buatJudul(
            String text) {

        TextView t =
                new TextView(this);

        t.setText(text);
        t.setTextSize(23);
        t.setGravity(
                Gravity.CENTER
        );

        t.setPadding(
                0, 15, 0, 25
        );

        return t;
    }

    private EditText input(
            String hint) {

        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setTextSize(16);
        e.setPadding(
                15, 10, 15, 10
        );

        return e;
    }

    private Button tombol(
            String text) {

        Button b =
                new Button(this);

        b.setText(text);
        b.setTextSize(15);

        return b;
    }

    private LinearLayout.LayoutParams params() {

        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void setNumeric(
            EditText editText) {

        editText.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
    }

    // =========================================================
    // HELPER FIRESTORE / DATA
    // =========================================================

    private String stringValue(
            Object value) {

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }

    private double numberValue(
            Object value) {

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value)
                    .doubleValue();
        }

        try {

            return Double.parseDouble(
                    String.valueOf(value)
                            .replace(",", ".")
            );

        } catch (Exception e) {
            return 0;
        }
    }

    private int intValue(
            Object value) {

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value)
                    .intValue();
        }

        try {
            return Integer.parseInt(
                    String.valueOf(value)
            );
        } catch (Exception e) {
            return 0;
        }
    }

    private double getDoubleField(
            EditText editText) {

        return parseDouble(
                editText.getText().toString()
        );
    }

    private double parseDouble(
            String text) {

        if (text == null ||
                text.trim().isEmpty()) {
            return 0;
        }

        try {

            return Double.parseDouble(
                    text.trim()
                            .replace(".", "")
                            .replace(",", ".")
            );

        } catch (Exception e) {

            try {
                return Double.parseDouble(
                        text.trim()
                );
            } catch (Exception ignored) {
                return 0;
            }
        }
    }

    private String rupiah(
            double value) {

        NumberFormat nf =
                NumberFormat.getCurrencyInstance(
                        localeIndonesia
                );

        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);

        return nf.format(value);
    }

    private String formatAngka(
            double value) {

        if (value == Math.floor(value)) {
            return String.valueOf(
                    (long) value
            );
        }

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }

    private boolean mengandung(
            String text,
            String cari) {

        if (text == null) {
            return false;
        }

        return text.toLowerCase(
                Locale.ROOT
        ).contains(cari);
    }

    // =========================================================
    // HELPER KATALOG
    // =========================================================

    private String katalogLabel(
            DocumentSnapshot doc) {

        String merk =
                stringValue(
                        doc.get("merk")
                );

        String model =
                stringValue(
                        doc.get("model")
                );

        String nama =
                stringValue(
                        doc.get("namaPart")
                );

        String kode =
                kodeKatalog(doc);

        StringBuilder s =
                new StringBuilder();

        if (!merk.isEmpty()) {
            s.append(merk);
        }

        if (!model.isEmpty()) {

            if (s.length() > 0) {
                s.append(" ");
            }

            s.append(model);
        }

        s.append(" - ")
                .append(nama);

        if (!kode.isEmpty()) {
            s.append(" [")
                    .append(kode)
                    .append("]");
        }

        return s.toString();
    }

    private String kodeKatalog(
            DocumentSnapshot doc) {

        String oem =
                stringValue(
                        doc.get("kodeOEM")
                );

        if (!oem.isEmpty()) {
            return oem;
        }

        return stringValue(
                doc.get("kodeAftermarket")
        );
    }

    private String ukuranText(
            DocumentSnapshot doc) {

        String ukuran =
                stringValue(
                        doc.get("ukuran")
                );

        if (!ukuran.isEmpty()) {
            return ukuran;
        }

        StringBuilder s =
                new StringBuilder();

        appendDimension(
                s,
                "ID",
                doc.get("diameterDalam")
        );

        appendDimension(
                s,
                "OD",
                doc.get("diameterLuar")
        );

        appendDimension(
                s,
                "P",
                doc.get("panjang")
        );

        appendDimension(
                s,
                "L",
                doc.get("lebar")
        );

        appendDimension(
                s,
                "T",
                doc.get("tebal")
        );

        if (s.length() == 0) {
            return "Belum ada ukuran";
        }

        return s.toString();
    }

    private void appendDimension(
            StringBuilder s,
            String nama,
            Object value) {

        double n =
                numberValue(value);

        if (n <= 0) {
            return;
        }

        if (s.length() > 0) {
            s.append(" | ");
        }

        s.append(nama)
                .append("=")
                .append(formatAngka(n))
                .append(" mm");
    }

    private String iconKecocokan(
            String text) {

        if ("SAMA UKURAN".equals(text)) {
            return "🟢";
        }

        if ("PERLU CEK".equals(text)) {
            return "🟡";
        }

        if ("UKURAN BERBEDA".equals(text)) {
            return "🔴";
        }

        return "🟡";
    }

    private void toast(
            String text) {

        Toast.makeText(
                this,
                text,
                Toast.LENGTH_LONG
        ).show();
    }
}
