package com.sensoftsarl.nfclibrary.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.sensoftsarl.nfclibrary.bean.TransmitParams;
import com.sensoftsarl.nfclibrary.util.NfcReaderUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NfcReaderManagerActivity extends Activity {

    //  BroadcastReceiverUsbDevice broadcastReceiverUsbDevice;
    IntentFilter filter = new IntentFilter();
    protected UsbManager mManager;
    protected Reader mReader;

    NfcReaderUtil nfcReaderUtil = new NfcReaderUtil();
    List<TransmitParams> paramsList = null;
    String deviceName;
    private static  String TAG = NfcReaderManagerActivity.class.getSimpleName();

    /**
     * path port usb device
     */
    private String pathUsb;
    protected boolean portOpen = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;

    public NfcReaderManagerActivity() {
    }


    String[] stateStrings = {"Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific"};
    String current = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Passage onCreate");
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mReader = new Reader(mManager);



    /*    mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {
            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {


                Log.d(TAG,"BEFORE setOnStateChangeListener slotNum == "+slotNum+", prevState == "+prevState+", currState == "+currState);
                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }
                Log.d(TAG,"AFTER setOnStateChangeListener slotNum == "+slotNum+", prevState == "+prevState+", currState == "+currState);

                current = stateStrings[currState];

                // Show output
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if ("Present".equals(current)) {
                            try {
                                Log.i(TAG, " Debut ecriture ");

                             } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
            }


        });*/

        // broadcastReceiverUsbDevice = new BroadcastReceiverUsbDevice(getApplicationContext());

//Intent intent = new Intent(this,BroadcastReceiverUsbDevice.class);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        Log.i(TAG, "Passage onCreate terminé");
        try {
            registerReceiver(mUsbDevice, filter);
        } catch (Exception e) {
            Log.d("Throwable => ", e.getMessage());
        }


        deviceName = refresh_auto();
        if (deviceName == null){
            Log.d(TAG,"Aucun device detecté!!");

            Toast.makeText(this,"Aucun device detecté!!",Toast.LENGTH_LONG).show();
        }else{
            ouvrir_port(deviceName);
        }

    }


    private final BroadcastReceiver mUsbDevice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    deviceName = refresh_auto();
                    Log.i(TAG, "Avant TimerTask");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            ouvrir_port(deviceName);

                            Log.i(TAG, "Aprés TimerTask");
                        }

                    }, 1000);


                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                synchronized (this) {

                    // Update reader list

                    Toast.makeText(context, "Lecteur est débranché ", Toast.LENGTH_LONG).show();


                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {

                        new CloseTask().execute();
                    }
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {


                synchronized (this) {

                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    portOpen = false;
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {

                            // Open reader

                            new OpenTask().execute(device);

                            Log.i(TAG, "ouverture de port effectuée");
                        }

                    }
                }

            }


        }
    };


    class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

        @Override
        protected Exception doInBackground(UsbDevice... params) {

            Exception result = null;

            try {

                mReader.open(params[0]);

            } catch (Exception e) {

                result = e;
            }

            return result;
        }


        @Override
        protected void onPostExecute(Exception result) {

        }

    }

    public  class CloseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            mReader.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

    }


    /**
     * permet de rafraichir le spinner et de selectionner le device usb
     */
    public String refresh_auto() {

        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                pathUsb = device.getDeviceName();
            }
        }

        return pathUsb;

    }


    /**
     * permet d'ouvrir le port automatiquement
     */
    public boolean ouvrir_port(String deviceName) {
        Log.i(TAG, "ouverture port");
        try {
            if (deviceName != null && !portOpen) {


                // For each device
                for (UsbDevice device : mManager.getDeviceList().values()) {

                    // If device name is found
                    if (deviceName.equals(device.getDeviceName())) {

                        // Request permission
                        mManager.requestPermission(device,
                                mPermissionIntent);

                        portOpen = true;
                        Log.i(TAG, "port ouvert");
                        return true;
                    }
                }

            }

        } catch (Exception e) {
            portOpen = false;
            Log.e("ERROR ouvrir port == > ", e.getMessage());
        }
        return false;
    }


}

