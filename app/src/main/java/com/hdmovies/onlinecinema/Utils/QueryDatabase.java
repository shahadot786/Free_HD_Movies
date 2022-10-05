package com.hdmovies.onlinecinema.Utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class QueryDatabase {
    private DatabaseReference databaseReference;
    public QueryDatabase(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference().child("Movies");
    }


    public Query get(String key){
        if (key == null){
            return databaseReference.orderByKey();
        }
        return databaseReference.orderByKey().startAfter(key).limitToFirst(39);

    }

}
