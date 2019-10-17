package com.example.yazlab3;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class AyarlarActivity extends AppCompatActivity {

    String seciliKatagori, _kategoriler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);
        EditText edtMesafe = findViewById(R.id.edtMesafe);
        Button btnEkle = findViewById(R.id.btnEkle);
        Button btnSifirla = findViewById(R.id.btnSifirla);
        Button btnKaydet = findViewById(R.id.btnKaydet);
        String[] kategoriler = {"Fakülte", "Restoran", "Fastfood", "Kafe", "AVM", "Hastane", "Eczane"};
        seciliKatagori = kategoriler[0];
        ArrayAdapter<String> katagoriAdapter = new ArrayAdapter<>(this, R.layout.katagori, kategoriler);
        Spinner spnKategori = findViewById(R.id.spnKategori);
        spnKategori.setAdapter(katagoriAdapter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String uID = FirebaseAuth.getInstance().getUid();
        _kategoriler = preferences.getString(uID+"_Kategoriler","");
        spnKategori.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                seciliKatagori = spnKategori.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btnEkle.setOnClickListener(view -> {
            if(_kategoriler.contains(seciliKatagori))
                Toast.makeText(this, "Zaten Ekli", Toast.LENGTH_SHORT).show();
            else
                _kategoriler += ";" + seciliKatagori;
        });


        btnSifirla.setOnClickListener(view -> _kategoriler = "");
        btnKaydet.setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(uID+"_Kategoriler", _kategoriler);
            if (edtMesafe.getText().length() > 0)
                editor.putInt(uID+"_Mesafe", Integer.parseInt(edtMesafe.getText().toString()));
            editor.apply();
            Toast.makeText(this, "Ayarlar kaydedildi!", Toast.LENGTH_SHORT).show();
        });
    }
}
