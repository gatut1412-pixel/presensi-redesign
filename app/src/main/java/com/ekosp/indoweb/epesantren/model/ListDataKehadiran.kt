package com.ekosp.indoweb.epesantren.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ListDataKehadiran(val tgl_hadir: String, val status: String) : Parcelable
