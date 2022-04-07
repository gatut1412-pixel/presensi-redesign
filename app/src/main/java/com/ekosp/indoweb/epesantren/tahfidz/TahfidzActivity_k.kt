package com.ekosp.indoweb.epesantren.tahfidz

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ekosp.indoweb.epesantren.AfterAbsen
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.GlobalVar
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.upload.AndroidMultiPartEntity
import kotlinx.android.synthetic.main.activity_tahfidz_k.*
import kotlinx.android.synthetic.main.activity_upload_image.*
import kotlinx.android.synthetic.main.activity_upload_image.btnUpload
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class TahfidzActivity_k : AppCompatActivity() {
    var totalSize: Long = 0
    private var session: SessionManager? = null
    private var dataUser: HashMap<String, String>? = null
    private var dataPonpes: HashMap<String, String>? = null
    private lateinit var countDownTimer: CountDownTimer
//    private var userName: String? = null
//    private var kodess: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tahfidz_k)

//        val intent = intent
//        if (intent.getStringExtra(GlobalVar.PARAM_DATA_USER) != null) {
//            userName = intent.getStringExtra(GlobalVar.PARAM_DATA_USER)
//        }
//        if (intent.getStringExtra(GlobalVar.PARAM_KODES_USER) != null) {
//            kodess = intent.getStringExtra(GlobalVar.PARAM_KODES_USER)
//        }
//        dataUser = session!!.dataUserPreference
//        dataPonpes = session!!.dataPonpesPreference
    }

    private fun startCountdown() {
        val durasi = 2 * 60 * 1000
        countDownTimer = object : CountDownTimer(durasi.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val milis = String.format(
                        "Upload Tahfidz %d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                                millisUntilFinished
                                        )
                                )
                )
//                tvCountdown!!.text = milis
                btnUpload!!.text = milis

                if (millisUntilFinished <= 1000)
                    Toast.makeText(
                            this@TahfidzActivity_k,
                            "Waktu habis.\nSilahkan upload ulang",
                            Toast.LENGTH_SHORT
                    ).show()
            }

            override fun onFinish() {
                finish()
            }
        }
        countDownTimer.start()
    }

    override fun onResume() {
        super.onResume()
        startCountdown()
    }

    public override fun onPause() {
        super.onPause()
        countDownTimer!!.cancel()
//        Toast.makeText(this, "Waktu habis.\nSilahkan checkin/checkout ulang", Toast.LENGTH_SHORT).show()
    }

    private inner class UploadFileToServer : AsyncTask<Void?, Int?, String?>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        private fun uploadFile(): String? {
            var responseString: String? = null
            val httpclient: HttpClient = DefaultHttpClient()
            var domains= dataPonpes?.get(SessionManager.KEY_DOMAIN_PONPES).toString()
            val httppost = if(domains=="demo") HttpPost(ApiClient.UPLOAD_TAHFIDZ_URL_DEMO) else HttpPost(
                    ApiClient.FILE_UPLOAD_URL
            )

            try {
                val entity = AndroidMultiPartEntity { num: Long -> publishProgress((num / totalSize.toFloat() * 100).toInt()) }
//                if (compressedImage != null) {
//                    entity.addPart("image", FileBody(compressedImage))
//                    Log.e(
//                            "UPLOAD_IMAGE",
//                            "path foto pas upload : " + compressedImage!!.absolutePath
//                    )
//                } else {
//                    //   Toast.makeText(UploadImage.this, "foto tidak disertakan", Toast.LENGTH_SHORT).show();
//                }

                // Extra parameters if you want to pass to server
                entity.addPart(
                        "device_id",
                        StringBody(session!!.deviceData.deviceId)
                )
                entity.addPart(
                        "imei",
                        StringBody(session!!.deviceData.imei)
                )
                entity.addPart(
                        "id_pegawai",
                        StringBody(dataUser!![SessionManager.KEY_USERNAME].toString())
                )
                entity.addPart(
                        "kode_sekolah",
                        StringBody(dataPonpes!![SessionManager.KEY_KODES].toString())
                )
                entity.addPart(
                        "domain",
                        StringBody(dataPonpes!![SessionManager.KEY_DOMAIN_PONPES].toString())
                )
                entity.addPart(
                        "jumlah",
                        StringBody(inputKeteranganjumlahhafalan!!.text.toString())
                )
                entity.addPart(
                        "hafal",
                        StringBody(inputKeteranganhafalan!!.text.toString())
                )
                entity.addPart(
                        "murojaah",
                        StringBody(inputKeteranganMurojaah!!.text.toString())
                )
                entity.addPart(
                        "baru",
                        StringBody(inputKeteranganmurojaahbaru!!.text.toString())
                )
//                entity.addPart(
//                        "keterangan",
//                        StringBody(inputKeterangan!!.text.toString())
//                )
//                entity.addPart("lokasi", StringBody(lokasiUser!!.text.toString()))
                totalSize = entity.contentLength
                httppost.entity = entity

                // Making server call
                val response = httpclient.execute(httppost)
                val r_entity = response.entity
                val statusCode = response.statusLine.statusCode
                responseString = if (statusCode == 200) {
                    // Server response
                    EntityUtils.toString(r_entity)
                } else {
                    ("Error occurred! Http Status Code: "
                            + statusCode)
                }
            } catch (e: ClientProtocolException) {
                responseString = e.toString()
            } catch (e: IOException) {
                responseString = e.toString()
            }
            return responseString
        }

        override fun onPostExecute(result: String?) {
            Log.e("CLOCKING", "result: $result")
            val pesan = "Berhasil Upload"
//            showAlert(pesan)
            super.onPostExecute(result)
            toAfterAbsen()
        }

        override fun doInBackground(vararg p0: Void?): String? {
            return uploadFile()
        }
    }

    /**
     * Method to show alert dialog
     */
//    private fun showAlert(message: String) {
//        Utils.showProgressDialog(this, "mohon tunggu ...").dismiss()
//
//        val builder = AlertDialog.Builder(this)
//        builder.setMessage(message).setTitle("Status")
//                .setCancelable(false)
//                .setPositiveButton("OK") { dialog, id -> backToHomepage() }
//        val alert = builder.create()
//        alert.show()
//
//        // do delete file after alert dialog
//        deleteGeneratedFoto()
//    }



//    private fun backToHomepage(){
//        val intent = Intent(this, Homepage::class.java)
//        startActivity(intent)
//        finish()
//        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
//    }

    private fun toAfterAbsen(){
        val intent = Intent(this, AfterAbsen::class.java)
//        intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

}