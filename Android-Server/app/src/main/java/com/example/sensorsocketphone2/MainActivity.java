package com.example.sensorsocketphone2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private SocketServerThread socketServerThread;
    private int serverPort = 8080;

    public TextView serverStatus, clientStatus;
    private MainHandler mainHandler = new MainHandler();
    private MaterialButton runButton;
    private TextInputEditText portTextEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverStatus = findViewById(R.id.server_status_tv);
        clientStatus = findViewById(R.id.client_tv);

        // port text-edit
        portTextEdit = findViewById(R.id.port_edit_text);
        portTextEdit.setText(R.string.default_port);
        portTextEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if ((view.getId() == portTextEdit.getId()) && (portTextEdit.getText() != null)) {
                        serverPort = Integer.parseInt(portTextEdit.getText().toString());
                    }
                }
            }
        });

        // setup the run button
        runButton = findViewById(R.id.run_btn);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socketServerThread == null) {
                    socketServerThread = new SocketServerThread(getApplicationContext(), mainHandler, serverPort);
                    socketServerThread.start();
                } else {
                    if (socketServerThread.isListening) {
                        socketServerThread.stopServer();
                    } else {
                        socketServerThread = new SocketServerThread(getApplicationContext(), mainHandler, serverPort);
                        socketServerThread.start();
                    }
                }
            }
        });
    }

    private class MainHandler extends Handler {

        public static final int SERVER_LISTENING = 1;
        public static final int SERVER_CLOSED = 2;
        public static final int SERVER_CONNECTED = 3;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVER_LISTENING:
                    serverStatus.setText(String.format(Locale.US, "Server is listening on port %d",
                            socketServerThread.socketServerPort));
                    runButton.setText("Stop");
                    break;

                case SERVER_CLOSED:
                    serverStatus.setText("Server closed");
                    runButton.setText("Run");
                    clientStatus.setText("");
                    break;

                case SERVER_CONNECTED:
                    clientStatus.setText(String.format("Server connected to %s", msg.obj.toString()));
                    break;
            }
        }
    }
}
