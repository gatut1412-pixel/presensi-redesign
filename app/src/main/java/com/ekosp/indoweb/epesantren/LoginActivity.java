package com.ekosp.indoweb.epesantren;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ekosp.indoweb.epesantren.helper.ApiClient;
import com.ekosp.indoweb.epesantren.helper.ApiInterface;
import com.ekosp.indoweb.epesantren.model.AndroidVersionResponse;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.ekosp.indoweb.epesantren.model.DataUser;
import com.ekosp.indoweb.epesantren.helper.SessionManager;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.okhttp.MediaType;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private MediaType JSON;
    private ProgressDialog progressDialog;
    private DataUser mDataUser;
    private DataPonpes mDataPonpes;
    private SessionManager session;
    private String latestAppVersion = "";
    private String thisAppVersion = "";

    @BindView(R.id.welcome_text)
    TextView _welcomeText;
    @BindView(R.id.img_from_url)
    ImageView img;
    @BindView(R.id.input_username)
    EditText _usernameText;
    @BindView(R.id.input_pin)
    EditText _pinText;
    @BindView(R.id.btn_login)
    ImageView _loginButton;
    @BindView(R.id.tv_versi_apk)
    TextView _versiApk;

    private PermissionManager permission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        JSON = MediaType.parse("application/json; charset=utf-8");
        // Session Manager
        session = new SessionManager(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        String user = session.getSavedUsername();
        if (user != null) {
            _usernameText.setText(user);
        }

        mDataPonpes= session.getSessionDataPonpes();

        Glide.with(this).load("http://"+mDataPonpes.getLogo()+"").into(img);

        _welcomeText.setText("Pondok Pesantren "+mDataPonpes.getNamaPonpes());

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            _versiApk.setText("versi " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        checkForLatestAppVersion();

    }

    public void hapusUsername(View v) {
        session.deleteSavedUsername();
        _usernameText.setText("");
//        Toast.makeText(this, "Data History Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT).show();
        StyleableToast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT, R.style.mytoast).show();
    }

    public void login() {
        Log.d(TAG, "Login");

        _loginButton.setEnabled(false);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Mohon Tunggu...");
        progressDialog.show();

        if (!validate()) {
            onLoginFailed();
            return;
        }

        //  LoginCustomerTask runner = new LoginCustomerTask(username, pin);
        //  runner.execute();
        executeLogin();

    }

    private void executeLogin() {
        String kodes = mDataPonpes.getKodes();
        String uname = _usernameText.getText().toString();
        String pass = _pinText.getText().toString();


        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataUser> call = apiService.checkLogin(kodes,uname, pass);
        call.enqueue(new Callback<DataUser>() {

            @Override
            public void onResponse(Call<DataUser> call, Response<DataUser> response) {
                if (response.isSuccessful()) {
                    mDataUser = response.body();
                    Log.i("RETROFIT", mDataUser.toString());
                    if (mDataUser.getCorrect()) {
                        gotoHomeActivity(mDataUser);
                    } else {
                        onLoginFailed();
                    }
                } else
//                    Toast.makeText(LoginActivity.this, "Terjadi gangguan koneksi ke server", Toast.LENGTH_SHORT).show();
                    StyleableToast.makeText(LoginActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
            }

            @Override
            public void onFailure(Call<DataUser> call, Throwable t) {
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
            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("Info");
            alertDialog.setMessage("Anda membutuhkan versi app terbaru. Versi "+latestAppVersion);
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
//                    Toast.makeText(LoginActivity.this, "Terjadi gangguan koneksi ke server", Toast.LENGTH_SHORT).show();
                    StyleableToast.makeText(LoginActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
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
        if (requestCode == REQUEST_SIGNUP) {
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

    public void backToPonpes (View v){
        Intent intent = new Intent(this, LoginPonpesActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }


    public void onLoginFailed() {
//        Toast.makeText(getBaseContext(), "Login Gagal\nSilakan Cek Detail Data Anda Terlebih Dahulu", Toast.LENGTH_LONG).show();
        StyleableToast.makeText(this, "Masukan Data Login Yang Terdaftar", Toast.LENGTH_LONG, R.style.mytoast_danger).show();
        _loginButton.setEnabled(true);
        progressDialog.dismiss();
    }

    public boolean validate() {
        boolean valid = true;
        String username = _usernameText.getText().toString();
        String pin = _pinText.getText().toString();

        if (username.isEmpty()) {
            _usernameText.setError("Masukan NIP Yang Valid");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (pin.isEmpty() || pin.length() < 2 || pin.length() > 20) {
            _pinText.setError("Panjang Password Min. 2 Karakter & Max. 20 Karakter");
            valid = false;
        } else {
            _pinText.setError(null);
        }
        return valid;
    }

    private void gotoHomeActivity(DataUser du) {
        _loginButton.setEnabled(true);
        progressDialog.dismiss();
        // simpan di sessionmanager
        session.createLoginSession(du);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        permission.checkResult(requestCode, permissions, grantResults);

    }
}
