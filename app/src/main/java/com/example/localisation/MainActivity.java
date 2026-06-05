package com.example.localisation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.localisation.network.VolleySingleton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOC = 100;
    private TextView tvLat, tvLon;
    private LocationManager locationManager;

    // À remplacer par l'IP de votre serveur (ex: 192.168.1.45)
    private final String INSERT_URL = "http://192.168.43.228/localisation/createPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);
        Button btnMap = findViewById(R.id.btnMap);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnMap.setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));

        askLocationPermissionAndStart();
    }

    private void askLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC);
        } else {
            startGpsUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startGpsUpdates() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60000,  // 60 secondes
                150,    // 150 mètres
                new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        double alt = location.getAltitude();
                        float acc = location.getAccuracy();

                        tvLat.setText("Latitude : " + lat);
                        tvLon.setText("Longitude : " + lon);

                        String msg = String.format(getString(R.string.new_location), lat, lon, alt, acc);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();

                        sendPositionToServer(lat, lon);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        String newStatus;
                        switch (status) {
                            case LocationProvider.OUT_OF_SERVICE:
                                newStatus = "HORS SERVICE";
                                break;
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                newStatus = "INDISPONIBLE TEMPORAIREMENT";
                                break;
                            case LocationProvider.AVAILABLE:
                                newStatus = "DISPONIBLE";
                                break;
                            default:
                                newStatus = "INCONNU";
                        }
                        String msg = String.format(getString(R.string.provider_new_status), provider, newStatus);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {
                        String msg = String.format(getString(R.string.provider_enabled), provider);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        String msg = String.format(getString(R.string.provider_disabled), provider);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void sendPositionToServer(double lat, double lon) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateStr = sdf.format(new Date());
        String imei = getDeviceIdentifier();

        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(lat));
        params.put("longitude", String.valueOf(lon));
        params.put("date", dateStr);
        params.put("imei", imei);

        StringRequest request = new StringRequest(Request.Method.POST, INSERT_URL,
                response -> {
                    // Réponse JSON (optionnel)
                },
                error -> Toast.makeText(MainActivity.this, "Erreur envoi : " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private String getDeviceIdentifier() {
        // Utiliser ANDROID_ID comme fallback (recommandé pour Android 10+)
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.trim().isEmpty()) {
            return androidId;
        }
        // Tentative IMEI (nécessite READ_PHONE_STATE)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String imei = tm.getDeviceId();
                if (imei != null && !imei.isEmpty()) return imei;
            }
        }
        return "UNKNOWN_DEVICE";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGpsUpdates();
        } else {
            Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(null);
        }
    }
}