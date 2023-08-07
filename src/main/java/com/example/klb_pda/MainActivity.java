package com.example.klb_pda;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    Button btnlogin, btnback, btnlanguage;
    EditText editID, editPassword;
    CheckBox onlinecheck, SaveCheck;
    TextView tv_ver;
    String g_package = "";
    Locale locale;
    String ID, PASSWORD;
    String TABLE_NAME = "acc_table";
    String accID = "accID";
    String pass = "pass";
    private SQLiteDatabase db = null;
    private CheckAppUpdate checkAppUpdate = null;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int REQUEST_UNKNOWN_SOURCES = 3;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };
    private static String[] PERMISSIONS_UNKNOWN_SOURCES = {
            Manifest.permission.REQUEST_INSTALL_PACKAGES
    };
    private int permissionIndex = 0;
    private String[][] permissions = {PERMISSIONS_STORAGE, PERMISSIONS_CAMERA, PERMISSIONS_UNKNOWN_SOURCES};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        g_package = this.getPackageName().toString();
        checkAppUpdate = new CheckAppUpdate(this);
        checkAppUpdate.checkVersion();

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + accID + " TEXT," + pass + " TEXT)";
        db = getApplicationContext().openOrCreateDatabase("Main.db", 0, null);
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
        }

        btnlogin = (Button) findViewById(R.id.btnlogin);
        btnback = (Button) findViewById(R.id.btnback);
        btnlanguage = (Button) findViewById(R.id.btnlanguage);
        editID = (EditText) findViewById(R.id.editID);
        editPassword = (EditText) findViewById(R.id.editPassword);
        onlinecheck = (CheckBox) findViewById(R.id.onlinecheck);
        SaveCheck = (CheckBox) findViewById(R.id.SaveCheck);
        tv_ver = (TextView) findViewById(R.id.tv_ver);
        btnlanguage.setOnClickListener(btnlanguageListener);
        btnlogin.setOnClickListener(btnloginListener);
        btnback.setOnClickListener(btnbackListener);

        Cursor c = db.rawQuery("SELECT accID,pass FROM " + TABLE_NAME + "", null);
        c.moveToFirst();
        Integer l_cn = c.getCount();
        if (l_cn > 0) {
            editID.setText(c.getString(0));
            editPassword.setText(c.getString(1));
            SaveCheck.setChecked(true);
        } else {
            editID.setText("");
            editPassword.setText("");
            SaveCheck.setChecked(false);
        }

        try {
            String verCode = String.valueOf(this.getPackageManager().getPackageInfo(g_package, 0).versionCode);
            String verName = this.getPackageManager().getPackageInfo(g_package, 0).versionName;
            tv_ver.setText("SV: "+ Constant_Class.server +" VerCode: " + verCode + " VerName: " + verName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        editID.requestFocus();
    }

    // Storage Permissions (S)
    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionAtIndex(permissionIndex);
        }
    }

    private void requestPermissionAtIndex(int index) {
        if (index < permissions.length) {
            String[] permissionGroup = permissions[index];
            boolean allPermissionsGranted = true;
            List<String> permissionsToRequest = new ArrayList<>();

            for (String permission : permissionGroup) {
                if (permission.equals(PERMISSIONS_UNKNOWN_SOURCES[0])) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !getPackageManager().canRequestPackageInstalls()) {
                        // Xử lý riêng cho nhóm PERMISSIONS_UNKNOWN_SOURCES
                        // Tạo Intent để mở cài đặt quyền hạn
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_UNKNOWN_SOURCES);
                    }
                } else {
                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        permissionsToRequest.add(permission);
                    }
                }
            }

            if (!allPermissionsGranted) {
                String[] permissionsArray = permissionsToRequest.toArray(new String[permissionsToRequest.size()]);
                requestPermissions(permissionsArray, index);
            } else {
                // Quyền hạn đã được cấp, tiến hành yêu cầu quyền tiếp theo
                permissionIndex++;
                requestPermissionAtIndex(permissionIndex);
            }
        } else {
            // Đã yêu cầu hết các quyền hạn, thực hiện các hành động tiếp theo
            checkAppUpdate = new CheckAppUpdate(this);
            checkAppUpdate.checkVersion();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == permissionIndex && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Quyền hạn đã được cấp, tiến hành yêu cầu quyền tiếp theo
            permissionIndex++;
            requestPermissionAtIndex(permissionIndex);
        } else {
            // Quyền hạn không được cấp, xử lý theo yêu cầu của bạn
        }
    }
    // Storage Permissions (E)

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!SaveCheck.isChecked()) {
            editID.setText("");
            editPassword.setText("");
        }
    }

    private View.OnClickListener btnloginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ID = editID.getText().toString();
            PASSWORD = editPassword.getText().toString();
            if (onlinecheck.isChecked()) {
                //離線登入
                if (ID.length() > 0) {
                    Intent login = new Intent();
                    login.setClass(MainActivity.this, Menu.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", editID.getText().toString());
                    login.putExtras(bundle);
                    startActivity(login);
                } else {
                    Toast alert = Toast.makeText(MainActivity.this, "請輸入帳號", Toast.LENGTH_LONG);
                    alert.show();
                }
            } else {
                login("http://172.16.40.20/" + Constant_Class.server + "/loginJson.php?ID=" + ID + "&PASSWORD=" + PASSWORD);
            }
        }
    };

    private View.OnClickListener btnbackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    };

    private void login(String apiurl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiurl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String result = reader.readLine();
                    reader.close();
                    if (result.contains("PASS")) {
                        if (SaveCheck.isChecked()) {
                            db.execSQL("DELETE FROM " + TABLE_NAME + "");
                            ContentValues args = new ContentValues();
                            args.put(accID, ID);
                            args.put(pass, PASSWORD);
                            db.insert(TABLE_NAME, null, args);
                        } else {
                            db.execSQL("DELETE FROM " + TABLE_NAME + "");
                        }

                        try{
                            JSONArray jsonarray = new JSONArray(result);
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonObject = jsonarray.getJSONObject(i);
                                Constant_Class.UserXuong = jsonObject.getString("TC_QRH003");
                                Constant_Class.UserKhau= jsonObject.getString("TC_QRH005");
                                Constant_Class.UserTramQR = jsonObject.getString("TC_QRH006");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent login = new Intent();
                        login.setClass(MainActivity.this, Menu.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", editID.getText().toString());
                        login.putExtras(bundle);
                        startActivity(login);
                    } else if (result.contains("FALSE")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast alert = Toast.makeText(MainActivity.this, getString(R.string.main_E02), Toast.LENGTH_LONG);
                                alert.show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast alert = Toast.makeText(MainActivity.this, getString(R.string.main_E03), Toast.LENGTH_LONG);
                                alert.show();
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast alert = Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG);
                            alert.show();
                        }
                    });
                }
            }
        }).start();
    }

    //切換語言按鈕事件
    private Button.OnClickListener btnlanguageListener = new Button.OnClickListener() {
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setSingleChoiceItems(new String[]{"中文", "Tiếng Việt"},
                    getSharedPreferences("Language", Context.MODE_PRIVATE).getInt("Language", 0),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {

                            SharedPreferences preferences = getSharedPreferences("Language", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("Language", i);
                            editor.apply();
                            dialogInterface.dismiss();

                            //重新載入APP
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    //設定顯示語言
    private void setLanguage() {
        SharedPreferences preferences = getSharedPreferences("Language", Context.MODE_PRIVATE);
        int language = preferences.getInt("Language", 0);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        switch (language) {
            case 0:
                locale = new Locale("zh");
                Locale.setDefault(locale);
                configuration.setLocale(locale);
                break;
            case 1:
                locale = new Locale("vi");
                Locale.setDefault(locale);
                configuration.setLocale(locale);
                break;

        }
        resources.updateConfiguration(configuration, displayMetrics);
    }
}