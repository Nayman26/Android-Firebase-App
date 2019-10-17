package com.example.yazlab3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class AnaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    public static MapboxMap mapboxMap;
    public static ArrayList<Firma> kampanyalar = new ArrayList<>();
    private static LatLng konumum;
    private static int mesafe;
    private static final int RC_SIGN_IN = 1453;
    private static boolean DINLE = true;
    private static String kategoriler = "";
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoicmVtemlhdGF5IiwiYSI6ImNqcW02YTd5ZTA3N2U0Mm5vdjNraDloNDUifQ.dpHEM2_kdba2rY_dE0bcbQ");
        setContentView(R.layout.activity_ana);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        final AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_login)
                .setGoogleButtonId(R.id.btn_sign_in_google)
                .setEmailButtonId(R.id.btn_sign_in_email)
                .build();
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAuthMethodPickerLayout(customLayout)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .build(),
                        RC_SIGN_IN);
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            setLocationComponent(true);
            @SuppressLint("MissingPermission") Location asilKonum = mapboxMap.getLocationComponent().getLastKnownLocation();
            if (asilKonum != null) {
                konumGuncelle(new LatLng(asilKonum));
                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(konumum.getLatitude(), konumum.getLongitude())).zoom(15).build());
            } else {
                Toast.makeText(AnaActivity.this, "Konumunuza erişemedik!", Toast.LENGTH_LONG).show();
            }
        });
        configuration();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates("gps", 5000, 0, new LocationListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onLocationChanged(Location location) {
                    if (DINLE)
                        konumGuncelle(new LatLng(location));
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
            });
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(MapboxMap _mapboxMap) {
        mapboxMap = _mapboxMap;
        setLocationComponent(true);
        @SuppressLint("MissingPermission") Location asilKonum = mapboxMap.getLocationComponent().getLastKnownLocation();
        if (asilKonum != null) {
            Toast.makeText(AnaActivity.this, "Konumunuza erişildi!", Toast.LENGTH_LONG).show();
            konumGuncelle(new LatLng(asilKonum));
        }

        Objects.requireNonNull(mapboxMap.getLayer("poi-label")).setProperties(textField("{name}"));


        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(40.821645, 29.923239)).zoom(15).build());

        double enlem = getIntent().getDoubleExtra("enlem",0);
        double boylam = getIntent().getDoubleExtra("boylam",0);

        if(enlem!=0 && boylam!=0) {
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(enlem, boylam)).zoom(15).build());
        }

        mapboxMap.addOnMapLongClickListener(point -> {
            setLocationComponent(false);
            konumGuncelle(point);
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                    mapboxMap.addMarker(new MarkerOptions().position(new LatLng(firma.getEnlem(), firma.getBoylam()))
                            .setTitle(firma.getFirmaAdı()));
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ana_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemFiltrele:
                Intent intent = new Intent(AnaActivity.this, KampanyaActivity.class);
                intent.putExtra("Lat", konumum.getLatitude());
                intent.putExtra("Lon", konumum.getLongitude());
                startActivity(intent);
                break;
            case R.id.itemCikis:
                AuthUI.getInstance().signOut(AnaActivity.this);
                break;
            case R.id.itemAyarlar:
                Intent intent2 = new Intent(AnaActivity.this, AyarlarActivity.class);
                startActivity(intent2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    private void setLocationComponent(boolean aktif) {
            DINLE = aktif;
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this);
            locationComponent.setLocationComponentEnabled(aktif);
    }

    private void konumGuncelle(LatLng point) {
        konumum = new LatLng(point);

        for (Polygon a : mapboxMap.getPolygons()) {
            mapboxMap.removePolygon(a);
        }
        mapboxMap.addPolygon(generatePerimeter(konumum, mesafe));
        kontrol();
    }

    private void kontrol() {
        for (Firma kampanya : kampanyalar) {
            if (konumum.distanceTo(new LatLng(kampanya.getEnlem(), kampanya.getBoylam())) < mesafe) {
                if (kategoriler.isEmpty() || kategoriler.contains(kampanya.getKatagori()))
                    bildirimYolla(kampanya);
            }
        }
    }

    private void bildirimYolla(Firma kampanya) {
        Intent intent = new Intent(AnaActivity.this, IcerikActivity.class);
        intent.putExtra("ad", kampanya.getFirmaAdı());
        intent.putExtra("enlem", kampanya.getEnlem());
        intent.putExtra("boylam", kampanya.getBoylam());
        intent.putExtra("baslik", kampanya.getKampanyaBaslik());
        intent.putExtra("icerik", kampanya.getKampanyaIcerik());
        intent.putExtra("katagori", kampanya.getKatagori());
        intent.putExtra("süre", kampanya.getKampanyaSuresi());

        PendingIntent contentIntent = PendingIntent.getActivity(AnaActivity.this, kampanyalar.indexOf(kampanya), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        @SuppressWarnings("deprecation") NotificationCompat.Builder b = new NotificationCompat.Builder(AnaActivity.this);
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(kampanya.getKampanyaBaslik())
                .setContentText("Detaylar için tıkla!")
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Kampanya");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(kampanyalar.indexOf(kampanya), b.build());
        }
    }

    private PolygonOptions generatePerimeter(LatLng merkez, double yaricap) {
        yaricap /= 1000;
        List<LatLng> positions = new ArrayList<>();
        double distanceX = yaricap / (111.319 * Math.cos(merkez.getLatitude() * Math.PI / 180));
        double distanceY = yaricap / 110.574;

        double slice = (2 * Math.PI) / 64;

        double theta;
        double x;
        double y;
        LatLng position;
        for (int i = 0; i < 64; ++i) {
            theta = i * slice;
            x = distanceX * Math.cos(theta);
            y = distanceY * Math.sin(theta);

            position = new LatLng(merkez.getLatitude() + y,
                    merkez.getLongitude() + x);
            positions.add(position);
        }
        return new PolygonOptions()
                .addAll(positions)
                .fillColor(Color.BLUE)
                .alpha(0.4f);
    }

    public void configuration() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                    , 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configuration();
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        mapView.onResume();
        String uID = FirebaseAuth.getInstance().getUid();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        kategoriler = preferences.getString(uID + "_Kategoriler", "");
        mesafe = preferences.getInt(uID + "_Mesafe", 150);
        if (konumum != null) {
            for (Polygon a : mapboxMap.getPolygons()) {
                mapboxMap.removePolygon(a);
            }
            mapboxMap.addPolygon(generatePerimeter(konumum, mesafe));
        }
        Intent servis = new Intent(this, BildirimServisi.class);
        stopService(servis);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFirebaseAuth != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        Intent servis = new Intent(this, BildirimServisi.class);
        startService(servis);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
