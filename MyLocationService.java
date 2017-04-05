package com.example.test.googlemapdemo;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;



public class MyLocationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    int mStartMode;
    public GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;
    double mlat, mlong;
    String mUserRemainderLocation;
    String mCurrentLocation;
    double mRemainderLatitude, mRemainderLongitude;
    public static String GEOFENCE_ID = "myGeoFence";
    public String geoFenceResult;
    /** interface for clients that bind */
    IBinder mBinder;

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    /** Called when the service is being created. */
    @Override
    public void onCreate() {

    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        mUserRemainderLocation = intent.getStringExtra("myLocation");
        mRemainderLatitude = intent.getDoubleExtra("myLatitude", 16.30);
        mRemainderLongitude = intent.getDoubleExtra("myLongitude", 80.45);
        Toast.makeText(this, "Service Started" + mUserRemainderLocation, Toast.LENGTH_LONG).show();
       // geoFenceResult = intent.getStringExtra("result");
        Toast.makeText(this,"Flag from GeoFenceService"+geoFenceResult,Toast.LENGTH_LONG).show();
      //  serviceStatus(geoFenceResult);
        googleApiClientConnection();
        return mStartMode;

    }

    //    public class MyLocationBinder extends Binder {
//        MyLocationService getBinder()
//        {
//            return MyLocationService.this;
//        }
//    }

//    public void serviceStatus(String result)
//    {
//        if(result == "Inside")
//        {
//            this.stopSelf();
//            Toast.makeText(this,"Service Stopped as Remainder Popped up",Toast.LENGTH_LONG).show();
//        }
//        else
//        {
//
//        }
//
//    }
    public void googleApiClientConnection() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient.connect();


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {

        String data = (String) intent.getExtras().get("User");
        Toast.makeText(this, "Data Recieved" + data, Toast.LENGTH_LONG).show();

    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

        Toast.makeText(this, "Location Changed " + location.toString(), Toast.LENGTH_SHORT).show();
        mlat = location.getLatitude();
        mlong = location.getLongitude();
        try {
            mCurrentLocation = returnAddress(mlat,mlong);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startGeoFenceMonitoring();
//        if(mUserRemainderLocation == mCurrentLocation)
//        {
//            Toast.makeText(this,"Location Matched",Toast.LENGTH_LONG).show();
//            this.stopSelf();
//        }


    }

    public String returnAddress(double latitude, double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String completeAddress = "  ";
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        String locality = null;
        StringBuilder returnCompleteAddress = new StringBuilder("");
        String returnAddress = null;
        if (addresses != null) {
            Address returnedAddress = addresses.get(0);
            for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                returnCompleteAddress.append(returnedAddress.getAddressLine(i)).append("\n");
            }
//            String address = addresses.get(0).getAddressLine(0);
//            locality = addresses.get(0).getLocality();
            returnAddress = returnCompleteAddress.toString();
        }
        return returnAddress;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
    }

    private void startGeoFenceMonitoring() {
        Geofence geoFenceObj = new Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(mRemainderLatitude, mRemainderLongitude, 200)
                .setExpirationDuration(Geofence.GEOFENCE_TRANSITION_EXIT)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geoFenceObj).build();

        Intent intent = new Intent(this, GeoFenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Google API Client is not connected");
        } else {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if(status.isSuccess())
                    {
                        Log.d(TAG,"Successfully added Geofence");

                    }
                    else
                    {
                        Log.d(TAG,"Adding Geofence Failed"+status.getStatus());
                    }
                }
            });
        }
    }


//    public class MyReciever extends BroadcastReceiver{
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(context,"OnReceive Method Invoked",Toast.LENGTH_LONG).show();
//        }
//    }
}
