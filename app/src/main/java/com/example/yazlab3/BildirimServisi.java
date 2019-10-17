package com.example.yazlab3;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BildirimServisi extends Service implements LocationListener {
    public LocationManager locationManager;
    private Handler handler = new Handler();
    private ArrayList<Firma> kampanyalar;
    private String kategoriler = "";
    private int mesafe = 150;


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            new Thread(() -> {
                kampanyalar = new ArrayList<>();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("Firmalar");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Firma firma = ds.getValue(Firma.class);
                            if (firma == null)
                                return;
                            boolean var = false;
                            for (Firma kampanya : kampanyalar) {
                                if (kampanya.getEnlem() == firma.getEnlem() && kampanya.getBoylam() == firma.getBoylam())
                                    var = true;
                            }
                            if (!var)
                                kampanyalar.add(firma);
                        }
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
            }).start();
            handler.postDelayed(runnable, 60000);
        }
    };

    @Override
    public int onStartCommand(Intent _intent, int flags, int startId) {
        Toast.makeText(this, "Servis Başladı", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return super.onStartCommand(_intent, flags, startId);
        }
        String uID = FirebaseAuth.getInstance().getUid();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        kategoriler = preferences.getString(uID+"_Kategoriler","");
        mesafe = preferences.getInt(uID+"_Mesafe", 150);
        handler.post(runnable);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, this);
        return super.onStartCommand(_intent, flags, startId);
    }

    private void bildirimYolla(Firma kampanya) {
        Intent intent = new Intent(BildirimServisi.this, IcerikActivity.class);
        intent.putExtra("ad", kampanya.getFirmaAdı());
        intent.putExtra("enlem",kampanya.getEnlem());
        intent.putExtra("boylam",kampanya.getBoylam());
        intent.putExtra("baslik",kampanya.getKampanyaBaslik());
        intent.putExtra("icerik",kampanya.getKampanyaIcerik());
        intent.putExtra("katagori",kampanya.getKatagori());
        intent.putExtra("süre",kampanya.getKampanyaSuresi());

        PendingIntent contentIntent = PendingIntent.getActivity(BildirimServisi.this, kampanya.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        @SuppressWarnings("deprecation") NotificationCompat.Builder b = new NotificationCompat.Builder(BildirimServisi.this);
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(kampanya.getKampanyaBaslik())
                .setContentText("Detaylar için tıkla!")
                .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(null)
                .setContentIntent(contentIntent)
                .setContentInfo("Kampanya");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(kampanya.hashCode(), b.build());
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        for (Firma kampanya : kampanyalar) {
            if (new LatLng(location).distanceTo(new LatLng(kampanya.getEnlem(),kampanya.getBoylam())) < mesafe){
                if(kategoriler.isEmpty() || kategoriler.contains(kampanya.getKatagori()))
                    bildirimYolla(kampanya);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}