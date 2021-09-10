package com.ekosp.indoweb.epesantren.model.data_laporan

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Detail(
    var jam_datang: String,
    var jam_pulang: String,
    var lokasi: String,
    var catatan_absen: String
)