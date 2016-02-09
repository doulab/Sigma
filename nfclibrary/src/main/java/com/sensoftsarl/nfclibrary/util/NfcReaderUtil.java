package com.sensoftsarl.nfclibrary.util;

import android.util.Log;

import com.sensoftsarl.nfclibrary.bean.NfReaderData;
import com.sensoftsarl.nfclibrary.bean.TransmitParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doulab on 27/01/16.
 */
public class NfcReaderUtil {
    public static final String HEADER_CMD_WRITE = "FF D6 00";
    public static final String HEADER_CMD_READ = "FF B0 00";
    public static final String END_CMD = "04";
    public static final int CONTROL_CODE = 3500;
    public static final int NOMBRE_BLOC_216 = 224;
    public static final int NOMBRE_BLOC_213 = 34;
    public static String TAG = NfcReaderUtil.class.getSimpleName();


    public static final int ECRITURE_COMPLETE= 0;
    public static final int ERREUR_ECRITURE_BLOC = 1;
    public static final int CARTE_ABSENTE = 2;

    public static final int LECTEUR_DEBRANCHE=3;

    public static final int ON_BACK_PRESSED=4;

    public static final int ERREUR_LECTURE_BLOC = 5;
    public static final int UNKNOW_STATUT = -1;
    public static final int LECTURE_COMPLETE= 6;
    public static final int ECHEC_LECTURE_BLOC = 7;
    public static final int ECHEC_ECRITURE_BLOC = 8;
    public static final String RESPONSE_CODE_ERREUR_TO_STRING = "63";
    public static final String RESPONSE_CODE_SUCCESS_TO_STRING = "fffd";


    public static final int INDICATEUR_MODE_ECRITURE = 1;
    public static final int INDICATEUR_MODE__LECTURE = 2;


    /**
     * RESULT CODE ERREUR
     */

    public static final int RESULT_CODE_ECHEC_LECTURE = 10;

    /**
     * FIN RESULT CODE ERREUR
     */

    public NfcReaderUtil() {

    }

    /**
     * Converts the HEX string to byte array.
     *
     * @param hexString the HEX string.
     * @return the byte array.
     */

    public byte[] toByteArray(String hexString) {

        int hexStringLength = hexString.length();
        byte[] byteArray = null;
        int count = 0;
        char c;
        int i;

        // Count number of hex characters
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        boolean first = true;
        int len = 0;
        int value;
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[len] = (byte) (value << 4);

                } else {

                    byteArray[len] |= value;
                    len++;
                }

                first = !first;
            }
        }

        return byteArray;
    }


    public String convertStringToHex(String str) {

        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }

        return hex.toString();
    }

    public String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        System.out.println("Decimal : " + temp.toString());

        return sb.toString();
    }

    // methode qui permet de decouper une chaine de caractere selon un regex ou une taille
    public String[] Decouper(String adecompos, String regex, int SizeMax) {

        String[] temp = adecompos.split(regex);
        int nmbrDepaseSize = 0;

        for (int i = 0; i < temp.length; i++) {

            nmbrDepaseSize = nmbrDepaseSize + (temp[i].length() / SizeMax);
            if (temp[i].length() % SizeMax == 0) {
                nmbrDepaseSize--;
            }
            System.out.println(nmbrDepaseSize);
        }

        String[] retour = new String[temp.length + nmbrDepaseSize];
        int j = 0;
        int tempo;
        for (int i = 0; i < temp.length; i++) {

            while (temp[i].length() > 0) {
                tempo = SizeMax;
                if (temp[i].length() < SizeMax) {
                    tempo = temp[i].length();
                }

                retour[j] = temp[i].substring(0, tempo);
                temp[i] = temp[i].substring(tempo);
                j++;
            }
        }

        return retour;

    }

    public TransmitParams preparerTransmitParamsEcriture(int i, String elementTexteDecouper) {
        TransmitParams params;
        String dynamic_hex;
        try {
            int slotNum = 0;
            params = new TransmitParams();
            // recuperation header commande
            String command_write = HEADER_CMD_WRITE;
            dynamic_hex = Integer.toHexString(i + 4);
            if (dynamic_hex.length() == 1) {
                command_write += " 0" + dynamic_hex + " " + END_CMD;

            } else if (dynamic_hex.length() == 2) {
                command_write += " " + dynamic_hex + " " + END_CMD;
            }
            params.slotNum = slotNum;
            params.controlCode = CONTROL_CODE;

            Log.d(TAG, "preparerTransmitParamsEcriture command_write =>  " + command_write);
            Log.d(TAG, "preparerTransmitParamsEcriture param elementTexteDecouper =>  " + elementTexteDecouper);
            //TODO  à  verifier
            if (!"".equals(elementTexteDecouper)) {

                // conversion de la commande en hexa

                params.commandString = command_write + convertStringToHex(rPad(elementTexteDecouper,4,' '));

                Log.d(TAG, " Avant conversion " + elementTexteDecouper + " convertStringToHex = >" + convertStringToHex(elementTexteDecouper));
            }
            else {
                Log.d(TAG, " Command chaine vide "+command_write+ "0000");

                params.commandString = command_write + "0000";
            }

        } catch (Exception e) {
            Log.e(TAG, "preparerTransmitParams: " + e.getMessage());
            params = null;
        }
        Log.d(TAG, "preparerTransmitParamsEcriture param commandString =>  " + params.commandString);
        return params;
    }

    public List<TransmitParams> preparerTransmitListParamsEcriture(String trame) throws IllegalArgumentException, Exception {


        Log.d(TAG, " " + trame);
        List<TransmitParams> paramsList = null;
        if (trame == null) {
            throw new IllegalArgumentException("La trame à encoder est null");
        }
        if ("".equals(trame)) {
            throw new IllegalArgumentException("La trame à encoder est une chaine vide");
        }
        if (trame.length() <= 4) {
            throw new IllegalArgumentException("La longueur de la trame est invalide");
        }
        try {

            paramsList = new ArrayList<TransmitParams>();
            // on decoupe le texte en bloc de 4 byte pour l'insertion dans les ntags 216
            String texte_decouper[] = Decouper(trame, ";", 4);

            for (int i = 0; i < texte_decouper.length; i++) {

                Log.i(TAG, "preparerTransmitListParams : texte_decouper = " + texte_decouper[i]);
                TransmitParams params = preparerTransmitParamsEcriture(i, texte_decouper[i]);
                if (params == null) {
                    throw new Exception("Echec  preparation des données écriture");

                }
                paramsList.add(params);
            }

            if (paramsList.isEmpty()) {
                Log.w(TAG, "preparerTransmitListParams : paramsList est vide ");
            }

            Log.i(TAG, "preparerTransmitListParams : preparation données à encoder  terminer");


        } catch (Exception e) {
            Log.e(TAG, "preparerTransmitParams: " + e.getMessage());
            throw new Exception(e.getMessage());

        }

        return paramsList;
    }


    /**
     *
     */

    public TransmitParams preparerTransmitParamsLecture(int j) {

        String command_lecture = HEADER_CMD_READ;
        TransmitParams params;
        String dynamic_hex;
        try {
            int slotNum = 0;
            params = new TransmitParams();

            dynamic_hex = Integer.toHexString(j + 4);
            if (dynamic_hex.length() == 1) {
                command_lecture += " 0" + dynamic_hex + " " + END_CMD;

            } else if (dynamic_hex.length() == 2) {
                command_lecture += " " + dynamic_hex + " " + END_CMD;

            }
            params.slotNum = slotNum;
            params.controlCode = CONTROL_CODE;
            //la commande est dejas en hexa
            params.commandString = command_lecture;

        } catch (Exception e) {
            Log.e(TAG, "preparerTransmitParams: " + e.getMessage());
            params = null;
        }
        return params;
    }

    public List<TransmitParams> preparerTransmitListParamsLecture(int nombreBloc) throws Exception {
        List<TransmitParams> paramsList = null;
        if (nombreBloc <= 0) {
            throw new IllegalArgumentException("Le nombre de bloc est negatif");
        }
        if (nombreBloc != NOMBRE_BLOC_216 && nombreBloc != NOMBRE_BLOC_213) {
            throw new IllegalArgumentException("La carte n'est pas pris en compte");
        }

        try {

            paramsList = new ArrayList<TransmitParams>();


            for (int j = 0; j < nombreBloc; j++) {
                Log.i(TAG, "preparerTransmitListParamsLecture : numero bloc = " + j);
                TransmitParams params = preparerTransmitParamsLecture(j);
                if (params == null) {
                    throw new Exception("Echec  preparation commande lecture");
                }
                paramsList.add(params);
            }
            if (paramsList.isEmpty()) {
                Log.w(TAG, "preparerTransmitListParamsLecture : paramsList est vide ");
            }

            Log.i(TAG, "preparerTransmitListParamsLecture : preparation commande lecture  terminer");


        } catch (Exception e) {
            Log.e(TAG, "preparerTransmitListParamsLecture: " + e.getMessage());
            throw new Exception(e.getMessage());

        }

        return paramsList;
    }


    public static String formatNfcReaderDataToEncode(NfReaderData nfReaderData, char car) throws Exception {
        String dataFormat = null;

        if (nfReaderData == null) {
            throw new IllegalArgumentException("l'objet contenant la Donnée est NULL");

        }
        if (nfReaderData.getData() == null) {
            throw new IllegalArgumentException("Donnée recu est invalide");

        }

        if (nfReaderData.getData().length() > nfReaderData.getSize()) {
            throw new IllegalArgumentException("la taille de la Donnée recu depasse la taille reservee");

        }

        try {


            String data  = nfReaderData.getData();
            //truncate
            //data = data.substring(0, nfReaderData.getSize());

            dataFormat = rPad(data, nfReaderData.getSize(), ' ');

        } catch (Exception e) {
            Log.e(TAG, "formatNfcReaderDataToEncode: " + e.getMessage());
            throw new Exception(e.getMessage());

        }

        return dataFormat;
    }


    public static String formatTrameToEncode(List<NfReaderData> nfReaderDataList, int nombreBloc) throws Exception {
        String trame = null;

        if (nfReaderDataList == null) {
            throw new IllegalArgumentException("liste à encoder contenant la donnée est NULL");

        }
        if (nfReaderDataList.isEmpty()) {
            throw new IllegalArgumentException("Liste de Donnée recu est invalide");

        }

        if (nombreBloc <= 0) {
            throw new IllegalArgumentException("Le nombre de bloc est negatif");
        }

        if (nombreBloc != NOMBRE_BLOC_216 && nombreBloc != NOMBRE_BLOC_213) {
            throw new IllegalArgumentException("La carte n'est pas pris en compte");
        }


        try {

             // construction trame
             StringBuffer dataConcat = new StringBuffer();
             dataConcat.append("");
             for (NfReaderData nfcReaderData : nfReaderDataList) {
                 formatNfcReaderDataToEncode(nfcReaderData,' ');
                 // ajout donnee dans trame
                dataConcat.append(nfcReaderData.getData());

            }

            trame = rPad(dataConcat.toString(), nombreBloc, ' ');

        } catch (Exception e) {
            Log.e(TAG, "formatNfcReaderDataToEncode: " + e.getMessage());
            throw new Exception(e.getMessage());

        }

        return trame;
    }

    /**
     * Generate a left padding.
     * @param str
     * @param length
     * @param car
     * @return
     */
    public static String lPad(String str, Integer length, char car) {
        if(str.length() == length){
            return str;
        }else{

            return String.format("%" + (length - str.length()) + "s", "").replace(
                    " ", String.valueOf(car))
                    + str;
        }
    }
    public static String rPad(String str, Integer length, char car) {
        String strReturn = "";

        if(str.length() == length){
            return str;
        }else{
            strReturn = str
                    + String.format("%" + (length - str.length()) + "s", "").replace(" ", String.valueOf(car));

        }

        return strReturn;
    }






}
