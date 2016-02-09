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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.acs.smartcard.Features;
import com.acs.smartcard.Reader;
import com.sensoftsarl.nfclibrary.R;
import com.sensoftsarl.nfclibrary.bean.TransmitParams;
import com.sensoftsarl.nfclibrary.bean.TransmitProgress;
import com.sensoftsarl.nfclibrary.util.NfcReaderUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activité permet de lire la trame de la carte
 */

public class LireScreenActivity extends Activity {


    private String pathUsb;
    protected boolean portOpen = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    IntentFilter filter = new IntentFilter();
    String deviceName;
    // trame passé en paramétre
    String trame;

    private UsbManager mManager;
    // classe (Reader SDK) permettant de communiquer avec le lecteur nfc
    private Reader mReader;
    private PendingIntent mPermissionIntent;

    private Features mFeatures = new Features();
    ProgressDialog progressBar;
    boolean requested = false;

    private static final String[] stateStrings = {"Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific"};

    private ArrayAdapter<String> mReaderAdapter;
    private String current;
    // classe utilitaire
    NfcReaderUtil nfcReaderUtil = new NfcReaderUtil();

    // liste des commandes à hexadecimal pour la lecture
    List<TransmitParams> paramsList = null;

    StringBuffer texte_complet = new StringBuffer();

    // nombre de bloc passer en parametre pour la lecture
    int nombreBloc;

    private static String TAG = LireScreenActivity.class.getSimpleName();
    public static int ECHEC_PREPARATION_COMMAND_LECTURE = 1;



    public int countBlocReaded = 0;
    String messageProgressBar;
    boolean onClosePort = false;

    public static final int RESULT_CODE_ECHEC_LECTURE = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lire_screen);
        Log.i(TAG, TAG + " DEBUT --- onCreate");


        // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Initialize reader
        mReader = new Reader(mManager);
        Intent intent = getIntent();
        if(intent.hasExtra("nombreBloc")) {

            nombreBloc = intent.getIntExtra("nombreBloc", 0);
        }
        deviceName = searchDevice();
        if (deviceName == null) {
            Log.d(TAG, "Aucun device detecté!!");
            Toast.makeText(this, "Aucun device detecté!!", Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "port ouvert : " + portOpen);

        try {
            // Recuper la liste des params
            paramsList = nfcReaderUtil.preparerTransmitListParamsLecture(nombreBloc);

        } catch (Exception e) {

            e.printStackTrace();
            // setResult du format de trame incorrect
            setResult(ECHEC_PREPARATION_COMMAND_LECTURE);
            finish();
        }
        //TODO renvoyer les types d'erreur en intent objet en cours de manipulation

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);


        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbDevice, filter);
        Log.i(TAG, "Passage onCreate terminé");


        Log.d(TAG, "Avant setOnStateChangeListener");

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


                current = stateStrings[currState];


                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if ("Present".equals(current)) {
                            try {
                                Log.i(TAG, " Debut LECTURE ");

                                // Lancement ProgressBar
                                progressBar();
                                // Lancement lecture
                                lire(paramsList);


                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, e.getMessage());
                                closePort(NfcReaderUtil.ECHEC_LECTURE_BLOC);

                            }
                        }


                    }
                });
            }
        });

        deviceName = searchDevice();
        if (deviceName == null) {
            Log.d(TAG, "Aucun device detecté!!");

            Toast.makeText(this, "Aucun device detecté!!", Toast.LENGTH_LONG).show();
        } else {
            openPort(deviceName);
        }

        Log.i(TAG, TAG + " FIN --- onCreate");

    }


    public void progressBar() {

        messageProgressBar = getResources().getString(R.string.info_wait_reading1);
        // prepare for a progress bar dialog
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage(messageProgressBar);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

    }

    /**
     * methode qui permet de lire par bloc
     */

    public void lire(List<TransmitParams> paramsList) {

        countBlocReaded = 0;
        Log.d(TAG, "lire " + "for ....");
        for (TransmitParams params : paramsList) {

            if (!onClosePort) {
                new LireTask().execute(params);
            }


        }

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

    /**
     * tâche qui permet l'ouverture d'un port
     */

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

    /**
     * tâche qui permet la fermeture du port
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


            // ======= lecture incomplete ====

            if (status == NfcReaderUtil.ECHEC_LECTURE_BLOC) {

                messageError = "Lecture incompléte";
                resultCodeEchecEcriture(messageError);


            } else if (status == NfcReaderUtil.ERREUR_LECTURE_BLOC) {

                messageError = "Lecture incompléte, Une erreur s'est produite";
                resultCodeEchecEcriture(messageError);

            } else if (status == NfcReaderUtil.CARTE_ABSENTE) {

                messageError = "Lecture incompléte, Veuillez aposer la carte";
                resultCodeEchecEcriture(messageError);

            } else if (status == NfcReaderUtil.LECTEUR_DEBRANCHE) {

                messageError = "Lecture incompléte, Lecteur debranché";
                resultCodeEchecEcriture(messageError);

            } else if (status == NfcReaderUtil.ON_BACK_PRESSED) {
                Toast.makeText(getApplicationContext(), "Lecture interrompue, Vous avez quitté le formulaire", Toast.LENGTH_LONG).show();

                finish();

            }
            // ======= fin lecture incomplete ====

            // ======= lecture complete ====

            else if (status == NfcReaderUtil.LECTURE_COMPLETE) {
                Intent intent = new Intent();
                intent.putExtra("trame", trame);
                setResult(Activity.RESULT_OK, intent);

                finish();

            }
            // ======= fin lecture complete ====

            // ======= Anomalie ====
            else {
                Toast.makeText(getApplicationContext(), "Aucun statut!!", Toast.LENGTH_LONG).show();

                finish();
            }
            // ======= fin Anomalie ====
            Log.d(TAG, "CloseTask => onPostExecute");

        }

    }

    /**
     * permet de rafraichir le spinner et de selectionner le device usb
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
     * permet d'ouvrir le port automatiquement
     */
    public boolean openPort(String deviceName) {
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
     * tâche permetant la lecture de tous les blocs
     */

    public class LireTask extends
            AsyncTask<TransmitParams, TransmitProgress, Void> {

        String lecture;

        @Override
        protected Void doInBackground(TransmitParams... params) {


            if (!onClosePort) {

                Log.d(TAG, " Lecture bloc en cours ....");
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
                        // command = params[0].commandString.substring(startIndex, foundIndex).getBytes();
                    } else {
                        command = nfcReaderUtil.toByteArray(params[0].commandString.substring(startIndex));
                        // command = params[0].commandString .substring(startIndex).getBytes();
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


                        Log.e(TAG, " Response " + nfcReaderUtil.convertStringToHex(new String(response)));
                        Log.e(TAG, " ResponseLength " + responseLength);


                    } catch (Exception e) {

                        progress.command = null;
                        progress.commandLength = 0;
                        progress.response = null;
                        progress.responseLength = 0;
                        progress.e = e;
                        Log.e(TAG, " Exception Lecture bloc ");
                    }

                    publishProgress(progress);

                } while (foundIndex >= 0);

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(TransmitProgress... progress) {
            Log.d(TAG, " LireNewTask onProgressUpdate ==> Lecture bloc en progression ...");

            if (!onClosePort) {

                Log.d(TAG, "LireNewTask onProgressUpdate ==> port ouvert ==> ");

                Log.d(TAG, "responseCode toHex = " + nfcReaderUtil.convertStringToHex(new String(progress[0].response)));

                String repons;
                StringBuffer reponse_text_concatener = new StringBuffer();
                String reponse_text = null;


                int deuxCaracte = 2;
                int deuxDigit = 2;

                // code retour commande 'ERROR => 63' 'SUCCESS = > 90'
                String responseCode = nfcReaderUtil.convertStringToHex(new String(progress[0].response));
                int responseLengthCode = progress[0].responseLength;

                // TEST de lecture avec code d'erreur
                if (responseLengthCode == 2 && NfcReaderUtil.RESPONSE_CODE_ERREUR_TO_STRING.equals(responseCode.substring(0, responseLengthCode))) {
                    Log.d(TAG, "Code ERREUR" + nfcReaderUtil.convertStringToHex(new String(progress[0].response)));
                    closePort(NfcReaderUtil.ECHEC_LECTURE_BLOC);
                }
                // TEST de lecture avec code Succuess
                else if (responseLengthCode == 6 && NfcReaderUtil.RESPONSE_CODE_SUCCESS_TO_STRING.equals(responseCode.substring((responseLengthCode - deuxCaracte) * deuxDigit, responseLengthCode * deuxDigit))) {

                    String command_text = new String(progress[0].command);

                    reponse_text = new String(progress[0].response);

                    reponse_text_concatener.append(nfcReaderUtil.convertHexToString(nfcReaderUtil.convertStringToHex(reponse_text)));
                }

                // recuperation de la reponse
                repons = reponse_text_concatener.toString();
                Log.i(TAG, "reponse text concatener length()== > " + reponse_text_concatener.length());

                // remplacer les caractéres de fin de bloc  ÿý par chainne vide
                repons = repons.replace("ÿý", "");

                Log.i(TAG, repons);

                texte_complet.append(repons);
                // return repons;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.d(TAG, "onPostExecute onClosePort ==> " + onClosePort);
            Log.d(TAG, "onPostExecute current " + current);


            if (!onClosePort) {

                Log.d(TAG, "onPostExecute port ouvert ==> ");


                if ("Absent".equals(current)) {
                    try {

                        Log.d(TAG, "Statut 'Absent' closePort(statut = " + NfcReaderUtil.CARTE_ABSENTE + ")");
                        progressBar.dismiss();
                        closePort(NfcReaderUtil.CARTE_ABSENTE);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if ("Present".equals(current)) {

                    //  texte_complet.append(lecture);
                    countBlocReaded++;
                    Log.d(TAG, " Fin lecture bloc... " + countBlocReaded);


                    if (progressBar != null && progressBar.isShowing()) {
                        //calcul de pourcentage de progression et mise a jour du statut
                        int percent = (countBlocReaded * 100) / paramsList.size();
                        progressBar.setProgress(percent);
                    }

                    if (countBlocReaded == paramsList.size()) {
                        Log.d(TAG, " Fin lecture des blocs ");
                        trame = texte_complet.toString();

                        Log.i(TAG, trame);
                        Log.d(TAG, "Statut 'LECTURE COMPLETE' closePort(statut = " + NfcReaderUtil.LECTURE_COMPLETE + ")");

                        closePort(NfcReaderUtil.LECTURE_COMPLETE);
                    }


                } else {
                    closePort(NfcReaderUtil.UNKNOW_STATUT);
                }

            }
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
        intent.putExtra("nombreBloc", nombreBloc);
        setResult(RESULT_CODE_ECHEC_LECTURE, intent);
        finish();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closePort(NfcReaderUtil.ON_BACK_PRESSED);

    }
}
