package com.sensoft.sigma.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sensoft.sigma.R;
import com.sensoft.sigma.model.Agent;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ConnexionActivity extends AppCompatActivity {

    private static final String TAG = ConnexionActivity.class.getSimpleName();

    EditText login, password;
    Realm realm;
    Agent agent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);
    }

    public void connexion(View v) {

        if (login.getText().toString().length() == 0) {
            login.setError("Veuillez saisir le login");
        } else if (password.getText().toString().length() == 0) {
            password.setError("Veuillez saisir le mot de passe");
        } else {
            agent = new Agent();
            realm = Realm.getInstance(this);
            RealmQuery<Agent> query = realm.where(Agent.class);

            query.equalTo("login", login.getText().toString()).equalTo("password", password.getText().toString());

            // Execute the query:
            RealmResults<Agent> result = query.findAll();
            if(result.size()!=0){

                try {

                    for (Agent agent : result) {

                        String prenom = agent.getPrenom();
                        String nom = agent.getNom();
                        String email = agent.getEmail();
                        Toast.makeText(getApplicationContext(),prenom+" "+nom,Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(),AcceuilActivity.class);
                        intent.putExtra("prenom",  prenom);
                        intent.putExtra("nom",  nom);
                        intent.putExtra("email",  email);
                        startActivity(intent);
                    }


                } catch (IllegalArgumentException e) {

                    Log.e(TAG, e.getMessage());
                }
            }
               else {
                Toast.makeText(getApplicationContext(),"email ou mot de passe incorrect!!",Toast.LENGTH_LONG).show();
            }

        }

    }
}
