/*package com.albatros.newsagency

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.albatros.newsagency.databinding.ActivityCameraBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class CameraActivity : AppCompatActivity() {

    lateinit var cameraSource: CameraSource
    private lateinit var binding: ActivityCameraBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val processor = object : Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                if (detections.detectedItems.isNotEmpty()) {
                    Log.d("!!!!", detections.detectedItems[0].displayValue)
                    val barcode = detections.detectedItems
                    if (barcode.size() > 0) {
                        Toast.makeText(
                            this@CameraActivity,
                            barcode.valueAt(0)?.displayValue ?: "j",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }

        val detector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        detector.setProcessor(processor)
        cameraSource = CameraSource.Builder(this, detector)
            .setRequestedFps(25f)
            .setAutoFocusEnabled(true).build()

        val surfaceCallBack = object : SurfaceHolder.Callback {

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ContextCompat.checkSelfPermission(
                        this@CameraActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                )                    cameraSource.start(holder)
                else requestPermissions(arrayOf<String>(Manifest.permission.CAMERA), 1001)
            }
        }
        binding.cameraSurfaceView.holder.addCallback(surfaceCallBack)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                }
                cameraSource.start(binding.cameraSurfaceView.holder)
            } else {
                Toast.makeText(this, "sd", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
} */
package com.albatros.newsagency

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.albatros.newsagency.databinding.ActivityCameraBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraSource: CameraSource

    private val surfaceCallback = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
                if (isPlayServicesAvailable(this@CameraActivity))
                    cameraSource.start(holder)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) = cameraSource.stop()

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    fun isPlayServicesAvailable(activity: Activity): Boolean {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return if (code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, code, code)?.show()
            false
        } else true
    }

    private val processor = object : Detector.Processor<Barcode> {

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            detections?.apply {
                if (detectedItems.isNotEmpty()) {
                    val qr = detectedItems.valueAt(0)
                    qr.displayValue.let {
                        Log.d("ID: $it", "!!")
                    }
                    finish()
                }
            }
        }

        override fun release() {}
    }

    private fun setWindowState() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun setupCameraView() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS).build().apply {
                    setProcessor(processor)
                    if (!isOperational) {
                        return
                    }
                    cameraSource =
                        CameraSource.Builder(this@CameraActivity, this).setAutoFocusEnabled(true)
                            .setFacing(CameraSource.CAMERA_FACING_BACK)
                            .setRequestedFps(25f)
                            .setAutoFocusEnabled(true)
                            .build()
                }
        } else
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setWindowState()
        setContentView(binding.root)
        setupCameraView()
        binding.cameraSurfaceView.holder.addCallback(surfaceCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.release()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.ALL_FORMATS).build().apply {
                        setProcessor(processor)
                        if (!isOperational) {
                            return
                        }
                        cameraSource =
                            CameraSource.Builder(this@CameraActivity, this)
                                .setAutoFocusEnabled(true)
                                .setFacing(CameraSource.CAMERA_FACING_BACK)
                                .setRequestedFps(25f)
                                .setAutoFocusEnabled(true)
                                .build()
                        binding.cameraSurfaceView.holder.addCallback(surfaceCallback)
                    }
            }
        }
    }
}