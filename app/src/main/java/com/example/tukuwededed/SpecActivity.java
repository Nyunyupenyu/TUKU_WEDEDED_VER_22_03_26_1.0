package com.example.tukuwededed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpecActivity extends AppCompatActivity {
    private EditText etVehicleName, etBore, etStroke, etVolumeKubah, etInjectorCC, etInjectorHole, etECU, etMapping, etNotes;
    private TextView tvHasilCC, tvHasilKompresi;
    private RadioGroup rgSilinder;
    private double currentCC = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spec);

        prefs = getSharedPreferences("SpecPrefs", MODE_PRIVATE);
        initViews();
        loadLastInput();

        TextWatcher specWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) { 
                hitungCC(); 
                hitungKompresi();
                autoSave();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        
        etVehicleName.addTextChangedListener(specWatcher);
        etBore.addTextChangedListener(specWatcher);
        etStroke.addTextChangedListener(specWatcher);
        etVolumeKubah.addTextChangedListener(specWatcher);
        etInjectorCC.addTextChangedListener(specWatcher);
        etInjectorHole.addTextChangedListener(specWatcher);
        etECU.addTextChangedListener(specWatcher);
        etMapping.addTextChangedListener(specWatcher);
        etNotes.addTextChangedListener(specWatcher);
        
        rgSilinder.setOnCheckedChangeListener((g, id) -> { 
            hitungCC(); 
            hitungKompresi();
            autoSave();
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> saveToHistory());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etVehicleName = findViewById(R.id.etVehicleName);
        tvHasilCC = findViewById(R.id.tvHasilCC); 
        tvHasilKompresi = findViewById(R.id.tvHasilKompresi);
        rgSilinder = findViewById(R.id.rgSilinder);
        etBore = findViewById(R.id.etBore); etStroke = findViewById(R.id.etStroke);
        etVolumeKubah = findViewById(R.id.etVolumeKubah);
        etInjectorCC = findViewById(R.id.etInjectorCC); etInjectorHole = findViewById(R.id.etInjectorHole);
        etECU = findViewById(R.id.etECU); etMapping = findViewById(R.id.etMapping);
        etNotes = findViewById(R.id.etNotes);
    }

    private void autoSave() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("v_name", etVehicleName.getText().toString());
        editor.putString("bore", etBore.getText().toString());
        editor.putString("stroke", etStroke.getText().toString());
        editor.putInt("cylId", rgSilinder.getCheckedRadioButtonId());
        editor.putString("vKubah", etVolumeKubah.getText().toString());
        editor.putString("injCC", etInjectorCC.getText().toString());
        editor.putString("injHole", etInjectorHole.getText().toString());
        editor.putString("ecu", etECU.getText().toString());
        editor.putString("mapping", etMapping.getText().toString());
        editor.putString("notes", etNotes.getText().toString());
        editor.apply();
    }

    private void hitungCC() {
        try {
            double b = Double.parseDouble(etBore.getText().toString());
            double s = Double.parseDouble(etStroke.getText().toString());
            int cyl = getCylCount();
            currentCC = (Math.PI * Math.pow(b/2, 2) * s * cyl) / 1000;
            tvHasilCC.setText(String.format(Locale.getDefault(), "%.1f CC", currentCC));
        } catch (Exception e) { tvHasilCC.setText("0 CC"); }
    }

    private void hitungKompresi() {
        try {
            double vKubah = Double.parseDouble(etVolumeKubah.getText().toString());
            double ccPerCyl = currentCC / getCylCount();
            double cr = (ccPerCyl + vKubah) / vKubah;
            tvHasilKompresi.setText(String.format(Locale.getDefault(), "Kompresi: %.1f:1", cr));
        } catch (Exception e) { tvHasilKompresi.setText("Kompresi: 0:1"); }
    }

    private int getCylCount() {
        int id = rgSilinder.getCheckedRadioButtonId();
        if(id == R.id.cyl2) return 2; if(id == R.id.cyl3) return 3; if(id == R.id.cyl4) return 4;
        return 1;
    }

    private void saveToHistory() {
        try {
            JSONArray historyArray = new JSONArray(prefs.getString("history_json", "[]"));
            JSONObject entry = new JSONObject();
            entry.put("v_name", etVehicleName.getText().toString());
            entry.put("bore", etBore.getText().toString());
            entry.put("stroke", etStroke.getText().toString());
            entry.put("cylId", rgSilinder.getCheckedRadioButtonId());
            entry.put("vKubah", etVolumeKubah.getText().toString());
            entry.put("injCC", etInjectorCC.getText().toString());
            entry.put("injHole", etInjectorHole.getText().toString());
            entry.put("ecu", etECU.getText().toString());
            entry.put("mapping", etMapping.getText().toString());
            entry.put("notes", etNotes.getText().toString());
            entry.put("cc", tvHasilCC.getText().toString());
            entry.put("timestamp", new java.util.Date().toString());

            historyArray.put(entry);
            prefs.edit().putString("history_json", historyArray.toString()).apply();
            Toast.makeText(this, "Spec Saved to History!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving history", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHistory() {
        try {
            JSONArray historyArray = new JSONArray(prefs.getString("history_json", "[]"));
            if (historyArray.length() == 0) {
                Toast.makeText(this, "Belum ada history.", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] items = new String[historyArray.length()];
            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject obj = historyArray.getJSONObject(i);
                items[i] = obj.optString("v_name", "Unit") + " | " + obj.optString("cc", "0 CC");
            }

            new AlertDialog.Builder(this)
                    .setTitle("History Engine Specs (Klik untuk load)")
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
        etVehicleName.setText(obj.optString("v_name", ""));
        etBore.setText(obj.optString("bore", ""));
        etStroke.setText(obj.optString("stroke", ""));
        rgSilinder.check(obj.optInt("cylId", R.id.cyl1));
        etVolumeKubah.setText(obj.optString("vKubah", ""));
        etInjectorCC.setText(obj.optString("injCC", ""));
        etInjectorHole.setText(obj.optString("injHole", ""));
        etECU.setText(obj.optString("ecu", ""));
        etMapping.setText(obj.optString("mapping", ""));
        etNotes.setText(obj.optString("notes", ""));
        hitungCC(); hitungKompresi();
        Toast.makeText(this, "Data Loaded!", Toast.LENGTH_SHORT).show();
    }

    private void loadLastInput() {
        etVehicleName.setText(prefs.getString("v_name", ""));
        etBore.setText(prefs.getString("bore", ""));
        etStroke.setText(prefs.getString("stroke", ""));
        rgSilinder.check(prefs.getInt("cylId", R.id.cyl1));
        etVolumeKubah.setText(prefs.getString("vKubah", ""));
        etInjectorCC.setText(prefs.getString("injCC", ""));
        etInjectorHole.setText(prefs.getString("injHole", ""));
        etECU.setText(prefs.getString("ecu", ""));
        etMapping.setText(prefs.getString("mapping", ""));
        etNotes.setText(prefs.getString("notes", ""));
        hitungCC(); hitungKompresi();
    }
}