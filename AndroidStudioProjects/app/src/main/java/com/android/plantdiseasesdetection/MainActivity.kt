package com.android.plantdiseasesdetection

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iv_detection.setOnClickListener(this)
        iv_list.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        setMode(item?.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun setMode(selectedMode: Int?) {
        if (selectedMode == R.id.action_about) {
            val moveAboutActivity = Intent (this@MainActivity, AboutActivity::class.java)
            startActivity(moveAboutActivity)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_detection -> {
                val mOptionFragment = OptionDialogFragment()
                val mFragmentManager = supportFragmentManager
                mOptionFragment.show(mFragmentManager, OptionDialogFragment::class.java.simpleName)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            iv_list -> {
                val moveListDiseasesIntent = Intent(this@MainActivity, ListDiseasesCategory::class.java)
                startActivity(moveListDiseasesIntent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }
}
