package com.example.newgooglemap.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.newgooglemap.R;
import com.example.newgooglemap.adapter.PlaceAdapter;
import com.example.newgooglemap.databinding.ActivityMainBinding;
import com.example.newgooglemap.db.PlaceDao;
import com.example.newgooglemap.db.PlaceDatabase;
import com.example.newgooglemap.model.Place;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
private ActivityMainBinding binding;
CompositeDisposable compositeDisposable = new CompositeDisposable();
PlaceDatabase db;
PlaceDao placeDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(binding.getRoot());
         db = Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();
         placeDao = db.placeDao();

         compositeDisposable.add(placeDao.getPlaceList()
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(MainActivity.this::handleResponse));
    }

    public void handleResponse(List<Place> placeList){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PlaceAdapter placeAdapter = new PlaceAdapter(placeList);
        binding.recyclerView.setAdapter(placeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(MainActivity.this);
        menuInflater.inflate(R.menu.travel_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.location_menu){
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}