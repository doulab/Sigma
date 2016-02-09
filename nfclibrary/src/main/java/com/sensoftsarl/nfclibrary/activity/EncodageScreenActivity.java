package com.sensoftsarl.nfclibrary.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acs.smartcard.Features;
import com.acs.smartcard.PinProperties;
import com.acs.smartcard.Reader;
import com.sensoftsarl.nfclibrary.R;
import com.sensoftsarl.nfclibrary.bean.TransmitParams;
import com.sensoftsarl.nfclibrary.bean.TransmitProgress;
import com.sensoftsarl.nfclibrary.util.NfcReaderUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activité qui permet d'encoder la trame sur la carte
 * <p/>
 * reçoit en paramétre la trame à encoder
 */

public class EncodageScreenActivity extends Activity {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String[] stateStrings = {"Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific"};

    public static int FORMAT_TRAME_INCORRECT = 1;

    boolean onClosePort = false;
    public static final int RESULT_CODE_ECHEC_ECRITURE = 9;
    private static String TAG = EncodageScreenActivity.class.getSimpleName();
    public int countBlocWrited = 0;
    protected boolean portOpen = false;
    private static final int ENCODAGE = 2;
    IntentFilter filter = new IntentFilter();

    String deviceName;
    // classe utilitaire
    NfcReaderUtil nfcReaderUtil = new NfcReaderUtil();
    ProgressDialog progressBar;
    boolean requested = false;
    // liste des commandes à hexadecimal pour l'écriture
    List<TransmitParams> paramsList = null;
    //    trame passé en paramétre
    String trame;
    String messageErreur;
    String messageProgressBar;

    private String pathUsb;
    private UsbManager mManager;
    // classe (Reader SDK) permettant de communiquer avec le lecteur nfc
    private Reader mReader;
    private PendingIntent mPermissionIntent;

    private LinearLayout linearLayoutMessageErreur;
    private TextView textViewMessageErreur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_encodage_screen);
        Log.i(TAG, TAG + " DEBUT --- onCreate");


        // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Initialize reader
        mReader = new Reader(mManager);

        //Get trame from intent
        Intent intent = getIntent();
        trame = intent.getStringExtra("trame");

        if (intent.hasExtra("messageErreur")) {


            messageErreur = intent.getStringExtra("messageErreur");


        }
        Log.d(TAG, "port ouvert : " + portOpen);


        try {
            // Recuper la liste des params
            paramsList = nfcReaderUtil.preparerTransmitListParamsEcriture(trame);

        } catch (Exception e) {

            e.printStackTrace();
            // setResult du format de trame incorrect
            setResult(FORMAT_TRAME_INCORRECT);
            finish();
        }

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);


        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbDevice, filter);
        Log.i(TAG, "Passage onCreate terminé");


        // Detection evenement liés au lecteur (presence carte)
        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {

            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                Log.d(TAG, "setOnStateChangeListener");
                Log.d(TAG, "BEFORE setOnStateChangeListener slotNum == " + slotNum + ", prevState == " + prevState + ", currState == " + currState);

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }


                // recup etat presence de la carte
                current = stateStrings[currState];


                // Show output
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if ("Present".equals(current)) {
                            try {

                                // Lancement progress bar
                                progressBar();

                                // appel methode ecriturez
                                ecrire(paramsList);


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                    }
                });
            }
        });
        // recuperation nom device
        deviceName = searchDevice();
        if (deviceName == null) {
            Log.w(TAG, "Aucun device detecté!!");
            Toast.makeText(this, "Aucun device detecté!!", Toast.LENGTH_LONG).show();
        } else {
            openPort(deviceName);
        }
        Log.d(TAG, TAG + " FIN --- onCreate");

    }


    /**
     * BroadcastReceiver permettant de detecter si le device est branché ou débranché  ainsi que la permission de l'utilisation du port usb
     */

    private final BroadcastReceiver mUsbDevice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    deviceName = searchDevice();
                    Log.i(TAG, "Avant TimerTask");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            openPort(deviceName);

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

                        // fermeture port statut : LECTEUR DEBRANCHE
                        closePort(NfcReaderUtil.LECTEUR_DEBRANCHE);
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
    private Features mFeatures = new Features();
    private String current;


    /**
     * methote ecriture sur carte
     *
     * @param paramsList liste des commandes d'écriture
     * @throws InterruptedException
     */
    public void ecrire(List<TransmitParams> paramsList) throws Exception {
        Log.d(TAG, "Debut methode ecrire");

        for (TransmitParams params : paramsList) {
            Log.d(TAG, " Lancement Ecriture bloc" + params);
            //  lancement tâche écriture par bloc

            if (!onClosePort) {
                new TransmitTask().execute(params);
            }

        }


    }

    /**
     * permet de rechercher un device connecté
     */
    public String searchDevice() {

        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                pathUsb = device.getDeviceName();
            }
        }

        return pathUsb;

    }

    /**
     * Methode fermeture port usb
     *
     * @param status
     */
    private void closePort(int status) {

        onClosePort = true;
        CloseTask closeTask = new CloseTask();
        closeTask.status = status;
        closeTask.execute();
    }

    /**
     * permet d'ouvrir le port automatiquement
     */

    public boolean openPort(String deviceName) {
        Log.i(TAG, " ouverture port ");
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

    public void progressBar() {

        messageProgressBar = getResources().getString(R.string.info_wait_writing1);

        // prepare for a progress bar dialog
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage(messageProgressBar);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closePort(NfcReaderUtil.ON_BACK_PRESSED);
    }

    /**
     * TransmitTask permet d'executer en tache de fond l'écriture d'un bloc sur la carte
     */

    public class TransmitTask extends
            AsyncTask<TransmitParams, TransmitProgress, Void> {

        @Override
        protected Void doInBackground(TransmitParams... params) {


            if (!onClosePort) {


                Log.d(TAG, " Ecriture bloc en cours ....");
                TransmitProgress progress = null;

                byte[] command = null;

                byte[] response = null;
                int responseLength = 0;
                int foundIndex = 0;
                int startIndex = 0;

                do {

                    // Find carriage return
                    foundIndex = params[0].commandString.indexOf('\n', startIndex);
                    if (foundIndex >= 0) {
                        command = nfcReaderUtil.toByteArray(params[0].commandString.substring(startIndex, foundIndex));

                    } else {
                        command = nfcReaderUtil.toByteArray(params[0].commandString.substring(startIndex));

                    }

                    // Set next start index
                    startIndex = foundIndex + 1;

                    response = new byte[300];
                    progress = new TransmitProgress();
                    progress.controlCode = params[0].controlCode;
                    try {

                        if (params[0].controlCode < 0) {

                            // Transmit APDU
                            responseLength = mReader.transmit(params[0].slotNum,
                                    command, command.length, response,
                                    response.length);

                        } else {

                            // Transmit control command
                            responseLength = mReader.control(params[0].slotNum,
                                    params[0].controlCode, command, command.length,
                                    response, response.length);
                        }

                        progress.command = command;
                        progress.commandLength = command.length;
                        progress.response = response;
                        progress.responseLength = responseLength;
                        progress.e = null;

                    } catch (Exception e) {

                        progress.command = null;
                        progress.commandLength = 0;
                        progress.response = null;
                        progress.responseLength = 0;
                        progress.e = e;
                    }

                    publishProgress(progress);

                } while (foundIndex >= 0);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(TransmitProgress... progress) {
            Log.d(TAG, " Ecriture bloc en progression ....");
            if (!onClosePort) {

                Log.d(TAG, "responseCode toHex = " + nfcReaderUtil.convertStringToHex(new String(progress[0].response)));
                Log.i(TAG, "reponse code length()== > " + progress[0].responseLength);
                int deuxCaracte = 2;
                int deuxDigit = 2;

                // code retour commande 'ERROR => 63' 'SUCCESS = > 90'
                String responseCode = nfcReaderUtil.convertStringToHex(new String(progress[0].response));
                int responseLengthCode = progress[0].responseLength;

                // TEST de lecture avec code d'erreur
                if (responseLengthCode == 2 && NfcReaderUtil.RESPONSE_CODE_ERREUR_TO_STRING.equals(responseCode.substring(0, responseLengthCode))) {
                    Log.d(TAG, "Code ERREUR" + nfcReaderUtil.convertStringToHex(new String(progress[0].response)));
                    closePort(NfcReaderUtil.ECHEC_ECRITURE_BLOC);
                }
                // TEST de lecture avec code Succuess
                else if (responseLengthCode == 2 && NfcReaderUtil.RESPONSE_CODE_SUCCESS_TO_STRING.equals(responseCode.substring(0, responseLengthCode))) {

                    String command_text = new String(progress[0].command);

                    String reponse_text = new String(progress[0].response);


                    if (progress[0].response != null
                            && progress[0].responseLength > 0) {

                        int controlCode;
                        int i;

                        // Show control codes for IOCTL_GET_FEATURE_REQUEST
                        if (progress[0].controlCode == Reader.IOCTL_GET_FEATURE_REQUEST) {

                            mFeatures.fromByteArray(progress[0].response,
                                    progress[0].responseLength);

                            //logMsg("Features:");
                            for (i = Features.FEATURE_VERIFY_PIN_START; i <= Features.FEATURE_CCID_ESC_COMMAND; i++) {

                                controlCode = mFeatures.getControlCode(i);

                            }

                        }
                        controlCode = mFeatures
                                .getControlCode(Features.FEATURE_IFD_PIN_PROPERTIES);
                        if (controlCode >= 0
                                && progress[0].controlCode == controlCode) {

                            PinProperties pinProperties = new PinProperties(
                                    progress[0].response,
                                    progress[0].responseLength);

                        }

                        controlCode = mFeatures
                                .getControlCode(Features.FEATURE_GET_TLV_PROPERTIES);

                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!onClosePort) {

                // STATUT CARTE ABSENTE lors de l'ecriture
                if ("Absent".equals(current)) {
                    try {
                        // fermeture progressBar
                        progressBar.dismiss();

                        // fermeture port
                        closePort(NfcReaderUtil.CARTE_ABSENTE);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if ("Present".equals(current)) {

                    countBlocWrited++;


                    if (progressBar != null && progressBar.isShowing()) {
                        //calcul de pourcentage de progression et mise a jour du statut
                        int percent = (countBlocWrited * 100) / paramsList.size();
                        progressBar.setProgress(percent);

                    }
                    Log.d(TAG, " Fin ecriture bloc " + countBlocWrited);

                    if (countBlocWrited == paramsList.size()) {
                        Log.d(TAG, " Fin ecriture des bloc ");
                        closePort(NfcReaderUtil.ECRITURE_COMPLETE);

                    }
                } else {
                    closePort(NfcReaderUtil.UNKNOW_STATUT);
                }
            }
        }
    }

    /**
     * OpenTask permet l'ouverture du port
     */
    class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

        @Override
        protected Exception doInBackground(UsbDevice... params) {
            Log.d(TAG, "OpenTask => doInBackground");
            Exception result = null;

            try {

                mReader.open(params[0]);

            } catch (Exception e) {
                Log.e(TAG, "OpenTask Exception =>" + e.getMessage());
                result = e;
            }

            return result;
        }


        @Override
        protected void onPostExecute(Exception result) {
            Log.d(TAG, "OpenTask => onPostExecute");

        }

    }

    /**
     * CloseTask permet la fermeture du port
     */
    public class CloseTask extends AsyncTask<Void, Void, Void> {

        private int status;


        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "CloseTask => doInBackground...");
            mReader.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            String messageError;

            if (progressBar != null && progressBar.isShowing()) {

                progressBar.dismiss();

            }

            // ======= écriture incomplete ====

            if (status == NfcReaderUtil.ECHEC_ECRITURE_BLOC) {

                messageError = "Encodage incomplet";
                resultCodeEchecEcriture(messageError);

            } else if (status == NfcReaderUtil.ERREUR_ECRITURE_BLOC) {

                messageError = "Encodage incomplet, Une erreur s'est produite";
                resultCodeEchecEcriture(messageError);

            } else if (status == NfcReaderUtil.CARTE_ABSENTE) {

                messageError = "Encodage incomplet, Veuillez aposer la carte";
                resultCodeEchecEcriture(messageError);

            } else if (status == NfcReaderUtil.LECTEUR_DEBRANCHE) {
                messageError = "Encodage incomplet, Lecteur debranché";
                resultCodeEchecEcriture(messageError);

            } else if (status == NfcReaderUtil.ON_BACK_PRESSED) {
                Toast.makeText(getApplicationContext(), "Encodage interrompu, Vous avez quitté le formulaire", Toast.LENGTH_LONG).show();
                finish();

            }
            // ======= fin écriture incomplete ====

            // ======= écriture complete ====

            else if (status == NfcReaderUtil.ECRITURE_COMPLETE) {
                setResult(Activity.RESULT_OK);
                finish();

            }
            // ======= fin écriture complete ====

            // ======= Anomalie ====
            else {
                messageError = "Aucun statut!!";
                resultCodeEchecEcriture(messageError);
            }
            // ======= fin Anomalie ====
            Log.d(TAG, "CloseTask => onPostExecute");

        }

    }

    /**
     * methode qui permet de gerrer les erreur
     *
     * @param messageErrorResultCodeErreur
     */

    public void resultCodeEchecEcriture(final String messageErrorResultCodeErreur) {
        Intent intent = new Intent();
        intent.putExtra("messageError", messageErrorResultCodeErreur);
        intent.putExtra("trame", trame);
        setResult(RESULT_CODE_ECHEC_ECRITURE, intent);
        finish();

    }


}
