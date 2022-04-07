// https://github.com/klaasnotfound/LocationAssistant
/*
 *    Copyright 2017 Klaas Klasing (klaas [at] klaasnotfound.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.ekosp.indoweb.epesantren

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.location.LocationManager
import com.ekosp.indoweb.epesantren.model.DataUser
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.MainActivity
import butterknife.BindView
import com.ekosp.indoweb.epesantren.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.os.Bundle
import butterknife.ButterKnife
import com.instacart.library.truetime.TrueTimeRx
import android.widget.Toast
import android.content.Intent
import com.ekosp.indoweb.epesantren.locator.MyLocationActivity
import com.ekosp.indoweb.epesantren.helper.GlobalVar
import android.content.DialogInterface
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.ijin.IjinActivity
import com.ekosp.indoweb.epesantren.model.DeviceData
import kotlinx.android.synthetic.main.activity_home_page.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private var manager: LocationManager? = null
    private var session: SessionManager? = null
    private var dataUser: DataUser? = null
    private var dataPonpes: DataPonpes? = null
    var numberOfSeconds = 1
    private val TAG = MainActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        ButterKnife.bind(this)
        if (!TrueTimeRx.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized.", Toast.LENGTH_SHORT).show()
            return
        }
        manager = getSystemService(LOCATION_SERVICE) as LocationManager
        session = SessionManager(this)
        dataUser = session!!.sessionDataUser
        dataPonpes = session!!.sessionDataPonpes

//        infoNamaPonpes.setText("Ponpes "+dataPonpes.getNamaPonpes());
//        infoNamaUser.setText(dataUser.getNama());
//        infoJabatan.setText(dataUser.getJabatan());
        setListener()
    }

    private fun setListener() {

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bn_main);
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home_menu-> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.open_home_page)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.hadir_menu-> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.open_laporan_page)
                    return@setOnNavigationItemSelectedListener true
                }

//                R.id.tahfidz_menu-> {
//                    findNavController(R.id.nav_host_fragment).navigate(R.id.open_tahfidz_page)
//                    return@setOnNavigationItemSelectedListener true
//                }

                R.id.profil_menu-> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.open_profile_page)
                    return@setOnNavigationItemSelectedListener true
                }

            }
            false

        }

    }


    fun checkOut(v: View?) {
        if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        } else {
            val intent = Intent(this, MyLocationActivity::class.java)
            intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.PARAM_PULANG)
            startActivity(intent)
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    fun checkIn(v: View?) {
        if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        } else {
            val intent = Intent(this, MyLocationActivity::class.java)
            intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.PARAM_DATANG)
            startActivity(intent)
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    fun ijinCuti(v: View?) {
        val intent = Intent(this, IjinActivity::class.java)
        intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser?.getUsername())
        intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes?.getKodes())
        startActivity(intent)
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    //    public void viewReport(View v) {
    //        Intent intent = new Intent(this, LaporanWeb.class);
    //        intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.getUsername());
    //        intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.getKodes());
    //        startActivity(intent);
    //
    //        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    //    }
    //
    //    public void ijinCuti(View v) {
    //        Intent intent = new Intent(this, IjinActivity.class);
    //        intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.getUsername());
    //        intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.getKodes());
    //        startActivity(intent);
    //
    //        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    //    }
    fun doLogout() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Akun?")
            .setMessage("Anda akan keluar akun ePesantren")
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes) { arg0: DialogInterface?, arg1: Int ->
                session!!.logoutUser(dataUser!!.nip, dataPonpes!!.kodes)
                super@MainActivity.onBackPressed()
            }.create().show()
    }

    override fun onResume() {
        super.onResume()
        detectIMEIandPhoneNUmber()
    }

    @SuppressLint("MissingPermission")
    private fun detectIMEIandPhoneNUmber() {
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        try {
            val IMEI = tm.deviceId
            val androiID = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
            session!!.saveDeviceData(DeviceData(IMEI, androiID))
            Log.e("data_hp", "imei = $IMEI")
            Log.e("data_hp", "android id = $androiID")
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Anda yakin ingin menutup aplikasi?")
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes) { arg0: DialogInterface?, arg1: Int -> super@MainActivity.onBackPressed() }
            .create().show()
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Sepertinya GPS mati, mohon hidupkan GPS untuk bisa melakukan absensi!")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog: DialogInterface?, id: Int ->
                startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            }
            .setNegativeButton("No") { dialog: DialogInterface, id: Int -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }
}