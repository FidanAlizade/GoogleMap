package com.example.newgooglemap.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.newgooglemap.R;
import com.example.newgooglemap.db.PlaceDao;
import com.example.newgooglemap.db.PlaceDatabase;
import com.example.newgooglemap.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.newgooglemap.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    ActivityResultLauncher<String> permissionLauncher;
    SharedPreferences sharedPreferences;
    Boolean info;
    Location lastLocation;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double selectedLatitude;
    Double selectedLongitude;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    Place notedPlace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();

        sharedPreferences = getSharedPreferences("com.example.newgooglemap", MODE_PRIVATE);

        db = Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class, "Places")
//                .allowMainThreadQueries()
                .build();
        placeDao = db.placeDao();
        binding.save.setEnabled(false);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();

        String incomingData =  intent.getStringExtra("info");
        if(incomingData.equals("new")){
            binding.save.setVisibility(View.VISIBLE);
            binding.delete.setVisibility(View.GONE);

            //This class(LocationManager) provides access to the system location services.
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        /*Used for receiving notifications when the device location has changed.
         These methods are called when the listener has been registered with the LocationManager. */
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
//          System.out.println("location: " + location.toString());
//
//                info = sharedPreferences.getBoolean("info",false);
//                if(!info){
//                    //Beləcə bu hissə hər dəfə təkrarlanmır
//                    LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,5));
//                    sharedPreferences.edit().putBoolean("info", true).apply();
//                }

                    //ve ya sadece bunu da istifade ede bilerik
                    mMap.setMyLocationEnabled(true);

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }
            };

            if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.getRoot(),"Permission needed!", Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //izin iste
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                }else{
                    //izin iste
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }

            }else {
                //izni verilmis lokasyonu guncelle
                //mekanin deqiqliyi provayderler,verdiyimiz icazeler ve location sorgusundaki secimlerimiz terefinden temin olunur
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                //son bilinen lokasyani al
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,5));
                }

                mMap.setMyLocationEnabled(true);
            }

        }else {

            mMap.clear();
            notedPlace = (Place) intent.getSerializableExtra("Place");

            LatLng notedLatLng = new LatLng(notedPlace.latitude,notedPlace.longitude);

            mMap.addMarker(new MarkerOptions().position(notedLatLng).title(notedPlace.placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(notedLatLng,15));

            binding.placeNameText.setText(notedPlace.placeName);
            binding.save.setVisibility(View.GONE);
            binding.delete.setVisibility(View.VISIBLE);


        }

    }


    public void registerLauncher(){
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {

            @SuppressLint("MissingPermission")
            @Override
            public void onActivityResult(Boolean result) {
            if(result){
                if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //izni verilmis lokasyonu guncelle
                    //mekanin deqiqliyi provayderler,verdiyimiz icazeler ve location sorgusundaki secimlerimiz terefinden temin olunur
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                    //son bilinen lokasyani al
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation != null) {
                        LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,5));
                    }
                }
            }else{
                Toast.makeText(MapsActivity.this,"Permission needed!", Toast.LENGTH_LONG).show();
            }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.save.setEnabled(true);
    }


    public void save(View view){

        Place place  = new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);
       // placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe();
        compositeDisposable.add(placeDao.insert(place).subscribeOn(Schedulers.io())
                //observeOn -u qoymasaq da olar, yene de mainThread-da gosterecekdir
        .observeOn(AndroidSchedulers.mainThread())
        //.subscribe());bele de saxlamaq olar ya da isimiz bitdikden sonra nese etmek isteyirikse
        .subscribe(MapsActivity.this::handleResponse)
        );

    }

    public void handleResponse(){
        Intent intent = new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void delete(View view){

        if(notedPlace != null){
            compositeDisposable.add(placeDao.delete(notedPlace)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}