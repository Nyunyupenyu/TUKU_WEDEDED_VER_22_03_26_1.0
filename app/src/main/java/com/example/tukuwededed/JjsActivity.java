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
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

        etHargaBensin.addTextChangedListener(new MoneyTextWatcher(etHargaBensin));
        etParkirAwal.addTextChangedListener(new MoneyTextWatcher(etParkirAwal));
        etParkirPerJam.addTextChangedListener(new MoneyTextWatcher(etParkirPerJam));
        etItemPrice.addTextChangedListener(new MoneyTextWatcher(etItemPrice));

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
                int price = Integer.parseInt(etItemPrice.getText().toString().replaceAll("[^0-9]", ""));
                String entry = name + " x" + qty + " = Rp " + formatRupiah(String.valueOf(qty * price));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ivPhotoParkir.setImageBitmap(imageBitmap);
                ivPhotoParkir.setVisibility(View.VISIBLE);
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
            int harga = Integer.parseInt(etHargaBensin.getText().toString().replaceAll("[^0-9]", ""));
            double total = (km / ksm) * harga;
            tvHasilJJS.setText(String.format(Locale.getDefault(), "Estimasi Bensin: Rp %s", formatRupiah(String.format(Locale.getDefault(), "%.0f", total))));
        } catch (Exception e) { tvHasilJJS.setText("Estimasi Bensin: Rp 0"); }
    }

    private void hitungParkir() {
        try {
            int awal = Integer.parseInt(etParkirAwal.getText().toString().replaceAll("[^0-9]", ""));
            int perJam = Integer.parseInt(etParkirPerJam.getText().toString().replaceAll("[^0-9]", ""));
            int durasi = Integer.parseInt(etDurasiParkir.getText().toString());
            int total = awal + (perJam * Math.max(0, durasi - 1));
            tvHasilParkir.setText(String.format(Locale.getDefault(), "Total Parkir: Rp %s", formatRupiah(String.valueOf(total))));
        } catch (Exception e) { tvHasilParkir.setText("Total Parkir: Rp 0"); }
    }

    private String formatRupiah(String value) {
        if (value.isEmpty()) return "";
        try {
            double parsed = Double.parseDouble(value.replaceAll("[^0-9]", ""));
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
            return df.format(parsed);
        } catch (Exception e) { return value; }
    }

    private class MoneyTextWatcher implements TextWatcher {
        private final EditText editText;
        public MoneyTextWatcher(EditText editText) { this.editText = editText; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            editText.removeTextChangedListener(this);
            String formatted = formatRupiah(s.toString());
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }

    private void saveJjsToHistory() {
        try {
            JSONArray historyArray = new JSONArray(prefs.getString("history_json", "[]"));
            JSONObject entry = new JSONObject();
            entry.put("jarak", etJarakJJS.getText().toString());
            entry.put("konsumsi", etKonsumsiBBM.getText().toString());
            entry.put("harga", etHargaBensin.getText().toString());
            entry.put("parkirAwal", etParkirAwal.getText().toString());
            entry.put("parkirPerJam", etParkirPerJam.getText().toString());
            entry.put("durasiParkir", etDurasiParkir.getText().toString());
            
            JSONArray listArray = new JSONArray();
            for (String s : currentShoppingList) listArray.put(s);
            entry.put("shopping_list", listArray);
            
            entry.put("hasil_bensin", tvHasilJJS.getText().toString());
            entry.put("hasil_parkir", tvHasilParkir.getText().toString());
            entry.put("timestamp", new java.util.Date().toString());

            historyArray.put(entry);
            prefs.edit().putString("history_json", historyArray.toString()).apply();
            Toast.makeText(this, "JJS Saved to History!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving history", Toast.LENGTH_SHORT).show();
        }
    }

    private void showJjsHistory() {
        try {
            JSONArray historyArray = new JSONArray(prefs.getString("history_json", "[]"));
            if (historyArray.length() == 0) {
                Toast.makeText(this, "Belum ada history JJS.", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] items = new String[historyArray.length()];
            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject obj = historyArray.getJSONObject(i);
                items[i] = "JJS: " + obj.optString("jarak", "0") + "km | " + obj.optString("hasil_parkir", "Rp 0");
            }

            new AlertDialog.Builder(this)
                    .setTitle("History JJS (Klik untuk load)")
                    .setItems(items, (dialog, which) -> {
                        try {
                            loadFromJSONObject(historyArray.getJSONObject(which));
                        } catch (Exception e) {
                            Toast.makeText(this, "Error loading history item", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setPositiveButton("OK", null)
                    .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("history_json").apply())
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromJSONObject(JSONObject obj) {
        etJarakJJS.setText(obj.optString("jarak", ""));
        etKonsumsiBBM.setText(obj.optString("konsumsi", ""));
        etHargaBensin.setText(obj.optString("harga", ""));
        etParkirAwal.setText(obj.optString("parkirAwal", ""));
        etParkirPerJam.setText(obj.optString("parkirPerJam", ""));
        etDurasiParkir.setText(obj.optString("durasiParkir", ""));
        
        currentShoppingList.clear();
        JSONArray listArray = obj.optJSONArray("shopping_list");
        if (listArray != null) {
            for (int i = 0; i < listArray.length(); i++) {
                currentShoppingList.add(listArray.optString(i));
            }
        }
        updateShoppingListView();
        hitungBensin(); hitungParkir();
        Toast.makeText(this, "Data JJS Loaded!", Toast.LENGTH_SHORT).show();
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