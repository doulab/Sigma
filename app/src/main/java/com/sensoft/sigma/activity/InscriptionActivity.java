package com.sensoft.sigma.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sensoft.sigma.R;
import com.sensoft.sigma.model.Agent;

import io.realm.Realm;

public class InscriptionActivity extends AppCompatActivity {

    EditText prenom, nom, email, password, confirmPassword;

    Realm realm;
    Agent agent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        // initialisation EditText
        prenom = (EditText) findViewById(R.id.prenom);
        nom = (EditText) findViewById(R.id.nom);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);
    }


    public void inscription(View v) {

        if (prenom.getText().toString().length() == 0) {
            prenom.setError("Veuillez saisir le prénom.");
        } else if (nom.getText().toString().length() == 0) {
            nom.setError("Veuillez saisir le nom.");
        } else if (email.getText().toString().length() == 0) {
            email.setError("Veuillez saisir l'e-mail ");
        } else if (password.getText().toString().length() == 0) {
            password.setError("Veuillez saisir le mot de passe");
        } else if (confirmPassword.getText().toString().length() == 0) {
            confirmPassword.setError("Veuillez confirmer le mot de passe");
        } else if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
            confirmPassword.setError("Le mot de passe est différend");
        } else {
            // obtain a realm instance
            realm = Realm.getInstance(this);

            realm.beginTransaction();

            // Creation de l'objet agent
            agent = realm.createObject(Agent.class);

            // ajout des donnees realm agent
            agent.setPrenom(prenom.getText().toString());
            agent.setNom(nom.getText().toString());
            agent.setEmail(email.getText().toString());
            agent.setPassword(password.getText().toString());

            // insertion dans la base realm
            realm.commitTransaction();

            Toast.makeText(this, "Insertion réussi!!",Toast.LENGTH_LONG).show();

        }
    }
}
