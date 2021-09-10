package com.ekosp.indoweb.epesantren.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Eko S.P on 16/10/2018.
 * contact ekosetyopurnomo@gmail.com
 */

@Parcelize
data class LocationModel (
    var longitude: Double?,
    var latitude: Double?
) : Parcelable