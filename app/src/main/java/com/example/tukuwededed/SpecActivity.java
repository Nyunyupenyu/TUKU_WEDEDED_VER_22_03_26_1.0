package com.example.tukuwededed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class SpecActivity extends AppCompatActivity {
    private EditText etBore, etStroke, etVolumeKubah, etInjectorCC, etInjectorHole, etECU, etMapping, etNotes;
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
        loadLastInput(); // Load current values

        TextWatcher specWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) { 
                hitungCC(); 
                hitungKompresi();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        
        etBore.addTextChangedListener(specWatcher);
        etStroke.addTextChangedListener(specWatcher);
        etVolumeKubah.addTextChangedListener(specWatcher);
        rgSilinder.setOnCheckedChangeListener((g, id) -> { hitungCC(); hitungKompresi(); });

        findViewById(R.id.btnSave).setOnClickListener(v -> saveToHistory());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initViews() {
        tvHasilCC = findViewById(R.id.tvHasilCC); 
        tvHasilKompresi = findViewById(R.id.tvHasilKompresi);
        rgSilinder = findViewById(R.id.rgSilinder);
        etBore = findViewById(R.id.etBore); etStroke = findViewById(R.id.etStroke);
        etVolumeKubah = findViewById(R.id.etVolumeKubah);
        etInjectorCC = findViewById(R.id.etInjectorCC); etInjectorHole = findViewById(R.id.etInjectorHole);
        etECU = findViewById(R.id.etECU); etMapping = findViewById(R.id.etMapping);
        etNotes = findViewById(R.id.etNotes);
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
        SharedPreferences.Editor editor = prefs.edit();
        // Save current input to reload later
        editor.putString("bore", etBore.getText().toString());
        editor.putString("stroke", etStroke.getText().toString());
        editor.putInt("cylId", rgSilinder.getCheckedRadioButtonId());
        editor.putString("vKubah", etVolumeKubah.getText().toString());
        
        // Add to history string
        String history = prefs.getString("history", "");
        String entry = String.format(Locale.getDefault(), 
            "\n• %s | %.1f CC | CR %.1f:1 | ECU: %s", 
            etNotes.getText().toString().isEmpty() ? "Spec" : etNotes.getText().toString(),
            currentCC, 
            Double.parseDouble(tvHasilKompresi.getText().toString().replaceAll("[^0-9.]", "")),
            etECU.getText().toString());
        
        editor.putString("history", history + entry);
        editor.apply();
        Toast.makeText(this, "Spec Saved to History!", Toast.LENGTH_SHORT).show();
    }

    private void showHistory() {
        String history = prefs.getString("history", "Belum ada history.");
        new AlertDialog.Builder(this).setTitle("History Engine Specs").setMessage(history)
                .setPositiveButton("OK", null)
                .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("history").apply())
                .show();
    }

    private void loadLastInput() {
        etBore.setText(prefs.getString("bore", ""));
        etStroke.setText(prefs.getString("stroke", ""));
        rgSilinder.check(prefs.getInt("cylId", R.id.cyl1));
        etVolumeKubah.setText(prefs.getString("vKubah", ""));
        hitungCC(); hitungKompresi();
    }
}