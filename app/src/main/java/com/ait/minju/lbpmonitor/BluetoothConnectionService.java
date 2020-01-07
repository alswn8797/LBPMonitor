package com.ait.minju.lbpmonitor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionSrv";
    private static final String appName = "LBPMonitor";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    Activity mActivity;
    String finalMsg;
    String inputStream = "";

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    /**
     * this thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                //Log.d(TAG, "AcceptThread: Setting up Server using: "+MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: "+e.getMessage());
            }

            mmServerSocket = tmp;
        }

        public void run(){
            //Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket socket = null;

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                //Log.d(TAG, "run: RFCOM server socket start.....");
                socket = mmServerSocket.accept();
                //Log.d(TAG, "run: RFCOM server socket accepted connection.");
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: "+e.getMessage());
            }

            if(socket != null){
                connected(socket, mmDevice);
            }

            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            //Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket: "+e.getMessage());
            }
        }
    }

    /**
     * This thread tuns while attempting to make an outgoing connection
     * with a device. It tunds straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            //Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                //Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "+MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not Create InsecureRfCommSocket "+e.getMessage());
            }

            mmSocket = tmp;

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                Global.chkConnection = true;

                //set the Start time once it is null and after bluetooth connected
                if(Global.startTime == null){
                    java.util.Date date = new java.util.Date();
                    Global.startTime = new Timestamp(date.getTime());

                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Global.startTimeString = sdf.format(date);
                }
                //Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                try {
                    // Close the socket
                    mmSocket.close();
                    //Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket "+e1.getMessage());
                }
                //Log.d(TAG, "run: ConnectThread: Could not connect to UUID: "+MY_UUID_INSECURE);
                Global.chkConnection = false;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel(){
            try {
                //Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed. "+e.getMessage());
            }
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume();
     */
    public synchronized void start(){
        //Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     */
    public void startClient(BluetoothDevice device, UUID uuid){
        //Log.d(TAG,"startClient: Started.");

        // initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true);
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            //Log.d(TAG, "ConnectedThread: Starting.");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                //dismiss the progressdialog when connection is established
                mProgressDialog.dismiss();

            } catch (NullPointerException e){
                e.printStackTrace();
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; //bytes returned from read)()

            // Keep listening to the InputStream until an exception occurs
            while(true){

                //Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);

                    char endChar = '#';

                    if(incomingMessage != null) {
                        if (incomingMessage.indexOf(endChar) == -1) {
                            finalMsg += incomingMessage;
                            //Log.d(TAG, "InputStream: "+finalMsg);
                        } else {
                            finalMsg = finalMsg + incomingMessage.replace(Character.toString(endChar), "");
                            //tvMessage.setText(finalMsg);
                            //Log.e("BF",finalMsg);

                            //Log.e(TAG, "InputStream: " + finalMsg);
                            inputStream = finalMsg;
                            finalMsg = "";
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading InputSteam. "+e.getMessage());
                    Global.BTdisconnected = true;
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            //Log.d(TAG, "write: Writing to OutputStream: "+text);

            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                //Log.e(TAG, "write: Error writing to OutputStream. "+e.getMessage());
            }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel(){
            try {
                mmSocket.close();
            } catch (IOException e){ }
        }

    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        //Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();


    }

    /**
     * Write to the connectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out){
        //Create temporary object
        ConnectedThread r;

        //Synchronize a copy of the connectedThread
        //Log.d(TAG, "write: Write Called.");
        //Perform the write
        mConnectedThread.write(out);
    }
}
