package com.ekosp.indoweb.epesantren.upload

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.ekosp.indoweb.epesantren.AfterAbsen
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.GlobalVar
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.helper.Utils
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.LocationModel
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.activity_upload_image.*
import kotlinx.android.synthetic.main.after_absen.*
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow


class UploadImage : AppCompatActivity() {

    var totalSize: Long = 0
    private var session: SessionManager? = null
    private var datUser: HashMap<String, String>? = null
    private var datPonpes: HashMap<String, String>? = null
    private var TYPE: String? = null
    private var compressedImage: File? = null
    private var locationModel: LocationModel? = null
//    private var dataPonpes: DataPonpes? =null
//    private lateinit var mRunnable: Runnable
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_image)
        ButterKnife.bind(this)
        val intent = intent
        TYPE = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI)
        locationModel = intent.getParcelableExtra(GlobalVar.PARAM_LAST_LOCATION)
        session = SessionManager(this)
        datUser = session!!.dataUserPreference
        datPonpes = session!!.dataPonpesPreference
        lokasiUser.text = datUser?.get(SessionManager.KEY_LOKASI).toString()

        // Checking camera availability
        if (!Utils.isDeviceSupportCamera(applicationContext)) {
            Toast.makeText(
                applicationContext,
                "Sorry! Your device doesn't support camera",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

        try {
            val pInfo = this.packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            val type =TYPE
//            tv_versi_apk!!.text="ePesantren Presensi v.$version"
            info_hdr_absen!!.text="Absen $type"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        setListener()
    }

    private fun setListener() {

        capturePicture.setOnClickListener{
            ImagePicker.with(this)
                    .cameraOnly()
                    .compress(250)         //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(500, 500)  //Final image resolution will be less than 1080 x 1080(Optional)
                    .start { resultCode, data ->
                        when (resultCode) {
                            Activity.RESULT_OK -> {
                                //Image Uri will not be null for RESULT_OK
                                val fileUri = data?.data
                                imgPreview!!.visibility = View.VISIBLE
                                imgPreview.setImageURI(fileUri)

                                //You can get File object from intent
                                compressedImage = ImagePicker.getFile(data)

                                //You can also get File Path from intent
                                val filePath: String? = ImagePicker.getFilePath(data)
                                Log.e(
                                    "Compressed Foto :", String.format(
                                        "Size : %s", getReadableFileSize(
                                            compressedImage!!.length()
                                        )
                                    )
                                )
                                Log.d("Compressed Foto", "Compressed image save in $filePath")
                            }
                            ImagePicker.RESULT_ERROR -> {
                                Toast.makeText(
                                    this@UploadImage,
                                    ImagePicker.getError(data),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Toast.makeText(
                                    this@UploadImage,
                                    "Ambil foto dibatalkan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
        }

        btnUpload.setOnClickListener{
            if (locationModel!!.latitude == 0.0 || locationModel!!.longitude == 0.0) {
                Toast.makeText(
                    this,
                    "Mohon aktifkan GPS Anda dan ulangi proses absen Anda",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Utils.showProgressDialog(this, "mohon tunggu ...").show()
                UploadFileToServer().execute()
            }
        }

        btn_bck.setOnClickListener(){
//            val intent = Intent(this, Homepage::class.java)
//            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }

//        btn_bckhp.setOnClickListener(){
//            val intent = Intent(this, Homepage::class.java)
//            startActivity(intent)
//            finish()
//            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
//        }
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

    /**
     * Uploading the file to server
     */
    private inner class UploadFileToServer : AsyncTask<Void?, Int?, String?>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        private fun uploadFile(): String? {
            var responseString: String? = null
            val httpclient: HttpClient = DefaultHttpClient()
            var domains= datPonpes?.get(SessionManager.KEY_DOMAIN_PONPES).toString()
//            val httppost = if(domains=="demo") HttpPost(ApiClient.FILE_UPLOAD_URL_DEMO) else HttpPost(
//                ApiClient.FILE_UPLOAD_URL
//            )
            val httppost = if(domains=="demo") HttpPost(ApiClient.FILE_UPLOAD_URL_DEMO)
            else if (domains=="ibnumasud") HttpPost(ApiClient.FILE_UPLOAD_URL_WITA)
            else if (domains=="annursulsel") HttpPost(ApiClient.FILE_UPLOAD_URL_WITA)
            else if (domains=="ibrahim") HttpPost(ApiClient.FILE_UPLOAD_URL_WITA)
            else if (domains=="sdiarrahmahpangkajene") HttpPost(ApiClient.FILE_UPLOAD_URL_WITA)
            else if (domains=="appsma") HttpPost(ApiClient.FILE_UPLOAD_URL_ALRIFAIE)
            else if (domains=="akademik") HttpPost(ApiClient.FILE_UPLOAD_URL_TRENDI)
            else HttpPost(ApiClient.FILE_UPLOAD_URL)

            try {
                val entity = AndroidMultiPartEntity { num: Long -> publishProgress((num / totalSize.toFloat() * 100).toInt()) }
                if (compressedImage != null) {
                    entity.addPart("image", FileBody(compressedImage))
                    Log.e(
                        "UPLOAD_IMAGE",
                        "path foto pas upload : " + compressedImage!!.absolutePath
                    )
                } else {
                    //   Toast.makeText(UploadImage.this, "foto tidak disertakan", Toast.LENGTH_SHORT).show();
                }

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
                    StringBody(datUser!![SessionManager.KEY_USERNAME].toString())
                )
                entity.addPart(
                    "kode_sekolah",
                    StringBody(datPonpes!![SessionManager.KEY_KODES].toString())
                )
                entity.addPart(
                    "domain",
                    StringBody(datPonpes!![SessionManager.KEY_DOMAIN_PONPES].toString())
                )
                entity.addPart(
                    "longi",
                    StringBody(locationModel!!.longitude.toString())
                )
                Log.i("UPLOAD", "Longitude:" + locationModel!!.longitude)
                entity.addPart(
                    "lati",
                    StringBody(locationModel!!.latitude.toString())
                )
                Log.i("UPLOAD", "latitude:" + locationModel!!.latitude)
                entity.addPart(
                    "type",
                    StringBody(TYPE)
                )
                entity.addPart(
                    "keterangan",
                    StringBody(inputKeterangan!!.text.toString())
                )
                entity.addPart("lokasi", StringBody(lokasiUser!!.text.toString()))
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
            val pesan = "Berhasil Absen"
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
    private fun showAlert(message: String) {
        Utils.showProgressDialog(this, "mohon tunggu ...").dismiss()

        val builder = AlertDialog.Builder(this)
        builder.setMessage(message).setTitle("Status")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, id -> backToHomepage() }
        val alert = builder.create()
        alert.show()

        // do delete file after alert dialog
        deleteGeneratedFoto()
    }

    private fun backToHomepage(){
//        val intent = Intent(this, Homepage::class.java)
//        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun toAfterAbsen(){
        val intent = Intent(this, AfterAbsen::class.java)
        intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE)

        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun deleteGeneratedFoto() {

        // delete original image
        if (compressedImage != null) if (compressedImage!!.exists()) {
            if (compressedImage!!.delete()) {
                Log.e("Foto Deleted :", compressedImage!!.path)
            }
        }

    }

    private fun startCountdown() {
        val durasi = 2 * 60 * 1000
        countDownTimer = object : CountDownTimer(durasi.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val milis = String.format(
                    "KIRIM ABSEN %d:%d",
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
                        this@UploadImage,
                        "Waktu habis.\nSilahkan absen ulang",
                        Toast.LENGTH_SHORT
                    ).show()
            }

            override fun onFinish() {
                finish()
            }
        }
        countDownTimer.start()
    }

}