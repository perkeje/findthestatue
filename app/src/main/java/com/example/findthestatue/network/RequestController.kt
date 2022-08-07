package com.example.findthestatue.network

import android.graphics.Bitmap
import android.util.Log
import com.example.findthestatue.utils.ImageConverter
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class RequestController(private var inputImgBitmap: Bitmap) {
    var call: Call? = null

    init {
        createCall()
    }

    companion object {
        private const val INPUT_IMG_HEIGHT = 256
        private const val INPUT_IMG_WIDTH = 256
        private const val URL =
            "https://serving-container-nsplv7rfta-ew.a.run.app/v1/models/statue-recognizer:predict"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    private fun createCall() {
        val client = OkHttpClient()
        call = client.newCall(createRESTRequest())
    }


    private fun createRESTRequest(): Request {
        val inputImg = IntArray(INPUT_IMG_HEIGHT * INPUT_IMG_WIDTH)
        var inputImgRGB = Array(1) {
            Array(INPUT_IMG_HEIGHT) {
                Array(INPUT_IMG_WIDTH) {
                    IntArray(3)
                }
            }
        }
        val cropped = ImageConverter.cropBitmap(inputImgBitmap)
        val resized = Bitmap.createScaledBitmap(cropped, INPUT_IMG_HEIGHT, INPUT_IMG_WIDTH, true)
        resized.getPixels(
            inputImg,
            0,
            INPUT_IMG_WIDTH,
            0,
            0,
            INPUT_IMG_WIDTH,
            INPUT_IMG_HEIGHT
        )

        Log.d("Dims", "${cropped.width} ${cropped.height}")
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

    fun makeRequest(): Array<Float> {
        call?.let {
            val response = it.execute()
            val json = response.body!!.string()
            response.close()
            val responseObject = JSONObject(json)
            val predictionsArray = responseObject.getJSONArray("predictions")
            return Gson().fromJson(predictionsArray[0].toString(), Array<Float>::class.java)
        }
        throw Error("Unable to execute request!")
    }

}