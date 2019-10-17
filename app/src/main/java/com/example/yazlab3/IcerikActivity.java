package com.example.yazlab3;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;


public class IcerikActivity extends AppCompatActivity {
    TextView textView;
    public static Button btnIcerikKonum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icerik);
        btnIcerikKonum = findViewById(R.id.btnIcerikKonum);
        textView = findViewById(R.id.IcerikTx);

        double enlem = getIntent().getDoubleExtra("enlem",0);
        double boylam = getIntent().getDoubleExtra("boylam",0);

        String text = getIntent().getStringExtra("ad") + "\n\n"
                + enlem + "\n\n" + boylam + "\n\n"
                + getIntent().getStringExtra("baslik") + "\n\n"
                + getIntent().getStringExtra("icerik") + "\n\n"
                + getIntent().getStringExtra("katagori") + "\n\n"
                + getIntent().getIntExtra("süre",0)+" gün";
        textView.setText(text);

        btnIcerikKonum.setOnClickListener(v -> {
            Intent intent = new Intent(IcerikActivity.this,AnaActivity.class);
            intent.putExtra("enlem",enlem);
            intent.putExtra("boylam",boylam);
            startActivity(intent);
        });
    }
}