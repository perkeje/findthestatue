package com.example.findthestatue




import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity



class InformationActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        val imageView = findViewById<ImageView>(R.id.photo_holder)
        val uri = intent.getStringExtra("URI")
        imageView.setImageURI(Uri.parse(uri))

    }



}