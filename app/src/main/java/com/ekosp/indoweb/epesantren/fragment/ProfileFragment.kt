package com.ekosp.indoweb.epesantren.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ekosp.indoweb.epesantren.MainActivity
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.ApiInterface
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.DataUser
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.muddz.styleabletoast.StyleableToast
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.bottom_sheet_logout.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.profile_image
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    //#1 Defining a BottomSheetBehavior instance
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CardView>
    private lateinit  var session: SessionManager
    private lateinit var dataUser: DataUser
    private lateinit var dataPonpes: DataPonpes

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //#2 Initializing the BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        setListener()
        setUI()

    }

    override fun onResume() {
        super.onResume()
        session = SessionManager(requireContext())
        dataUser = session.getSessionDataUser()
        dataPonpes = session.getSessionDataPonpes()

        val kodes: String = dataPonpes.getKodes()
        val uname: String = dataUser.getNip()
        val pass: String = "onlogin"


        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val call = apiService.checkLogin(kodes, uname, pass)
        call.enqueue(object : Callback<DataUser?> {
            override fun onResponse(call: Call<DataUser?>, response: Response<DataUser?>) {
                dataUser = response.body()!!
                info_nama_user_2.setText(dataUser.getNama())
                jabatan.setText(dataUser.getJabatan())
                nip.setText(dataUser.getNip())
                email.setText(dataUser.getEmail())
                no_ponsel.setText(dataUser.getPhone())

                Glide.with(this@ProfileFragment).load(dataUser.getPhoto()).into(profile_image)
                Glide.with(this@ProfileFragment)
                    .load(dataUser.getPhoto())
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(50, 3)))
                    .into(foto_blur)
            }

            override fun onFailure(call: Call<DataUser?>, t: Throwable) {
                Log.d("RETROFIT", "failed to fetch data from API$t")
            }
        })

        val call_2 = apiService.checkPonpes(kodes)
        call_2.enqueue(object : Callback<DataPonpes?> {
            override fun onResponse(call_2: Call<DataPonpes?>, response: Response<DataPonpes?>) {
                dataPonpes = response.body()!!
                pesantren.setText(dataPonpes.getNamaPonpes())
            }

            override fun onFailure(call_2: Call<DataPonpes?>, t: Throwable) {
                Log.d("RETROFIT", "failed to fetch data from API$t")
            }
        })

    }

    private fun setUI() {

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.peekHeight = 5

    }

    private fun setListener() {

        btn_logout.setOnClickListener {
            val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_COLLAPSED
            else
                BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.state = state
        }


        btn_batal.setOnClickListener{
//            Toast.makeText(requireContext(), "Klik button batal", Toast.LENGTH_SHORT).show()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        btn_keluar.setOnClickListener{
//            Toast.makeText(requireContext(), "Klik button keluar", Toast.LENGTH_SHORT).show()
            //        Toast.makeText(this, "Data History Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT).show();
            StyleableToast.makeText(
                requireContext(),
                "Berhasil Keluar",
                Toast.LENGTH_SHORT,
                R.style.mytoast
            ).show()
            (activity as MainActivity).finish()
        }


    }

}