package com.example.findthestatue

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Error

class Statue(name: String,description:String,imgUrl:String) {

    companion object{
        fun fromIndex(index:Int):Statue{
            var name:String = ""
            var description:String = ""
            var imgUrl:String = ""
            DatabaseRef.getDBRef().child(index.toString()).child("name").get().addOnSuccessListener {
                name = it.value.toString()
            }.addOnFailureListener{
                throw Error("Error getting data")
            }
            DatabaseRef.getDBRef().child(index.toString()).child("name").get().addOnSuccessListener {
                description = it.value.toString()
            }.addOnFailureListener{
                throw Error("Error getting data")
            }
            DatabaseRef.getDBRef().child(index.toString()).child("name").get().addOnSuccessListener {
                imgUrl = it.value.toString()
            }.addOnFailureListener{
                throw Error("Error getting data")
            }
            return Statue(name, description, imgUrl)
        }
    }



}