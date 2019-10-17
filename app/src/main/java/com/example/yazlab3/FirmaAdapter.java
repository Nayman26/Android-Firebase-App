package com.example.yazlab3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class FirmaAdapter extends ArrayAdapter {
    private ArrayList<Firma> firmaList;
    private LayoutInflater inflater;

    @SuppressWarnings("unchecked")
     FirmaAdapter(Context context, int resource, ArrayList<Firma> firmaList) {
        super(context,resource,firmaList);
        this.firmaList=firmaList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @SuppressLint({"ViewHolder", "InflateParams","SetTextI18n"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView =inflater.inflate(R.layout.firmabox,null);
        TextView firmaAdi = convertView.findViewById(R.id.Firmabox_Ad);
        TextView kBaslik = convertView.findViewById(R.id.Firmabox_kbaslik);
        TextView kSure = convertView.findViewById(R.id.Firmabox_kSüre);
        firmaAdi.setText(firmaList.get(position).getFirmaAdı());
        kBaslik.setText(firmaList.get(position).getKampanyaBaslik());
        kSure.setText("Kampanya Süresi: "+firmaList.get(position).getKampanyaSuresi()+" gün");
        return convertView;
    }
}
