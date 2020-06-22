package com.example.tensorflowex

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 10
private lateinit var bt : BluetoothSPP
// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CameraActivity : AppCompatActivity(), LifecycleOwner{
    var count = 0

    var messageText: TextView? = null
    var serverResponseCode = 0
    var dialog: ProgressDialog? = null
    var upLoadServerUri: String? = null

    //유니크한 단말 번호 >>> Android ID 사용
    lateinit var android_id:String

    /**********  File Path  *************/
    lateinit var uploadFilePath:String // "/mnt/sdcard/DCIM/Camera/"
    var uploadFileName:String? = null // "20200520_155136.jpg"

    private lateinit var file : File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewFinder = findViewById(R.id.view_finder1)
        messageText = findViewById<View>(R.id.messageText) as TextView

        //유니크한 단말 번호 >>> Android ID 사용
        android_id = Settings.Secure.getString(
            this.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        messageText!!.text = android_id

        /************* Php script path ****************/
        upLoadServerUri = "http://192.168.25.11/project/upload.php"
        // "http://192.168.112.38/project/upload.php";

        // Request permission from the user
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_NETWORK_STATE),
                0
            )
        }

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
                    uploadFileName = "$timeStamp.jpg"
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

    fun takePicture(){ //uploadFileName: String
        dialog = ProgressDialog.show(this, "", "Uploading file...", true)

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
                Log.i("path", "${file.absolutePath}");
                galleryAddPic()
                viewFinder.post {
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
                uploadFilePath = "${file.absolutePath}"
                Thread(Runnable {
                    //runOnUiThread { messageText!!.text = "uploading started....." }
                    uploadFile(uploadFilePath, android_id) //  + "" + uploadFileName
                }).start()
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

    fun uploadFile(sourceFileUri: String, androidId: String): Int {
        var conn: HttpURLConnection? = null
        var dos: DataOutputStream? = null
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****"
        var bytesRead: Int
        var bytesAvailable: Int
        var bufferSize: Int
        val buffer: ByteArray
        val maxBufferSize = 1 * 1024 * 1024

        val sourceFile = File(sourceFileUri)

        return if (!sourceFile.isFile) {
            dialog!!.dismiss()
            Log.e(
                "uploadFile",
                "Source File not exist :" + uploadFilePath //  + "" + uploadFileName
            )
            runOnUiThread {
                //messageText!!.text = "Source File not exist :$uploadFilePath" + uploadFileName
            }
            0
        } else {
            try {
                // open a URL connection to the Servlet
                val fileInputStream = FileInputStream(sourceFile)
                val url = URL(upLoadServerUri)

                // Open a HTTP  connection to  the URL
                conn = url.openConnection() as HttpURLConnection
                conn.doInput = true // Allow Inputs
                conn!!.doOutput = true // Allow Outputs
                conn.useCaches = false // Don't use a Cached Copy
                conn.requestMethod = "POST"
                conn.setRequestProperty("Connection", "Keep-Alive")
                conn.setRequestProperty("ENCTYPE", "multipart/form-data")
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
                conn.setRequestProperty("uploaded_file", sourceFileUri)
                conn.setRequestProperty("img_owner", androidId)

                dos = DataOutputStream(conn.outputStream)

                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes("Content-Disposition: form-data; name=\'img_owner\'$lineEnd")
                dos.writeBytes(lineEnd)

                dos.writeBytes(androidId)
                dos.writeBytes(lineEnd)

                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes("Content-Disposition: form-data; name=\'uploaded_file\';filename=\'$sourceFileUri\'$lineEnd")
                dos.writeBytes(lineEnd)

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd)
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                // Responses from the server (code and message)
                serverResponseCode = conn.responseCode
                val serverResponseMessage = conn.responseMessage
                Log.i(
                    "uploadFile",
                    "HTTP Response is : $serverResponseMessage: $serverResponseCode"
                )
                if (serverResponseCode == 200) {
                    runOnUiThread {
                        val msg =
                            "File Upload Completed.\n\n See uploaded file here : \n\n " +
                                    "http://192.168.25.11/project/uploads/${uploadFileName}"
                        messageText!!.text = msg
                        Toast.makeText(
                            this,
                            "File Upload Complete.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                //close the streams //
                fileInputStream.close()
                //}
                dos.flush()
                dos.close()
            } catch (ex: MalformedURLException) {
                dialog!!.dismiss()
                ex.printStackTrace()
                runOnUiThread {
                    messageText!!.text = "MalformedURLException Exception : check script url."
                    Toast.makeText(
                        this,
                        "MalformedURLException",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("Upload file to server", "error: " + ex.message, ex)
            } catch (e: Exception) {
                dialog!!.dismiss()
                e.printStackTrace()
                runOnUiThread {
                    messageText!!.text = "Got Exception : see logcat "
                    Toast.makeText(
                        this,
                        "Got Exception : see logcat ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("uploadFile toServer Err", "Exception : " + e.message, e)
            }
            dialog!!.dismiss()
            serverResponseCode
        } // End else block
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
            uploadFileName = "$timeStamp.jpg"
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
                        "Permissions are not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
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