package com.example.tukuwededed;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class FuelActivity extends AppCompatActivity {
    private EditText etDistance, etFuelAmount;
    private TextView tvResult;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel);

        prefs = getSharedPreferences("FuelLog", MODE_PRIVATE);
        etDistance = findViewById(R.id.etDistance);
        etFuelAmount = findViewById(R.id.etFuelAmount);
        tvResult = findViewById(R.id.tvResult);

        loadLastInput();

        TextWatcher fuelWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                hitungBbm();
                autoSave();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etDistance.addTextChangedListener(fuelWatcher);
        etFuelAmount.addTextChangedListener(fuelWatcher);

        findViewById(R.id.btnSave).setOnClickListener(v -> saveToHistory());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
    }

    private void autoSave() {
        prefs.edit()
            .putString("last_dist", etDistance.getText().toString())
            .putString("last_fuel", etFuelAmount.getText().toString())
            .apply();
    }

    private void hitungBbm() {
        try {
            double dist = Double.parseDouble(etDistance.getText().toString());
            double fuel = Double.parseDouble(etFuelAmount.getText().toString());
            if (fuel > 0) {
                double res = dist / fuel;
                tvResult.setText(String.format(Locale.getDefault(), "%.1f Km/L", res));
            }
        } catch (Exception e) { tvResult.setText("0.0 Km/L"); }
    }

    private void saveToHistory() {
        String history = prefs.getString("history", "");
        String entry = String.format(Locale.getDefault(), "\n• %s Km | %s L | Efisiensi: %s", 
                etDistance.getText().toString(), etFuelAmount.getText().toString(), tvResult.getText().toString());
        prefs.edit().putString("history", history + entry).apply();
        Toast.makeText(this, "Fuel Log Saved!", Toast.LENGTH_SHORT).show();
    }

    private void showHistory() {
        String history = prefs.getString("history", "Belum ada history BBM.");
        new AlertDialog.Builder(this).setTitle("History Konsumsi BBM").setMessage(history)
                .setPositiveButton("OK", null)
                .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("history").apply())
                .show();
    }

    private void loadLastInput() {
        etDistance.setText(prefs.getString("last_dist", ""));
        etFuelAmount.setText(prefs.getString("last_fuel", ""));
        hitungBbm();
    }
}