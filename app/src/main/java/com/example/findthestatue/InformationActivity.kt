package com.example.findthestatue


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.icu.text.CaseMap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.findthestatue.ml.StatueRecognizerv01
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer


class InformationActivity : AppCompatActivity() {
    private val names = arrayOf("Blažena Djevica Marija", "Kip Svetog Trojstva", "Sv. Franjo Ksaverski", "Sv. Ignacije Loyola", "Sv. Ivan Nepomuk", "Sv. Josip", "Sv. Katarina Aleksandrijska", "Sv. Rok", "Sv. Sebastijan")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        val imageView = findViewById<ImageView>(R.id.photo_holder)
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet_layout)
        val description = findViewById<TextView>(R.id.description)
        val title = findViewById<TextView>(R.id.name)
        val uri = intent.getStringExtra("URI")
        var bitmap:Bitmap
         if(intent.getStringExtra("picTaken") == "camera") {
            bitmap = BitmapFactory.decodeFile(uri )
            val matix = Matrix()
            matix.postRotate(90F)
            bitmap =  Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matix,true)
        }
        else {
            bitmap = getCapturedImage(Uri.parse(uri))
        }

        imageView.setImageURI(Uri.parse(uri))
        val index = classifyImage(bitmap)
        title.text = names[index]
        val bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet).apply {
            peekHeight = 400
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheet.setOnClickListener{
            if(bottomSheetBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED) bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }




    }
    private fun classifyImage(image: Bitmap): Int{
        val bmp = image.copy(Bitmap.Config.ARGB_8888, true)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224,224,ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0F,255F))
            .build()

        var tensorImg = TensorImage(DataType.FLOAT32)
        tensorImg.load(bmp)
        tensorImg = imageProcessor.process(tensorImg)

        val model = StatueRecognizerv01.newInstance(applicationContext)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImg.buffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature0.floatArray

        val maxIdx = confidences.indices.maxBy { confidences[it] }

        model.close()
        return maxIdx
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
}