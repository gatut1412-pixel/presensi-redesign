package com.ekosp.indoweb.epesantren.helper;

import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by eko.purnomo on 28/07/2017.
 * You can contact me at : ekosetyopurnomo@gmail.com
 * or for more detail at  : http://ekosp.com
 */

public class ApiClient {

//    public static final String BASE_URL = "https://mobile.epesantren.co.id/"; //server mobile epesantren
    public static final String BASE_URL = "http://ujipresensi.my.id/mobile-ep/"; //server mobile epesantren
    public static final String BASE_URL_DEMO = "https://demo.epesantren.co.id/"; //server demo epesantren
    public static final String BASE_URL_ALRIFAIE = "https://appsma.alrifaie2.sch.id/";
    public static final String BASE_URL_TRENDI = "https://akademik.daarel-ihsaan.com/";
    public static final String BASE_REPORT_URL = BASE_URL + "rest-api/redirect_laporan_web.php?id_pegawai=";
    private static Retrofit retrofit = null;
    // File upload url (replace the ip with your server address)
//    public static final String FILE_UPLOAD_URL = BASE_URL + "rest-api/fileUpload.php";
    public static final String FILE_UPLOAD_URL = BASE_URL + "rest-api/fileUpload_coba.php";
    public static final String FILE_UPLOAD_URL_DEMO = BASE_URL_DEMO + "rest-api/fileUpload_coba.php";
    public static final String FILE_UPLOAD_URL_WITA = BASE_URL + "rest-api/fileUpload_coba_wita.php";
    public static final String FILE_UPLOAD_URL_ALRIFAIE = BASE_URL_ALRIFAIE + "rest-api/fileUpload_coba.php";
    public static final String FILE_UPLOAD_URL_TRENDI = BASE_URL_TRENDI + "rest-api/fileUpload_coba.php";
    public static final String UPLOAD_TAHFIDZ_URL_DEMO = BASE_URL_DEMO + "rest-api/uploadtahfidz_coba.php";

    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "uploads/";


    public static Retrofit getClient() {
        if (retrofit == null) {
            // add to fix java.net.SocketTimeoutException: timeout
            //https://stackoverflow.com/questions/39219094/sockettimeoutexception-in-retrofit
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS).build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }

}
