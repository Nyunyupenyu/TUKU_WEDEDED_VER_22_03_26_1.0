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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JjsActivity extends AppCompatActivity {
    private EditText etJarakJJS, etKonsumsiBBM, etHargaBensin, etItemName, etItemQty, etItemPrice;
    private EditText etParkirAwal, etParkirPerJam, etDurasiParkir;
    private TextView tvHasilJJS, tvListBelanja, tvHasilParkir;
    private ImageView ivPhotoParkir;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;
    private SharedPreferences prefs;
    private List<String> currentShoppingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jjs);

        prefs = getSharedPreferences("JjsPrefs", MODE_PRIVATE);
        initViews();
        loadLastData();

        TextWatcher autoSaveWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { 
                hitungBensin(); hitungParkir(); autoSave();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etJarakJJS.addTextChangedListener(autoSaveWatcher);
        etKonsumsiBBM.addTextChangedListener(autoSaveWatcher);
        etHargaBensin.addTextChangedListener(autoSaveWatcher);
        etParkirAwal.addTextChangedListener(autoSaveWatcher);
        etParkirPerJam.addTextChangedListener(autoSaveWatcher);
        etDurasiParkir.addTextChangedListener(autoSaveWatcher);

        findViewById(R.id.btnCaptureParkir).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });

        findViewById(R.id.btnAddItem).setOnClickListener(v -> {
            try {
                String name = etItemName.getText().toString();
                int qty = Integer.parseInt(etItemQty.getText().toString());
                int price = Integer.parseInt(etItemPrice.getText().toString());
                String entry = name + " x" + qty + " = Rp " + (qty * price);
                currentShoppingList.add(entry);
                updateShoppingListView();
                autoSave();
                etItemName.setText(""); etItemQty.setText(""); etItemPrice.setText("");
            } catch (Exception e) {
                Toast.makeText(this, "Isi data belanja dulu, Nyu!", Toast.LENGTH_SHORT).show();
            }
        });

        tvListBelanja.setOnClickListener(v -> {
            if (currentShoppingList.isEmpty()) return;
            showRemoveItemDialog();
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> saveJjsToHistory());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showJjsHistory());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void autoSave() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("jarak", etJarakJJS.getText().toString());
        editor.putString("konsumsi", etKonsumsiBBM.getText().toString());
        editor.putString("harga", etHargaBensin.getText().toString());
        editor.putString("parkirAwal", etParkirAwal.getText().toString());
        editor.putString("parkirPerJam", etParkirPerJam.getText().toString());
        editor.putString("durasiParkir", etDurasiParkir.getText().toString());
        
        StringBuilder sb = new StringBuilder();
        for (String s : currentShoppingList) sb.append(s).append(";");
        editor.putString("temp_list", sb.toString());
        editor.apply();
    }

    private void updateShoppingListView() {
        StringBuilder sb = new StringBuilder("Daftar Belanja (Klik untuk hapus):");
        for (String item : currentShoppingList) {
            sb.append("\n• ").append(item);
        }
        tvListBelanja.setText(sb.toString());
    }

    private void showRemoveItemDialog() {
        String[] items = currentShoppingList.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Hapus Item Belanja")
                .setItems(items, (dialog, which) -> {
                    currentShoppingList.remove(which);
                    updateShoppingListView();
                    autoSave();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Aplikasi Kamera tidak ditemukan.", Toast.LENGTH_LONG).show();
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
        String history = prefs.getString("history", "");
        StringBuilder sb = new StringBuilder();
        for(String s : currentShoppingList) sb.append("\n   - ").append(s);
        
        String entry = String.format("\n• Session JJS: %s | Parkir: %s\n  Belanja: %s", 
                tvHasilJJS.getText().toString(), tvHasilParkir.getText().toString(), sb.toString());
        prefs.edit().putString("history", history + entry).apply();
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
        
        String savedList = prefs.getString("temp_list", "");
        if (!savedList.isEmpty()) {
            String[] items = savedList.split(";");
            for (String s : items) if(!s.isEmpty()) currentShoppingList.add(s);
            updateShoppingListView();
        }
        
        hitungBensin(); hitungParkir();
    }
}