package com.br0ke.travelbookjava.roomdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.br0ke.travelbookjava.model.Place;

@Database(entities = {Place.class},version = 1)
public abstract class PlaceDatabase extends RoomDatabase {
    public  abstract PlaceDao placeDao();
}
