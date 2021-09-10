package com.ekosp.indoweb.epesantren.laporan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.adapter.ListRekapKehadiranAdapter
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.ApiInterface
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.DataUser
import com.ekosp.indoweb.epesantren.model.data_laporan.DataLaporan
import com.ekosp.indoweb.epesantren.model.data_laporan.Detail
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.*
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.num_alpha
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.num_hadir
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.num_ijin
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.num_terlambat
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.progres_bar
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.progress_hari
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.progress_value
import kotlinx.android.synthetic.main.activity_kehadiran_tahunan.view.*
import kotlinx.android.synthetic.main.fragment_laporan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class KehadiranTahunanActivity : AppCompatActivity() {
    private lateinit  var session: SessionManager
    private lateinit var dataUser: DataUser
    private lateinit var dataPonpes: DataPonpes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kehadiran_tahunan)

        getDataLaporanTahunan()
    }

    private fun getDataLaporanTahunan(){
        val yearSelect = intent.getStringExtra("yearsSelect")
        val yearNow = Calendar.getInstance().get(Calendar.YEAR)

        tahun.text = yearNow.toString()
//        session = SessionManager(requireContext())
        dataUser = session.getSessionDataUser()
        dataPonpes = session.getSessionDataPonpes()

        val tahun: String = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val kode_sekolah: String = dataPonpes.getKodes()
        val id_pegawai: String = dataUser.getNip()


        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val call = apiService.getDataLaporan(
                kode_sekolah,
                id_pegawai,
                "TAHUNAN",
                tahun,
                "",
        )

        call.enqueue(object : Callback<DataLaporan?> {
            override fun onResponse(call: Call<DataLaporan?>, response: Response<DataLaporan?>) {
                if (response.isSuccessful) {
                    val persentase = response.body()?.percentase

                    progress_value.text = "$persentase%"
                    progress_hari.text = response.body()?.percentase_hari
                    progres_bar.progress = persentase?.let { it/100 }!!

                    num_hadir.text = response.body()?.hadir + " Hari"
                    num_ijin.text = response.body()?.izin_cuti + " Hari"
                    num_alpha.text = response.body()?.alpa.toString() + " Hari"
                    num_terlambat.text = response.body()?.terlambat + " Jam"
                }
            }

            override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {

            }
        })
    }
}