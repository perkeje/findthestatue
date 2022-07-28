package com.example.findthestatue

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.opengl.Visibility
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class InformationActivity : AppCompatActivity() {
    private  lateinit var description:TextView
    private  lateinit var title:TextView
    private lateinit var  controlImg:ImageView
    private lateinit var favouriteImg:ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        val imageView = findViewById<ImageView>(R.id.photo_holder)
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet_layout)
        favouriteImg = findViewById(R.id.favourite_btn)
        description = findViewById(R.id.description)
        title = findViewById(R.id.name)
        controlImg = findViewById(R.id.control_img)
        val uri = intent.getStringExtra("URI")
        var bitmap:Bitmap

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
            try {
                var client = OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .callTimeout(20, TimeUnit.SECONDS)
                    .build()
                val response = client.newCall(request).execute()
                val string = response.body!!.string()
                val responseObject = JSONObject(string)
                val predictionsArray = responseObject.getJSONArray("predictions")
                val predictions = Gson().fromJson(predictionsArray[0].toString(),Array<Float>::class.java)
                val maxIdx = predictions.indices.maxBy { predictions[it] }
                this?.runOnUiThread{
                    setText(maxIdx)
                    favouriteImg.setImageResource(R.drawable.favourite_foreground)
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
            favouriteImg.setImageResource(R.drawable.favourite_filled_foreground)
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

}


