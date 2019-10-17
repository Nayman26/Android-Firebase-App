package com.example.yazlab3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

public class KampanyaActivity extends AppCompatActivity {
    FirmaAdapter adapter;
    private String seciliKatagori;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kampanya);
        Button btnFiltrele = findViewById(R.id.Filtrele);
        Button btnFiltreleAction = findViewById(R.id.btnKaydet);
        EditText mesafeTx = findViewById(R.id.edtMesafe);
        EditText adTx = findViewById(R.id.edtAd);
        LinearLayout filtreleLayout = findViewById(R.id.Filtrele_layout);
        ListView lv = findViewById(R.id.list);
        ArrayList<Firma> firmaList = new ArrayList<>(AnaActivity.kampanyalar);

        String[] katagoriler = {"Seçiniz", "Fakülte", "Restoran", "Fastfood", "Kafe", "AVM", "Hastane", "Eczane"};
        ArrayAdapter<String> katagoriAdapter = new ArrayAdapter<>(this, R.layout.katagori, katagoriler);
        Spinner katagoriSp = findViewById(R.id.spnKategori);
        katagoriSp.setAdapter(katagoriAdapter);
        adapter = new FirmaAdapter(getApplicationContext(), R.layout.firmabox, firmaList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener((adapterView, view, position, id) -> {
            Firma firma = firmaList.get(position);
            Intent intent = new Intent(KampanyaActivity.this, IcerikActivity.class);
            intent.putExtra("ad", firma.getFirmaAdı());
            intent.putExtra("enlem", firma.getEnlem());
            intent.putExtra("boylam", firma.getBoylam());
            intent.putExtra("baslik", firma.getKampanyaBaslik());
            intent.putExtra("icerik", firma.getKampanyaIcerik());
            intent.putExtra("katagori", firma.getKatagori());
            intent.putExtra("süre", firma.getKampanyaSuresi());
            startActivity(intent);
        });

        btnFiltrele.setOnClickListener(view -> {
            if (filtreleLayout.getVisibility() == View.VISIBLE)
                filtreleLayout.setVisibility(View.GONE);
            else if (filtreleLayout.getVisibility() == View.GONE) {
                filtreleLayout.setVisibility(View.VISIBLE);
            }
        });

        katagoriSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                seciliKatagori = katagoriSp.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Intent intent = getIntent();
        LatLng konum = new LatLng(intent.getDoubleExtra("Lat", 0.0), intent.getDoubleExtra("Lon", 0.0));
        btnFiltreleAction.setOnClickListener(view -> {
            ArrayList<Firma> _firmaList = new ArrayList<>(AnaActivity.kampanyalar);
            int i = 0;
            while (i < _firmaList.size()){
                Firma firma = _firmaList.get(i);
                if((mesafeTx.getText().length() > 0 && konum.distanceTo
                        (new LatLng(firma.getEnlem(), firma.getBoylam())) > Integer.parseInt(mesafeTx.getText().toString()))
                        || (adTx.getText().length() > 0 && !adTx.getText().toString().equals(firma.getFirmaAdı()))
                        || (!seciliKatagori.equals("Seçiniz") && !seciliKatagori.equals(firma.getKatagori())))
                    _firmaList.remove(firma);
                else
                    i++;
            }

//            Log.e("AAAA", ""+firmaList.size());
//            for (Firma f : _firmaList) {
//                Log.e("AAAAAAAAAA", f.getFirmaAdı());
//
//            }
            adapter = new FirmaAdapter(getApplicationContext(), R.layout.firmabox, _firmaList);
            lv.setAdapter(adapter);
        });
    }

}
