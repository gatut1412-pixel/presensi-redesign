/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ekosp.indoweb.epesantren.locator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ekosp.indoweb.epesantren.MainActivity;
import com.ekosp.indoweb.epesantren.R;
import com.ekosp.indoweb.epesantren.helper.GlobalVar;
import com.ekosp.indoweb.epesantren.helper.NewGPSTracker;
import com.ekosp.indoweb.epesantren.helper.SessionManager;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.ekosp.indoweb.epesantren.model.DataUser;
import com.ekosp.indoweb.epesantren.model.LocationModel;
import com.ekosp.indoweb.epesantren.upload.UploadImage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MyLocationActivity extends AppCompatActivity implements
        OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private MarkerOptions options = new MarkerOptions();
    private MarkerOptions markerAnda = new MarkerOptions();
    private ArrayList<LatLng> latlngs = new ArrayList<>();
    private static final String TAG = MyLocationActivity.class.getSimpleName();
    private int radius_lokasi = 100; // default dalam meter
    private float jarak;

    private SessionManager session;
    private DataUser dataUser;
    private DataPonpes dataPonpes;
    private boolean validasiLokasi = true;
    private Location _lokasi_tujuan;
    private String TYPE;
    private String nama_lokasi_tujuan;
    private Circle circle;
    private List<Address> addresses;

    private LocationModel locationModel;

    Location mLastLocation;
    Marker mCurrLocationMarker;

    // GPSTracker gps;
    private NewGPSTracker tracker;
    private boolean mLocationPermissionGranted = false;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);

        session = new SessionManager(this);
        tracker = new NewGPSTracker(this);
        geocoder = new Geocoder(this);


//        if (locationModel == null) {
//            locationModel = new LocationModel(0.0,0.0);
//        }

        Intent intent = getIntent();
        if (intent != null) TYPE = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI);

        TextView btnCA = (TextView) findViewById(R.id.info_hdr_absen);
        btnCA.setText("Absen "+TYPE);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        options.position(latlngs.get(0));
        options.title("LOKASI ABSENSI");
        options.snippet(nama_lokasi_tujuan);
        mMap.addMarker(options);

        CircleOptions circleoptions = new CircleOptions()
                .center(latlngs.get(0))
                .radius(radius_lokasi)
                .strokeWidth(2)
                //.fillColor(this.getResources().getColor(R.color.iron))
                .strokeColor(this.getResources().getColor(R.color.red));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlngs.get(0)));

        circle = mMap.addCircle(circleoptions.center(latlngs.get(0)).radius(500.0));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(circleoptions.getCenter(), 15.0f));
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                Toast.makeText(getApplicationContext(), latLng.toString(), Toast.LENGTH_LONG).show();
            }
        });

        //To setup location manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //To request location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);

        if (locationManager != null) {
            mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mLastLocation != null) {
                jarak = mLastLocation.distanceTo(_lokasi_tujuan);
                if (locationModel == null) {
                    locationModel = new LocationModel(0.0,0.0);
                }
                locationModel.setLatitude(mLastLocation.getLatitude());
                locationModel.setLongitude(mLastLocation.getLongitude());

                //   LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                markerOptions.title("Posisi Anda");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }
                mCurrLocationMarker = mMap.addMarker(markerOptions);
            }
        }


    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Jarak dari lokasi absen: " + getReadableDistance(jarak), Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Jarak dari lokasi absen: " + getReadableDistance(jarak), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        if (locationModel == null) {
            locationModel = new LocationModel(0.0,0.0);
        }

        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }


    }

    public void backToHome (View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onLocationChanged(Location location) {

        //To hold location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //To create marker in map
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Posisi Anda");
        //adding marker to the map

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //opening position with some zoom level in the map
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
        mLastLocation = location;
        jarak = mLastLocation.distanceTo(_lokasi_tujuan);
        if (locationModel == null) {
            if (mLastLocation != null)
                locationModel = new LocationModel(0.0,0.0);
        }
        locationModel.setLatitude(mLastLocation.getLatitude());
        locationModel.setLongitude(mLastLocation.getLongitude());

        try {
            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude(),1);
            String address = addresses.get(0).getAddressLine(0);
            TextView btm_map_txt = (TextView) findViewById(R.id.btm_txt_map);
            btm_map_txt.setText(dataUser.getLokasi()+" "+dataPonpes.getNamaPonpes()+" ,"+address);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void check(View v) {
        if (isLocationModelEmpty())
            Toast.makeText(this, "Data real lokasi Anda tidak valid\nPastikan GPS anda aktif", Toast.LENGTH_SHORT).show();
        else if ((jarak < radius_lokasi) || !validasiLokasi) {
//        else if ((jarak != radius_lokasi) || !validasiLokasi) {
            Toast.makeText(this, "Anda di dalam radius lokasi " + TYPE + "\nJarak anda: " + getReadableDistance(jarak), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, UploadImage.class);
            intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE);
            intent.putExtra(GlobalVar.PARAM_LAST_LOCATION, locationModel);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            Toast.makeText(this, "Anda masih di luar radius " + TYPE + "\nJarak absen anda: " + getReadableDistance(jarak), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (locationModel == null) {
            locationModel = new LocationModel(0.0,0.0);
        }
        dataUser = session.getSessionDataUser();
        dataPonpes = session.getSessionDataPonpes();

        _lokasi_tujuan = new Location(LocationManager.GPS_PROVIDER);
        _lokasi_tujuan.setLatitude(dataUser.getLatitude());
        _lokasi_tujuan.setLongitude(dataUser.getLongitude());

        radius_lokasi = Integer.valueOf(dataUser.getJarak_radius().equals("") ? "0" : dataUser.getJarak_radius());
        if (dataUser.getValidasi().equalsIgnoreCase("y")) {
            validasiLokasi = true;
        } else {
            validasiLokasi = false;
        }

        nama_lokasi_tujuan = dataUser.getLokasi();
        latlngs.add(new LatLng(dataUser.getLatitude(), dataUser.getLongitude())
        );

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private String getReadableDistance(float size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"Meter", "KM", "MM", "GM", "TM"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1000, digitGroups)) + " " + units[digitGroups];
    }

    private boolean isLocationModelEmpty() {
        if (mLastLocation != null) {
            if (locationModel == null) {
                locationModel = new LocationModel(0.0,0.0);
            }
            if (locationModel.getLatitude() == null )
                locationModel.setLatitude(mLastLocation.getLatitude());
            if (locationModel.getLongitude() == null)
                locationModel.setLongitude(mLastLocation.getLongitude());
        } else {
            return true;
        }

        return false;
    }

}
