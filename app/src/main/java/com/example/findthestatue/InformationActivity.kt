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
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.findthestatue.ml.StatueRecognizerv01
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer


class InformationActivity : AppCompatActivity() {
    private  lateinit var description:TextView
    private  lateinit var title:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        val imageView = findViewById<ImageView>(R.id.photo_holder)
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet_layout)
        description = findViewById<TextView>(R.id.description)
        title = findViewById<TextView>(R.id.name)
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
        setText(classifyImage(bitmap))
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
        Log.d("Confidence","${confidences[maxIdx]}")
        model.close()
        return maxIdx
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