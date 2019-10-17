package com.example.yazlab3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirmaActivity extends AppCompatActivity {
    EditText adTx,kBaslikTx,kIcerikTx, kSureTx,enlemTx, boylamTx;
    Spinner katagoriSp;
    Button btnEkle;
    FirebaseDatabase db;
    String seciliKatagori;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firma);
        adTx = findViewById(R.id.FirmaAdı);
        kBaslikTx = findViewById(R.id.KampanyaBaslik);
        kIcerikTx = findViewById(R.id.KampanyaIcerik);
        kSureTx = findViewById(R.id.KampanyaSuresi);
        enlemTx = findViewById(R.id.Enlem);
        boylamTx = findViewById(R.id.Boylam);
        btnEkle = findViewById(R.id.btnEkle);
        String[] katagoriler = {"Seçiniz","Fakülte","Restoran","Fastfood","Kafe","AVM","Hastane","Eczane"};
        ArrayAdapter<String> katagoriAdapter = new ArrayAdapter<>(this, R.layout.katagori, katagoriler);
        katagoriSp = findViewById(R.id.Katagori);
        katagoriSp.setAdapter(katagoriAdapter);
        katagoriSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                seciliKatagori=katagoriSp.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        db = FirebaseDatabase.getInstance();
        btnEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firmaAdi = adTx.getText().toString();
                String kampanyaBaslik = kBaslikTx.getText().toString();
                String kampanyaIcerik = kIcerikTx.getText().toString();
                double enlem = Double.parseDouble(enlemTx.getText().toString());
                double boylam = Double.parseDouble(boylamTx.getText().toString());
                int kampanyaSuresi = Integer.parseInt(kSureTx.getText().toString());
                DatabaseReference dbRef = db.getReference("Firmalar");
                String key = dbRef.push().getKey();
                DatabaseReference dbRefKey = db.getReference("Firmalar/" + key);
                dbRefKey.setValue(new Firma(firmaAdi, enlem, boylam, kampanyaBaslik, kampanyaIcerik, seciliKatagori, kampanyaSuresi));
                Toast.makeText(FirmaActivity.this, "Firma Eklendi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
