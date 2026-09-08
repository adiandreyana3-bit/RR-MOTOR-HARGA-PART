package com.rrmotor.hargapart;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
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

    // ============================================================
    // DATA MASTER JASA
    // Format:
    // nomor|kategori|nama jasa|harga
    // ============================================================

    private final String[] DATA_JASA = {

            "1|SERVIS UMUM|Cek kondisi motor|15000",
            "2|SERVIS UMUM|Cek mesin umum|20000",
            "3|SERVIS UMUM|Servis ringan|75000",
            "4|SERVIS UMUM|Servis berkala|100000",
            "5|SERVIS UMUM|Servis lengkap|150000",
            "6|SERVIS UMUM|Tune up motor|100000",
            "7|SERVIS UMUM|Tune up ringan|75000",
            "8|SERVIS UMUM|Pemeriksaan sebelum perjalanan|30000",
            "9|SERVIS UMUM|Pemeriksaan motor bekas|100000",
            "10|SERVIS UMUM|Cek suara mesin|25000",
            "11|SERVIS UMUM|Cek kebocoran mesin|25000",
            "12|SERVIS UMUM|Cek sistem bahan bakar|30000",
            "13|SERVIS UMUM|Cek sistem kelistrikan|30000",
            "14|SERVIS UMUM|Cek sistem pengereman|20000",
            "15|SERVIS UMUM|Cek kaki-kaki|30000",
            "16|SERVIS UMUM|Cek motor tidak normal|30000",
            "17|SERVIS UMUM|Pemeriksaan menyeluruh|50000",
            "18|SERVIS UMUM|Setting mesin|50000",
            "19|SERVIS UMUM|Setting langsam|25000",
            "20|SERVIS UMUM|Setting idle|25000",

            "21|OLI & PELUMASAN|Ganti oli mesin|15000",
            "22|OLI & PELUMASAN|Ganti oli gardan|10000",
            "23|OLI & PELUMASAN|Ganti oli mesin + gardan|20000",
            "24|OLI & PELUMASAN|Flush oli mesin|25000",
            "25|OLI & PELUMASAN|Ganti oli setelah servis|10000",
            "26|OLI & PELUMASAN|Tambah oli mesin|5000",
            "27|OLI & PELUMASAN|Grease komponen|15000",
            "28|OLI & PELUMASAN|Grease komstir|30000",
            "29|OLI & PELUMASAN|Grease bearing|25000",
            "30|OLI & PELUMASAN|Grease CVT|25000",

            "31|BUSI & PENGAPIAN|Ganti busi|15000",
            "32|BUSI & PENGAPIAN|Cek busi|10000",
            "33|BUSI & PENGAPIAN|Bersihkan busi|10000",
            "34|BUSI & PENGAPIAN|Setel celah busi|10000",
            "35|BUSI & PENGAPIAN|Ganti cop busi|15000",
            "36|BUSI & PENGAPIAN|Ganti kabel busi|20000",
            "37|BUSI & PENGAPIAN|Cek pengapian|25000",
            "38|BUSI & PENGAPIAN|Cek koil|25000",
            "39|BUSI & PENGAPIAN|Ganti koil|25000",
            "40|BUSI & PENGAPIAN|Cek CDI|25000",

            "41|CVT MOTOR MATIC|Servis CVT|75000",
            "42|CVT MOTOR MATIC|Bongkar CVT|50000",
            "43|CVT MOTOR MATIC|Bersihkan CVT|60000",
            "44|CVT MOTOR MATIC|Ganti V-Belt|35000",
            "45|CVT MOTOR MATIC|Ganti roller|35000",
            "46|CVT MOTOR MATIC|Ganti rumah roller|35000",
            "47|CVT MOTOR MATIC|Ganti slider|25000",
            "48|CVT MOTOR MATIC|Ganti kampas ganda|50000",
            "49|CVT MOTOR MATIC|Ganti mangkok kopling|35000",
            "50|CVT MOTOR MATIC|Ganti pulley depan|50000",
            "51|CVT MOTOR MATIC|Ganti pulley belakang|50000",
            "52|CVT MOTOR MATIC|Ganti boss pulley|30000",
            "53|CVT MOTOR MATIC|Ganti bearing CVT|50000",
            "54|CVT MOTOR MATIC|Ganti seal CVT|40000",
            "55|CVT MOTOR MATIC|Bongkar pasang CVT lengkap|75000",
            "56|CVT MOTOR MATIC|Cleaning rumah CVT|30000",
            "57|CVT MOTOR MATIC|Cleaning kampas ganda|25000",
            "58|CVT MOTOR MATIC|Amplas kampas ganda|25000",
            "59|CVT MOTOR MATIC|Setting CVT|40000",
            "60|CVT MOTOR MATIC|Diagnosa bunyi CVT|25000",

            "61|MESIN|Bongkar cover mesin|30000",
            "62|MESIN|Bongkar head|100000",
            "63|MESIN|Pasang head|100000",
            "64|MESIN|Bongkar blok|100000",
            "65|MESIN|Pasang blok|100000",
            "66|MESIN|Bongkar piston|150000",
            "67|MESIN|Pasang piston|150000",
            "68|MESIN|Ganti ring piston|150000",
            "69|MESIN|Ganti piston|150000",
            "70|MESIN|Ganti packing head|100000",
            "71|MESIN|Ganti packing blok|100000",
            "72|MESIN|Ganti seal klep|100000",
            "73|MESIN|Bongkar klep|100000",
            "74|MESIN|Skir klep|75000",
            "75|MESIN|Setel klep|40000",
            "76|MESIN|Cek kompresi|30000",
            "77|MESIN|Turun mesin ringan|300000",
            "78|MESIN|Turun mesin|500000",
            "79|MESIN|Overhaul ringan|400000",
            "80|MESIN|Overhaul mesin|750000",

            "81|KLEP / HEAD|Setel klep|40000",
            "82|KLEP / HEAD|Cek klep|25000",
            "83|KLEP / HEAD|Bongkar cover klep|25000",
            "84|KLEP / HEAD|Ganti seal klep|100000",
            "85|KLEP / HEAD|Skir klep 1 set|75000",
            "86|KLEP / HEAD|Skir klep 2 klep|100000",
            "87|KLEP / HEAD|Ganti klep|125000",
            "88|KLEP / HEAD|Ganti per klep|100000",
            "89|KLEP / HEAD|Servis head|150000",
            "90|KLEP / HEAD|Poles port head|150000",

            "91|INJEKSI|Servis injeksi|75000",
            "92|INJEKSI|Tune up injeksi|100000",
            "93|INJEKSI|Scan ECU|75000",
            "94|INJEKSI|Diagnosa ECU|75000",
            "95|INJEKSI|Reset ECU|50000",
            "96|INJEKSI|Reset indikator servis|30000",
            "97|INJEKSI|Cleaning injector|50000",
            "98|INJEKSI|Tes injector|40000",
            "99|INJEKSI|Bongkar injector|40000",
            "100|INJEKSI|Pasang injector|40000",
            "101|INJEKSI|Cleaning throttle body|50000",
            "102|INJEKSI|Bongkar throttle body|50000",
            "103|INJEKSI|Pasang throttle body|50000",
            "104|INJEKSI|Setel throttle body|40000",
            "105|INJEKSI|Cek TPS|30000",
            "106|INJEKSI|Cek ISC|30000",
            "107|INJEKSI|Cek sensor O2|30000",
            "108|INJEKSI|Cek sensor suhu|30000",
            "109|INJEKSI|Cek sensor MAP|30000",
            "110|INJEKSI|Cek sensor CKP|30000",

            "111|KARBURATOR|Servis karburator|60000",
            "112|KARBURATOR|Bongkar karburator|40000",
            "113|KARBURATOR|Pasang karburator|40000",
            "114|KARBURATOR|Cleaning karburator|50000",
            "115|KARBURATOR|Setel karburator|30000",
            "116|KARBURATOR|Setel langsam karburator|20000",
            "117|KARBURATOR|Setel campuran udara|25000",
            "118|KARBURATOR|Ganti pelampung|30000",
            "119|KARBURATOR|Ganti jarum skep|30000",
            "120|KARBURATOR|Ganti manifold|30000",

            "121|REM|Ganti kampas rem depan|25000",
            "122|REM|Ganti kampas rem belakang|25000",
            "123|REM|Servis rem depan|30000",
            "124|REM|Servis rem belakang|30000",
            "125|REM|Bersihkan kaliper|35000",
            "126|REM|Bongkar kaliper|40000",
            "127|REM|Pasang kaliper|40000",
            "128|REM|Ganti cakram|30000",
            "129|REM|Ganti master rem|40000",
            "130|REM|Ganti seal master rem|75000",
            "131|REM|Bleeding rem|35000",
            "132|REM|Ganti minyak rem|25000",
            "133|REM|Ganti selang rem|40000",
            "134|REM|Setel rem tromol|20000",
            "135|REM|Bongkar tromol|25000",
            "136|REM|Bersihkan tromol|25000",
            "137|REM|Ganti kabel rem|25000",
            "138|REM|Setel tuas rem|15000",
            "139|REM|Cek sistem rem|20000",
            "140|REM|Servis rem lengkap|75000",

            "141|RANTAI & GEAR|Setel rantai|15000",
            "142|RANTAI & GEAR|Bersihkan rantai|20000",
            "143|RANTAI & GEAR|Lumasi rantai|10000",
            "144|RANTAI & GEAR|Ganti rantai|30000",
            "145|RANTAI & GEAR|Ganti gear depan|20000",
            "146|RANTAI & GEAR|Ganti gear belakang|30000",
            "147|RANTAI & GEAR|Ganti gear set|50000",
            "148|RANTAI & GEAR|Bongkar gear set|40000",
            "149|RANTAI & GEAR|Setel posisi roda belakang|20000",
            "150|RANTAI & GEAR|Cek rantai & gear|15000",

            "151|BAN & RODA|Bongkar roda depan|20000",
            "152|BAN & RODA|Pasang roda depan|20000",
            "153|BAN & RODA|Bongkar roda belakang|25000",
            "154|BAN & RODA|Pasang roda belakang|25000",
            "155|BAN & RODA|Ganti ban depan|25000",
            "156|BAN & RODA|Ganti ban belakang|25000",
            "157|BAN & RODA|Pasang ban tubeless|25000",
            "158|BAN & RODA|Bongkar ban tubeless|20000",
            "159|BAN & RODA|Tambal ban tubeless|15000",
            "160|BAN & RODA|Tambal ban dalam|15000",
            "161|BAN & RODA|Ganti ban dalam|20000",
            "162|BAN & RODA|Cek kebocoran ban|10000",
            "163|BAN & RODA|Isi angin|5000",
            "164|BAN & RODA|Cek tekanan ban|5000",
            "165|BAN & RODA|Setel keseimbangan roda|25000",
            "166|BAN & RODA|Bongkar bearing roda|40000",
            "167|BAN & RODA|Pasang bearing roda|40000",
            "168|BAN & RODA|Ganti seal roda|30000",
            "169|BAN & RODA|Servis roda|40000",
            "170|BAN & RODA|Cek velg|15000",

            "171|SHOCK & KAKI-KAKI|Ganti shock depan|50000",
            "172|SHOCK & KAKI-KAKI|Ganti shock belakang|35000",
            "173|SHOCK & KAKI-KAKI|Servis shock depan|100000",
            "174|SHOCK & KAKI-KAKI|Ganti seal shock depan|100000",
            "175|SHOCK & KAKI-KAKI|Ganti oli shock|75000",
            "176|SHOCK & KAKI-KAKI|Bongkar shock depan|75000",
            "177|SHOCK & KAKI-KAKI|Pasang shock depan|50000",
            "178|SHOCK & KAKI-KAKI|Pasang shock belakang|35000",
            "179|SHOCK & KAKI-KAKI|Cek shock|20000",
            "180|SHOCK & KAKI-KAKI|Cek kaki-kaki|30000",

            "181|KOMSTIR / KEMUDI|Setel komstir|30000",
            "182|KOMSTIR / KEMUDI|Bongkar komstir|75000",
            "183|KOMSTIR / KEMUDI|Pasang komstir|75000",
            "184|KOMSTIR / KEMUDI|Ganti bearing komstir|100000",
            "185|KOMSTIR / KEMUDI|Grease komstir|50000",
            "186|KOMSTIR / KEMUDI|Cek komstir|20000",
            "187|KOMSTIR / KEMUDI|Setel stang|20000",
            "188|KOMSTIR / KEMUDI|Bongkar segitiga|100000",
            "189|KOMSTIR / KEMUDI|Pasang segitiga|100000",
            "190|KOMSTIR / KEMUDI|Cek segitiga|30000",

            "191|AKI & KELISTRIKAN|Ganti aki|25000",
            "192|AKI & KELISTRIKAN|Cek aki|15000",
            "193|AKI & KELISTRIKAN|Cas aki|20000",
            "194|AKI & KELISTRIKAN|Cek pengisian aki|25000",
            "195|AKI & KELISTRIKAN|Cek kiprok|25000",
            "196|AKI & KELISTRIKAN|Ganti kiprok|30000",
            "197|AKI & KELISTRIKAN|Cek spul|40000",
            "198|AKI & KELISTRIKAN|Ganti spul|75000",
            "199|AKI & KELISTRIKAN|Cek starter|25000",
            "200|AKI & KELISTRIKAN|Servis dinamo starter|75000",
            "201|AKI & KELISTRIKAN|Ganti dinamo starter|50000",
            "202|AKI & KELISTRIKAN|Ganti relay starter|25000",
            "203|AKI & KELISTRIKAN|Ganti sekring|10000",
            "204|AKI & KELISTRIKAN|Perbaikan kabel sederhana|25000",
            "205|AKI & KELISTRIKAN|Perbaikan kabel kompleks|75000",
            "206|AKI & KELISTRIKAN|Cek korsleting|50000",
            "207|AKI & KELISTRIKAN|Cek kelistrikan total|75000",
            "208|AKI & KELISTRIKAN|Pasang alarm|75000",
            "209|AKI & KELISTRIKAN|Pasang USB charger|40000",
            "210|AKI & KELISTRIKAN|Pasang lampu tambahan|40000",

            "211|LAMPU, KLAKSON & AKSESORI|Ganti lampu depan|15000",
            "212|LAMPU, KLAKSON & AKSESORI|Ganti lampu belakang|15000",
            "213|LAMPU, KLAKSON & AKSESORI|Ganti lampu sein|15000",
            "214|LAMPU, KLAKSON & AKSESORI|Ganti lampu rem|15000",
            "215|LAMPU, KLAKSON & AKSESORI|Ganti LED|25000",
            "216|LAMPU, KLAKSON & AKSESORI|Pasang lampu tambahan|40000",
            "217|LAMPU, KLAKSON & AKSESORI|Ganti klakson|20000",
            "218|LAMPU, KLAKSON & AKSESORI|Pasang klakson|25000",
            "219|LAMPU, KLAKSON & AKSESORI|Ganti saklar lampu|25000",
            "220|LAMPU, KLAKSON & AKSESORI|Ganti saklar sein|25000",

            "221|FUEL SYSTEM / TANGKI|Cek fuel pump|30000",
            "222|FUEL SYSTEM / TANGKI|Bongkar fuel pump|75000",
            "223|FUEL SYSTEM / TANGKI|Pasang fuel pump|50000",
            "224|FUEL SYSTEM / TANGKI|Cleaning fuel pump|75000",
            "225|FUEL SYSTEM / TANGKI|Ganti filter bensin|25000",
            "226|FUEL SYSTEM / TANGKI|Cek tekanan bensin|50000",
            "227|FUEL SYSTEM / TANGKI|Bersihkan tangki|100000",
            "228|FUEL SYSTEM / TANGKI|Bongkar tangki|50000",
            "229|FUEL SYSTEM / TANGKI|Pasang tangki|40000",
            "230|FUEL SYSTEM / TANGKI|Cek kebocoran tangki|30000",

            "231|PENDINGIN / RADIATOR|Ganti coolant|30000",
            "232|PENDINGIN / RADIATOR|Flush radiator|75000",
            "233|PENDINGIN / RADIATOR|Servis radiator|100000",
            "234|PENDINGIN / RADIATOR|Bongkar radiator|75000",
            "235|PENDINGIN / RADIATOR|Pasang radiator|50000",
            "236|PENDINGIN / RADIATOR|Ganti selang radiator|40000",
            "237|PENDINGIN / RADIATOR|Ganti thermostat|50000",
            "238|PENDINGIN / RADIATOR|Ganti water pump|100000",
            "239|PENDINGIN / RADIATOR|Cek kipas radiator|30000",
            "240|PENDINGIN / RADIATOR|Ganti kipas radiator|50000",

            "241|BODY & AKSESORI|Pasang spion|10000",
            "242|BODY & AKSESORI|Ganti spion|10000",
            "243|BODY & AKSESORI|Pasang cover body|50000",
            "244|BODY & AKSESORI|Bongkar cover body|50000",
            "245|BODY & AKSESORI|Pasang windshield|25000",
            "246|BODY & AKSESORI|Pasang box belakang|50000",
            "247|BODY & AKSESORI|Pasang bracket box|40000",
            "248|BODY & AKSESORI|Pasang handgrip|20000",
            "249|BODY & AKSESORI|Pasang footstep|25000",
            "250|BODY & AKSESORI|Pasang standar samping|25000",

            "251|PEKERJAAN KHUSUS / DARURAT|Motor mogok ringan|50000",
            "252|PEKERJAAN KHUSUS / DARURAT|Diagnosa motor mogok|50000",
            "253|PEKERJAAN KHUSUS / DARURAT|Bongkar busi motor mogok|25000",
            "254|PEKERJAAN KHUSUS / DARURAT|Cek bahan bakar|20000",
            "255|PEKERJAAN KHUSUS / DARURAT|Cek pengapian motor mogok|30000",
            "256|PEKERJAAN KHUSUS / DARURAT|Bantuan starter|20000",
            "257|PEKERJAAN KHUSUS / DARURAT|Darurat kelistrikan ringan|50000",
            "258|PEKERJAAN KHUSUS / DARURAT|Bongkar baut macet|25000",
            "259|PEKERJAAN KHUSUS / DARURAT|Cabut baut patah|50000",
            "260|PEKERJAAN KHUSUS / DARURAT|Cabut baut dol|50000",

            "261|JASA TAMBAHAN|Cuci komponen mesin|25000",
            "262|JASA TAMBAHAN|Cleaning komponen|20000",
            "263|JASA TAMBAHAN|Cleaning rantai|20000",
            "264|JASA TAMBAHAN|Cleaning rem|25000",
            "265|JASA TAMBAHAN|Cleaning CVT|30000",
            "266|JASA TAMBAHAN|Cleaning throttle body|50000",
            "267|JASA TAMBAHAN|Cleaning injector|50000",
            "268|JASA TAMBAHAN|Pemeriksaan suara mesin|25000",
            "269|JASA TAMBAHAN|Pemeriksaan getaran motor|25000",
            "270|JASA TAMBAHAN|Pemeriksaan asap knalpot|25000"
    };

    // ============================================================
    // ON CREATE
    // ============================================================

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

        root.addView(judul);

        TextView subjudul = new TextView(this);
        subjudul.setText("CEK HARGA PART & JASA");
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

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

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

            auth.signInWithEmailAndPassword(email, password)
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

                            String pesan = "Login gagal";

                            if (task.getException() != null) {
                                pesan = task.getException().getMessage();
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
                "Email",
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
                    emailInput.getText().toString().trim();

            String password =
                    passwordInput.getText().toString();

            String ulang =
                    ulangInput.getText().toString();

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

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 50);

        scrollView.addView(root);

        TextView judul = new TextView(this);

        judul.setText(
                "🏍️ RR MOTOR\nCEK HARGA PART & JASA"
        );

        judul.setTextSize(25);
        judul.setGravity(Gravity.CENTER);
        judul.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        judul.setPadding(0, 10, 0, 25);

        root.addView(judul);

        // ========================================================
        // CARI PART
        // ========================================================

        TextView cariTitle = new TextView(this);

        cariTitle.setText("🔎 CARI PART");
        cariTitle.setTextSize(19);
        cariTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        cariTitle.setPadding(5, 10, 5, 8);

        root.addView(cariTitle);

        EditText cariInput =
                buatInput("Nama atau Kode Part");

        root.addView(cariInput);

        Button cariButton = new Button(this);

        cariButton.setText("🔎 CARI PART");
        cariButton.setAllCaps(false);
        cariButton.setTextSize(17);

        root.addView(cariButton);

        hasilPencarianText = new TextView(this);
        hasilPencarianText.setTextSize(16);
        hasilPencarianText.setPadding(10, 10, 10, 20);

        root.addView(hasilPencarianText);

        cariButton.setOnClickListener(
                v -> cariPart(
                        cariInput.getText().toString().trim()
                )
        );

        Button daftarPartButton = new Button(this);

        daftarPartButton.setText(
                "📋 DAFTAR PART TERSIMPAN"
        );
        daftarPartButton.setTextSize(17);
        daftarPartButton.setAllCaps(false);

        root.addView(daftarPartButton);

        daftarPartButton.setOnClickListener(
                v -> tampilkanDaftarPart()
        );

        // ========================================================
        // MENU JASA
        // ========================================================

        TextView garisJasa = new TextView(this);
        garisJasa.setText("────────────────────────");
        garisJasa.setGravity(Gravity.CENTER);
        garisJasa.setPadding(0, 20, 0, 10);

        root.addView(garisJasa);

        TextView jasaTitle = new TextView(this);

        jasaTitle.setText("🔧 JASA BENGKEL");
        jasaTitle.setTextSize(19);
        jasaTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        jasaTitle.setPadding(5, 5, 5, 8);

        root.addView(jasaTitle);

        Button daftarJasaButton = new Button(this);

        daftarJasaButton.setText(
                "🔧 DAFTAR JASA BENGKEL"
        );
        daftarJasaButton.setTextSize(17);
        daftarJasaButton.setAllCaps(false);

        root.addView(daftarJasaButton);

        daftarJasaButton.setOnClickListener(
                v -> tampilkanDaftarJasa()
        );

        Button updateJasaButton = new Button(this);

        updateJasaButton.setText(
                "☁️ CEK / UPDATE MASTER JASA"
        );
        updateJasaButton.setTextSize(16);
        updateJasaButton.setAllCaps(false);

        root.addView(updateJasaButton);

        updateJasaButton.setOnClickListener(
                v -> konfirmasiUpdateMasterJasa()
        );

        // ========================================================
        // GARIS
        // ========================================================

        TextView garis = new TextView(this);

        garis.setText("────────────────────────");
        garis.setGravity(Gravity.CENTER);
        garis.setPadding(0, 20, 0, 15);

        root.addView(garis);

        // ========================================================
        // TAMBAH PART
        // ========================================================

        TextView tambahTitle = new TextView(this);

        tambahTitle.setText("➕ TAMBAH PART");
        tambahTitle.setTextSize(19);
        tambahTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        tambahTitle.setPadding(5, 5, 5, 10);

        root.addView(tambahTitle);

        namaPartInput =
                buatInput("Nama Part *");

        hargaPokokInput =
                buatInput("Harga Pokok / Modal *");

        kodePartInput =
                buatInput("Kode Part (opsional)");

        stokInput =
                buatInput("Stok (opsional)");

        supplierInput =
                buatInput("Supplier (opsional)");

        catatanInput =
                buatInput("Catatan (opsional)");

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

        Button hitungButton = new Button(this);

        hitungButton.setText(
                "💰 HITUNG HARGA JUAL"
        );
        hitungButton.setTextSize(17);
        hitungButton.setAllCaps(false);

        root.addView(hitungButton);

        hasilHargaText = new TextView(this);

        hasilHargaText.setTextSize(17);
        hasilHargaText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        hasilHargaText.setPadding(10, 10, 10, 10);

        root.addView(hasilHargaText);

        hitungButton.setOnClickListener(
                v -> hitungHargaJual()
        );

        Button simpanButton = new Button(this);

        simpanButton.setText("💾 SIMPAN PART");
        simpanButton.setTextSize(17);
        simpanButton.setAllCaps(false);

        root.addView(simpanButton);

        simpanButton.setOnClickListener(
                v -> simpanPart()
        );

        Button scanButton = new Button(this);

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

        TextView garis2 = new TextView(this);

        garis2.setText(
                "────────────────────────"
        );
        garis2.setGravity(Gravity.CENTER);
        garis2.setPadding(0, 25, 0, 10);

        root.addView(garis2);

        Button keluarButton = new Button(this);

        keluarButton.setText("🚪 KELUAR AKUN");
        keluarButton.setAllCaps(false);

        root.addView(keluarButton);

        keluarButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Keluar Akun")
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
    // DAFTAR JASA
    // ============================================================

    private void tampilkanDaftarJasa() {

        AlertDialog dialog =
                new AlertDialog.Builder(this).create();

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(15, 10, 15, 10);

        TextView judul =
                new TextView(this);

        judul.setText(
                "🔧 DAFTAR JASA BENGKEL"
        );

        judul.setTextSize(21);
        judul.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        judul.setGravity(Gravity.CENTER);
        judul.setPadding(5, 5, 5, 15);

        root.addView(judul);

        EditText pencarian =
                buatInput("🔎 Cari nama jasa...");

        root.addView(pencarian);

        Spinner spinnerKategori =
                new Spinner(this);

        root.addView(spinnerKategori);

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

        LinearLayout tombolBawah =
                new LinearLayout(this);

        tombolBawah.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button tambah =
                new Button(this);

        tambah.setText("➕ TAMBAH");
        tambah.setAllCaps(false);

        Button tutup =
                new Button(this);

        tutup.setText("TUTUP");
        tutup.setAllCaps(false);

        tombolBawah.addView(
                tambah,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        tombolBawah.addView(
                tutup,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        root.addView(tombolBawah);

        dialog.setView(root);

        final List<DocumentSnapshot> semuaJasa =
                new ArrayList<>();

        final List<String> kategori =
                new ArrayList<>();

        kategori.add("SEMUA KATEGORI");

        Runnable isiKategori = () -> {

            Set<String> set =
                    new LinkedHashSet<>();

            set.add("SEMUA KATEGORI");

            for (DocumentSnapshot doc :
                    semuaJasa) {

                String k =
                        getStringField(
                                doc,
                                "kategori"
                        );

                if (!k.isEmpty()) {
                    set.add(k);
                }
            }

            kategori.clear();
            kategori.addAll(set);

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            kategori
                    );

            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );

            spinnerKategori.setAdapter(adapter);
        };

        Runnable tampilkanData = () -> {

            daftar.removeAllViews();

            String kata =
                    pencarian.getText()
                            .toString()
                            .trim()
                            .toLowerCase(
                                    Locale.getDefault()
                            );

            String kat =
                    spinnerKategori.getSelectedItem() == null
                            ? "SEMUA KATEGORI"
                            : spinnerKategori
                            .getSelectedItem()
                            .toString();

            int jumlah = 0;

            for (DocumentSnapshot doc :
                    semuaJasa) {

                boolean aktif =
                        getBooleanField(
                                doc,
                                "aktif",
                                true
                        );

                if (!aktif) {
                    continue;
                }

                String nama =
                        getStringField(
                                doc,
                                "namaJasa"
                        );

                String kategoriData =
                        getStringField(
                                doc,
                                "kategori"
                        );

                String gabungan =
                        (
                                nama + " " +
                                        kategoriData
                        )
                                .toLowerCase(
                                        Locale.getDefault()
                                );

                if (!kata.isEmpty() &&
                        !gabungan.contains(kata)) {
                    continue;
                }

                if (!kat.equals(
                        "SEMUA KATEGORI"
                ) &&
                        !kategoriData.equals(kat)) {
                    continue;
                }

                daftar.addView(
                        buatCardJasa(
                                doc,
                                dialog,
                                semuaJasa,
                                tampilkanData
                        )
                );

                jumlah++;
            }

            if (jumlah == 0) {

                TextView kosong =
                        new TextView(this);

                kosong.setText(
                        "❌ Jasa tidak ditemukan."
                );

                kosong.setTextSize(16);
                kosong.setGravity(Gravity.CENTER);
                kosong.setPadding(
                        20,
                        40,
                        20,
                        40
                );

                daftar.addView(kosong);
            }
        };

        pencarian.addTextChangedListener(
                new TextWatcher() {

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
                            Editable s
                    ) {
                    }
                }
        );

        spinnerKategori.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        tampilkanData.run();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );

        tambah.setOnClickListener(
                v -> tampilkanFormJasa(
                        null,
                        () -> {

                            db.collection("jasa")
                                    .get()
                                    .addOnSuccessListener(
                                            result -> {

                                                semuaJasa.clear();
                                                semuaJasa.addAll(
                                                        result.getDocuments()
                                                );

                                                isiKategori.run();
                                                tampilkanData.run();
                                            }
                                    );
                        }
                )
        );

        tutup.setOnClickListener(
                v -> dialog.dismiss()
        );

        db.collection("jasa")
                .get()
                .addOnSuccessListener(
                        result -> {

                            semuaJasa.clear();

                            semuaJasa.addAll(
                                    result.getDocuments()
                            );

                            isiKategori.run();

                            if (!semuaJasa.isEmpty()) {
                                tampilkanData.run();
                            } else {

                                Toast.makeText(
                                        this,
                                        "Data jasa belum ada. Gunakan UPDATE MASTER JASA.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Gagal mengambil jasa: " +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    (int)(
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels * 0.95
                    ),
                    (int)(
                            getResources()
                                    .getDisplayMetrics()
                                    .heightPixels * 0.88
                    )
            );
        }
    }

    // ============================================================
    // CARD JASA
    // ============================================================

    private LinearLayout buatCardJasa(
            DocumentSnapshot doc,
            AlertDialog dialog,
            List<DocumentSnapshot> semuaJasa,
            Runnable refresh
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                12,
                12,
                12,
                12
        );

        String nama =
                getStringField(
                        doc,
                        "namaJasa"
                );

        String kategori =
                getStringField(
                        doc,
                        "kategori"
                );

        long harga =
                getLongField(
                        doc,
                        "hargaAktif"
                );

        if (harga <= 0) {

            harga =
                    getLongField(
                            doc,
                            "harga2026"
                    );
        }

        String satuan =
                getStringField(
                        doc,
                        "satuan"
                );

        if (satuan.isEmpty()) {
            satuan = "per jasa";
        }

        TextView info =
                new TextView(this);

        info.setText(
                "🔧 " + nama +
                        "\n📂 " + kategori +
                        "\n💰 " + formatRupiah(harga) +
                        " / " + satuan
        );

        info.setTextSize(16);

        card.addView(info);

        LinearLayout tombol =
                new LinearLayout(this);

        tombol.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button edit =
                new Button(this);

        edit.setText("✏️ EDIT");
        edit.setAllCaps(false);

        Button hapus =
                new Button(this);

        hapus.setText("🗑️ HAPUS");
        hapus.setAllCaps(false);

        tombol.addView(
                edit,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        tombol.addView(
                hapus,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        card.addView(tombol);

        edit.setOnClickListener(
                v -> tampilkanFormJasa(
                        doc,
                        refresh
                )
        );

        hapus.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Hapus Jasa")
                    .setMessage(
                            "Hapus jasa:\n\n" +
                                    nama +
                                    "\n\nData akan dihapus dari Firestore."
                    )
                    .setNegativeButton(
                            "BATAL",
                            null
                    )
                    .setPositiveButton(
                            "HAPUS",
                            (d, which) -> {

                                db.collection("jasa")
                                        .document(doc.getId())
                                        .delete()
                                        .addOnSuccessListener(
                                                unused -> {

                                                    Toast.makeText(
                                                            this,
                                                            "Jasa dihapus",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                    semuaJasa.remove(doc);

                                                    refresh.run();
                                                }
                                        )
                                        .addOnFailureListener(
                                                e ->
                                                        Toast.makeText(
                                                                this,
                                                                "Gagal menghapus: " +
                                                                        e.getMessage(),
                                                                Toast.LENGTH_LONG
                                                        ).show()
                                        );
                            }
                    )
                    .show();
        });

        TextView garis =
                new TextView(this);

        garis.setText(
                "────────────────────────"
        );

        garis.setGravity(
                Gravity.CENTER
        );

        card.addView(garis);

        return card;
    }

    // ============================================================
    // FORM TAMBAH / EDIT JASA
    // ============================================================

    private void tampilkanFormJasa(
            DocumentSnapshot doc,
            Runnable setelahSimpan
    ) {

        boolean editMode = doc != null;

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                15,
                5,
                15,
                5
        );

        EditText nama =
                buatInput("Nama Jasa *");

        EditText kategori =
                buatInput("Kategori *");

        EditText harga =
                buatInput("Harga Jasa *");

        EditText satuan =
                buatInput("Satuan");

        EditText keterangan =
                buatInput("Keterangan");

        harga.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        keterangan.setSingleLine(false);
        keterangan.setMinLines(2);

        root.addView(nama);
        root.addView(kategori);
        root.addView(harga);
        root.addView(satuan);
        root.addView(keterangan);

        if (editMode) {

            nama.setText(
                    getStringField(
                            doc,
                            "namaJasa"
                    )
            );

            kategori.setText(
                    getStringField(
                            doc,
                            "kategori"
                    )
            );

            long hargaData =
                    getLongField(
                            doc,
                            "hargaAktif"
                    );

            if (hargaData <= 0) {
                hargaData =
                        getLongField(
                                doc,
                                "harga2026"
                        );
            }

            harga.setText(
                    String.valueOf(hargaData)
            );

            satuan.setText(
                    getStringField(
                            doc,
                            "satuan"
                    )
            );

            keterangan.setText(
                    getStringField(
                            doc,
                            "keterangan"
                    )
            );
        } else {

            satuan.setText("jasa");
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                editMode
                                        ? "✏️ EDIT JASA"
                                        : "➕ TAMBAH JASA"
                        )
                        .setView(root)
                        .setNegativeButton(
                                "BATAL",
                                null
                        )
                        .setPositiveButton(
                                editMode
                                        ? "SIMPAN"
                                        : "TAMBAH",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button positive =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    positive.setOnClickListener(
                            v -> {

                                String namaData =
                                        nama.getText()
                                                .toString()
                                                .trim();

                                String kategoriData =
                                        kategori.getText()
                                                .toString()
                                                .trim();

                                long hargaData =
                                        angka(harga);

                                String satuanData =
                                        satuan.getText()
                                                .toString()
                                                .trim();

                                String keteranganData =
                                        keterangan.getText()
                                                .toString()
                                                .trim();

                                if (namaData.isEmpty()) {
                                    nama.setError(
                                            "Nama wajib diisi"
                                    );
                                    nama.requestFocus();
                                    return;
                                }

                                if (kategoriData.isEmpty()) {
                                    kategori.setError(
                                            "Kategori wajib diisi"
                                    );
                                    kategori.requestFocus();
                                    return;
                                }

                                if (hargaData <= 0) {
                                    harga.setError(
                                            "Harga wajib diisi"
                                    );
                                    harga.requestFocus();
                                    return;
                                }

                                if (satuanData.isEmpty()) {
                                    satuanData = "jasa";
                                }

                                Map<String, Object> data =
                                        new HashMap<>();

                                data.put(
                                        "namaJasa",
                                        namaData
                                );

                                data.put(
                                        "kategori",
                                        kategoriData
                                );

                                data.put(
                                        "harga2026",
                                        hargaData
                                );

                                data.put(
                                        "hargaAktif",
                                        hargaData
                                );

                                data.put(
                                        "tahunBerlaku",
                                        2026
                                );

                                data.put(
                                        "satuan",
                                        satuanData
                                );

                                data.put(
                                        "keterangan",
                                        keteranganData
                                );

                                data.put(
                                        "aktif",
                                        true
                                );

                                data.put(
                                        "updatedAt",
                                        System.currentTimeMillis()
                                );

                                positive.setEnabled(false);

                                if (editMode) {

                                    db.collection("jasa")
                                            .document(doc.getId())
                                            .set(
                                                    data
                                            )
                                            .addOnSuccessListener(
                                                    unused -> {

                                                        Toast.makeText(
                                                                this,
                                                                "Jasa berhasil diperbarui",
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                        dialog.dismiss();

                                                        if (setelahSimpan != null) {
                                                            setelahSimpan.run();
                                                        }
                                                    }
                                            )
                                            .addOnFailureListener(
                                                    e -> {

                                                        positive.setEnabled(true);

                                                        Toast.makeText(
                                                                this,
                                                                "Gagal menyimpan: " +
                                                                        e.getMessage(),
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                    }
                                            );

                                } else {

                                    db.collection("jasa")
                                            .add(data)
                                            .addOnSuccessListener(
                                                    unused -> {

                                                        Toast.makeText(
                                                                this,
                                                                "Jasa berhasil ditambahkan",
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                        dialog.dismiss();

                                                        if (setelahSimpan != null) {
                                                            setelahSimpan.run();
                                                        }
                                                    }
                                            )
                                            .addOnFailureListener(
                                                    e -> {

                                                        positive.setEnabled(true);

                                                        Toast.makeText(
                                                                this,
                                                                "Gagal menambah: " +
                                                                        e.getMessage(),
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                    }
                                            );
                                }
                            }
                    );
                }
        );

        dialog.show();
    }

    // ============================================================
    // UPDATE MASTER 270 JASA
    // ============================================================

    private void konfirmasiUpdateMasterJasa() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "☁️ MASTER JASA"
                )
                .setMessage(
                        "Aplikasi akan memasukkan / memperbarui " +
                                DATA_JASA.length +
                                " jasa bengkel ke Firestore.\n\n" +
                                "Data part tidak akan berubah.\n\n" +
                                "Apakah dilanjutkan?"
                )
                .setNegativeButton(
                        "BATAL",
                        null
                )
                .setPositiveButton(
                        "UPDATE",
                        (dialog, which) ->
                                updateMasterJasa()
                )
                .show();
    }

    private void updateMasterJasa() {

        Toast.makeText(
                this,
                "⏳ Menyiapkan master jasa...",
                Toast.LENGTH_SHORT
        ).show();

        WriteBatch batch =
                db.batch();

        int nomorBerhasil = 0;

        for (String baris :
                DATA_JASA) {

            String[] p =
                    baris.split(
                            "\\|",
                            -1
                    );

            if (p.length < 4) {
                continue;
            }

            String nomor = p[0];
            String kategori = p[1];
            String nama = p[2];

            long harga;

            try {
                harga =
                        Long.parseLong(
                                p[3]
                        );
            } catch (Exception e) {
                continue;
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "namaJasa",
                    nama
            );

            data.put(
                    "kategori",
                    kategori
            );

            data.put(
                    "harga2026",
                    harga
            );

            data.put(
                    "hargaAktif",
                    harga
            );

            data.put(
                    "tahunBerlaku",
                    2026
            );

            data.put(
                    "satuan",
                    "jasa"
            );

            data.put(
                    "keterangan",
                    "Jasa saja, part/bahan terpisah"
            );

            data.put(
                    "aktif",
                    true
            );

            data.put(
                    "updatedAt",
                    System.currentTimeMillis()
            );

            // ID J001 sampai J270
            String id =
                    "J" +
                            String.format(
                                    Locale.US,
                                    "%03d",
                                    Integer.parseInt(
                                            nomor
                                    )
                            );

            batch.set(
                    db.collection("jasa")
                            .document(id),
                    data
            );

            nomorBerhasil++;
        }

        final int total =
                nomorBerhasil;

        batch.commit()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "✅ " +
                                            total +
                                            " jasa berhasil diperbarui.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "❌ Update gagal:\n" +
                                                e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show()
                );
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
    // HITUNG HARGA PART
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
                                (
                                        modal *
                                                margin[0] /
                                                100.0
                                )
                );

        long hargaMax =
                Math.round(
                        modal +
                                (
                                        modal *
                                                margin[1] /
                                                100.0
                                )
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
    // MARGIN
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
                (
                        double
                                (
                                        nilai -
                                                batas1
                                )
                )
                        /
                        (
                                double
                                        (
                                                batas2 -
                                                        batas1
                                        )
                        );

        double min =
                min1 +
                        (
                                (min2 - min1)
                                        * posisi
                        );

        double max =
                max1 +
                        (
                                (max2 - max1)
                                        * posisi
                        );

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
                        e ->
                                Toast.makeText(
                                        this,
                                        "Gagal menyimpan: " +
                                                e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show()
                );
    }

    // ============================================================
    // DAFTAR PART
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

        tutup.setText("TUTUP");
        tutup.setAllCaps(false);

        root.addView(tutup);

        dialog.setView(root);

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
                                nama +
                                        " " +
                                        kode
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

                daftar.addView(kosong);
            }
        };

        cari.addTextChangedListener(
                new TextWatcher() {

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
                            Editable s
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

                            daftar.addView(error);
                        }
                );

        tutup.setOnClickListener(
                v -> dialog.dismiss()
        );

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    (int)(
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels *
                                    0.95
                    ),
                    (int)(
                            getResources()
                                    .getDisplayMetrics()
                                    .heightPixels *
                                    0.85
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
                                                nama +
                                                        " " +
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
    // SCAN NOTA
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
    // HASIL OCR
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

                            if (namaPartInput != null) {

                                namaPartInput.setText(
                                        namaInput.getText()
                                                .toString()
                                );
                            }

                            if (hargaPokokInput != null) {

                                hargaPokokInput.setText(
                                        hargaInput.getText()
                                                .toString()
                                );
                            }

                            if (stokInput != null) {

                                stokInput.setText(
                                        qtyInput.getText()
                                                .toString()
                                );
                            }

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
    // FIRESTORE STRING
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

    // ============================================================
    // FIRESTORE LONG
    // ============================================================

    private long getLongField(
            DocumentSnapshot doc,
            String key
    ) {

        Object value =
                doc.get(key);

        if (value instanceof Number) {

            return (
                    (Number)value
            ).longValue();
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
    // FIRESTORE BOOLEAN
    // ============================================================

    private boolean getBooleanField(
            DocumentSnapshot doc,
            String key,
            boolean defaultValue
    ) {

        Object value =
                doc.get(key);

        if (value instanceof Boolean) {

            return (Boolean)value;
        }

        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(
                String.valueOf(value)
        );
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
    // RUPIAH
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

    // ============================================================
    // PERSEN
    // ============================================================

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
