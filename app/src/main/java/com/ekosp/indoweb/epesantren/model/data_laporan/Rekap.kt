package com.ekosp.indoweb.epesantren.model.data_laporan

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Rekap(
    var hari: String,
    var status: String,
    var detail: Detail
)