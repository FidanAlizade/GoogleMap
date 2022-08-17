package com.example.newgooglemap.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Place implements Serializable {

    @PrimaryKey (autoGenerate = true)
    public int id;

    @ColumnInfo(name = "placeName")
    public String placeName;

    @ColumnInfo (name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    public Place(String placeName, double latitude, double longitude){
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
