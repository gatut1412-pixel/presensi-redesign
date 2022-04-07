package com.ekosp.indoweb.epesantren.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.alarm.AlarmActivity
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.ApiInterface
import com.ekosp.indoweb.epesantren.helper.GlobalVar
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.ijin.IjinActivity
import com.ekosp.indoweb.epesantren.laporan.LaporanWeb
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.DataUser
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_laporan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var session: SessionManager
    private lateinit var dataUser: DataUser
    private lateinit var dataPonpes: DataPonpes
    var numberOfSeconds = 1


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
        setLaporanweb()
        setUI()
//        setAlarm()

    }

    override fun onResume() {
        super.onResume()

        session = SessionManager(requireContext())
        dataUser = session.getSessionDataUser()
        dataPonpes = session.getSessionDataPonpes()

        val today: Date = Calendar.getInstance().getTime() //getting date

        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy") //formating according to my need

        val date: String = formatter.format(today)
        hari_tanggal!!.text=date

        val kodes: String = dataPonpes.getKodes()
        val uname: String = dataUser.getNip()
        val pass: String = "onlogin"
        var yearsSelect = ""


        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val call = apiService.checkLogin(kodes, uname, pass)
        call.enqueue(object : Callback<DataUser?> {
            override fun onResponse(call: Call<DataUser?>, response: Response<DataUser?>) {
                dataUser = response.body()!!
                datang_time.setText(dataUser.getMaxDatang())
                pulang_time.setText(dataUser.getMaxPulang())
                Glide.with(this@HomeFragment).load(dataUser.getPhoto()).into(profile_image)
            }

            override fun onFailure(call: Call<DataUser?>, t: Throwable) {
                Log.d("RETROFIT", "failed to fetch data from API$t")
            }
        })

    }

    private fun setUI() {

        session = SessionManager(requireContext())
        dataUser = session.getSessionDataUser()
        dataPonpes = session.getSessionDataPonpes()

        info_nama_ponpes.setText("Ponpes " + dataPonpes.getNamaPonpes())
        info_nama_user.setText(dataUser.getNama())
        info_jabatan.setText(dataUser.getJabatan())

    }

    private fun setLaporanweb() {
        laporanweb.setOnClickListener {
            val intent = Intent(requireActivity(), LaporanWeb::class.java)
            intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.username)
            intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.kodes)
            startActivity(intent)
        }
    }

//    private fun setAlarm() {
//        setelpengingat.setOnClickListener {
//            val go = Intent(requireActivity(), AlarmActivity::class.java)
//            startActivity(go)
//        }
//    }

//    private fun setTahfidz() {
//        tahfidz.setOnClickListener {
//            activity?.let {
//                val intent = Intent(it, TahfidzActivity_k::class.java)
//                intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.username)
//                intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.kodes)
//                it.startActivity(intent)
//            }
//        }
//    }

    private fun setListener() {

        fun viewReport(v: View?) {
            val intent = Intent(activity, LaporanWeb::class.java)
            intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.username)
            intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.kodes)
            startActivity(intent)
//            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }

        fun ijinCuti(v: View?) {
            val intent = Intent(activity, IjinActivity::class.java)
            intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.username)
            intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.kodes)
            startActivity(intent)
//            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }

    }

}