package com.sensoft.sigma.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.sensoft.sigma.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends AppCompatActivity {

    private static String TAG = SplashScreenActivity.class.getSimpleName();

    private ProgressBar spinner;
    private ImageView imageView;
    String messageProgressBar;
    ProgressDialog progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

         spinner = (ProgressBar) findViewById(R.id.progressBar);
        imageView = (ImageView) findViewById(R.id.imageView);

        // demarrage de l'asyncTAsk
        new demarrageTask().execute();



    }

    /**
     * methode permettant de faire une animation de rotation d'une imageView
     *
     * @param imageView
     */

    private void rotateImageView(ImageView imageView) {

        RotateAnimation anim = new RotateAnimation(0.0f, -360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(1000);
        imageView.startAnimation(anim);

    }


    public void progressBar() {

      //  messageProgressBar = getResources().getString(R.string.info_wait_writing1);

        // prepare for a progress bar dialog
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage(messageProgressBar);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

    }
       public class demarrageTask extends AsyncTask {

           @Override
           protected Object doInBackground(Object[] params) {

             //  rotateImageView(imageView);
               return null;
           }


           @Override
           protected void onProgressUpdate(Object[] values) {
               super.onProgressUpdate(values);
           }

           @Override
           protected void onPostExecute(Object o) {
               super.onPostExecute(o);


               new Timer().schedule(new TimerTask() {
                   @Override
                   public void run() {

                       Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                       startActivity(intent);
                       finish();

                       Log.i(TAG, "Aprés TimerTask");
                   }

               }, 5000);

           }
       }

}
