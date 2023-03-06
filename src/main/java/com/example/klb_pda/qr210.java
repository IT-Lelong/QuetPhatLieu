package com.example.klb_pda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.ScannerConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;


public class qr210 extends AppCompatActivity implements EMDKListener, StatusListener, DataListener {
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    qr210DB db = null;
    JSONObject ujsonobject;
    JSONArray ujsonArray;
    TextView head1;
    Spinner head2;
    ListView list01;
    ListView dialoglist01;
    String ID, g_server;
    UIHandler uiHandler;
    Button btnupload, btnclear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr210);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        db = new qr210DB(this);
        db.open();
        db.close();
        db.open();
        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_server = getbundle.getString("SERVER");
        head1 = (TextView) findViewById(R.id.qr210_head1);
        head2 = (Spinner) findViewById(R.id.qr210_head2);
        list01 = (ListView) findViewById(R.id.qr210_list01);
        btnupload = (Button) findViewById(R.id.qr210_btnupload);
        btnclear = (Button) findViewById(R.id.qr210_btnclear);
        btnupload.setOnClickListener(btnuploadlistener);
        btnclear.setOnClickListener(btnclearlistener);
        head2.setOnItemSelectedListener(spnOnitemSelected);
        list01.setOnItemClickListener(lsit01listener);
        String[] type = {"1.極版 Lá chì", "2.槽蓋 Vỏ nắp"};
        ArrayAdapter<String> typelist = new ArrayAdapter<>(qr210.this, android.R.layout.simple_spinner_dropdown_item, type);
        head2.setAdapter(typelist);
        uiHandler = new UIHandler();
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //statusTextView.setText("EMDKManager Request Failed");
            updateStatus("EMDKManager Request Failed");
        }

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
    protected void onDestroy() {
        super.onDestroy();
        if (emdkManager != null) {

// Clean up the objects created by EMDK manager
            emdkManager.release();
            emdkManager = null;
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

    private void initBarcodeManager() {
        // Get the feature object such as BarcodeManager object for accessing the feature.
        barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        // Add external scanner connection listener.
        if (barcodeManager == null) {
            Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
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
            }
            if (head1.length() > 0) {
                updatedetail(dataStr);
            } else {
                updateData(dataStr);
            }

        }
    }


    private void updateData(final String dataStr) {
        Thread scan = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = getdata(dataStr);
                    if (result.equals("FALSE")) {
                        qr210.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr210_msg01));
                                builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else {
                        ujsonobject = new JSONObject(result);
                        head1.setText(dataStr);
                        uiHandler.sendEmptyMessage(0);
                    }
                } catch (Exception e) {
                    qr210.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
                            builder.setTitle("ERROR");
                            builder.setMessage(getString(R.string.qr210_msg01));
                            builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
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

    private void updatedetail(String datastr) {
        try {
            if (datastr.startsWith("new")) {
                String qr01 = "", qr02 = "";
                Integer qr03 = 0;
                //重印、拆單標籤格式 new_料號_批號_數量
                int index1 = datastr.indexOf("_");
                int index2 = datastr.indexOf("_", index1 + 1);
                int index3 = datastr.indexOf("_", index2 + 1);
                int index4 = datastr.indexOf("_", index3 + 1); //預備給 測試料號會多出 T_

                //料號
                qr01 = datastr.substring(4, index2);
                //批號
                qr02 = datastr.substring(index2 + 1, index3);
                if (qr01.equals("T")) {
                    qr01 = datastr.substring(index2 + 1, index4);
                    qr03 = Integer.valueOf(datastr.substring(index4 + 1));
                } else {
                    //取得數量
                    qr03 = Integer.valueOf(datastr.substring(index3 + 1));
                }
                scan(datastr, qr01, qr02, qr03);

            } else if (datastr.substring(0, 5).equals("BC525") || datastr.substring(0, 5).equals("BC527") || datastr.substring(0, 5).equals("BB525") || datastr.substring(0, 5).equals("BB527")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String qr01 = "", qr02 = "";
                        Integer qr03 = 0;
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
                            qr03 = Integer.valueOf(datastr.substring(index4 + 1));
                        } else {
                            //取得數量
                            qr03 = Integer.valueOf(datastr.substring(index3 + 1));
                        }
                        scan(datastr, qr01, qr02, qr03);
                    }
                });
                api.start();

            } else if (datastr.substring(0, 5).equals("CC510") || datastr.substring(0, 5).equals("CC512") || datastr.substring(0, 5).equals("CC513") || datastr.substring(0, 5).equals("CC514") || datastr.substring(0, 5).equals("CC515") || datastr.substring(0, 5).equals("CC518") || datastr.substring(0, 8).equals("OLDSTAMP")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //廣泰標籤 CC512-2010000697-108(CC5A2_KD91-1_NULL)_(03020489V_nap_WPX7A-BA)_20201026007002
                        int index1 = datastr.indexOf("-", 6);
                        int index2 = datastr.indexOf("("); //第一個(位置
                        int index3 = datastr.indexOf("(", index2 + 1); //第二個(位置
                        int index4 = datastr.indexOf("_", index3); //料號後的_位置
                        //取得料號
                        String qr01 = datastr.substring(index3 + 1, index4);
                        //取得批號
                        String qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 1);
                        //取得數量
                        Integer qr03 = Integer.valueOf(datastr.substring(index1 + 1, index2));
                        scan(datastr, qr01, qr02, qr03);
                    }
                });
                api.start();
            } else {
                qr210.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
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

    private void scan(String xqr210b_00, String xqr210b_01, String xqr210b_02, Integer xqr210b_03) {
        try {
            String result = db.scan(xqr210b_00, xqr210b_01, xqr210b_02, xqr210b_03);
            if (result.equals("FALSE")) {
                qr210.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg03));
                        builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else if (result.equals("NORECORD")) {
                qr210.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg04));
                        builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else if (result.equals("OVERQTY")) {
                qr210.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg05));
                        builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
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

    public String getdata(String dataStr) {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_QR210/getdata.php?tc_ptt001=" + dataStr);
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

    public String upload() {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_QR210/upload.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            Cursor c = db.getall();
            Cursor c1 = db.getallb();
            JSONArray jarray = cur2Json(c);
            JSONArray jarray1 = cur2Json(c1);
            JSONObject jobejct = new JSONObject();
            jobejct.put("QR_PTT06", ID);
            jobejct.put("QR_PTT01", head1.getText());
            jobejct.put("QR_PTT02", String.valueOf(head2.getSelectedItemId() + 1));
            jobejct.put("detail", jarray);
            jobejct.put("detail2", jarray1);
            writer.write(jobejct.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {

            return "FALSE";
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

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //資料新增
                case 0:
                    try {
                        db.append(ujsonobject);
                        Cursor getdetail = db.getdetail(String.valueOf(head2.getSelectedItemId() + 1));
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;
                //資料清除
                case 1:
                    try {
                        db.close();
                        db.open();
                        head1.setText("");
                        Cursor getdetail = db.getdetail(String.valueOf(head2.getSelectedItemId() + 1));
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;
                //選擇資料
                case 2:
                    try {
                        Cursor getdetail = db.getdetail(String.valueOf(head2.getSelectedItemId() + 1));
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;

            }
        }
    }

    public void UpdateAdapter(Cursor cursor) {
        float floatNumber = 4337.574646265748567f;
        Locale locale = new Locale("en", "EN");
        String pattern = "###,###.##";
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);

        try {
            if (cursor != null && cursor.getCount() >= 0) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_qr210_view01, cursor,
                        new String[]{"rownum", "qr210_01", "qr210_02", "qr210_03", "qr210_04"},
                        new int[]{R.id.qr210_view01_item01, R.id.qr210_view01_item02, R.id.qr210_view01_item03, R.id.qr210_view01_item04, R.id.qr210_view01_item05}, 0);
                list01.setAdapter(adapter);
            }
        } catch (Exception e) {
            String x = e.toString();
        } finally {

        }
    }

    public void UpdateAdapterdialog(Cursor cursor) {
        try {
            if (cursor != null && cursor.getCount() >= 0) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(qr210.this, R.layout.activity_qr210_dialog01_view, cursor,
                        new String[]{"rownum", "qr210b_02", "qr210b_03", "qr210b_04"},
                        new int[]{R.id.qr210_dialog01_item01, R.id.qr210_dialog01_item02, R.id.qr210_dialog01_item03, R.id.qr210_dialog01_item04}, 0);
                dialoglist01.setAdapter(adapter);

            }
        } catch (Exception e) {

        } finally {

        }
    }

    private AdapterView.OnItemSelectedListener spnOnitemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            uiHandler.sendEmptyMessage(2);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private AdapterView.OnItemClickListener lsit01listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Button dialogbtn01;


            TextView item02 = (TextView) view.findViewById(R.id.qr210_view01_item02);
            String qr210_01 = item02.getText().toString();
            final Dialog dialog = new Dialog(qr210.this);
            dialog.setContentView(R.layout.activity_qr210_dialog01);

            dialogbtn01 = (Button) dialog.findViewById(R.id.qr210_dialog01_btn01);
            dialoglist01 = (ListView) dialog.findViewById(R.id.qr210_dialog01_list01);
            Cursor cursor = db.getdialogdetail(qr210_01);
            UpdateAdapterdialog(cursor);
            dialogbtn01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uiHandler.sendEmptyMessage(2);
                    dialog.dismiss();

                }
            });
            //點選刪除
            dialoglist01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView dialogitem04;
                    dialogitem04 = (TextView) view.findViewById(R.id.qr210_dialog01_item04);
                    if (!dialogitem04.getText().toString().equals("ok")) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(qr210.this);
                        builder1.setCancelable(false);
                        builder1.setTitle(getString(R.string.qr210_msg06));
                        builder1.setMessage(getString(R.string.qr210_msg07));
                        builder1.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TextView dialogitem02, dialogitem03;
                                dialogitem02 = (TextView) view.findViewById(R.id.qr210_dialog01_item02);
                                dialogitem03 = (TextView) view.findViewById(R.id.qr210_dialog01_item03);
                                String xqr210b_02 = dialogitem02.getText().toString();
                                Integer xqr210b_03 = Integer.valueOf(dialogitem03.getText().toString());
                                String result = db.delscan(qr210_01, xqr210b_02, xqr210b_03);
                                if (result.equals("TRUE")) {
                                    Cursor cursor = db.getdialogdetail(qr210_01);
                                    UpdateAdapterdialog(cursor);
                                }
                            }
                        });
                        builder1.show();
                    } else {
                        Toast.makeText(qr210.this, R.string.qr210_msg15, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.show();
        }
    };

    private View.OnClickListener btnuploadlistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
            builder.setCancelable(false);
            builder.setTitle(getString(R.string.qr210_msg08));
            builder.setMessage(getString(R.string.qr210_msg09));
            builder.setNegativeButton(getString(R.string.main_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String result = upload();
                            if (result.equals("TRUE")) {
                                qr210.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
                                        builder.setTitle(getString(R.string.qr210_msg08));
                                        builder.setMessage(getString(R.string.qr210_msg10));
                                        builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        builder.show();
                                        uiHandler.sendEmptyMessage(1);
                                    }
                                });
                            } else {
                                qr210.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
                                        builder.setTitle(getString(R.string.qr210_msg08));
                                        builder.setMessage(getString(R.string.qr210_msg11));
                                        builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
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
                    thread.start();

                }
            });
            builder.show();
        }
    };

    private View.OnClickListener btnclearlistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(qr210.this);
            builder.setCancelable(false);
            builder.setTitle(getString(R.string.qr210_msg12));
            builder.setMessage(getString(R.string.qr210_msg13));
            builder.setNegativeButton(getString(R.string.main_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    db.close();
                    db.open();
                    uiHandler.sendEmptyMessage(1);
                }
            });
            builder.show();
        }
    };

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
                        rowObject.put(cursor.getColumnName(i), cursor.getString(i));
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

