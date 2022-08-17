package com.example.newgooglemap.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.newgooglemap.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {

    @Insert
    public Completable insert(Place place);

    @Delete
    public Completable delete(Place place);


    @Query("SELECT * FROM Place")
    Flowable<List<Place>> getPlaceList();


}
