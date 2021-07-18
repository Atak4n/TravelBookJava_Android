package com.br0ke.travelbookjava.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Insert;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.view.View;

import com.br0ke.travelbookjava.R;
import com.br0ke.travelbookjava.model.Place;
import com.br0ke.travelbookjava.roomdb.PlaceDao;
import com.br0ke.travelbookjava.roomdb.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.br0ke.travelbookjava.databinding.ActivityMapsBinding;
import com.google.android.material.datepicker.CompositeDateValidator;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double selectedLatitude;
    Double selectedLongitude;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    Place selectedPlace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        sharedPreferences = this.getSharedPreferences("com.br0ke.javamaps",MODE_PRIVATE);
        info=false;

        db = Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places")
                //.allowMainThreadQueries()
                .build();
        placeDao = db.placeDao();

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;

        binding.saveButton.setEnabled(false);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String intentInfo = intent.getStringExtra("info");

        if(intentInfo.equals("new")) {

            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.br0ke.travelbookjava",MODE_PRIVATE);
                    boolean trackBoolean = sharedPreferences.getBoolean("trackBoolean",false);
                    if (!trackBoolean){
                        LatLng userLocation = new LatLng(location.getLatitude(),location.getAltitude());
                        mMap.moveCamera((CameraUpdateFactory.newLatLngZoom(userLocation,15)));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();
                    }
                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null){
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }
                mMap.setMyLocationEnabled(true);
            }


        } else {

            mMap.clear();

            selectedPlace =(Place) intent.getSerializableExtra("place");

            LatLng latLng = new LatLng(selectedPlace.latitude,selectedPlace.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            binding.placeNameText.setText(selectedPlace.name);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

            }
        }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0){
            if(requestCode==1){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                    Intent intent = getIntent();
                    String info = intent.getStringExtra("info");
                    if(info.matches("new")){
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null){
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }

                    }
                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled(true);

    }

    public void save(View view){
        Place place = new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);
        //threading -> Main(UI), Default (CPU Intensive), IO (network, database)

        //placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe();

        compositeDisposable.add(placeDao.insert(place)
        .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handleResponse)
        );
    }

    private void handleResponse(){
        Intent intent = new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void delete(View view){

        if (selectedPlace != null){
            compositeDisposable.add(placeDao.delete(selectedPlace)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse)
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}