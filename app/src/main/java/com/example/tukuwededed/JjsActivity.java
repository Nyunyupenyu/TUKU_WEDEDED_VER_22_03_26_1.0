package com.example.tukuwededed;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Locale;

public class JjsActivity extends AppCompatActivity {
    private EditText etJarakJJS, etKonsumsiBBM, etHargaBensin, etItemName, etItemQty, etItemPrice;
    private EditText etParkirAwal, etParkirPerJam, etDurasiParkir;
    private TextView tvHasilJJS, tvListBelanja, tvHasilParkir;
    private ImageView ivPhotoParkir;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jjs);

        prefs = getSharedPreferences("JjsPrefs", MODE_PRIVATE);
        initViews();
        loadLastData();

        // 1. Logic Hitung Bensin Otomatis
        TextWatcher bensinWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { hitungBensin(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etJarakJJS.addTextChangedListener(bensinWatcher);
        etKonsumsiBBM.addTextChangedListener(bensinWatcher);
        etHargaBensin.addTextChangedListener(bensinWatcher);

        // 2. Logic Hitung Parkir
        TextWatcher parkirWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) { hitungParkir(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etParkirAwal.addTextChangedListener(parkirWatcher);
        etParkirPerJam.addTextChangedListener(parkirWatcher);
        etDurasiParkir.addTextChangedListener(parkirWatcher);

        // 3. Kamera Parkir dengan Permission Check & Query declaration
        findViewById(R.id.btnCaptureParkir).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });

        // 4. Tambah List Belanja
        findViewById(R.id.btnAddItem).setOnClickListener(v -> {
            try {
                String name = etItemName.getText().toString();
                int qty = Integer.parseInt(etItemQty.getText().toString());
                int price = Integer.parseInt(etItemPrice.getText().toString());
                String entry = "\n• " + name + " x" + qty + " = Rp " + (qty * price);
                tvListBelanja.append(entry);
                etItemName.setText(""); etItemQty.setText(""); etItemPrice.setText("");
            } catch (Exception e) {
                Toast.makeText(this, "Isi data belanja dulu, Nyu!", Toast.LENGTH_SHORT).show();
            }
        });

        // Save & History Buttons
        findViewById(R.id.btnSave).setOnClickListener(v -> saveJjsToHistory());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showJjsHistory());

        // 5. Navigasi
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Aplikasi Kamera tidak ditemukan. Cek permission atau install Kamera.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission Kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        etJarakJJS = findViewById(R.id.etJarakJJS);
        etKonsumsiBBM = findViewById(R.id.etKonsumsiBBM);
        etHargaBensin = findViewById(R.id.etHargaBensin);
        tvHasilJJS = findViewById(R.id.tvHasilJJS);
        etParkirAwal = findViewById(R.id.etParkirAwal);
        etParkirPerJam = findViewById(R.id.etParkirPerJam);
        etDurasiParkir = findViewById(R.id.etDurasiParkir);
        tvHasilParkir = findViewById(R.id.tvHasilParkir);
        ivPhotoParkir = findViewById(R.id.ivPhotoParkir);
        etItemName = findViewById(R.id.etItemName);
        etItemQty = findViewById(R.id.etItemQty);
        etItemPrice = findViewById(R.id.etItemPrice);
        tvListBelanja = findViewById(R.id.tvListBelanja);
    }

    private void hitungBensin() {
        try {
            double km = Double.parseDouble(etJarakJJS.getText().toString());
            double ksm = Double.parseDouble(etKonsumsiBBM.getText().toString());
            int harga = Integer.parseInt(etHargaBensin.getText().toString());
            double total = (km / ksm) * harga;
            tvHasilJJS.setText(String.format(Locale.getDefault(), "Estimasi Bensin: Rp %.0f", total));
        } catch (Exception e) { tvHasilJJS.setText("Estimasi Bensin: Rp 0"); }
    }

    private void hitungParkir() {
        try {
            int awal = Integer.parseInt(etParkirAwal.getText().toString());
            int perJam = Integer.parseInt(etParkirPerJam.getText().toString());
            int durasi = Integer.parseInt(etDurasiParkir.getText().toString());
            int total = awal + (perJam * Math.max(0, durasi - 1));
            tvHasilParkir.setText(String.format(Locale.getDefault(), "Total Parkir: Rp %d", total));
        } catch (Exception e) { tvHasilParkir.setText("Total Parkir: Rp 0"); }
    }

    private void saveJjsToHistory() {
        SharedPreferences.Editor editor = prefs.edit();
        // Save last input for reload
        editor.putString("jarak", etJarakJJS.getText().toString());
        editor.putString("konsumsi", etKonsumsiBBM.getText().toString());
        editor.putString("harga", etHargaBensin.getText().toString());
        editor.putString("parkirAwal", etParkirAwal.getText().toString());
        editor.putString("parkirPerJam", etParkirPerJam.getText().toString());
        editor.putString("durasiParkir", etDurasiParkir.getText().toString());

        // Save session result to history string
        String history = prefs.getString("history", "");
        String entry = String.format("\n• Bensin: %s | Parkir: %s | Belanja: %s", 
                tvHasilJJS.getText().toString(), 
                tvHasilParkir.getText().toString(),
                tvListBelanja.getText().toString().replace("Daftar Belanja:", ""));
        editor.putString("history", history + entry);
        editor.apply();
        Toast.makeText(this, "JJS Activity Saved!", Toast.LENGTH_SHORT).show();
    }

    private void showJjsHistory() {
        String history = prefs.getString("history", "Belum ada history JJS.");
        new AlertDialog.Builder(this).setTitle("History JJS").setMessage(history)
                .setPositiveButton("OK", null)
                .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("history").apply())
                .show();
    }

    private void loadLastData() {
        etJarakJJS.setText(prefs.getString("jarak", ""));
        etKonsumsiBBM.setText(prefs.getString("konsumsi", ""));
        etHargaBensin.setText(prefs.getString("harga", ""));
        etParkirAwal.setText(prefs.getString("parkirAwal", ""));
        etParkirPerJam.setText(prefs.getString("parkirPerJam", ""));
        etDurasiParkir.setText(prefs.getString("durasiParkir", ""));
        hitungBensin(); hitungParkir();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            ivPhotoParkir.setImageBitmap(imageBitmap);
            ivPhotoParkir.setVisibility(View.VISIBLE);
        }
    }
}