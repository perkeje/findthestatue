package com.example.findthestatue

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object DatabaseRef {
    private var myRef: DatabaseReference
    init {
        val database = Firebase.database
        myRef = database.getReference("/")
    }
    fun getDBRef(): DatabaseReference{
        return myRef
    }
}