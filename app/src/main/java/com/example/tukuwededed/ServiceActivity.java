package com.example.tukuwededed;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ServiceActivity extends AppCompatActivity {
    private EditText etLastOli, etDateOli, etLastBusi, etLastFilter, etLastCleaner;
    private TextView tvEstimasiOli, tvEstimasiBusi, tvEstimasiFilter, tvEstimasiCleaner;
    private SharedPreferences prefs;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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
        etDateOli.addTextChangedListener(serviceWatcher);
        etLastBusi.addTextChangedListener(serviceWatcher);
        etLastFilter.addTextChangedListener(serviceWatcher);
        etLastCleaner.addTextChangedListener(serviceWatcher);
        
        findViewById(R.id.btnSave).setOnClickListener(v -> saveToHistory());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etLastOli = findViewById(R.id.etLastOli);
        etDateOli = findViewById(R.id.etDateOli);
        etLastBusi = findViewById(R.id.etLastBusi);
        etLastFilter = findViewById(R.id.etLastFilter);
        etLastCleaner = findViewById(R.id.etLastCleaner);
        tvEstimasiOli = findViewById(R.id.tvEstimasiOli);
        tvEstimasiBusi = findViewById(R.id.tvEstimasiBusi);
        tvEstimasiFilter = findViewById(R.id.tvEstimasiFilter);
        tvEstimasiCleaner = findViewById(R.id.tvEstimasiCleaner);
    }

    private void updateEstimasi() {
        // Estimasi Oli (KM)
        try {
            int km = Integer.parseInt(etLastOli.getText().toString());
            tvEstimasiOli.setText("Estimasi Ganti: KM " + (km + 3000));
        } catch (Exception e) {
            tvEstimasiOli.setText("Estimasi Ganti: +3000km");
        }

        // Estimasi Tanggal (Oli, Busi, Filter, Cleaner)
        tvEstimasiOli.append(" atau " + getFutureDate(etDateOli.getText().toString(), 3));
        tvEstimasiBusi.setText("Estimasi Ganti Busi: " + getFutureDate(etLastBusi.getText().toString(), 6));
        tvEstimasiFilter.setText("Estimasi Ganti Filter: " + getFutureDate(etLastFilter.getText().toString(), 6));
        tvEstimasiCleaner.setText("Estimasi Cleaner: " + getFutureDate(etLastCleaner.getText().toString(), 6));
    }

    private String getFutureDate(String inputDate, int months) {
        try {
            Date date = sdf.parse(inputDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MONTH, months);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return "+" + months + " Bulan";
        }
    }

    private void saveToHistory() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_oli", etLastOli.getText().toString());
        editor.putString("date_oli", etDateOli.getText().toString());
        editor.putString("last_busi", etLastBusi.getText().toString());
        editor.putString("last_filter", etLastFilter.getText().toString());
        editor.putString("last_cleaner", etLastCleaner.getText().toString());

        String history = prefs.getString("history", "");
        String entry = String.format("\n• [%s] Oli: %s km | Busi: %s | Filter: %s | Cleaner: %s", 
                etDateOli.getText().toString(), etLastOli.getText().toString(), etLastBusi.getText().toString(), 
                etLastFilter.getText().toString(), etLastCleaner.getText().toString());
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
        etDateOli.setText(prefs.getString("date_oli", ""));
        etLastBusi.setText(prefs.getString("last_busi", ""));
        etLastFilter.setText(prefs.getString("last_filter", ""));
        etLastCleaner.setText(prefs.getString("last_cleaner", ""));
        updateEstimasi();
    }
}