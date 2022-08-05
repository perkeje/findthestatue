package com.example.findthestatue

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.findthestatue.databinding.ActivityMainBinding
import java.io.File
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var file: File

    private lateinit var camera : Camera

    private val prefs = Prefs()


    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null){
            startInfo(it.toString(),"galery")
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        prefs.setView(window)

        val previewView = viewBinding.viewFinder
        val focusRectangleView = viewBinding.focusRect

        val visibilityDelay = Runnable { focusRectangleView.visibility = View.GONE }
        val handler = Handler(Looper.getMainLooper())

        rectangleDelay(handler,visibilityDelay)
        openAndClearCache()
        setFlash(viewBinding.setFlashBtn,prefs.getFlash(this))

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }


        viewBinding.setFlashBtn.setOnClickListener{
            changeFlash(viewBinding.setFlashBtn)
        }

        viewBinding.addPhotoBtn.setOnClickListener{
            pickImage.launch("image/*")
        }

        viewBinding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()


        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 0F

                val delta = detector.scaleFactor

                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)

                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(this, listener)

        previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    val factory = previewView.meteringPointFactory

                    val x =event.x
                    val y = event.y
                    val point = factory.createPoint(x, y)
                    focusRectangleView.visibility = View.VISIBLE
                    focusRectangleView.x= x-(focusRectangleView.width/2)
                    focusRectangleView.y =y-(focusRectangleView.height/2)
                    val action = FocusMeteringAction.Builder(point).build()

                    camera.cameraControl.startFocusAndMetering(action)
                    rectangleDelay(handler,visibilityDelay)

                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        viewBinding.showHistoryBtn.setOnClickListener {
            startHistory()
        }

    }

    private fun takePhoto() {

        val imageCapture = imageCapture ?: return



        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(file)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){

                    startInfo(file.absolutePath,"camera")
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()

                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().setFlashMode(prefs.getFlash(this)).build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                 camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,imageCapture)



            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))



    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    companion object {
        private const val TAG = "FindTheStatue"
        private const val FILENAME = "appPrivate"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


    private fun changeFlash(btn:ImageButton){
        var mode = ImageCapture.FLASH_MODE_AUTO
        when(imageCapture?.flashMode){
            ImageCapture.FLASH_MODE_AUTO -> {
                mode = ImageCapture.FLASH_MODE_ON
            }
            ImageCapture.FLASH_MODE_ON -> {
                mode = ImageCapture.FLASH_MODE_OFF
            }
            ImageCapture.FLASH_MODE_OFF -> {
                mode = ImageCapture.FLASH_MODE_AUTO
            }
        }
        setFlash(btn,mode)
        prefs.saveFlash(this,mode)
    }

    private fun setFlash(btn:ImageButton,mode: Int){
        when(mode){
            ImageCapture.FLASH_MODE_AUTO -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
                btn.setImageResource(R.drawable.ic_auto_flash)
            }
            ImageCapture.FLASH_MODE_ON -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                btn.setImageResource(R.drawable.ic_flash_on)
            }
            ImageCapture.FLASH_MODE_OFF -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                btn.setImageResource(R.drawable.ic_flash_off)
            }
        }
    }

    private fun startInfo(URI : String,taken:String){
        val informationIntent = Intent(this, InformationActivity::class.java)
        informationIntent.putExtra("URI", URI)
        informationIntent.putExtra("picTaken",taken)
        startActivity(informationIntent)
    }
    private fun startHistory(){
        val historyIntent = Intent(this, HistoryActivity::class.java)
        startActivity(historyIntent)
    }

    private fun openAndClearCache(){
        File.createTempFile(FILENAME,null,this.cacheDir)
        file = File(this.cacheDir, FILENAME)
        file.delete()
    }

    private fun rectangleDelay(handler: Handler,runnable: Runnable){
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable,
            1000)
    }


}
