package com.example.tukuwededed;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.cardVehicle).setOnClickListener(v -> startActivity(new Intent(this, VehicleActivity.class)));
        findViewById(R.id.cardDriver).setOnClickListener(v -> startActivity(new Intent(this, DriverActivity.class)));
        findViewById(R.id.cardSpec).setOnClickListener(v -> startActivity(new Intent(this, SpecActivity.class)));
        findViewById(R.id.cardRaceway).setOnClickListener(v -> startActivity(new Intent(this, RacewayActivity.class)));
        findViewById(R.id.cardService).setOnClickListener(v -> startActivity(new Intent(this, ServiceActivity.class)));
        findViewById(R.id.cardFuel).setOnClickListener(v -> startActivity(new Intent(this, FuelActivity.class)));
        findViewById(R.id.cardJjs).setOnClickListener(v -> startActivity(new Intent(this, JjsActivity.class)));
        findViewById(R.id.cardCredits).setOnClickListener(v -> startActivity(new Intent(this, CreditsActivity.class)));

        findViewById(R.id.cardRTMC).setOnClickListener(v -> {
            android.net.Uri uri = android.net.Uri.parse("https://bsw.kotabogor.go.id/cctv");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }
}