package robo.sdm.br.edu.ifsp.androidrobo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Principal extends Activity {
    private static final String TAG = "LEDOnOff";

    Button btnFrente, btnEsquerda, btnParar, btnDireita, btnTras, btnExecutar;
    EditText edtComando;

    private boolean desenvolvimento = false;

    private static final int REQUEST_ENABLE_BT = 1234;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    // Well known SPP UUID
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Insert your bluetooth devices MAC address
    private static String address = "98:D3:31:60:30:51";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        if(!desenvolvimento) {
            verificaStatusBluetooth();
        }
        fazerConexoesDoLayoutListeners();
    }

    @Override
    public void onPause() { super.onPause(); }
    private void verificaStatusBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
    public void fazerConexoesDoLayoutListeners() {
        btnFrente = (Button) findViewById(R.id.btnFrente);
        btnEsquerda = (Button) findViewById(R.id.btnEsquerda);
        btnParar = (Button) findViewById(R.id.btnParar);
        btnDireita = (Button) findViewById(R.id.btnDireita);
        btnTras = (Button) findViewById(R.id.btnTras);
        btnExecutar = (Button) findViewById(R.id.btnExecutar);
        edtComando = (EditText) findViewById(R.id.txtComando);

        btnFrente.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("8");
                Toast msg = Toast.makeText(getBaseContext(),
                        "Veículo Andando para Frente", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        btnEsquerda.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("4");
                Toast msg = Toast.makeText(getBaseContext(),
                        "Veículo Virar a Esquerda", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        btnParar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("5");
                Toast msg = Toast.makeText(getBaseContext(),
                        "Veículo Parar", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        btnDireita.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("6");
                Toast msg = Toast.makeText(getBaseContext(),
                        "Veículo Virar a Direita", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        btnTras.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("2");
                Toast msg = Toast.makeText(getBaseContext(),
                        "Veículo Marcha Ré", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        btnExecutar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cliqueBotao();
                Toast msg = Toast.makeText(getBaseContext(),
                        "Comando Personalizado", Toast.LENGTH_SHORT);
                msg.show();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(!desenvolvimento) {
            switch (requestCode) {
                case REQUEST_ENABLE_BT:
                    if (resultCode == Activity.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "Bluetooth foi ativado", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Bluetooth nao foi ativado", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!desenvolvimento) {
            Log.d(TAG, "...In onResume - Attempting client connect...");
            // Set up a pointer to the remote node using it's address.
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            }
            // Discovery is resource intensive. Make sure it isn't going on when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();
            // Establish the connection. This will block until it connects.
            Log.d(TAG, "...Connecting to Remote...");
            try {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }
            // Create a data stream so we can talk to server.
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
            }
        }
    }
    private void errorExit(String title, String message){
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }
    private void sendData(String message) {
        if(!desenvolvimento) {// verifica se esta em modo de desenvolvimento
            byte[] msgBuffer = message.getBytes();
            try {
                outStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address to the correct address in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
                errorExit("Fatal Error", msg);
            }
        }
    }


    private void cliqueBotao() {

        new AsyncTask<Void, Void, Void>(){

            String comando = edtComando.getText().toString();

            @Override
            protected void onPreExecute() {
                btnFrente.setEnabled(false);
                btnTras.setEnabled(false);
                btnDireita.setEnabled(false);
                btnEsquerda.setEnabled(false);
                btnParar.setEnabled(false);
                btnExecutar.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Void v) {
                btnFrente.setEnabled(true);
                btnTras.setEnabled(true);
                btnDireita.setEnabled(true);
                btnEsquerda.setEnabled(true);
                btnParar.setEnabled(true);
                btnExecutar.setEnabled(true);
            }

            @Override
            protected Void doInBackground(Void... params) {

                try {

                    List<Comando> comandos = tratarComando( comando );

                    for ( Comando c : comandos ) {

                        switch ( c.getDirecao() ) {

                            case "F":
                                sendData("8");
                                System.out.printf("vá para a FRENTE em %d unidades!\n", c.getQuantidade());
                                Thread.sleep((long) (c.getQuantidade() * 21.8));
                                sendData("5");
                                Thread.sleep(200);
                                break;

                            case "T":
                                sendData("2");
                                System.out.printf("vá para TRÁS em %d unidades!\n", c.getQuantidade());
                                Thread.sleep((long) (c.getQuantidade() * 21.8));
                                sendData("5");
                                Thread.sleep(200);
                                break;

                            case "D":
                                sendData("6");
                                System.out.printf("vá para a DIREITA em %d unidades!\n", c.getQuantidade());
                                Thread.sleep((long) (c.getQuantidade() * 5.1));
                                sendData("5");
                                Thread.sleep(200);
                                break;

                            case "E":
                                sendData("4");
                                System.out.printf("vá para a ESQUERDA em %d unidades!\n", c.getQuantidade());
                                Thread.sleep((long) (c.getQuantidade() * 5.1));
                                sendData("5");
                                Thread.sleep(200);
                                break;

                            case "P":
                                sendData("5");
                                System.out.printf("Parando\n");
                                break;

                        }
                        System.out.println("Processou");
                    }


                } catch ( IllegalArgumentException exc ) {
                    Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
                }catch ( InterruptedException exc ){
                    exc.printStackTrace();
                }
                return null;
            }
        }.execute();



    }

    //método para tratar comando lido do txtComando
    public List<Comando> tratarComando( String comando )
            throws IllegalArgumentException {

        // lista com os comandos que serão obtidos
        List<Comando> comandos = new ArrayList<>();

        // o comando é válido, ou seja, direção, quantidade repetidas 1 ou mais vezes
        // com um P obrigatoriamente no final
        if ( comando.matches( "^([FTED][\\d]+)+P$" ) ) {

            // padrão agrupado para F ou B ou E ou D, seguido de 1 ou mais dígitos, ou P.
            Pattern p = Pattern.compile( "([FTED])([\\d]+)|(P)" );

            // cria uma matcher usando o padrão confrontado com a string de entrada
            Matcher m = p.matcher( comando );

            // procura pelo padrão
            while ( m.find() ) {

                // se encontrou (m.find() acima)

                // pega o resultado do match
                MatchResult mr = m.toMatchResult();

                if ( mr.group(3) == null ) {

                    // cria um comando com o grupo 1 (letras) e o grupo 2 (números)
                    Comando c = new Comando( mr.group( 1 ), Integer.parseInt( mr.group( 2 ) ) );

                    // aqui, se vc fosse usar um array seria um problema,
                    // pois não saberia o tamanho do mesmo até rodar o while inteiro
                    // fazendo com que a lista seja mais apropriada

                    // adiciona na lista
                    comandos.add( c );

                } else {
                    Comando c = new Comando( mr.group( 3 ), 0 );
                    comandos.add( c );
                }

            }

        } else {
            throw new IllegalArgumentException( "sintaxe inválida!" );
        }

        return comandos;

    }

}
    Ultrasonic ultrasonic(trigPin,echoPin);