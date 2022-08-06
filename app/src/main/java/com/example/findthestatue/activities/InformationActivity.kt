package com.example.findthestatue.activities

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.findthestatue.R
import com.example.findthestatue.interfaces.FirebaseCallback
import com.example.findthestatue.models.Statue
import com.example.findthestatue.network.RequestController
import com.example.findthestatue.utils.ImageConverter
import com.example.findthestatue.utils.Prefs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONException


class InformationActivity : AppCompatActivity() {
    private lateinit var description: TextView
    private lateinit var title: TextView
    private lateinit var controlImg: ImageView
    private lateinit var favouriteImg: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomSheet: ConstraintLayout
    private lateinit var reqController: RequestController
    private var maxIdx = -1

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        Prefs.setView(window)

        bottomSheet = findViewById(R.id.bottom_sheet_layout)
        progressBar = findViewById(R.id.progressBar)
        favouriteImg = findViewById(R.id.favourite_btn)
        description = findViewById(R.id.description)
        title = findViewById(R.id.name)
        controlImg = findViewById(R.id.control_img)

        val imageView = findViewById<ImageView>(R.id.photo_holder)
        val backBtn = findViewById<ImageButton>(R.id.back_btn)

        val uri = intent.getStringExtra("URI").toString()
        val mode = intent.getStringExtra("picTaken").toString()
        val imgController = ImageConverter(uri, mode, this)

        val bitmap = imgController.getBitmap()

        imageView.setImageURI(Uri.parse(uri))

        reqController = RequestController(bitmap)

        GlobalScope.launch {
            handleResponse()
        }

        val bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet).apply {
            peekHeight = 400
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheet.setOnClickListener {
            if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED) bottomSheetBehaviour.state =
                BottomSheetBehavior.STATE_EXPANDED
        }

        imageView.setOnClickListener {
            if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) bottomSheetBehaviour.state =
                BottomSheetBehavior.STATE_COLLAPSED
        }


        favouriteImg.setOnClickListener {
            var favourites = Prefs.getArrayList(this)
            if (favourites != null) {
                if (favourites.contains(maxIdx)) {
                    favouriteImg.setImageResource(R.drawable.favourite_foreground)
                    favourites.remove(maxIdx)
                    Prefs.saveArrayList(favourites, this)
                } else {
                    favourites.add(maxIdx)
                    favouriteImg.setImageResource(R.drawable.favourite_filled_foreground)
                    Prefs.saveArrayList(favourites, this)
                }
            } else {
                favourites = ArrayList()
                favourites.add(maxIdx)
                favouriteImg.setImageResource(R.drawable.favourite_filled_foreground)
                Prefs.saveArrayList(favourites, this)
            }
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onPause() {
        super.onPause()
        reqController.call?.cancel()

    }

    private fun setText(index: Int) {

        Statue.fromIndex(index, object : FirebaseCallback {
            override fun onResponse(statue: Statue?) {
                runOnUiThread {
                    statue?.let {
                        title.text = statue.name
                        description.text = statue.description
                        Glide.with(baseContext)
                            .load(statue.img)
                            .into(controlImg)
                    }
                    progressBar.visibility = View.GONE
                    bottomSheet.visibility = View.VISIBLE
                    statue?.let {
                        val favourites = Prefs.getArrayList(baseContext)
                        if (favourites != null && favourites.contains(index)) {
                            favouriteImg.setImageResource(R.drawable.favourite_filled_foreground)
                        } else {
                            favouriteImg.setImageResource(R.drawable.favourite_foreground)
                        }
                        favouriteImg.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun handleResponse() {
        try {
            val predictions = reqController.makeRequest()
            maxIdx = predictions.indices.maxBy { predictions[it] }
            setText(maxIdx)

        } catch (e: IOException) {
            Log.e(TAG, e.message!!)

        } catch (e: JSONException) {
            Log.e(TAG, e.message!!)
        } catch (e: Error) {
            Log.e(TAG, e.message!!)
        }
    }
}
