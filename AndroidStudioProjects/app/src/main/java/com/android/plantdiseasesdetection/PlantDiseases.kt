package com.android.plantdiseasesdetection

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlantDiseases (var picture: Int?,
                          var name: String?,
                          var detail: Int?
) : Parcelable