package com.example.findthestatue

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject


class InformationActivity : AppCompatActivity() {
    private  lateinit var description:TextView
    private  lateinit var title:TextView
    private lateinit var  controlImg:ImageView
    private lateinit var favouriteImg:ImageButton
    private lateinit var call: Call
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        val imageView = findViewById<ImageView>(R.id.photo_holder)
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet_layout)
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        favouriteImg = findViewById(R.id.favourite_btn)
        description = findViewById(R.id.description)
        title = findViewById(R.id.name)
        controlImg = findViewById(R.id.control_img)
        val uri = intent.getStringExtra("URI")
        var bitmap:Bitmap
        var maxIdx = -1

         if(intent.getStringExtra("picTaken") == "camera") {
            bitmap = BitmapFactory.decodeFile(uri )
            val matrix = Matrix()
            matrix.postRotate(90F)
            bitmap =  Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true)
        }
        else {
            bitmap = getCapturedImage(Uri.parse(uri))
             bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        imageView.setImageURI(Uri.parse(uri))

        val request = createRESTRequest(bitmap)
        val thread = Thread {
            val client = OkHttpClient()
            try {
                call = client.newCall(request)
                val response = call.execute()
                val json = response.body!!.string()
                response.close()
                val responseObject = JSONObject(json)
                val predictionsArray = responseObject.getJSONArray("predictions")
                val predictions = Gson().fromJson(predictionsArray[0].toString(),Array<Float>::class.java)
                maxIdx = predictions.indices.maxBy { predictions[it] }
                this.runOnUiThread{
                    setText(maxIdx)
                    val favourites = getArrayList()
                    if (favourites != null && favourites.contains(maxIdx)) {
                        favouriteImg.setImageResource(R.drawable.favourite_filled_foreground)
                    }
                    else{
                        favouriteImg.setImageResource(R.drawable.favourite_foreground)
                    }
                    favouriteImg.visibility = View.VISIBLE
                }
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)

            } catch (e: JSONException) {
                Log.e(TAG, e.message!!)

            }
        }

        thread.start()

        val bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet).apply {
            peekHeight = 400
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheet.setOnClickListener{
            if(bottomSheetBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED) bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }

        imageView.setOnClickListener{
            if(bottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        }


        favouriteImg.setOnClickListener{
            var favourites = getArrayList()
            if (favourites != null) {
                if(favourites.contains(maxIdx)){
                    favouriteImg.setImageResource(R.drawable.favourite_foreground)
                    favourites.remove(maxIdx)
                    saveArrayList(favourites)
            }
                else{
                    favourites.add(maxIdx)
                    favouriteImg.setImageResource(R.drawable.favourite_filled_foreground)
                    saveArrayList(favourites)
                }
            }
            else{
                favourites = ArrayList()
                favourites.add(maxIdx)
                favouriteImg.setImageResource(R.drawable.favourite_filled_foreground)
                saveArrayList(favourites)
            }
        }
        backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onPause() {
        super.onPause()
        if (call != null){
            call.cancel()
        }
    }

    private fun setText(index : Int){
        val database = Firebase.database
        val myRef = database.getReference("/")


        myRef.child(index.toString()).child("name").get().addOnSuccessListener {
            title.text = it.value.toString()
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        myRef.child(index.toString()).child("description").get().addOnSuccessListener {
            description.text = it.value.toString()
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        myRef.child(index.toString()).child("img").get().addOnSuccessListener {
            Glide.with(this)
                .load(it.value.toString())
                .into(controlImg)
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

    }

private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
    val bitmap = when {
        Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            selectedPhotoUri
        )
        else -> {
            val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
            ImageDecoder.decodeBitmap(source)
        }
    }
    return bitmap
}

    private fun createRESTRequest(inputImgBitmap: Bitmap): Request {
        val inputImg = IntArray(INPUT_IMG_HEIGHT * INPUT_IMG_WIDTH)
        val inputImgRGB = Array(1) {
            Array(INPUT_IMG_HEIGHT) {
                Array(INPUT_IMG_WIDTH) {
                    IntArray(3)
                }
            }
        }
        inputImgBitmap.getPixels(
            inputImg,
            0,
            INPUT_IMG_WIDTH,
            0,
            0,
            INPUT_IMG_WIDTH,
            INPUT_IMG_HEIGHT
        )
        var pixel: Int

        for (i in 0 until INPUT_IMG_HEIGHT) {
            for (j in 0 until INPUT_IMG_WIDTH) {
                pixel = inputImg[i * INPUT_IMG_WIDTH + j]
                inputImgRGB[0][i][j][0] = pixel shr 16 and 0xff
                inputImgRGB[0][i][j][1] = pixel shr 8 and 0xff
                inputImgRGB[0][i][j][2] = pixel and 0xff
            }
        }
        val requestBody =
            ("{\"instances\": " + inputImgRGB.contentDeepToString() + "}").toRequestBody(JSON)


        return Request.Builder()
            .url(URL)
            .post(requestBody)
            .build()
    }

    companion object {
        private const val INPUT_IMG_HEIGHT = 256
        private const val INPUT_IMG_WIDTH = 256
        private const val URL = "https://serving-container-nsplv7rfta-ew.a.run.app/v1/models/statue-recognizer:predict"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    private fun saveArrayList(list: ArrayList<Int?>?) {
        val prefs = this.getSharedPreferences("saved",Context.MODE_PRIVATE)
        val gson = Gson()
        val dist = list?.distinct()
        val json = gson.toJson(dist)
        prefs.edit()
            .putString("saved",json)
            .apply()
    }

    private fun getArrayList():ArrayList<Int?>?{
        val prefs = this.getSharedPreferences("saved",Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("saved",null)
        val type= object : TypeToken<ArrayList<Int?>?>() {}.type
        return gson.fromJson(json,type)
    }

}


