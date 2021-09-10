package com.ekosp.indoweb.epesantren.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.adapter.ListRekapKehadiranAdapter
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.ApiInterface
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.helper.parseDateToddMMyyyy
import com.ekosp.indoweb.epesantren.laporan.KehadiranTahunanActivity
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.DataUser
import com.ekosp.indoweb.epesantren.model.data_laporan.DataLaporan
import com.ekosp.indoweb.epesantren.model.data_laporan.Rekap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.bottom_sheet_detail_kehadiran.*
import kotlinx.android.synthetic.main.fragment_laporan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class LaporanFragment : Fragment(R.layout.fragment_laporan),
        ListRekapKehadiranAdapter.ClickListener, AdapterView.OnItemSelectedListener{

    private var dataKehadiran: MutableList<Rekap>? = null
    private lateinit var detailKehadiranTahun: DataLaporan
    private lateinit var adapter: ListRekapKehadiranAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<MaterialCardView>

    private lateinit  var session: SessionManager
    private lateinit var dataUser: DataUser
    private lateinit var dataPonpes: DataPonpes

    var sheetBehavior: BottomSheetBehavior<*>? = null

    var bottom_sheet: BottomSheetDialog? = null
    var selectMonth = ""
    var yearsSelect = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadKehadiran()
        setSpinner()
        setListener()
//        setUI()
    }

    private fun setSpinner(){
        sp_month_year!!.setOnItemSelectedListener(this)

        val formatter = SimpleDateFormat("MMMM yyyy")
        val listMonth = arrayListOf<String>()
        Calendar.getInstance().let { calendar ->
            calendar.add(Calendar.MONTH, -11)
            for (i in 0 until 12) {
                listMonth.add(formatter.format(calendar.timeInMillis))
                calendar.add(Calendar.MONTH, 1)
            }
        }

        val aa = ArrayAdapter(requireContext(), R.layout.item_spinner, listMonth)
        aa.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        sp_month_year!!.setAdapter(aa)

    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        selectMonth = sp_month_year.getSelectedItem().toString()
        yearsSelect = selectMonth.substring(selectMonth.length - 4)
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }

    private fun setListener() {
        kehadiran_tahunan.setOnClickListener {
            // buka halaman detail laporan tahun ini
            val i = Intent(requireActivity(), KehadiranTahunanActivity::class.java)
//            i.putExtra("dataKehadiranTahun", detailKehadiranTahun)
            i.putExtra("yearsSelect", yearsSelect)
            activity?.startActivity(i)

        }
    }

    private fun loadKehadiran() {
        rv_kehadiran.setHasFixedSize(true)
        rv_kehadiran.itemAnimator = DefaultItemAnimator()
        rv_kehadiran.layoutManager = LinearLayoutManager(requireContext())

        session = SessionManager(requireContext())
        dataUser = session.getSessionDataUser()
        dataPonpes = session.getSessionDataPonpes()

        val kode_sekolah: String = dataPonpes.getKodes()
        val id_pegawai: String = dataUser.getNip()
        val tahun: String = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val bulan: String = SimpleDateFormat("MM", Locale.getDefault()).format(Date())

        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val call = apiService.getDataLaporan(
                kode_sekolah,
                id_pegawai,
                "BULANAN",
                tahun,
                bulan,
        )

        call.enqueue(object : Callback<DataLaporan?> {
            override fun onResponse(call: Call<DataLaporan?>, response: Response<DataLaporan?>) {
                if (response.isSuccessful) {
                    val persentase = response.body()?.percentase

                    progress_value.text = "$persentase %"
                    progress_hari.text = response.body()?.percentase_hari
                    progres_bar.progress = persentase?.let { it / 100 }!!

                    num_hadir.text = response.body()?.hadir + " Hari"
                    num_ijin.text = response.body()?.izin_cuti + " Hari"
                    num_alpha.text = response.body()?.alpa.toString() + " Hari"
                    num_terlambat.text = response.body()?.terlambat + " Jam"
                    presentase_hadir_tahun_ini.text = response.body()?.hadir_tahun_ini + "%"

                    detailKehadiranTahun = response.body()!!
                    dataKehadiran = response.body()?.rekap

                    adapter = ListRekapKehadiranAdapter(requireContext(), dataKehadiran!!, this@LaporanFragment)
                    rv_kehadiran!!.adapter = adapter
                }
            }

            override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {

            }
        })
    }

    override fun selectKehadiran(data: Rekap) {

        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_detail_kehadiran, null)
        if (sheetBehavior?.getState() === BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }

        when (data.status){
            "Hadir" -> view.findViewById<TextView>(R.id.txt_status).setTextColor(ContextCompat.getColor(requireContext(), R.color.green_pesantren_2))
            "Sakit" -> view.findViewById<TextView>(R.id.txt_status).setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Cuti" -> view.findViewById<TextView>(R.id.txt_status).setTextColor(ContextCompat.getColor(requireContext(), R.color.lime_dark))
            "Keperluan Lain" -> view.findViewById<TextView>(R.id.txt_status).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        view.findViewById<TextView>(R.id.txt_status).text = data.status

        val tglRekap = parseDateToddMMyyyy(data.hari)
        view.findViewById<TextView>(R.id.txt_tgl_kehadiran).text = tglRekap

        view.findViewById<TextView>(R.id.txt_jam_datang).text = data.detail.jam_datang
        view.findViewById<TextView>(R.id.txt_jam_pulang).text = data.detail.jam_pulang
        view.findViewById<TextView>(R.id.txt_lokasi).text = data.detail.lokasi
        view.findViewById<TextView>(R.id.txt_catatan).text = data.detail.catatan_absen

        bottom_sheet = BottomSheetDialog(requireContext())
        bottom_sheet?.setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bottom_sheet?.getWindow()?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        bottom_sheet?.show()
        bottom_sheet?.setOnDismissListener(DialogInterface.OnDismissListener { bottom_sheet = null })

    }

}