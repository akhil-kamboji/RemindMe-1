package com.example.test.googlemapdemo;

import android.*;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LatLng point;
    double mapLat;
    double mapLon;
    MyLocationService mMyLocationService;
    String mreturnAddress;
    String mRemainderLocation;
    StringBuilder mIntentObject;
    private Marker marker;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
//        Intent serviceIntent = new Intent(this,MyLocationService.class);
//        serviceIntent.putExtra("myLocation", mRemainderLocation);
//        startService(serviceIntent);



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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                String completeAddress = null;
                point = latLng;
                mapLat = point.latitude;
                mapLon = point.longitude;

                Toast.makeText(MapsActivity.this,"OnMapClickMethod",Toast.LENGTH_LONG).show();
                try {
                    completeAddress = returnCompleteAddress(mapLat, mapLon);
                }catch (Exception e)
                {
                    Toast.makeText(MapsActivity.this,"Exception Occured",Toast.LENGTH_SHORT).show();
                }
                mMap.addMarker(new MarkerOptions().position(point).title(completeAddress));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15));


            }

        });
        Button setMe  = (Button)findViewById(R.id.button1);
        setMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this,"Button Clicked",Toast.LENGTH_SHORT).show();
                try {
                      mRemainderLocation = returnCompleteAddress(mapLat,mapLon);
//                    mIntentObject = new StringBuilder(" ");
//                    mIntentObject.append(mapLat);
//                    mIntentObject.append(mapLon);
                    Toast.makeText(MapsActivity.this,"Location is "+mRemainderLocation,Toast.LENGTH_SHORT).show();
                    Intent serviceIntent = new Intent(MapsActivity.this,MyLocationService.class);
                    serviceIntent.putExtra("myLocation", mRemainderLocation);
                    serviceIntent.putExtra("myLatitude",mapLat);
                    serviceIntent.putExtra("myLongitude",mapLon);
                    startService(serviceIntent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
               // startService(new Intent(getBaseContext(),MyLocationService.class));

            }
        });
    }


    private String returnCompleteAddress(double latitude,double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String completeAddress = "  ";
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        String locality = null;
        StringBuilder returnCompleteAddress = new StringBuilder("");
        mreturnAddress = null;
        if (addresses != null) {
            Address returnedAddress = addresses.get(0);
            for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                returnCompleteAddress.append(returnedAddress.getAddressLine(i)).append("\n");
            }
//            String address = addresses.get(0).getAddressLine(0);
//            locality = addresses.get(0).getLocality();
            mreturnAddress = returnCompleteAddress.toString();
        }
        return mreturnAddress;
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat, lon;
        lat = location.getLatitude();
        lon = location.getLongitude();
        LatLng mylatlng = new LatLng(lat, lon);
        Toast.makeText(this, "OnLocation method is invoked", Toast.LENGTH_LONG).show();
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
