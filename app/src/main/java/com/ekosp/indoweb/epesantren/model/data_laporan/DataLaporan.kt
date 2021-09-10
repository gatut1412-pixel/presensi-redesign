package com.ekosp.indoweb.epesantren.model.data_laporan

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class DataLaporan(
    var hadir: String,
    var izin_cuti: String,
    var alpa: Int,
    var terlambat: String,
    var percentase: Float,
    var percentase_hari: String,
    var hadir_tahun_ini: String,
    var rekap: MutableList<Rekap> ?
)