package com.android.plantdiseasesdetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import kotlinx.android.synthetic.main.activity_list_diseases_detail.*

class ListDiseasesDetail : AppCompatActivity() {

    companion object {
        const val EXTRA_PLANT_DISEASE = "extra_plant_disease"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_diseases_detail)

        if (supportActionBar != null) {
            (supportActionBar as ActionBar).title = "Detail Penyakit Tanaman"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val plantDisease = intent.getParcelableExtra(EXTRA_PLANT_DISEASE) as PlantDiseases
        val image = plantDisease.picture
        val namePlantDisease = plantDisease.name.toString()
        val detail = plantDisease.detail
        image?.let { iv_picture_received.setImageResource(it) }
        tv_name_received.text = namePlantDisease
        if (detail != null) {
            vs_detail_received.layoutResource = detail
            vs_detail_received.inflate()
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
