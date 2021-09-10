package com.ekosp.indoweb.epesantren.ijin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.util.Pair;

import com.ekosp.indoweb.epesantren.MainActivity;
import com.ekosp.indoweb.epesantren.R;
import com.ekosp.indoweb.epesantren.helper.ApiClient;
import com.ekosp.indoweb.epesantren.helper.ApiInterface;
import com.ekosp.indoweb.epesantren.helper.GlobalVar;
import com.ekosp.indoweb.epesantren.helper.SessionManager;
import com.ekosp.indoweb.epesantren.model.DataIjin;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.ekosp.indoweb.epesantren.model.LocationModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.okhttp.MediaType;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IjinActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "IjinActivity";
    private static final int REQUEST_IJIN = 0;
    private PermissionManager permission;
    private MediaType JSON;
    private SessionManager session;
    private LocationModel lastLocation;
    private ApiInterface apiService;
    private ProgressDialog progressDialog;
    private DataPonpes mDataPonpes;

    private String userName;
    private String kodess;

    @BindView(R.id.input_tgl)
    EditText tgl;

    @BindView(R.id.jenis)
    EditText jenis;

    @BindView(R.id.input_tgl_awal)
    EditText tgl_awal;

    @BindView(R.id.input_tgl_akhir)
    EditText tgl_akhir;

    @BindView(R.id.input_keterangan)
    EditText keterangan;

    @BindView(R.id.btn_submit)
    Button submit;

    @SerializedName("is_correct")
    @Expose
    private Boolean isCorrect;

    private RadioGroup list_opsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijin);

        ButterKnife.bind(this);

        permission = new PermissionManager() {};
        permission.checkAndRequestPermissions(this);

        JSON = MediaType.parse("application/json; charset=utf-8");
        // Session Manager
        session = new SessionManager(this);
        mDataPonpes = session.getSessionDataPonpes();
        Intent intent = getIntent();
        if(intent.getParcelableExtra(GlobalVar.PARAM_LAST_LOCATION)!= null) lastLocation = intent.getParcelableExtra(GlobalVar.PARAM_LAST_LOCATION);

        if (intent.getStringExtra(GlobalVar.PARAM_DATA_USER) != null){
            userName = intent.getStringExtra(GlobalVar.PARAM_DATA_USER);
        }
        if (intent.getStringExtra(GlobalVar.PARAM_KODES_USER) != null){
            kodess = intent.getStringExtra(GlobalVar.PARAM_KODES_USER);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpan();
            }
        });

        list_opsi = findViewById(R.id.radio_group);
        list_opsi.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id){
                    case R.id.cuti:
                        jenis.setText("CUTI");
                        break;
                    case R.id.sakit:
                        jenis.setText("SAKIT");
                        break;
                    case R.id.lain:
                        jenis.setText("LAIN-LAIN");
                        break;
                }
            }
        });

        tgl.setInputType(InputType.TYPE_NULL);

        MaterialDatePicker.Builder<Pair<Long,Long>> builder =MaterialDatePicker.Builder.dateRangePicker();
                builder.setTitleText("Pilih Tanggal");
                final MaterialDatePicker dateRangePicker = builder.build();

        tgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateRangePicker.show(getSupportFragmentManager(),"DATE_PICKER");
            }
        });

        dateRangePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long,Long> selection) {
                Calendar calendar = Calendar.getInstance();
                Calendar calendar_2 = Calendar.getInstance();
                Long startDate = selection.first;
                Long endDate = selection.second;
                calendar.setTimeInMillis(startDate);
                calendar_2.setTimeInMillis(endDate);
                SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
                SimpleDateFormat format_2 = new SimpleDateFormat("yyyy-MM-dd");
                String date = format.format(calendar.getTime())+" - "+format.format(calendar_2.getTime());
                tgl.setText(date);
                tgl_awal.setText(format_2.format(calendar.getTime()));
                tgl_akhir.setText(format_2.format(calendar_2.getTime()));
            }
        });

    }

    public void backToHome (View v){
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public String toString() {
        return "IjinActivity{" +
                "isCorrect=" + isCorrect +'}';
    }

    public void simpan() {
        Log.d(TAG, "Login");

        submit.setEnabled(false);
        progressDialog = new ProgressDialog(IjinActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Mohon Tunggu...");
        progressDialog.show();

        if (!validate()) {
            onSimpanFailed("Tambah Presensi Ijin Gagal\nSilahkan Periksa Data Anda Terlebih Dahulu");
            return;
        }

        executeSimpan();

    }

    public void onSimpanFailed(String t) {
        StyleableToast.makeText(this, ""+t+"", Toast.LENGTH_LONG, R.style.mytoast_danger).show();
//        Toast.makeText(getBaseContext(), ""+t+"", Toast.LENGTH_LONG).show();
        submit.setEnabled(true);
        progressDialog.dismiss();
    }

    public void onSimpanSuccess(String t) {
//        Toast.makeText(getBaseContext(), ""+t+"", Toast.LENGTH_LONG).show();
        StyleableToast.makeText(this, ""+t+"", Toast.LENGTH_LONG, R.style.mytoast).show();
        submit.setEnabled(true);
        progressDialog.dismiss();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void executeSimpan() {

        String kodes = kodess;
        String uname = userName;
        String jenis_ = jenis.getText().toString();
        String tgl_awal_ = tgl_awal.getText().toString();
        String tgl_akhir_ = tgl_akhir.getText().toString();
        String keterangan_ = keterangan.getText().toString();

        apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataIjin> call = apiService.submitIjin(kodes,uname,jenis_,tgl_awal_,tgl_akhir_,keterangan_);
        call.enqueue(new Callback<DataIjin>() {

            @Override
            public void onResponse(Call<DataIjin> call, Response<DataIjin> response) {
                if (response.isSuccessful()) {
                    DataIjin ijin = response.body();
                    Log.i("RETROFIT", ijin.toString());
                    if (ijin.getCorrect()) {
                        onSimpanSuccess(""+ijin.getMessage()+"");
                    } else {
                        onSimpanFailed(""+ijin.getMessage()+"");
                    }
                } else
                    Toast.makeText(IjinActivity.this, "Terjadi gangguan koneksi ke server", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<DataIjin> call, Throwable t) {
                Log.d("RETROFIT", "failed to fetch data from API" + t);
            }

        });

    }

    public boolean validate() {
        boolean valid = true;
        String jenise = jenis.getText().toString();
        String tgl_a = tgl_awal.getText().toString();
        String tgl_ak = tgl_akhir.getText().toString();
        String keterangane_ = keterangan.getText().toString();

        if (jenise.isEmpty()) {
            jenis.setError("Pilih Salah Satu Jenis");
            valid = false;
        } else {
            jenis.setError(null);
        }

        if (tgl_a.isEmpty()) {
            tgl_awal.setError("Pilih Tanggal");
            valid = false;
        } else {
            tgl_awal.setError(null);
        }

        if (tgl_ak.isEmpty()) {
            tgl_akhir.setError("Pilih Tanggal");
            valid = false;
        } else {
            tgl_akhir.setError(null);
        }

        if (keterangane_.isEmpty()) {
            keterangan.setError("Keterangan Tidak Boleh Kosong");
            valid = false;
        } else {
            keterangan.setError(null);
        }

        return valid;
    }

    public void bckHP (View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IJIN) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission.checkResult(requestCode, permissions, grantResults);

    }

}
