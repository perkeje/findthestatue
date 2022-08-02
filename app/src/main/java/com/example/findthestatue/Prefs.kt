package com.example.findthestatue

import android.content.Context
import androidx.camera.core.ImageCapture
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Prefs {
    fun saveArrayList(list: ArrayList<Int?>?,context: Context) {
        val prefs = context.getSharedPreferences("saved", Context.MODE_PRIVATE)
        val gson = Gson()
        val dist = list?.distinct()
        val json = gson.toJson(dist)
        prefs.edit()
            .putString("saved",json)
            .apply()
    }

    fun getArrayList(context: Context):ArrayList<Int?>?{
        val prefs = context.getSharedPreferences("saved", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("saved",null)
        val type= object : TypeToken<ArrayList<Int?>?>() {}.type
        return gson.fromJson(json,type)
    }
    fun saveFlash(context: Context, mode:Int){
        val prefs = context.getSharedPreferences("flash", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("flash",mode)
            .apply()
    }
    fun getFlash(context: Context):Int{
        val prefs = context.getSharedPreferences("flash", Context.MODE_PRIVATE)
        return prefs.getInt("flash", ImageCapture.FLASH_MODE_AUTO)
    }
}