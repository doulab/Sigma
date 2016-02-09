package com.sensoftsarl.nfclibrary.broadcastReceiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.acs.smartcard.Reader;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by doulab on 26/01/16.
 */

public class BroadcastReceiverUsbDevice extends BroadcastReceiver {


    private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;
    String deviceName;

    /**
     * path port usb device
     */
    private String pathUsb;
    boolean portOpen = false;


    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private Context context;


    public BroadcastReceiverUsbDevice(Context context) {
        super();
        this.context = context;
        mManager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
        mReader = new Reader(mManager);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();


        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            synchronized (this) {
                deviceName = refresh_auto();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ouvrir_port(deviceName);  // this code will be executed after 1 seconds
                    }
                }, 1000);


                if (ACTION_USB_PERMISSION.equals(action)) {


                    synchronized (this) {

                        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        portOpen = false;
                        if (intent.getBooleanExtra(
                                UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {

                                // Open reader

                                new OpenTask().execute(device);
                            }

                        }
                    }

                }

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
        }
    }


    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

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

    private class CloseTask extends AsyncTask<Void, Void, Void> {

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
