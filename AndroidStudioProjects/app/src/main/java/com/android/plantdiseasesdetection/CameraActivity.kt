package com.android.plantdiseasesdetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.priyankvasa.android.cameraviewex.ErrorLevel
import com.priyankvasa.android.cameraviewex.Modes
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.fragment_loading_dialog.*
import java.io.IOException


class CameraActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val REQUEST_CAMERA_CODE = 100
        private const val STATE_FLASHLIGHT = "state_flashlight"
        private const val STATE_RESULTS = "state_results"
    }

    private lateinit var classifier: Classification
    private var detectionTask: AsyncTask<*, *, *>? = null
    private var flashModes: Int = 1
    private val mLoadingFragment = LoadingDialogFragment()
    private val mFragmentManager = supportFragmentManager
    private var isButtonClicked = false
    private var canUseCamera = true

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        if (supportActionBar != null) {
            (supportActionBar as ActionBar).title = "Deteksi dengan Kamera"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        classifier = Classification(assets)

        if (!canUseCamera()) {
            requestCameraPermissions()
        } else {
            setDetectionMode()
            btn_capture.setOnClickListener(this)
            iv_flash_mode.setOnClickListener(this)
            detectPicture()
            detectPreviewFrames()
        }

        if (savedInstanceState != null) {
            val tvResults = savedInstanceState.getString(STATE_RESULTS) as String
            tv_result.text = tvResults
            flashModes = if (flashModes != 0) {
                val flashMode = savedInstanceState.getInt(STATE_FLASHLIGHT)
                flashMode
            } else {
                4
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_RESULTS, tv_result.text.toString())
        outState.putInt(STATE_FLASHLIGHT, flashModes)
    }

    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (REQUEST_CAMERA_CODE == requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectPicture()
                detectPreviewFrames()
            } else {
                Toast.makeText(this, "Tidak dapat menggunakan kamera", Toast.LENGTH_LONG).show()
                requestCameraPermissions()
            }
        }
    }

    private fun canUseCamera() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override fun onResume() {
        try {
            super.onResume()
            if (canUseCamera()) {
                camera.start()
            }
        } catch (e: IOException) {
            canUseCamera = false
            e.printStackTrace()
            Toast.makeText(this, "Kamera tidak dapat diakses", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            canUseCamera = false
            e.printStackTrace()
            Toast.makeText(this, "Kamera tidak dapat diakses", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        if (canUseCamera()) {
            camera.stop()
            if (ll_loading_dialog != null || isButtonClicked) {
                isButtonClicked = false
                mLoadingFragment.dismiss()
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (canUseCamera()) {
            camera.destroy()
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_info, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_info -> {
                val mOptionFragment = InstructionCameraDialogFragment()
                val mFragmentManager = supportFragmentManager
                mOptionFragment.show(mFragmentManager, InstructionCameraDialogFragment::class.java.simpleName)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    private fun setDetectionMode() {
        sw_detection_mode.setOnCheckedChangeListener { _:CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                camera.disableCameraMode(Modes.CameraMode.SINGLE_CAPTURE)
                camera.setCameraMode(Modes.CameraMode.CONTINUOUS_FRAME)
                btn_capture.visibility = View.GONE
            } else {
                camera.disableCameraMode(Modes.CameraMode.CONTINUOUS_FRAME)
                camera.setCameraMode(Modes.CameraMode.SINGLE_CAPTURE)
                btn_capture.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setFlashModeAuto() {
        iv_flash_mode.setImageResource(R.drawable.flash_auto)
        camera.flash = Modes.Flash.FLASH_AUTO
    }

    @SuppressLint("MissingPermission")
    private fun setFlashModeOn() {
        iv_flash_mode.setImageResource(R.drawable.flash_on)
        camera.flash = Modes.Flash.FLASH_ON
    }

    @SuppressLint("MissingPermission")
    private fun setFlashModeOff() {
        iv_flash_mode.setImageResource(R.drawable.flash_off)
        camera.flash = Modes.Flash.FLASH_OFF
    }

    @SuppressLint("MissingPermission")
    private fun setFlashModeTorch() {
        iv_flash_mode.setImageResource(R.drawable.flash_torch)
        camera.flash = Modes.Flash.FLASH_TORCH
    }

    @SuppressLint("MissingPermission")
    override fun onClick(v: View?) {
        when (v) {
            btn_capture -> if (ll_loading_dialog == null && !isButtonClicked && canUseCamera) try {
                isButtonClicked = true
                camera.capture()
            } catch (e: IOException) {
                if (ll_loading_dialog != null && isButtonClicked) {
                    isButtonClicked = false
                    mLoadingFragment.dismiss()
                }
                Toast.makeText(this, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                if (ll_loading_dialog != null && isButtonClicked) {
                    isButtonClicked = false
                    mLoadingFragment.dismiss()
                }
                Toast.makeText(this, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
            } finally {
                getCameraErrorListener()
            }
            iv_flash_mode -> try {
                flashModes++
                when (flashModes) {
                    1 -> setFlashModeAuto()
                    2 -> setFlashModeOn()
                    3 -> setFlashModeOff()
                    4 -> {
                        setFlashModeTorch()
                        flashModes = 0
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                iv_flash_mode.setImageResource(R.drawable.flash_off)
                Toast.makeText(this, "Lampu kilat tidak didukung", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                iv_flash_mode.setImageResource(R.drawable.flash_off)
                Toast.makeText(this, "Lampu kilat tidak didukung", Toast.LENGTH_LONG).show()
            } finally {
                getCameraErrorListener()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun detectPicture() {
        try {
            camera.addPictureTakenListener {
                mLoadingFragment.show(mFragmentManager, LoadingDialogFragment::class.java.simpleName)
                AsyncTask.execute {
                    val recognitions = classifier.recognizeTakenPicture(it.data)
                    val results = recognitions.joinToString("\n")
                    runOnUiThread {
                        tv_result.text = results
                        Toast.makeText(this, "Selesai!", Toast.LENGTH_SHORT).show()
                        mLoadingFragment.dismiss()
                        isButtonClicked = false
                    }
                }
            }
        } catch (e: IOException) {
            isButtonClicked = false
            if (ll_loading_dialog != null) mLoadingFragment.dismiss()
            e.printStackTrace()
            Toast.makeText(this, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            isButtonClicked = false
            if (ll_loading_dialog != null) mLoadingFragment.dismiss()
            e.printStackTrace()
            Toast.makeText(this, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun detectPreviewFrames() {
        try {
            camera.setContinuousFrameListener(10f) {
                if (detectionTask != null && detectionTask?.status == AsyncTask.Status.RUNNING) {
                    detectionTask?.cancel(true)
                    detectionTask = null
                }
                detectionTask = object : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg params: Void?): Void? {
                        val recognitions = classifier.recognizePreviewFrames(it)
                        val results = recognitions.joinToString("\n")
                        runOnUiThread {
                            tv_result.text = results
                        }
                        return null
                    }
                }.execute()
            }
        } catch (e: IOException) {
            Toast.makeText(this, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCameraErrorListener() {
        camera.addCameraErrorListener { t, errorLevel ->
            when (errorLevel) {
                ErrorLevel.Error -> Toast.makeText(this, "Kamera Error!", Toast.LENGTH_LONG).show()
                ErrorLevel.Warning -> Toast.makeText(this, t.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
