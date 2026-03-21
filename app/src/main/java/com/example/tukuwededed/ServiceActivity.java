package com.example.tukuwededed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ServiceActivity extends AppCompatActivity {
    private EditText etLastOli, etLastBusi, etLastFilter, etLastCleaner;
    private TextView tvEstimasiOli;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        prefs = getSharedPreferences("ServicePrefs", MODE_PRIVATE);
        initViews();
        loadHistory();

        TextWatcher serviceWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                updateEstimasi();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etLastOli.addTextChangedListener(serviceWatcher);
        
        findViewById(R.id.btnSave).setOnClickListener(v -> saveToHistory());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etLastOli = findViewById(R.id.etLastOli);
        etLastBusi = findViewById(R.id.etLastBusi);
        etLastFilter = findViewById(R.id.etLastFilter);
        etLastCleaner = findViewById(R.id.etLastCleaner);
        tvEstimasiOli = findViewById(R.id.tvEstimasiOli);
    }

    private void updateEstimasi() {
        try {
            int km = Integer.parseInt(etLastOli.getText().toString());
            tvEstimasiOli.setText("Estimasi Ganti Selanjutnya: KM " + (km + 3000));
        } catch (Exception e) {
            tvEstimasiOli.setText("Estimasi Ganti Selanjutnya: +3000km");
        }
    }

    private void saveToHistory() {
        SharedPreferences.Editor editor = prefs.edit();
        String oli = etLastOli.getText().toString();
        String busi = etLastBusi.getText().toString();
        String filter = etLastFilter.getText().toString();
        String cleaner = etLastCleaner.getText().toString();
        
        editor.putString("last_oli", oli);
        editor.putString("last_busi", busi);
        editor.putString("last_filter", filter);
        editor.putString("last_cleaner", cleaner);

        String history = prefs.getString("history", "");
        String entry = String.format("\n• Oli: %s km | Busi: %s | Filter: %s | Cleaner: %s", oli, busi, filter, cleaner);
        editor.putString("history", history + entry);
        
        editor.apply();
        Toast.makeText(this, "Service Log Saved!", Toast.LENGTH_SHORT).show();
    }

    private void showHistory() {
        String history = prefs.getString("history", "Belum ada history service.");
        new AlertDialog.Builder(this).setTitle("History Service").setMessage(history)
                .setPositiveButton("OK", null)
                .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("history").apply())
                .show();
    }

    private void loadHistory() {
        etLastOli.setText(prefs.getString("last_oli", ""));
        etLastBusi.setText(prefs.getString("last_busi", ""));
        etLastFilter.setText(prefs.getString("last_filter", ""));
        etLastCleaner.setText(prefs.getString("last_cleaner", ""));
        updateEstimasi();
    }
}