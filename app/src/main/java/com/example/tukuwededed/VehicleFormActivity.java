package com.example.tukuwededed;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;

public class VehicleFormActivity extends AppCompatActivity {
    private EditText etName, etType, etPlate, etReg, etFrame, etEngine, etBore, etStroke, etInjCC, etComp, etHead, etNoken, etKlep, etTB, etPerKopling, etECU, etMDepan, etMBelakang, etKDepan, etKBelakang, etSelang, etHandle, etBanD, etBanB, etAdd, etPajak, etTglPajak;
    private TextView tvAutoCC;
    private Spinner spnHoles;
    private Button btnSave, btnDelete;
    private SharedPreferences prefs;
    private int vehicleIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_form);

        prefs = getSharedPreferences("VehiclePrefs", MODE_PRIVATE);
        vehicleIndex = getIntent().getIntExtra("vehicle_index", -1);

        initViews();
        setupHoleSpinner();
        setupCCCalculation();

        if (vehicleIndex != -1) {
            loadVehicleData();
            btnDelete.setVisibility(View.VISIBLE);
        }

        btnSave.setOnClickListener(v -> saveVehicle());
        btnDelete.setOnClickListener(v -> deleteVehicle());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etName = findViewById(R.id.etName); etType = findViewById(R.id.etType); etPlate = findViewById(R.id.etPlate);
        etReg = findViewById(R.id.etRegisteredName); etFrame = findViewById(R.id.etFrameNum); etEngine = findViewById(R.id.etEngineNum);
        etBore = findViewById(R.id.etBore); etStroke = findViewById(R.id.etStroke); etInjCC = findViewById(R.id.etInjectorCC);
        etComp = findViewById(R.id.etCompression); etHead = findViewById(R.id.etHead); etNoken = findViewById(R.id.etNoken);
        etKlep = findViewById(R.id.etKlep); etTB = findViewById(R.id.etTB); etPerKopling = findViewById(R.id.etPerKopling);
        etECU = findViewById(R.id.etECU); etMDepan = findViewById(R.id.etMasterDepan); etMBelakang = findViewById(R.id.etMasterBelakang);
        etKDepan = findViewById(R.id.etKaliperDepan); etKBelakang = findViewById(R.id.etKaliperBelakang); etSelang = findViewById(R.id.etSelangRem);
        etHandle = findViewById(R.id.etHandleKopling); etBanD = findViewById(R.id.etBanDepan); etBanB = findViewById(R.id.etBanBelakang);
        etAdd = findViewById(R.id.etAdditional); etPajak = findViewById(R.id.etPajak); etTglPajak = findViewById(R.id.etTglPajak);
        tvAutoCC = findViewById(R.id.tvAutoCC); spnHoles = findViewById(R.id.spnHoles);
        btnSave = findViewById(R.id.btnSave); btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupHoleSpinner() {
        String[] holes = new String[20];
        for (int i = 1; i <= 20; i++) holes[i - 1] = i + " Hole";
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, holes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnHoles.setAdapter(adapter);
    }

    private void setupCCCalculation() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateCC(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etBore.addTextChangedListener(watcher);
        etStroke.addTextChangedListener(watcher);
    }

    private void calculateCC() {
        try {
            double bore = Double.parseDouble(etBore.getText().toString());
            double stroke = Double.parseDouble(etStroke.getText().toString());
            double cc = 0.785 * Math.pow(bore, 2) * stroke / 1000.0;
            tvAutoCC.setText(String.format(Locale.getDefault(), "%.1f CC", cc));
        } catch (Exception e) {
            tvAutoCC.setText("0.0 CC");
        }
    }

    private void loadVehicleData() {
        try {
            JSONArray array = new JSONArray(prefs.getString("vehicles", "[]"));
            JSONObject obj = array.getJSONObject(vehicleIndex);
            etName.setText(obj.optString("name")); etType.setText(obj.optString("type")); etPlate.setText(obj.optString("plate"));
            etReg.setText(obj.optString("reg")); etFrame.setText(obj.optString("frame")); etEngine.setText(obj.optString("engine"));
            etBore.setText(obj.optString("bore")); etStroke.setText(obj.optString("stroke")); etInjCC.setText(obj.optString("injCC"));
            etComp.setText(obj.optString("comp")); etHead.setText(obj.optString("head")); etNoken.setText(obj.optString("noken"));
            etKlep.setText(obj.optString("klep")); etTB.setText(obj.optString("tb")); etPerKopling.setText(obj.optString("perKopling"));
            etECU.setText(obj.optString("ecu")); etMDepan.setText(obj.optString("mDepan")); etMBelakang.setText(obj.optString("mBelakang"));
            etKDepan.setText(obj.optString("kDepan")); etKBelakang.setText(obj.optString("kBelakang")); etSelang.setText(obj.optString("selang"));
            etHandle.setText(obj.optString("handle")); etBanD.setText(obj.optString("banD")); etBanB.setText(obj.optString("banB"));
            etAdd.setText(obj.optString("add")); etPajak.setText(obj.optString("pajak")); etTglPajak.setText(obj.optString("tglPajak"));
            spnHoles.setSelection(obj.optInt("holes", 0));
            calculateCC();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void saveVehicle() {
        try {
            JSONArray array = new JSONArray(prefs.getString("vehicles", "[]"));
            JSONObject obj = (vehicleIndex != -1) ? array.getJSONObject(vehicleIndex) : new JSONObject();
            
            obj.put("name", etName.getText().toString()); obj.put("type", etType.getText().toString()); obj.put("plate", etPlate.getText().toString());
            obj.put("reg", etReg.getText().toString()); obj.put("frame", etFrame.getText().toString()); obj.put("engine", etEngine.getText().toString());
            obj.put("bore", etBore.getText().toString()); obj.put("stroke", etStroke.getText().toString()); obj.put("injCC", etInjCC.getText().toString());
            obj.put("comp", etComp.getText().toString()); obj.put("head", etHead.getText().toString()); obj.put("noken", etNoken.getText().toString());
            obj.put("klep", etKlep.getText().toString()); obj.put("tb", etTB.getText().toString()); obj.put("perKopling", etPerKopling.getText().toString());
            obj.put("ecu", etECU.getText().toString()); obj.put("mDepan", etMDepan.getText().toString()); obj.put("mBelakang", etMBelakang.getText().toString());
            obj.put("kDepan", etKDepan.getText().toString()); obj.put("kBelakang", etKBelakang.getText().toString()); obj.put("selang", etSelang.getText().toString());
            obj.put("handle", etHandle.getText().toString()); obj.put("banD", etBanD.getText().toString()); obj.put("banB", etBanB.getText().toString());
            obj.put("add", etAdd.getText().toString()); obj.put("pajak", etPajak.getText().toString()); obj.put("tglPajak", etTglPajak.getText().toString());
            obj.put("holes", spnHoles.getSelectedItemPosition());

            if (vehicleIndex == -1) array.put(obj);
            else array.put(vehicleIndex, obj);

            prefs.edit().putString("vehicles", array.toString()).apply();
            Toast.makeText(this, "Vehicle Saved!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void deleteVehicle() {
        try {
            JSONArray array = new JSONArray(prefs.getString("vehicles", "[]"));
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                if (i != vehicleIndex) newArray.put(array.get(i));
            }
            prefs.edit().putString("vehicles", newArray.toString()).apply();
            Toast.makeText(this, "Vehicle Deleted", Toast.LENGTH_SHORT).show();
            finish();
        } catch (JSONException e) { e.printStackTrace(); }
    }
}