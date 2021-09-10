package com.ekosp.indoweb.epesantren;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.ekosp.indoweb.epesantren.helper.ApiClient;
import com.ekosp.indoweb.epesantren.helper.ApiInterface;
import com.ekosp.indoweb.epesantren.helper.SessionManager;
import com.ekosp.indoweb.epesantren.model.AndroidVersionResponse;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.okhttp.MediaType;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPonpesActivity extends Activity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "LoginPonpesActivity";
    private static final int REQUEST_LANJUT = 0;
    private MediaType JSON;
    private ProgressDialog progressDialog;
    private DataPonpes mDataPonpes;
    private SessionManager session;
    private String latestAppVersion = "";
    private String thisAppVersion = "";

    @BindView(R.id.input_kodes)
    EditText _kodesText;
    @BindView(R.id.btn_lanjut)
    ImageView _lanjutButton;
    @BindView(R.id.tv_versi_apk)
    TextView _versiApk;

    private PermissionManager permission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ponpes);
        ButterKnife.bind(this);

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        JSON = MediaType.parse("application/json; charset=utf-8");
        // Session Manager
        session = new SessionManager(this);

        _lanjutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanjut();
            }
        });

        String kodes = session.getSavedKodes();
        if (kodes != null) {
            _kodesText.setText(kodes);
        }

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            _versiApk.setText("versi " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        checkForLatestAppVersion();

    }

    public void hapusKodes(View v) {
        session.deleteSavedKodes();
        _kodesText.setText("");
//        Toast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT).show();
        StyleableToast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT, R.style.mytoast).show();
    }

    public void lanjut() {
        Log.d(TAG, "Lanjut");

        _lanjutButton.setEnabled(false);
        progressDialog = new ProgressDialog(LoginPonpesActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Mohon Tunggu...");
        progressDialog.show();

        if (!validate()) {
            onLanjutFailed();
            return;
        }
        executeLanjut();

    }

    private void executeLanjut() {
        String kodes = _kodesText.getText().toString();


        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataPonpes> call = apiService.checkPonpes(kodes);
        call.enqueue(new Callback<DataPonpes>() {

            @Override
            public void onResponse(Call<DataPonpes> call, Response<DataPonpes> response) {
                if (response.isSuccessful()) {
                    mDataPonpes = response.body();
                    Log.i("RETROFIT", mDataPonpes.toString());
                    if (mDataPonpes.getCorrect()) {
                        gotoLoginActivity(mDataPonpes);
                    } else {
                        onLanjutFailed();
                    }
                } else
//                    Toast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT).show();
                    StyleableToast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
            }

            @Override
            public void onFailure(Call<DataPonpes> call, Throwable t) {
                Log.d("RETROFIT", "failed to fetch data from API" + t);
            }
        });

    }

    private void isThisAppLatestVersion(String latestAppVersion){


        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            thisAppVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!thisAppVersion.equalsIgnoreCase(latestAppVersion)){
            // close apps, need updated apps
            AlertDialog alertDialog = new AlertDialog.Builder(LoginPonpesActivity.this).create();
            alertDialog.setTitle("Info");
            alertDialog.setMessage("Anda Membutuhkan Versi App Terbaru. Versi "+latestAppVersion);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            alertDialog.show();
        }

    }

    private void checkForLatestAppVersion() {

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<AndroidVersionResponse> call = apiService.getAppsVersion();
        call.enqueue(new Callback<AndroidVersionResponse>() {

            @Override
            public void onResponse(Call<AndroidVersionResponse> call, Response<AndroidVersionResponse> response) {
                if (response.isSuccessful()) {
                    latestAppVersion = response.body().getData().getVersionName();
                    isThisAppLatestVersion(latestAppVersion);
                } else
//                    Toast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT).show();
                    StyleableToast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
            }

            @Override
            public void onFailure(Call<AndroidVersionResponse> call, Throwable t) {
                Log.d("RETROFIT", "failed to fetch data from API" + t);
            }
        });

//        return latestAppVersion;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LANJUT) {
            if (resultCode == RESULT_OK) {
                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the LaporanWeb
        moveTaskToBack(true);

    }


    public void onLanjutFailed() {
        StyleableToast.makeText(this, "Masukan Kode Pesantren Yang Terdaftar", Toast.LENGTH_LONG, R.style.mytoast_danger).show();
//        Toast.makeText(getBaseContext(), "Kode Sekolah Belum Terdaftar\nSilakan Cek Detail Data Anda Terlebih Dahulu", Toast.LENGTH_LONG).show();
        _lanjutButton.setEnabled(true);
        progressDialog.dismiss();
    }

    public boolean validate() {
        boolean valid = true;
        String kodes = _kodesText.getText().toString();

        if (kodes.isEmpty()) {
            _kodesText.setError("Masukan Kode Pesantren Yang Valid");
            valid = false;
        } else {
            _kodesText.setError(null);
        }

        return valid;
    }

    private void gotoLoginActivity(DataPonpes dp) {
        _lanjutButton.setEnabled(true);
        progressDialog.dismiss();
        // simpan di sessionmanager
        session.createLanjutSession(dp);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        permission.checkResult(requestCode, permissions, grantResults);

    }
}
