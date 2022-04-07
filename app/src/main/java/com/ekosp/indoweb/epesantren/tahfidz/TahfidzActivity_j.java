package com.ekosp.indoweb.epesantren.tahfidz;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ekosp.indoweb.epesantren.R;

import java.util.HashMap;

public class TahfidzActivity_j extends AppCompatActivity {
    private EditText editTextjmlhafalan;
    private EditText editTexthafalan;
    private EditText editTextmurojaah;
    private EditText editTextmurojaahbaru;
    private Button buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tahfidz_j);
        editTextjmlhafalan = (EditText) findViewById(R.id.inputjumlahhafalan);
        editTexthafalan = (EditText) findViewById(R.id.keteranganhafalan);
        editTextmurojaah = (EditText) findViewById(R.id.inputMurojaah);
        editTextmurojaahbaru = (EditText) findViewById(R.id.inputmurojaahbaru);
        buttonAdd = (Button) findViewById(R.id.btnUploadtahfidz);

//        buttonAdd.setOnClickListener(this);
//        buttonView.setOnClickListener(this);
    }

//    private void addMhs(){
//        final String jmlhafalan = editTextjmlhafalan.getText().toString().trim();
//        final String hafalan = editTexthafalan.getText().toString().trim();
//        final String murojaah = editTextmurojaah.getText().toString().trim();
//        final String murojaahbaru = editTextmurojaahbaru.getText().toString().trim();
////        Log.d("kelas",kelas);
//
//        class AddMhs extends AsyncTask<Void,Void,String> {
//            ProgressDialog loading;
//            @Override protected void onPreExecute() {
//                super.onPreExecute();
//                loading = ProgressDialog.show(TahfidzActivity_j.this,"Menambahkan...","Tunggu...",false,false);
//            }
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//                loading.dismiss();
//                Toast.makeText(TahfidzActivity_j.this,s,Toast.LENGTH_LONG).show();
//            }
//            @Override
//            protected String doInBackground(Void... v) {
//                HashMap<String,String> params = new HashMap<>();
//                params.put(konfigurasi.KEY_MHS_JUMLAH,jmlhafalan);
//                params.put(konfigurasi.KEY_MHS_HAFAL,hafalan);
//                params.put(konfigurasi.KEY_MHS_MUROJAAH,murojaah);
//                params.put(konfigurasi.KEY_MHS_MUROJAAHBARU,murojaahbaru);
//
//                RequestHandler rh = new RequestHandler();
//                String res = rh.sendPostRequest(konfigurasi.URL_ADD, params);
//                Log.d("res",res);
//                return res;
//            }
//        }
//        AddMhs ae = new AddMhs();
//        ae.execute();
//    }

//    private void reset(){
//
//    }
//
//    private void AddTahfidz(){
//        final String jmlhafalan = editTextjmlhafalan.getText().toString().trim();
//        final String hafalan = editTexthafalan.getText().toString().trim();
//        final String murojaah = editTextmurojaah.getText().toString().trim();
//        final String murojaahbaru = editTextmurojaahbaru.getText().toString().trim();
//        class AddEmployee extends AsyncTask<Void,Void,String >{
//
//            ProgressDialog loading;
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                loading = ProgressDialog.show(
//                        TahfidzActivity_j.this,
//                        "Menambahkan...",
//                        "Tunggu Sebentar...",
//                        false,false);
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//                loading.dismiss();
//                reset();
//                Toast.makeText(TahfidzActivity_j.this, s, Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            protected String doInBackground(Void... voids) {
//                HashMap<String,String> params = new HashMap<>();
//                params.put(konfigurasi.KEY_EMP_JUMLAH,jmlhafalan);
//                params.put(konfigurasi.KEY_EMP_HAFAL,hafalan);
//                params.put(konfigurasi.KEY_EMP_MUROJAAH,murojaah);
//                params.put(konfigurasi.KEY_EMP_MUROJAAHBARU,murojaahbaru);
//                RequestHandler requestHandler = new RequestHandler();
//                return requestHandler.sendPostRequest(konfigurasi.URL_ADD,params);
//            }
//        }
//
//        new AddEmployee().execute();
//    }
//
//    @Override
//    public void onClick(View v) {
//        if(v == buttonAdd){
//            AddTahfidz();
//        }
////        if(v == buttonView){
////            startActivity(new Intent(this,TampilSemuaMhs.class));
////        }
//    }
}