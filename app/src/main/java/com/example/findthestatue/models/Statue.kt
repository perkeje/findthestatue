package com.example.findthestatue.models

import android.content.ContentValues.TAG
import android.util.Log
import com.example.findthestatue.interfaces.FirebaseCallback
import com.example.findthestatue.network.DatabaseRef


class Statue(var description: String? = "", var img: String? = "", var name: String? = "") {
    companion object {

        fun fromIndex(index: Int, callback: FirebaseCallback) {
            DatabaseRef.getDBRef().child(index.toString()).get().addOnSuccessListener {
                val statue = it.getValue(Statue::class.java)
                callback.onResponse(statue)
            }.addOnFailureListener {
                Log.d(TAG, it.message.toString())
            }

        }
    }
}
