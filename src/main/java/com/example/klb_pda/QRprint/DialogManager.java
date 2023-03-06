package com.example.klb_pda.QRprint;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;

import java.util.Set;

public class DialogManager {
    static void showBluetoothDialog(Context context, final Set<BluetoothDevice> pairedDevices) {
        final String[] items = new String[pairedDevices.size()];
        int index = 0;
        for (BluetoothDevice device : pairedDevices) {
            items[index++] = device.getAddress();
        }

        new AlertDialog.Builder(context).setTitle("Paired Bluetooth printers")
                .setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        qrcode_print.mBixolonPrinter.connect(items[which]);

                    }
                }).show();
    }
}
