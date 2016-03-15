package com.sensoft.sigma.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.sensoft.sigma.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);


    }


    public void connexion(View v) {

        Intent intent = new Intent(MainActivity.this, ConnexionActivity.class);
        startActivity(intent);
    }

    public void inscription(View v) {
        Intent intent = new Intent(MainActivity.this, InscriptionActivity.class);
        startActivity(intent);
    }

}
