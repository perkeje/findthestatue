package com.example.findthestatue



import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class InformationActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        val imageView = findViewById<ImageView>(R.id.photo_holder)
        val byteArray = intent.getByteArrayExtra("image")
//        var image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
//
//        imageView.setImageBitmap(image)
    }



}