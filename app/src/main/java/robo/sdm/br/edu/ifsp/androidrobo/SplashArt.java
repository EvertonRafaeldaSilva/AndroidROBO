package robo.sdm.br.edu.ifsp.androidrobo;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

    public class SplashArt extends Activity implements Runnable {

        //Metodo run fica aguardando o termino da metodo onCreate
        public void run(){
            startActivity(new Intent(this, Principal.class));
            finish();
        }



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash_art);

            //Aciona a tela de splash apos aguardar o tempo especificado em milisegundo
            Handler handler = new Handler();
            handler.postDelayed(this, 3000);
        }

    }

