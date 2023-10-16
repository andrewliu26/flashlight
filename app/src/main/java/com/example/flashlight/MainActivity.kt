package com.example.flashlight

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.text.Editable
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var flashlightSwitch: SwitchMaterial
    private lateinit var actionInput: EditText
    private var isFlashlightOn: Boolean = false

    companion object {
        const val CAMERA_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request camera permission
        checkCameraPermission()

        // Set listener for gestures
        gestureDetector = GestureDetectorCompat(this, MyGestureListener())

        flashlightSwitch = findViewById(R.id.flashlightSwitch)
        actionInput = findViewById(R.id.actionInput)

        // Set listener for flashlight switch
        flashlightSwitch.setOnCheckedChangeListener { _, isChecked ->
            isFlashlightOn = isChecked
            handleFlashlight()
        }
        // Set listener for edit text
        actionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim().uppercase()
                if (input == "ON") {
                    isFlashlightOn = true
                    flashlightSwitch.isChecked = true
                } else if (input == "OFF") {
                    isFlashlightOn = false
                    flashlightSwitch.isChecked = false
                }
                handleFlashlight()
            }
        })


    }

    // Inner class for gesture detection, reduces required methods
    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        // Handles vertical fling gestures
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean {
            val deltaY = e2.y - e1.y
            if (abs(deltaY) > 100 && abs(velY) > 100) {
                isFlashlightOn = deltaY < 0
                flashlightSwitch.isChecked = isFlashlightOn
                handleFlashlight()
                return true
            }
            return false
        }
    }

    private fun handleFlashlight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        try {
            // Checks for flashlight on current device
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cameraManager.setTorchMode(cameraId, isFlashlightOn)
            } else {
                Toast.makeText(this, "No flashlight available on this device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: CameraAccessException) {
            Toast.makeText(this, "Flashlight not available", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Flash unit not available on this camera", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, perms: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, perms, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Camera permission granted
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Camera permission denied
                    Toast.makeText(this, "Camera permission is required to use flashlight", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }


}