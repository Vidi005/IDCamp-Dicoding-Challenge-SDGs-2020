package com.android.plantdiseasesdetection

import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_list_diseases_category.*

class ListDiseasesCategory : AppCompatActivity() {

    companion object {
        private const val STATE_SEARCH_VIEW = "state_search_view"
    }

    private var plantDiseases: ArrayList<PlantDiseases> = arrayListOf()
    private var plantDiseasesAdapter = PlantDiseasesAdapter(plantDiseases)
    private lateinit var searchString: String
    private lateinit var searchView : SearchView
    private lateinit var dataPicture: TypedArray
    private lateinit var dataName: Array<String>
    private lateinit var dataDetail: TypedArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_diseases_category)

        if (supportActionBar != null) {
            (supportActionBar as ActionBar).title = "Daftar Penyakit Tanaman"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rv_list_diseases.setHasFixedSize(true)

        addItem()
        showRecyclerGrid()

        if (savedInstanceState != null) {
            val searchResults = savedInstanceState.getString(STATE_SEARCH_VIEW) as String
            searchString = searchResults
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        searchString = searchView.query.toString()
        outState.putString(STATE_SEARCH_VIEW, searchString)
    }

    private fun showRecyclerGrid() {
        rv_list_diseases.layoutManager = GridLayoutManager(this, 2)
        rv_list_diseases.adapter = plantDiseasesAdapter

        plantDiseasesAdapter.setOnItemClickCallback(object : PlantDiseasesAdapter.OnItemClickCallback {
            override fun onItemClicked(data: PlantDiseases) {
                showSelectedPlantDisease(data)
            }
        })
    }

    private fun showSelectedPlantDisease(plantDisease: PlantDiseases) {
        val moveWithObjectIntent = Intent(this@ListDiseasesCategory, ListDiseasesDetail::class.java)
        moveWithObjectIntent.putExtra(ListDiseasesDetail.EXTRA_PLANT_DISEASE, plantDisease)
        startActivity(moveWithObjectIntent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun addItem(): ArrayList<PlantDiseases> {
        prepare()
        for (position in dataName.indices) {
            val plantDisease = PlantDiseases(
                dataPicture.getResourceId(position, -1),
                dataName[position],
                dataDetail.getResourceId(position, -1)
            )
            plantDiseases.add(plantDisease)
        }
        return plantDiseases
    }

    private fun prepare() {
        dataPicture = resources.obtainTypedArray(R.array.leaves_picture)
        dataName = resources.getStringArray(R.array.name)
        dataDetail = resources.obtainTypedArray(R.array.detail_layout)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem : MenuItem = menu!!.findItem(R.id.app_bar_search)
        searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Cari Nama Penyakit Tanaman"
        searchView.setOnQueryTextListener(object  : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(nextText: String?): Boolean {
                plantDiseasesAdapter.filter.filter(nextText)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
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
