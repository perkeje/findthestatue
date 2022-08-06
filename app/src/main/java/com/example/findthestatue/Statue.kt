package com.example.findthestatue

import android.content.ContentValues.TAG
import android.util.Log


class Statue(var description: String?="", var img: String?="", var name: String?="")  {
    companion object{

        fun fromIndex(index:Int,callback:FirebaseCallback) {
            DatabaseRef.getDBRef().child(index.toString()).get().addOnSuccessListener {
                val statue = it.getValue(Statue::class.java)
                callback.onResponse(statue);
            }.addOnFailureListener{
                Log.d(TAG, it.message.toString());
            }

        }
    }
}
