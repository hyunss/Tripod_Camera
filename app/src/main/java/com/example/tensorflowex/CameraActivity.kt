package com.example.tensorflowex

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 10
private lateinit var bt : BluetoothSPP
// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CameraActivity : AppCompatActivity(), LifecycleOwner{
    var count = 0

    private lateinit var file : File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewFinder = findViewById(R.id.view_finder1)

        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        //bluetooth spp
        bt = BluetoothSPP(this)
        if(!bt.isBluetoothAvailable){
            Toast.makeText(applicationContext, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            finish()
        }
        bt.setBluetoothConnectionListener(object:BluetoothSPP.BluetoothConnectionListener{
            override fun onDeviceDisconnected() {
                Toast.makeText(
                        applicationContext
                        , "Connection lost", Toast.LENGTH_SHORT).show()
            }

            override fun onDeviceConnected(name: String?, address: String?) {
                Toast.makeText(
                        applicationContext
                        , "Connected to $name\n$address"
                        , Toast.LENGTH_SHORT).show()
            }

            override fun onDeviceConnectionFailed() {
                Toast.makeText(
                        applicationContext
                        , "Unable to connect", Toast.LENGTH_SHORT).show()
            }

        })
        val btnConnect : ImageButton = findViewById(R.id.btnConnect)
        btnConnect.setOnClickListener{
            /*
            if (bt.serviceState == BluetoothState.STATE_CONNECTED) {
                bt.disconnect()
            } else {
                val intent = Intent(applicationContext, DeviceList::class.java)
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
            }*/
            val intent = Intent(baseContext, MainActivity::class.java)
            startActivity(intent)

        }

        bt.setOnDataReceivedListener { _, message ->  //데이터 수신
            if(count<6){
                if(message == "1"){
                    preview.setOnPreviewOutputUpdateListener {

                        // To update the SurfaceTexture, we have to remove it and re-add it
                        val parent = viewFinder.parent as ViewGroup
                        parent.removeView(viewFinder)
                        parent.addView(viewFinder, 0)

                        viewFinder.surfaceTexture = it.surfaceTexture
                        updateTransform()

                    }
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    file = File(externalMediaDirs.first(),
                        "${timeStamp}.jpg")
                    takePicture()
                    setup()
                    count++
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bt.stopService()
    }
    override fun onStart() {
        super.onStart()
        if (!bt.isBluetoothEnabled) { //
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT)
        } else {
            if (!bt.isServiceAvailable) {
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
                setup()
            }
        }

    }

    private fun setup() {
        bt.send("1",true)
    }


    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView
    val previewConfig = PreviewConfig.Builder().apply {
        setTargetResolution(Size(640, 480))
    }.build()


    // Build the viewfinder use case
    val preview = Preview(previewConfig)

    // Every time the viewfinder is updated, recompute layout
    val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

    // Build the image capture use case and attach button click listener


    val imageCapture = ImageCapture(imageCaptureConfig)

    fun takePicture(){
                imageCapture.takePicture(file, executor,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(
                            imageCaptureError: ImageCapture.ImageCaptureError,
                            message: String,
                            exc: Throwable?)
                    {
                        val msg = "Photo capture failed: $message"
                        Log.e("CameraXApp", msg, exc)
                        viewFinder.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Log.d("CameraXApp", msg)
                        galleryAddPic()
                        viewFinder.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    @Suppress("DEPRECATION")
                    private fun galleryAddPic() {
                        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                            val f = File(file.absolutePath)
                            mediaScanIntent.data = Uri.fromFile(f)
                            sendBroadcast(mediaScanIntent)
                        }
                    }
                })

    }
    private fun startCamera() {
        // Create configuration object for the viewfinder use case
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            file = File(externalMediaDirs.first(),
                "${timeStamp}.jpg")
            takePicture()
        }


        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            count = 0
            bt.send("1",true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE){
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data)
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                bt.setupService()
                bt.startService(BluetoothState.DEVICE_OTHER)
                setup()
            }
            else{
                Toast.makeText(applicationContext, "BlueTooth was not enabled", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}