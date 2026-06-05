package com.example.localisation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.localisation.network.VolleySingleton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String SHOW_URL = "http://192.168.43.228/localisation/showPositions.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadMarkers();
    }

    private void loadMarkers() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, SHOW_URL, null,
                response -> {
                    try {
                        JSONArray positions = response.getJSONArray("positions");
                        if (positions.length() == 0) {
                            Toast.makeText(this, "Aucune position enregistrée", Toast.LENGTH_SHORT).show();
                        }
                        for (int i = 0; i < positions.length(); i++) {
                            JSONObject pos = positions.getJSONObject(i);
                            double lat = pos.getDouble("latitude");
                            double lon = pos.getDouble("longitude");
                            String date = pos.getString("date");
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title(date));
                        }
                        // Centrer sur le premier marker si existant
                        if (positions.length() > 0) {
                            JSONObject first = positions.getJSONObject(0);
                            double lat = first.getDouble("latitude");
                            double lon = first.getDouble("longitude");
                            mMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12f));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur chargement : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}