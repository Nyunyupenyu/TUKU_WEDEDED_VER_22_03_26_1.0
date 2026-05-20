package com.example.tukuwededed;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class DriverActivity extends AppCompatActivity {
    private EditText etName, etBirth, etDomisili, etSimA, etSimC, etKta, etHeight, etWeight;
    private Spinner spnBlood;
    private RadioGroup rgGender;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        prefs = getSharedPreferences("DriverPrefs", MODE_PRIVATE);

        etName = findViewById(R.id.etDriverName);
        etBirth = findViewById(R.id.etBirthDate);
        spnBlood = findViewById(R.id.spnBloodType);
        etDomisili = findViewById(R.id.etDomisili);
        rgGender = findViewById(R.id.rgGender);
        etSimA = findViewById(R.id.etSimA);
        etSimC = findViewById(R.id.etSimC);
        etKta = findViewById(R.id.etKta);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);

        String[] bloodTypes = {"A", "B", "AB", "O"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bloodTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnBlood.setAdapter(adapter);

        loadData();

        findViewById(R.id.btnSaveDriver).setOnClickListener(v -> saveData());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
    }

    private void loadData() {
        etName.setText(prefs.getString("name", ""));
        etBirth.setText(prefs.getString("birth", ""));
        etDomisili.setText(prefs.getString("domisili", ""));
        etSimA.setText(prefs.getString("simA", ""));
        etSimC.setText(prefs.getString("simC", ""));
        etKta.setText(prefs.getString("kta", ""));
        etHeight.setText(prefs.getString("height", ""));
        etWeight.setText(prefs.getString("weight", ""));
        
        String blood = prefs.getString("blood", "A");
        for (int i = 0; i < spnBlood.getCount(); i++) {
            if (spnBlood.getItemAtPosition(i).equals(blood)) {
                spnBlood.setSelection(i);
                break;
            }
        }

        if (prefs.getString("gender", "Laki-laki").equals("Laki-laki")) {
            ((RadioButton)findViewById(R.id.rbMale)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.rbFemale)).setChecked(true);
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", etName.getText().toString());
        editor.putString("birth", etBirth.getText().toString());
        editor.putString("domisili", etDomisili.getText().toString());
        editor.putString("simA", etSimA.getText().toString());
        editor.putString("simC", etSimC.getText().toString());
        editor.putString("kta", etKta.getText().toString());
        editor.putString("height", etHeight.getText().toString());
        editor.putString("weight", etWeight.getText().toString());
        editor.putString("blood", spnBlood.getSelectedItem().toString());
        
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedGenderId);
        if (rb != null) editor.putString("gender", rb.getText().toString());

        editor.apply();
        Toast.makeText(this, "Driver Info Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}