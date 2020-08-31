package com.android.plantdiseasesdetection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.util.*
import kotlin.collections.ArrayList

class PlantDiseasesAdapter (private var listPlantDiseases: ArrayList<PlantDiseases>) : RecyclerView.Adapter<PlantDiseasesAdapter.GridViewHolder>(), Filterable {

    private lateinit var onItemClickCallback: OnItemClickCallback
    private var filterListPlantDiseases: ArrayList<PlantDiseases> = listPlantDiseases

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val itemSearch = constraint.toString()
                if (itemSearch.isEmpty()) {
                    filterListPlantDiseases = listPlantDiseases
                } else {
                    val itemList = ArrayList<PlantDiseases>()
                    for (item in listPlantDiseases) {
                        if (item.name?.toLowerCase(Locale.ROOT)?.contains(itemSearch.toLowerCase(Locale.ROOT))!!) {
                            itemList.add(item)
                        }
                    }
                    filterListPlantDiseases = itemList
                }
                val filterResults = FilterResults()
                filterResults.values = filterListPlantDiseases
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterListPlantDiseases = results?.values as ArrayList<PlantDiseases>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_list_diseases, parent, false)
        return GridViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filterListPlantDiseases.size
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val plantDisease = filterListPlantDiseases[position]
        Glide.with(holder.itemView.context)
            .load(filterListPlantDiseases[position].picture)
            .apply(RequestOptions().override(300, 300))
            .into(holder.ivPicture)
        with(holder) {
            tvName.text = plantDisease.name

            itemView.setOnClickListener { onItemClickCallback.onItemClicked(filterListPlantDiseases[holder.adapterPosition]) }
        }
    }

    inner class GridViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        var ivPicture: ImageView = itemView.findViewById(R.id.iv_item_picture)
        var tvName: TextView = itemView.findViewById(R.id.tv_item_name)
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: PlantDiseases)
    }
}