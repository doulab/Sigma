package com.sensoftsarl.nfclibrary.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import com.acs.smartcard.Reader;

/**
 * Created by doulab on 26/01/16.
 */
public class BroadcastReceiverMreceiver extends BroadcastReceiver {

    private Reader mReader;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private Context context;

    public BroadcastReceiverMreceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {



        String action = intent.getAction();

        if (ACTION_USB_PERMISSION.equals(action)) {


            synchronized (this) {

                UsbDevice device = (UsbDevice) intent
                        .getParcelableExtra(UsbManager.EXTRA_DEVICE);

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

}
