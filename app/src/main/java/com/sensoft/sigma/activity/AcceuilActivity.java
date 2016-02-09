package com.sensoft.sigma.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.sensoft.sigma.R;
import com.sensoft.sigma.model.Agent;

public class AcceuilActivity extends AppCompatActivity {

    Agent agent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceuil);
        Intent intent = getIntent();
        String prenom = intent.getStringExtra("prenom");
        String nom = intent.getStringExtra("nom");


        if(intent.hasExtra("agent")){
            agent = (Agent) intent.getSerializableExtra("agent");

            if(agent == null){
                Toast.makeText(getApplicationContext(), "L'objet envoi reçu ne peut pas être null...", Toast.LENGTH_LONG).show();

                finish();
            }
        }else{
            //Nouvelle agent
            agent = new Agent();
        }

        Toast.makeText(getApplicationContext(), prenom+" "+nom, Toast.LENGTH_LONG).show();
    }
}
