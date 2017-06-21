package com.example.parthnaik.beerbuddy;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

public class mapLocate  extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double[] lat, lng,blat,blng;

    String[] UName,bName;
    Random r=new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_locate);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent i = getIntent();
        Bundle extras = i.getExtras();

        if (extras != null) {
            lat = i.getDoubleArrayExtra("lat");
            lng = i.getDoubleArrayExtra("lng");
            UName = i.getStringArrayExtra("Uname");
            blat = i.getDoubleArrayExtra("latit");
            blng = i.getDoubleArrayExtra("longit");
            bName = i.getStringArrayExtra("bName");


            Log.e("bName",""+bName);
            Log.e("ltt", "" + lat);

        }
        for (int j = 0; j < bName.length; j++) {

            Log.e("ltt" + j, "" + blat);
            mMap.addMarker(new MarkerOptions().position(new LatLng(blat[j], blng[j])).title(bName[j]).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.beer_bar)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(blat[j], blng[j])));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(blat[0], blng[0]), 12.0f));

        }
        for (int j = 0; j < UName.length; j++) {

            Log.e("ltt" + j, "" + lat);
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat[j], lng[j])).title(UName[j]).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat[j], lng[j])));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat[0], lng[0]), 12.0f));

        }

    }
}