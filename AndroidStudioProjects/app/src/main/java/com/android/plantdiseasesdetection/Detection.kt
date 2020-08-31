package com.android.plantdiseasesdetection

data class Detection(
    val name: String,
    val accuracy: Float
) {
    override fun toString(): String {
        return "Nama Penyakit: $name\nProbabilitas: ${accuracy*100}%"
    }
}