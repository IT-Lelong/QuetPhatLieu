package com.example.klb_pda.QRprint;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.format.Time;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;

import com.example.klb_pda.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.symbol.emdk.barcode.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

public class qrcode_print extends AppCompatActivity  implements EMDKListener, Scanner.StatusListener, Scanner.DataListener {
    private EMDKManager emdkManager=null;
    private BarcodeManager barcodeManager=null;
    private Scanner scanner=null;

    static BixolonPrinter mBixolonPrinter;
    private qrcode_print_DB db = null;
    private String mConnectedDeviceName = null;
    private boolean mIsConnected;
    private AlertDialog mSampleDialog;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    private boolean firstDetected = true;
    SoundPool OKPool;
    int oksound;
    String tempcode = "";
    String ID, g_server;
    ListView list01;
    //SurfaceView surv01;
    EditText edt1, edt2, edt3, edt4;
    TextView tv5;
    JSONObject ujsonobject;
    String res1, res2, res3,name_result;
    //DecimalFormat formater = new DecimalFormat("#.##");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_print);
        /*android 4.2 ： 跳過 android.os.NetworkOnMainThreadException 錯誤 (S)*/
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        /*android 4.2 ： 跳過 android.os.NetworkOnMainThreadException 錯誤 (E)*/
        //mBixolonPrinter.findBluetoothPrinters();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //actionBar.setTitle("ABC");

        db = new qrcode_print_DB(this);
        db.open();
        db.close();
        db.open();
        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_server = getbundle.getString("SERVER");
        Button btn01 = (Button) findViewById(R.id.btnok);
        Button btn02 = (Button) findViewById(R.id.btnprint);
        Button btn03 = (Button) findViewById(R.id.btncancel);
        //surv01 = (SurfaceView) findViewById(R.id.qrcode_print_surv01);
        btn01.setOnClickListener(btnlistener);
        btn02.setOnClickListener(btnlistener);
        btn03.setOnClickListener(btnlistener);
        edt1 = (EditText) findViewById(R.id.qrcode_print_tv01);
        edt2 = (EditText) findViewById(R.id.qrcode_print_tv02);
        edt3 = (EditText) findViewById(R.id.qrcode_print_tv03);
        edt4 = (EditText) findViewById(R.id.qrcode_print_tv04);
        tv5 = (TextView) findViewById(R.id.qrcode_print_tv05);
        list01 = (ListView) findViewById(R.id.qrcode_print_list01);
        list01.setOnItemClickListener(listviewListener);
        EMDKResults results=EMDKManager.getEMDKManager(getApplicationContext(),this);
        if(results.statusCode !=EMDKResults.STATUS_CODE.SUCCESS ){
            //statusTextView.setText("EMDKManager Request Failed");
            updateStatus("EMDKManager Request Failed");
        }
        OKPool = new SoundPool.Builder().build();
        oksound = OKPool.load(qrcode_print.this, R.raw.ok, 1);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        /*cameraSource = new CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(300, 300).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).build();*/
        /*surv01.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                    return;
                try {
                    cameraSource.start(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            //收到掃描資料

            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() != 0 && firstDetected) {
                    firstDetected = false;
                    //QRCODE取出
                    final String qrcode = qrCodes.valueAt(0).displayValue;
                    OKPool.play(oksound, 1, 1, 0, 0, 1);
                    getcode(qrcode);
                }

            }

        });
        */
        mBixolonPrinter = new BixolonPrinter(this, mHandler, null);
        mBixolonPrinter.findBluetoothPrinters();
    }

    @Override
    public void onOpened(EMDKManager emdkManager){
        // Get a reference to EMDKManager
        this.emdkManager =  emdkManager;

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
            emdkManager= null;
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
        mBixolonPrinter.disconnect();
    }

    @Override
    public void onStatus(StatusData statusData) {
        // The status will be returned on multiple cases. Check the state and take the action.
        // Get the current state of scanner in background
        StatusData.ScannerStates state =  statusData.getState();
        String statusStr = "";
        // Different states of Scanner
        switch (state) {
            case IDLE:
                // Scanner is idle and ready to change configuration and submit read.
                statusStr = statusData.getFriendlyName()+" is   enabled and idle...";
                // Change scanner configuration. This should be done while the scanner is in IDLE state.
                setConfig();
                try {
                    // Starts an asynchronous Scan. The method will NOT turn ON the scanner beam,
                    //but puts it in a  state in which the scanner can be turned on automatically or by pressing a hardware trigger.
                    scanner.read();
                }
                catch (ScannerException e)   {
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

    private void initBarcodeManager(){
        // Get the feature object such as BarcodeManager object for accessing the feature.
        barcodeManager =  (BarcodeManager)emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
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
            if(scanner != null) {
                // Implement the DataListener interface and pass the pointer of this object to get the data callbacks.
                scanner.addDataListener(this);

                // Implement the StatusListener interface and pass the pointer of this object to get the status callbacks.
                scanner.addStatusListener(this);

                // Hard trigger. When this mode is set, the user has to manually
                // press the trigger on the device after issuing the read call.
                // NOTE: For devices without a hard trigger, use TriggerType.SOFT_ALWAYS.
                scanner.triggerType =  Scanner.TriggerType.HARD;

                try{
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
            } catch (Exception e)   {
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
        if (scanner != null) {try {
            // Get scanner config
            ScannerConfig config = scanner.getConfig();
            // Enable haptic feedback
            if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                config.scanParams.decodeHapticFeedback = true;
            }
            // Set scanner config
            scanner.setConfig(config);
        } catch (ScannerException e)   {
            updateStatus(e.getMessage());
        }
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        String dataStr = "";
        if ((scanDataCollection != null) &&   (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanDataCollection.ScanData> scanData =  scanDataCollection.getScanData();
            // Iterate through scanned data and prepare the data.
            for (ScanDataCollection.ScanData data:scanData){
                String a=data.getData();
                //ScanDataCollection.LabelType labelType=data.getLabelType();
                dataStr=a;
            }
            if(edt1.length() == 0){
                getcode(dataStr);
            }

        }
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        mBixolonPrinter.disconnect();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qrcode_print_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                mBixolonPrinter.findBluetoothPrinters();
                break;
        }
        return false;
    }


    private final Handler mHandler = new Handler(new Handler.Callback() {
        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mIsConnected = true;
                            invalidateOptionsMenu();
                            break;

                        case BixolonPrinter.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;

                        case BixolonPrinter.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            mIsConnected = false;
                            invalidateOptionsMenu();
                            break;
                    }
                    return true;

                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    if (msg.obj == null) {
                        Toast.makeText(getApplicationContext(), "No paired device", Toast.LENGTH_SHORT).show();
                    } else {
                        DialogManager.showBluetoothDialog(qrcode_print.this, (Set<BluetoothDevice>) msg.obj);
                    }
                    return true;

                case BixolonPrinter.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), mConnectedDeviceName, Toast.LENGTH_LONG).show();
                    return true;
            }
            return false;
        }
    });


    private final void setStatus(int resId) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subtitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subtitle);
    }


    private Button.OnClickListener btnlistener = new Button.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnok:
                    try {
                        if (edt1.getText().length() > 0 && edt3.getText().length() > 0 && edt4.getText().length() > 0) {
                            ujsonobject = new JSONObject();
                            ujsonobject.put("ima01", edt1.getText().toString());
                            Thread api = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (chkima("http://172.16.40.20/" + g_server + "/QRprint/chkima.php")) {
                                        creatlist(Integer.parseInt(edt4.getText().toString()));
                                        get_imaud04(edt1.getText().toString().trim());
                                    } else {
                                        Toast.makeText(getApplicationContext(), "無此料號", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            api.start();
                        }
                    } catch (Exception e) {
                    }
                    break;
                case R.id.btnprint:
                    if (list01.getCount() > 0) {
                        Boolean chk = checkdata();
                        if (chk) {
                            Cursor cursor = db.getAll();
                            printSample2(cursor);
                            //cursor.close();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.qtyNotMatch, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    break;
                case R.id.btncancel:
                    cancel();
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener listviewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
            if (position != Integer.parseInt(edt4.getText().toString()) - 1) {
                try {
                    TextView qrb00 = (TextView) view.findViewById(R.id.qrb00);
                    TextView qrb01 = (TextView) view.findViewById(R.id.qrb01);
                    TextView qrb02 = (TextView) view.findViewById(R.id.qrb02);
                    final int xqrb00 = Integer.parseInt(qrb00.getText().toString());
                    final String xqrb01 = qrb01.getText().toString();
                    String xqrb02 = qrb02.getText().toString();
                    if (xqrb02.equals("")) {
                        xqrb02 = "NULL";
                    }

                    //顯示對話框，輸入修改數量
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View layout = inflater.inflate(R.layout.qrcode_print_dialog01, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(qrcode_print.this);
                    builder.setTitle(R.string.changePrintQty);
                    builder.setView(layout);
                    final String finalXqrb02 = xqrb02;
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            EditText ed1 = (EditText) layout.findViewById(R.id.qrcode_print_dialog01_ed1);
                            Double qty = Double.parseDouble(ed1.getText().toString());

                            String code = "new_" + xqrb01 + "_" + finalXqrb02 + "_" + qty;
                            db.update(xqrb00, qty, code);
                            String res = recal_qty();


                            Cursor cursor = db.getAll();
                            UpdateAdapter(cursor);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });


                    builder.create().show();
                } catch (Exception e) {
                } finally {
                }
            }

        }
    };

    private void getcode(final String qrcode) {

        try {
            if (qrcode.startsWith("new")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //重印、拆單標籤格式 new_料號_批號_數量
                                int index1 = qrcode.indexOf("_");
                                int index2 = qrcode.indexOf("_", index1 + 1);
                                int index3 = qrcode.indexOf("_", index2 + 1);
                                int index4 = qrcode.indexOf("_", index3 + 1);  //預備給 測試料號會多出 T_

                                if (qrcode.substring(4, index2).equals("T")) {
                                    //料號
                                    edt1.setText(qrcode.substring(4, index3));
                                    //品名-規格
                                    get_imaud04(qrcode.substring(4, index3));

                                    //批號
                                    String g_solo = "";
                                    if (qrcode.substring(index3 + 1, index4).equals("NULL")) {
                                        g_solo = "";
                                    } else {
                                        g_solo = qrcode.substring(index3 + 1, index4);
                                    };

                                    edt2.setText(g_solo);
                                    //數量
                                    edt3.setText(qrcode.substring(index4 + 1));
                                } else {
                                    //料號
                                    edt1.setText(qrcode.substring(4, index2));
                                    //品名-規格
                                    get_imaud04(qrcode.substring(4, index2));

                                    //批號
                                    String g_solo = "";
                                    if (qrcode.substring(index2 + 1, index3).equals("NULL")) {
                                        g_solo = "";
                                    } else {
                                        g_solo = qrcode.substring(index2 + 1, index3);
                                    };

                                    edt2.setText(g_solo);
                                    //數量
                                    edt3.setText(qrcode.substring(index3 + 1));
                                }
                                //拆量
                                edt4.setText("2");
                            }
                        });
                    }

                });
                api.start();
            } else if (qrcode.substring(0, 5).equals("BC525") || qrcode.substring(0, 5).equals("BC527") || qrcode.substring(0, 5).equals("BB525") || qrcode.substring(0, 5).equals("BB527")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //極版標籤 BC527-2101000198_1_07030333C_29568
                                int index1 = qrcode.indexOf("_");
                                int index2 = qrcode.indexOf("_", index1 + 1);
                                int index3 = qrcode.indexOf("_", index2 + 1);
                                int index4 = qrcode.indexOf("_", index3 + 1);  //預備給 測試料號會多出 T_

                                if (qrcode.substring(index2 + 1, index3).equals("T")) {
                                    //取得料號
                                    edt1.setText(qrcode.substring(index2 + 1, index4));
                                    //品名-規格
                                    get_imaud04(qrcode.substring(index2 + 1, index4));
                                    //取得批號
                                    String g_solo = "";
                                    if (getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + qrcode + "&kind=" + 2).equals("NULL")) {
                                        g_solo = "";
                                    } else {
                                        g_solo = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + qrcode + "&kind=" + 2);
                                    }
                                    ;
                                    edt2.setText(g_solo);
                                    //取得數量
                                    edt3.setText(qrcode.substring(index4 + 1));

                                } else {
                                    //取得料號
                                    edt1.setText(qrcode.substring(index2 + 1, index3));
                                    //品名-規格
                                    get_imaud04(qrcode.substring(index2 + 1, index3));

                                    //取得批號
                                    String g_solo = "";
                                    if (getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + qrcode + "&kind=" + 2).equals("NULL")) {
                                        g_solo = "";
                                    } else {
                                        g_solo = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + qrcode + "&kind=" + 2);
                                    }
                                    ;
                                    edt2.setText(g_solo);
                                    //取得數量
                                    edt3.setText(qrcode.substring(index3 + 1));
                                }
                                //拆量
                                edt4.setText("2");


                            }
                        });
                    }
                });
                api.start();
            } else if (qrcode.substring(0, 5).equals("CC512") || qrcode.substring(0, 5).equals("CC513") || qrcode.substring(0, 5).equals("CC514") || qrcode.substring(0, 5).equals("CC515") || qrcode.substring(0, 5).equals("CC518") || qrcode.substring(0, 8).equals("OLDSTAMP")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //廣泰標籤 CC512-2010000697-108(CC5A2_KD91-1_NULL)_(03020489V_nap_WPX7A-BA)_20201026007002
                                int index1 = qrcode.indexOf("-", 6);
                                int index2 = qrcode.indexOf("("); //第一個(位置
                                int index3 = qrcode.indexOf("(", index2 + 1); //第二個(位置
                                int index4 = qrcode.indexOf("_", index3); //料號後的_位置
                                //取得料號
                                edt1.setText(qrcode.substring(index3 + 1, index4));
                                //品名-規格
                                get_imaud04(qrcode.substring(index3 + 1, index4));

                                //取得批號
                                String g_solo = "";
                                if (getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + qrcode + "&kind=" + 1).equals("NULL")) {
                                    g_solo = "";
                                } else {
                                    g_solo = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + qrcode + "&kind=" + 1);
                                }
                                ;
                                edt2.setText(g_solo);
                                //取得數量
                                if (index2 > 32) {
                                    String g_qr03_datastr = qrcode.substring(0, index2);
                                    Integer g_len_g_qr03_datastr = g_qr03_datastr.length();
                                    Double l_sl1 = 0.0, l_sl2 = 0.0, l_sl3 = 0.0;
                                    Integer k = 0;

                                    for (int i = 0; i <= g_len_g_qr03_datastr; i++) {
                                        String l_code = "";
                                        String j = "";
                                        if (i < g_len_g_qr03_datastr) {
                                            j = g_qr03_datastr.substring(i, i + 1);
                                        }

                                        if (j.equals("_") || i == g_len_g_qr03_datastr) {
                                            if (i == g_len_g_qr03_datastr) {
                                                l_code = g_qr03_datastr.substring(k, i);
                                                l_sl3 = Double.valueOf(l_code.substring(17, l_code.length()));
                                            } else {
                                                l_code = g_qr03_datastr.substring(k, i);
                                                if (l_sl1 == 0) {
                                                    l_sl1 = Double.valueOf(l_code.substring(17, l_code.length()));
                                                } else {
                                                    l_sl2 = Double.valueOf(l_code.substring(17, l_code.length()));
                                                }
                                            }
                                            k = i + 1;
                                        }
                                    }

                                    edt3.setText(Double.valueOf(l_sl1 + l_sl2 + l_sl3).toString());
                                } else {
                                    edt3.setText(qrcode.substring(index1 + 1, index2));
                                }



                                //拆量
                                edt4.setText("2");


                            }
                        });
                    }
                });
                api.start();
            } else if (qrcode.startsWith("-", 5)) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //供應商條碼 BB421-2101000169_2_07100071A_20211007_792
                                int index1 = qrcode.indexOf("_");
                                int index2 = qrcode.indexOf("_", index1 + 1);
                                int index3 = qrcode.indexOf("_", index2 + 1);
                                int index4 = qrcode.indexOf("_", index3 + 1);
                                //取得料號
                                edt1.setText(qrcode.substring(index2 + 1, index3));
                                //品名-規格
                                get_imaud04(qrcode.substring(index2 + 1, index3));
                                //取得批號
                                String g_solo = "";
                                if (qrcode.substring(index3 + 1, index4).equals("NULL")) {
                                    g_solo = "";
                                } else {
                                    g_solo = qrcode.substring(index3 + 1, index4);
                                }
                                ;
                                edt2.setText(g_solo);
                                //取得數量
                                edt3.setText(qrcode.substring(index4 + 1));
                                //拆量
                                edt4.setText("2");
                            }
                        });
                    }
                });
                api.start();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.E23, Toast.LENGTH_SHORT).show();
                        firstDetected = true;
                    }
                });
            }
            ;
        } catch (Exception e) {
        }
    }

    private void get_imaud04(String x_ima01) {

        new print_getItemData().execute("http://172.16.40.20/" + g_server + "/QR220/get_ItemData.php?item=" + x_ima01.trim());
        /*String res = "";

        res1 = "";
        res2 = "";
        res3 = "";

        try {
            res = new print_getItemData().execute("http://172.16.40.20/" + g_server + "/QR220/get_ItemData.php?item=" + x_ima01.trim()).get();
            if (!res.equals("FAIL")) {
                try {
                    JSONArray jsonarray = new JSONArray(res);
                    JSONObject jsonObject = jsonarray.getJSONObject(0);
                    res1 = jsonObject.getString("TA_IMA02_1"); //品名越文;
                    res2 = jsonObject.getString("TA_IMA021_1"); //規格;
                    res3 = jsonObject.getString("IMA02"); //品名中文;
                    result = res1 + "\n" + res2;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

    }

    //Hàm cập nhật thông tin ima_file của vật liệu vừa được quét
    private class print_getItemData extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            return docNoiDung_Tu_URL(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            name_result = "";
            res1 = "";
            res2 = "";
            res3 = "";
            try {
                if(!s.equals("FAIL")) {
                    JSONArray jsonarray = new JSONArray(s);
                    JSONObject jsonObject = jsonarray.getJSONObject(0);
                    res1 = jsonObject.getString("TA_IMA02_1"); //品名越文;
                    res2 = jsonObject.getString("TA_IMA021_1"); //規格;
                    res3 = jsonObject.getString("IMA02"); //品名中文;
                    name_result = res1 + "\n" + res2;
                    tv5.setText(name_result);
                }
            }catch (Exception e){}
        }
    }


    private void creatlist(int num) {
        try {
            db.close();
            db.open();
            final Double count = Double.valueOf(edt3.getText().toString());//總數量
            Double qty = Double.valueOf(Math.round(count / num));//拆標後平均數量
            Double count2 = count;//計算餘數
            //取得單頭資料
            String xqrb01 = edt1.getText().toString().trim();
            String xqrb02 = edt2.getText().toString().trim();
            Double xqrb03 = qty;
            for (int i = 1; i <= num; i++) {
                if (xqrb02.length() == 0) {
                    xqrb02 = "NULL";
                }
                if (i == num) {
                    //避免有餘數，最後一個標籤要是剩餘量
                    String code = "new_" + xqrb01 + "_" + xqrb02 + "_" + count2;
                    if (xqrb02.equals("NULL")) {
                        xqrb02 = "";
                    }
                    db.append(i, xqrb01, xqrb02, count2, code);
                } else {
                    String code = "new_" + xqrb01 + "_" + xqrb02 + "_" + xqrb03;
                    if (xqrb02.equals("NULL")) {
                        xqrb02 = "";
                    }
                    db.append(i, xqrb01, xqrb02, xqrb03, code);
                }
                count2 = count2 - qty;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = db.getAll();
                    UpdateAdapter(cursor);
                }
            });
        } catch (Exception e) {

        }
    }


    @SuppressLint("Range")
    private String recal_qty() {
        String res = "OK";
        Double val_edt3 = Double.parseDouble(edt3.getText().toString());
        Integer val_edt4 = Integer.parseInt(edt4.getText().toString());
        Double sumY = 0.0, sumN = 0.0;
        Integer countY = 0, countN = 0;
        Double tt_slpb = 0.0, slpb = 0.0;

        try {
            Cursor g_cursor = db.sum_qrb03("Y");
            g_cursor.moveToFirst();
            sumY = Double.parseDouble(g_cursor.getString(g_cursor.getColumnIndex("SUMQRB03"))); //條碼總數
            if (sumY > val_edt3) {
                res = "FAIL";
            }

            g_cursor = db.sum_qrb03("N");
            g_cursor.moveToFirst();
            sumN = Double.parseDouble(g_cursor.getString(g_cursor.getColumnIndex("SUMQRB03"))); //條碼總數

            g_cursor = db.count_qrb05("Y");
            g_cursor.moveToFirst();
            countY = Integer.parseInt(g_cursor.getString(g_cursor.getColumnIndex("COUNTQRB05"))); //條碼隻數
            countN = val_edt4 - countY;

            tt_slpb = val_edt3 - sumY; //再分攤數量
            slpb = Double.valueOf(Math.round(tt_slpb / countN));
            db.recal(countN, tt_slpb, slpb);
        } catch (Exception e) {
        }

        return res;
    }

    private void cancel() {
        try {
            db.close();
            db.open();
            edt1.setText("");
            edt2.setText("");
            edt3.setText("");
            edt4.setText("");
            tv5.setText("");
            Cursor cursor = db.getAll();
            UpdateAdapter(cursor);
            firstDetected = true;
        } catch (Exception e) {

        }


    }

    private boolean checkdata() {
        try {
            double count = Double.parseDouble(edt3.getText().toString()); //總數量
            double qtycount = 0.0;
            Cursor cursor = db.getAll();
            while (cursor.moveToNext()) {
                @SuppressLint("Range") double qty = Double.parseDouble(cursor.getString(cursor.getColumnIndex("qrb03")));
                qtycount = qtycount + qty;
            }
            cursor.close();
            if (qtycount == count) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    private boolean chkima(String apiUrl) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            writer.write(ujsonobject.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            if (result.equals("TRUE")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

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

    public void UpdateAdapter(Cursor cursor) {
        try {
            if (cursor != null && cursor.getCount() >= 0) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.qrcode_print_listview, cursor,
                        new String[]{"qrb00", "qrb01", "qrb02", "qrb03"}, new int[]{R.id.qrb00, R.id.qrb01, R.id.qrb02, R.id.qrb03}, 0);
                list01.setAdapter(adapter);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void printSample2(final Cursor cursor1) {
        try {
            if (mSampleDialog == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View view = inflater.inflate(R.layout.dialog_page_mode_sample, null);
                cursor1.moveToFirst();
                @SuppressLint("Range") String xqrb01 = cursor1.getString(cursor1.getColumnIndex("qrb01"));
                @SuppressLint("Range") String xqrb02 = cursor1.getString(cursor1.getColumnIndex("qrb02"));
                @SuppressLint("Range") Double xqrb03 = cursor1.getDouble(cursor1.getColumnIndex("qrb03"));
                @SuppressLint("Range") String xqrb04 = cursor1.getString(cursor1.getColumnIndex("qrb04"));
                String today = String.valueOf(new Time(Time.getCurrentTimezone()));

                SimpleDateFormat dateFormat;
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                today = dateFormat.format(new Date());


                final ImageView img = (ImageView) view.findViewById(R.id.img01);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap1 = encoder.encodeBitmap(xqrb04, BarcodeFormat.QR_CODE, 300, 300);
                img.setImageBitmap(bitmap1);


                TextView tx01 = (TextView) view.findViewById(R.id.tx01);
                TextView tx02 = (TextView) view.findViewById(R.id.tx02);
                TextView tx03 = (TextView) view.findViewById(R.id.tx03);
                TextView tx04 = (TextView) view.findViewById(R.id.tx04);
                TextView tx05 = (TextView) view.findViewById(R.id.tx05);
                TextView tx_printDatetime= view.findViewById(R.id.tx_printDatetime);
                //img = (ImageView) view.findViewById(R.id.img01);
                tx01.setText(xqrb01);
                tx02.setText(xqrb02);
                tx03.setText(xqrb03.toString());
                tx04.setText(res3 + "-" + res1);
                tx05.setText(res2);
                tx_printDatetime.setText(String.format(today, "%k:%M:%S"));

                mSampleDialog = new AlertDialog.Builder(qrcode_print.this).setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cursor1.moveToFirst()) {
                            do {
                                try {
                                    @SuppressLint("Range") String xqrb01 = cursor1.getString(cursor1.getColumnIndex("qrb01"));
                                    @SuppressLint("Range") String xqrb02 = cursor1.getString(cursor1.getColumnIndex("qrb02"));
                                    @SuppressLint("Range") Double xqrb03 = cursor1.getDouble(cursor1.getColumnIndex("qrb03"));
                                    @SuppressLint("Range") String xqrb04 = cursor1.getString(cursor1.getColumnIndex("qrb04"));
                                    if (xqrb03 > 0) {
                                        /*TextView tx01 = (TextView) view.findViewById(R.id.tx01);
                                        TextView tx02 = (TextView) view.findViewById(R.id.tx02);
                                        TextView tx03 = (TextView) view.findViewById(R.id.tx03);
                                        TextView tx04 = (TextView) view.findViewById(R.id.tx04);
                                        TextView tx05 = (TextView) view.findViewById(R.id.tx05);
                                        ImageView img = (ImageView) view.findViewById(R.id.img01);*/
                                        tx01.setText(xqrb01);
                                        tx02.setText(xqrb02);
                                        tx03.setText(xqrb03.toString());
                                        tx04.setText(res3 + "-" + res1);
                                        tx05.setText(res2);
                                        //Bitmap bitmap1 = Create2DCode(xqrb04);
                                        BarcodeEncoder encoder = new BarcodeEncoder();
                                        Bitmap bitmap1 = encoder.encodeBitmap(xqrb04, BarcodeFormat.QR_CODE, 300, 300);
                                        img.setImageBitmap(bitmap1);
                                        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
                                        layout.buildDrawingCache();
                                        Bitmap bitmap = layout.getDrawingCache();
                                        mBixolonPrinter.setPageMode();
                                        //mBixolonPrinter.setPrintDirection(BixolonPrinter.DIRECTION_180_DEGREE_ROTATION);
                                        mBixolonPrinter.setPrintArea(0, 0, 680, 425);
                                        mBixolonPrinter.setAbsoluteVerticalPrintPosition(397);
                                        mBixolonPrinter.printBitmap(bitmap, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.BITMAP_WIDTH_FULL, 100, false);
                                        mBixolonPrinter.formFeed(true);

                                    }
                                    ;
                                } catch (WriterException e) {
                                } catch (Exception e) {

                                }
                            } while (cursor1.moveToNext());
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).create();

            }
            mSampleDialog.show();
        } catch (Exception e) {

        } finally {
            mSampleDialog = null;
        }

    }

    public Bitmap Create2DCode(String str) throws WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 200, 200);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private String docNoiDung_Tu_URL(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            // create a url object
            URL url = new URL(theUrl);
            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();
            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                //content.append(line + "\n");
                content.append(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

}