package com.albatros.newsagency.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.isNotEmpty
import com.albatros.newsagency.R
import com.albatros.newsagency.containers.SiteManager
import com.albatros.newsagency.databinding.ActivityCameraBinding
import com.albatros.newsagency.utils.XmlFeedParser
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.coroutines.DelicateCoroutinesApi

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraSource: CameraSource

    private val surfaceCallback = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
            if (ActivityCompat.checkSelfPermission(this@CameraActivity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && isPlayServicesAvailable(this@CameraActivity))
                cameraSource.start(holder)
            else requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
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

    @DelicateCoroutinesApi
    private val processor = object : Detector.Processor<Barcode> {

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            detections?.apply {
                if (detectedItems.isNotEmpty()) {
                    val qr = detectedItems.valueAt(0)
                    qr.displayValue.let {
                        XmlFeedParser.parseSiteDoc(it)
                        NavActivity.increaseBottomBadge(R.id.navigation_notifications, SiteManager.sitesCount, true)
                        finish()
                    }
                }
            }
        }

        override fun release() {}
    }

    private fun setWindowState() {
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    @DelicateCoroutinesApi
    private fun setupCameraView() {
        BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build().apply {
            setProcessor(processor)
            if (!isOperational)
                return
            cameraSource = CameraSource.Builder(this@CameraActivity, this).setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(25f)
                .setAutoFocusEnabled(true)
                .build()
        }
    }

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setWindowState()
        setContentView(binding.root)
        binding.cameraSurfaceView.holder.addCallback(surfaceCallback)
        setupCameraView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.release()
    }

    @DelicateCoroutinesApi
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                setupCameraView()
    }
}