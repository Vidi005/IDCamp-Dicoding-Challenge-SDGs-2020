package com.android.plantdiseasesdetection

import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detect_with_image.*
import java.io.IOException

class DetectWithImageActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val REQUEST_GALLERY_CODE = 110
        private const val MODEL_INPUT_SIZE = 224
        private const val STATE_PICTURE = "state_picture"
        private const val STATE_RESULTS = "state_results"
    }

    private lateinit var classifier: Classification
    private var picture: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect_with_image)

        if (supportActionBar != null) {
            (supportActionBar as ActionBar).title = "Deteksi dengan Gambar"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        classifier = Classification(assets)
        if (picture != null) {
            picture = Bitmap.createScaledBitmap(picture!!, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true)
            iv_picture.setImageBitmap(picture)
        }

        btn_import.setOnClickListener(this)

        if (savedInstanceState != null) {
            if (picture != null) {
                val ivResult = savedInstanceState.getParcelable<Bitmap>(STATE_PICTURE) as Bitmap
                iv_picture.setImageBitmap(ivResult)
            }
            val tvResults = savedInstanceState.getString(STATE_RESULTS) as String
            tv_img_result.text = tvResults
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_PICTURE, picture)
        outState.putString(STATE_RESULTS, tv_img_result.text.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_info, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_info -> {
                val mOptionFragment = InstructionImageDialogFragment()
                val mFragmentManager = supportFragmentManager
                mOptionFragment.show(mFragmentManager, InstructionImageDialogFragment::class.java.simpleName)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_import) {
            val callGalleryIntent = Intent(Intent.ACTION_PICK)
            callGalleryIntent.type = "image/*"
            startActivityForResult(callGalleryIntent, REQUEST_GALLERY_CODE)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY_CODE) {
            if (data != null) {
                val uri = data.data
                try {
                    picture = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    try {
                        iv_picture.setImageBitmap(picture)
                        val mLoadingFragment = LoadingDialogFragment()
                        val mFragmentManager = supportFragmentManager
                        mLoadingFragment.show(mFragmentManager, LoadingDialogFragment::class.java.simpleName)
                        AsyncTask.execute {
                            val recognitions = picture?.let { classifier.recognizeImage(it) }
                            val results = recognitions?.joinToString("\n")
                            runOnUiThread {
                                tv_img_result.text = results
                                mLoadingFragment.dismiss()
                                Toast.makeText(this, "Selesai!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: IOException) {
                        Toast.makeText(this, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, e.printStackTrace().toString(), Toast.LENGTH_LONG).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Galeri tidak dapat diakses", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Galeri tidak dapat diakses", Toast.LENGTH_LONG).show()
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
