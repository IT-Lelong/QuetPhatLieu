package com.example.klb_pda;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.klb_pda.QRprint.qrcode_print;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Menu extends AppCompatActivity {

    String g_server = "";
    Button btnQR210, btnQR230, btnprint;
    TextView menuID;
    String ID;
    private CheckAppUpdate checkAppUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_server = getbundle.getString("SERVER");
        btnQR210 = (Button) findViewById(R.id.btnQR210);
        btnQR230 = (Button) findViewById(R.id.btnQR230);
        menuID = (TextView) findViewById(R.id.menuID);
        btnprint = (Button) findViewById(R.id.btnprint);


        btnQR210.setOnClickListener(btnlistener);
        btnQR230.setOnClickListener(btnlistener);
        btnprint.setOnClickListener(btnlistener);


        getIDname("http://172.16.40.20/" + g_server + "/getid.php?ID=" + ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAppUpdate = new CheckAppUpdate(this,g_server);
        checkAppUpdate.checkVersion();
    }

    private void getIDname(String apiUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = "";
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    result = reader.readLine();
                    reader.close();
                    result = result.replaceAll("-", "\n");
                    menuID.setText(result);
                } catch (Exception e) {
                    Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                    alert.show();
                }
            }
        }).start();
    }

    private View.OnClickListener btnlistener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnQR210: {
                    try {
                        Intent QR210 = new Intent();
                        QR210.setClass(Menu.this, qr210.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("SERVER", g_server);
                        QR210.putExtras(bundle);
                        startActivity(QR210);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }

                    break;
                }
                case R.id.btnQR230: {
                    try {
                        Intent QR230 = new Intent();
                        QR230.setClass(Menu.this, qr230.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("SERVER", g_server);
                        QR230.putExtras(bundle);
                        startActivity(QR230);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }

                    break;
                }

                case R.id.btnprint: {
                    try {
                        Intent printqrcode = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("SERVER", g_server);
                        printqrcode.setClass(Menu.this, qrcode_print.class);
                        printqrcode.putExtras(bundle);
                        startActivity(printqrcode);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }
                    break;
                }
            }
        }
    };

}