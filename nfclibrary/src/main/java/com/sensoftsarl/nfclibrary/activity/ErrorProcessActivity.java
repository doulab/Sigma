package com.sensoftsarl.nfclibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sensoftsarl.nfclibrary.R;
import com.sensoftsarl.nfclibrary.util.NfcReaderUtil;

public class ErrorProcessActivity extends AppCompatActivity {

    // trame a encoder
    String trame;

    // messqge erreur
    String messageErreur;

    int indicateur;
    private static final int ENCODAGE = 1;


    private LinearLayout linearLayoutMessageErreur;
    private TextView textViewMessageErreur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_process);


        linearLayoutMessageErreur = (LinearLayout) findViewById(R.id.linearLayoutMessageError);
        textViewMessageErreur = (TextView) findViewById(R.id.textViewResultMessageError) ;

        //Get trame from intent
        Intent intent = getIntent();
        if(intent.hasExtra("trame")){
            trame = intent.getStringExtra("trame");

        }


        if(intent.hasExtra("indicateur")){
            indicateur = intent.getIntExtra("indicateur", 0);

        }

        if(intent.hasExtra("messageErreur")){


            messageErreur = intent.getStringExtra("messageErreur");

            if(messageErreur != null && !"".equals(messageErreur)){

                linearLayoutMessageErreur.setVisibility(View.VISIBLE);
                textViewMessageErreur.setText(messageErreur);
            }
        }
    }




    public void annuler(View v) {

    }

    public void reessayer(View v) {

        if (indicateur == NfcReaderUtil.INDICATEUR_MODE_ECRITURE) {
            // encodqge
            startActivityEncodage(trame);
        }
        if (indicateur == NfcReaderUtil.INDICATEUR_MODE__LECTURE) {

        }

    }

    private void startActivityEncodage(String trameAEncode){
        Intent intent = new Intent(this, EncodageScreenActivity.class);
        intent.putExtra("trame", trameAEncode);

        finish();
    }
}
