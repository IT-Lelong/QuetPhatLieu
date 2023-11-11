package com.example.klb_pda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.internal.zach;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CheckDateTime extends AppCompatActivity implements EMDKManager.EMDKListener, DataListener, StatusListener {
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    String ID, g_server, l_ngay;
    TextView head1;
    ListView list01;
    Button btnupload, btnclear;
    UIHandler uiHandler;
    JSONObject ujsonobject;
    JSONArray ujsonArray;
    ListView dialoglist01;
    qr230DB db = null;
    private CheckAppUpdate checkAppUpdate = null;
    DecimalFormat decimalFormat;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ArrayList<List_CheckDT> dsCheckDTS;
    CheckDT_Adapter checkDateTimeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_date_time);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        db = new qr230DB(this);
        db.open();
        //db.close();
        db.createtable();

        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_server = getbundle.getString("SERVER");
        list01 = (ListView) findViewById(R.id.lv_check_dt);
        uiHandler = new UIHandler();

        Locale locale = new Locale("en", "EN");
        String pattern = "###,###,###.##";
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);
        //mangLVDT = new ArrayList<List_CheckDT>();
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //statusTextView.setText("EMDKManager Request Failed");
            updateStatus("EMDKManager Request Failed");
        }


        dsCheckDTS = new ArrayList<>();

        checkDateTimeAdapter = new CheckDT_Adapter(CheckDateTime.this,
                R.layout.checkdt_row,
                dsCheckDTS);
        list01.setAdapter(checkDateTimeAdapter);
    }

    private void updateStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update the status text view on UI thread with current scanner state
                //statusTextView.setText(""+  status);
                //vqrb00.setText(status);
            }
        });
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
// Get a reference to EMDKManager
        this.emdkManager = emdkManager;

        // Get a  reference to the BarcodeManager feature object
        initBarcodeManager();

        // Initialize the scanner
        initScanner();
    }

    private void initScanner() {
        if (scanner == null) {
            // Get default scanner defined on the device
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            if (scanner != null) {
                // Implement the DataListener interface and pass the pointer of this object to get the data callbacks.
                scanner.addDataListener(this);

                // Implement the StatusListener interface and pass the pointer of this object to get the status callbacks.
                scanner.addStatusListener(this);

                // Hard trigger. When this mode is set, the user has to manually
                // press the trigger on the device after issuing the read call.
                // NOTE: For devices without a hard trigger, use TriggerType.SOFT_ALWAYS.
                scanner.triggerType = Scanner.TriggerType.HARD;

                try {
                    // Enable the scanner
                    // NOTE: After calling enable(), wait for IDLE status before calling other scanner APIs
                    // such as setConfig() or read().
                    scanner.enable();

                } catch (ScannerException e) {
                    updateStatus(e.getMessage());
                    deInitScanner();
                }
            } else {
                updateStatus("Failed to   initialize the scanner device.");
            }
        }
    }

    private void initBarcodeManager() {
        // Get the feature object such as BarcodeManager object for accessing the feature.
        barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        // Add external scanner connection listener.
        if (barcodeManager == null) {
            Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onClosed() {
// The EMDK closed unexpectedly. Release all the resources.
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
        updateStatus("EMDK closed unexpectedly! Please close and restart the application.");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emdkManager != null) {

            // Clean up the objects created by EMDK manager
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (scanner != null) {
                // releases the scanner hardware resources for other application
                // to use. You must call this as soon as you're done with the
                // scanning.
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
                scanner.disable();
                scanner = null;
            }
        } catch (ScannerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        String dataStr = "";
        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
            // Iterate through scanned data and prepare the data.
            for (ScanDataCollection.ScanData data : scanData) {
                String a = data.getData();
                //ScanDataCollection.LabelType labelType=data.getLabelType();
                dataStr = a;
                //String kytu = dataStr.substring(0, 1);
            }
            if (dataStr.length() == 16) {
                try {
                    scanner.disable();
                } catch (ScannerException e) {
                    e.printStackTrace();
                }
                //updatedetail(dataStr); //Quét code vật liệu
                updateData(dataStr);
            } else {
                try {
                    scanner.disable();
                } catch (ScannerException e) {
                    e.printStackTrace();
                }
                updateData1(dataStr); //Quét đơn

            }

        }
    }

    //取得調撥單內容
    private void updateData(final String dataStr) {
        Thread scan = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = getdata_dt(dataStr);
                    if (result.equals("FALSE")) {
                        CheckDateTime.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr230_msg01));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else {
                        ujsonobject = new JSONObject(result);
                        if (ujsonobject.getJSONArray("detail2").length() == 0) {
                            CheckDateTime.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                                    builder.setTitle("ERROR");
                                    builder.setMessage(getString(R.string.qr230_msg02));
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                    builder.show();
                                }
                            });
                        } else {
                            //head1.setText(dataStr);
                            //uiHandler.sendEmptyMessage(0);
                            dsCheckDTS.clear();

                            for (int i = 0; i < ujsonobject.getJSONArray("detail2").length(); i++) {
                                JSONObject jsonObject = ujsonobject.getJSONArray("detail2").getJSONObject(i);
                                Integer xqr230b_01 = i + 1; //stt
                                String xqr230b_02 = jsonObject.getString("QR_IMN01"); //Mã đơn phát hàng
                                Integer xqr230b_03 = Integer.valueOf(jsonObject.getString("QR_IMN02")); //hạng mục
                                String xqr230b_04 = jsonObject.getString("QR_IMN03"); //Mã tem
                                String xqr230b_05 = jsonObject.getString("QR_IMN04"); //Mã vật liệu
                                String xqr230b_06 = jsonObject.getString("TA_IMA02_1"); //Tên vật liệu
                                String xqr230b_07 = jsonObject.getString("TA_IMA021_1"); //Tên quy cách
                                String xqr230b_08 = jsonObject.getString("QR_IMN12"); //Ngay quet
                                String xqr230b_09 = jsonObject.getString("QR_IMN07"); //Nguoi quet
                                Integer xqr230b_10 = Integer.valueOf(jsonObject.getString("QR_IMN06")); //Số lượng
                                String xqr230b_11 = jsonObject.getString("RVB38"); //Nguoi quet

                                dsCheckDTS.add(new List_CheckDT(xqr230b_01, xqr230b_02, xqr230b_03, xqr230b_04, xqr230b_05, xqr230b_06, xqr230b_07, xqr230b_08, xqr230b_09, xqr230b_10,xqr230b_11));

                                //trangthai.setText("Chưa chuyển");

                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkDateTimeAdapter.notifyDataSetChanged();
                                    try {
                                        scanner.enable();
                                    } catch (ScannerException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });

                        }
                    }
                } catch (Exception e) {
                    //Toast.makeText(CheckDateTime.this, e.toString(), Toast.LENGTH_SHORT).show();

                    CheckDateTime.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                            builder.setTitle("ERROR");
                            builder.setMessage(getString(R.string.qr230_msg01) + " " + e.toString());
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();
                        }
                    });
                }

            }
        });
        scan.start();
    }

    private void updateData1(final String dataStr) {
        Thread scan = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = getdata(dataStr);
                    if (result.equals("FALSE")) {
                        CheckDateTime.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr230_msg01));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else {
                        ujsonobject = new JSONObject(result);
                        if (ujsonobject.getJSONArray("detail2").length() == 0) {
                            CheckDateTime.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                                    builder.setTitle("ERROR");
                                    builder.setMessage(getString(R.string.qr230_msg02));
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                    builder.show();
                                }
                            });
                        } else {
                            //head1.setText(dataStr);
                            //uiHandler.sendEmptyMessage(0);

                            dsCheckDTS.clear();

                            for (int i = 0; i < ujsonobject.getJSONArray("detail2").length(); i++) {
                                JSONObject jsonObject = ujsonobject.getJSONArray("detail2").getJSONObject(i);
                                Integer xqr230b_01 = i + 1; //stt
                                String xqr230b_02 = jsonObject.getString("QR_IMN01"); //Mã đơn phát hàng
                                Integer xqr230b_03 = Integer.valueOf(jsonObject.getString("QR_IMN02")); //hạng mục
                                String xqr230b_04 = jsonObject.getString("QR_IMN03"); //Mã tem
                                String xqr230b_05 = jsonObject.getString("QR_IMN04"); //Mã vật liệu
                                String xqr230b_06 = jsonObject.getString("TA_IMA02_1"); //Tên vật liệu
                                String xqr230b_07 = jsonObject.getString("TA_IMA021_1"); //Tên quy cách
                                String xqr230b_08 = jsonObject.getString("QR_IMN12"); //Ngay quet
                                String xqr230b_09 = jsonObject.getString("QR_IMN07"); //Nguoi quet
                                Integer xqr230b_10 = Integer.valueOf(jsonObject.getString("QR_IMN06")); //Số lượng
                                String xqr230b_11 = jsonObject.getString("RVB38"); //Nguoi quet


                                dsCheckDTS.add(new List_CheckDT(xqr230b_01, xqr230b_02, xqr230b_03, xqr230b_04, xqr230b_05, xqr230b_06, xqr230b_07, xqr230b_08, xqr230b_09, xqr230b_10,xqr230b_11));

                                //trangthai.setText("Chưa chuyển");

                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkDateTimeAdapter.notifyDataSetChanged();
                                    try {
                                        scanner.enable();
                                    } catch (ScannerException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });

                        }
                    }
                } catch (Exception e) {
                    //Toast.makeText(CheckDateTime.this, e.toString(), Toast.LENGTH_SHORT).show();

                    CheckDateTime.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                            builder.setTitle("ERROR");
                            builder.setMessage(getString(R.string.qr230_msg01) + " " + e.toString());
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();
                        }
                    });
                }

            }
        });
        scan.start();
    }

    public String getdata(String dataStr) {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_QR230/getdataquery_tem.php?imm01=" + dataStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public String getdata_dt(String dataStr) {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_QR230/getdataquery.php?imm01=" + dataStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {

            return "FALSE";
        }
    }

    private void updatedetail(String datastr) {
        try {
            if (datastr.startsWith("new")) {
                String qr01 = "", qr02 = "";
                Double qr03 = 0.0;
                //重印、拆單標籤格式 new_料號_批號_數量
                int index1 = datastr.indexOf("_");
                int index2 = datastr.indexOf("_", index1 + 1);
                int index3 = datastr.indexOf("_", index2 + 1);
                int index4 = datastr.indexOf("_", index3 + 1);  //預備給 測試料號會多出 T_
                //料號
                qr01 = datastr.substring(4, index2);
                if (qr01.equals("T")) {
                    qr01 = datastr.substring(4, index3);
                    qr02 = datastr.substring(index3 + 1, index4);
                    qr03 = Double.valueOf(datastr.substring(index4 + 1));
                } else {
                    //批號
                    qr02 = datastr.substring(index2 + 1, index3);
                    //數量
                    qr03 = Double.valueOf(datastr.substring(index3 + 1));
                }

                scan(datastr, qr01, qr02, qr03, String.valueOf(dateFormat.format(new Date()).toString()), ID);

            } else if (datastr.substring(0, 5).equals("BC525") || datastr.substring(0, 5).equals("BC527") || datastr.substring(0, 5).equals("BB525") || datastr.substring(0, 5).equals("BB527")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String qr01 = "", qr02 = "";
                        Double qr03 = 0.0;

                        //極版標籤 BC527-2101000198_1_07030333C_29568
                        int index1 = datastr.indexOf("_");
                        int index2 = datastr.indexOf("_", index1 + 1);
                        int index3 = datastr.indexOf("_", index2 + 1);
                        int index4 = datastr.indexOf("_", index3 + 1); //預備給 測試料號會多出 T_

                        //取得料號
                        qr01 = datastr.substring(index2 + 1, index3);
                        //取得批號
                        qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 2);
                        if (qr01.equals("T")) {
                            qr01 = datastr.substring(index2 + 1, index4);
                            qr03 = Double.valueOf(datastr.substring(index4 + 1));
                        } else {
                            //取得數量
                            qr03 = Double.valueOf(datastr.substring(index3 + 1));
                        }
                        scan(datastr, qr01, qr02, qr03, String.valueOf(dateFormat.format(new Date()).toString()), ID);
                    }
                });
                api.start();
            } else if (datastr.substring(0, 5).equals("CC510") || datastr.substring(0, 5).equals("CC512") || datastr.substring(0, 5).equals("CC513") || datastr.substring(0, 5).equals("CC514") || datastr.substring(0, 5).equals("CC515") || datastr.substring(0, 5).equals("CC518") || datastr.substring(0, 8).equals("OLDSTAMP")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Double qr03 = 0.0;
                        //廣泰標籤 CC512-2010000697-108(CC5A2_KD91-1_NULL)_(03020489V_nap_WPX7A-BA)_20201026007002
                        //特殊標籤 CC514-2207000073-1492_CC514-2206000253-236(CC5A4_KD91-2_NULL)_(03010374D_vỏ bình ắc quy_WP8.5-12)_20220711059001
                        int index1 = datastr.indexOf("-", 6);
                        int index2 = datastr.indexOf("("); //第一個(位置
                        int index3 = datastr.indexOf("(", index2 + 1); //第二個(位置
                        int index4 = datastr.indexOf("_", index3); //料號後的_位置
                        //取得料號
                        String qr01 = datastr.substring(index3 + 1, index4);
                        //取得批號
                        String qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 1);
                        //取得數量
                        if (index2 > 32) {
                            String g_qr03_datastr = datastr.substring(0, index2);
                            Integer g_len_g_qr03_datastr = g_qr03_datastr.length();
                            Integer l_sl1 = 0, l_sl2 = 0, l_sl3 = 0, k = 0;

                            for (int i = 0; i <= g_len_g_qr03_datastr; i++) {
                                String l_code = "";
                                String j = "";
                                if (i < g_len_g_qr03_datastr) {
                                    j = g_qr03_datastr.substring(i, i + 1);
                                }

                                if (j.equals("_") || i == g_len_g_qr03_datastr) {
                                    if (i == g_len_g_qr03_datastr) {
                                        l_code = g_qr03_datastr.substring(k, i);
                                        l_sl3 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                    } else {
                                        l_code = g_qr03_datastr.substring(k, i);
                                        if (l_sl1 == 0) {
                                            l_sl1 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                        } else {
                                            l_sl2 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                        }
                                    }
                                    k = i + 1;
                                }
                            }

                            qr03 = Double.valueOf(l_sl1 + l_sl2 + l_sl3);
                        } else {
                            qr03 = Double.valueOf(datastr.substring(index1 + 1, index2));
                        }

                        scan(datastr, qr01, qr02, qr03, String.valueOf(dateFormat.format(new Date()).toString()), ID);
                    }
                });
                api.start();
            } else if (datastr.startsWith("-", 5)) {
                //供應商條碼 BB421-2101000169_2_07100071A_20211007_792
                int index1 = datastr.indexOf("_");
                int index2 = datastr.indexOf("_", index1 + 1);
                int index3 = datastr.indexOf("_", index2 + 1);
                int index4 = datastr.indexOf("_", index3 + 1);
                //取得料號
                String qr01 = datastr.substring(index2 + 1, index3);
                //取得批號
                String qr02 = datastr.substring(index3 + 1, index4);
                //取得數量
                Double qr03 = Double.valueOf(datastr.substring(index4 + 1));
                scan(datastr, qr01, qr02, qr03, String.valueOf(dateFormat.format(new Date()).toString()), ID);
            } else {
                CheckDateTime.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg02));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            }
        } catch (Exception e) {

        }
    }

    //QR_code,MVL,Số Lô, Số Lượng
    private void scan(String xqr230b_02, String xqr230b_03, String xqr230b_04, Double xqr230b_05, String xqr230b_06, String xqr230b_07) {
        try {
            String result = db.scan(xqr230b_02, xqr230b_03, xqr230b_04, xqr230b_05, xqr230b_06, xqr230b_07);
            if (result.equals("FALSE")) {
                CheckDateTime.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg03));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else if (result.equals("NORECORD")) {
                CheckDateTime.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg04));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else if (result.equals("OVERQTY")) {
                CheckDateTime.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CheckDateTime.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg05));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else {
                uiHandler.sendEmptyMessage(2);
            }
        } catch (Exception e) {

        }
    }

    //取得批號
    private String getdatecode(String apiUrl) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String jsonstring = reader.readLine();
            reader.close();
            if (!jsonstring.equals("FALSE")) {
                return jsonstring;
            } else {
                return "NULL";
            }
        } catch (Exception e) {
            return "NULL";
        }
    }

    @Override
    public void onStatus(StatusData statusData) {
        // The status will be returned on multiple cases. Check the state and take the action.
        // Get the current state of scanner in background
        StatusData.ScannerStates state = statusData.getState();
        String statusStr = "";
        // Different states of Scanner
        switch (state) {
            case IDLE:
                // Scanner is idle and ready to change configuration and submit read.
                statusStr = statusData.getFriendlyName() + " is   enabled and idle...";
                // Change scanner configuration. This should be done while the scanner is in IDLE state.
                setConfig();
                try {
                    // Starts an asynchronous Scan. The method will NOT turn ON the scanner beam,
                    //but puts it in a  state in which the scanner can be turned on automatically or by pressing a hardware trigger.
                    scanner.read();
                } catch (ScannerException e) {
                    updateStatus(e.getMessage());
                }
                break;
            case WAITING:
                // Scanner is waiting for trigger press to scan...
                statusStr = "Scanner is waiting for trigger press...";
                break;
            case SCANNING:
                // Scanning is in progress...
                statusStr = "Scanning...";
                break;
            case DISABLED:
                // Scanner is disabledstatusStr = statusData.getFriendlyName()+" is disabled.";
                break;
            case ERROR:
                // Error has occurred during scanning
                statusStr = "An error has occurred.";
                break;
            default:
                break;
        }
        // Updates TextView with scanner state on UI thread.
        updateStatus(statusStr);
    }

    private void setConfig() {
        if (scanner != null) {
            try {
                // Get scanner config
                ScannerConfig config = scanner.getConfig();
                // Enable haptic feedback
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true;
                }
                // Set scanner config
                scanner.setConfig(config);
            } catch (ScannerException e) {
                updateStatus(e.getMessage());
            }
        }
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //資料新增
                case 0:
                    try {
                        db.append(ujsonobject);
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                        scanner.enable();
                    } catch (Exception e) {
                    }
                    break;
                //資料清除
                case 1:
                    try {
                        db.close();
                        db.open();
                        db.createtable();
                        head1.setText("");
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                        scanner.enable();
                    } catch (Exception e) {
                    }
                    break;
                //掃描標籤
                case 2:
                    try {
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                        scanner.enable();
                    } catch (Exception e) {
                    }
                    break;

            }
        }
    }

    public void UpdateAdapter(Cursor cursor) {
        try {
            if (cursor != null && cursor.getCount() >= 0) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_qr210_view01, cursor, new String[]{"qr230_01", "qr230_02", "qr230_03", "qr230_04", "qr230_05", "qr230_09", "qr230_10"}, new int[]{R.id.qr210_view01_item01, R.id.qr210_view01_item02, R.id.qr210_view01_item03, R.id.qr210_view01_item04, R.id.qr210_view01_item05, R.id.qr210_view01_item06, R.id.qr210_view01_item07}, 0);

                adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        TextView textView = (TextView) view;
                        if (columnIndex == 4) {
                            textView.setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                            return true;
                        }

                        if (columnIndex == 5) {
                            textView.setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                            return true;
                        }

                        return false;
                    }
                });

                list01.setAdapter(adapter);
            }
        } catch (Exception e) {
            String x = e.toString();
        } finally {

        }
    }

    private void deInitScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                scanner.release();
            } catch (Exception e) {
                updateStatus(e.getMessage());
            }
            scanner = null;
        }
    }

    //Cursor 轉 Json
    public JSONArray cur2Json(Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        rowObject.put(cursor.getColumnName(i),
                                cursor.getString(i));
                    } catch (Exception e) {
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;

    }
}