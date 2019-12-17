package com.example.sensorsocketphone2;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketServerThread extends Thread {

    public boolean isListening = false;
    private Context context;

    public int socketServerPort = 8080;
    private ServerSocket serverSocket;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public static final int SERVER_LISTENING = 1;
    public static final int SERVER_CLOSED = 2;
    public static final int SERVER_CONNECTED = 3;

    private Handler mainHandler;
    private ServiceThread serviceThread;


    public SocketServerThread(Context context, Handler mainHandler, int port) {
        this.mainHandler = mainHandler;
        this.socketServerPort = port;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            // create ServerSocket using the specified port
            serverSocket = new ServerSocket(socketServerPort);

            while (true) {
                Message msg = Message.obtain();
                msg.what = SERVER_LISTENING;
                mainHandler.sendMessage(msg);
                // wait for any connection
                isListening = true;
                Socket client;

                try {
                    client = serverSocket.accept();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    isListening = false;
                    break;
                }

                // send connection message to the UI thread
                Message msg2 = Message.obtain();
                msg2.what = SERVER_CONNECTED;
                msg2.obj = client.getInetAddress().toString();
                mainHandler.sendMessage(msg2);

                // get the socket stream resources
                serviceThread = new ServiceThread(context, client);
                serviceThread.start();

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                Message msg = Message.obtain();
                msg.what = SERVER_CLOSED;
                mainHandler.sendMessage(msg);
                isListening = false;
            }
        }
    }

}
