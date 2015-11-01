package com.onaissi.btmouse;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by alionaissi on 15/10/2015.
 */
public class Send extends Activity implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener{
    private BluetoothAdapter btAdapter;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothSocket btSocket;
    private OutputStream outStream;
    private GestureDetectorCompat mDetector;
    private static final String DEBUG_TAG = "Gestures";

    BluetoothDevice device;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);


        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Bundle bundle = getIntent().getExtras();
        device = btAdapter.getRemoteDevice(bundle.getString("ADDRESS"));
        Toast.makeText(this,device.getName(),Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        try {
            btSocket.connect();
           // Log.d(TAG, "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }

    }


    private void errorExit(String title, String message){
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_LONG);
        msg.show();
        //finish();
    }


    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        //Log.d(TAG, "...Sending data: " + message + "...");

        try {
            outStream.write(msgBuffer);
            //editText.setText("");
        } catch (IOException e) {
            String msg = "In onResume() an exception occurred during write: " + e.getMessage();
            errorExit("Fatal Error", msg);
        }
    }



    //============================ gestures =========================
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        //Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + e.toString());
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //Log.d(DEBUG_TAG, "onDoubleTap: " + e.toString());

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        //Log.d(DEBUG_TAG, "onDoubleTapEvent: " + e.toString());
        sendData("q");
        sendData("q");
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //Log.d(DEBUG_TAG,"onDown: " + e.toString());
        if (e.getPointerCount()>1) {
            //two fingers touched for right click
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        //Log.d(DEBUG_TAG, "onShowPress: " + e.toString());

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //Log.d(DEBUG_TAG, "onSingleTapUp: " + e.toString());
        sendData("q");
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
        int threshold = 200;
        float distX= e1.getX() - e2.getX();
        float distY = e1.getY() - e2.getY();

        if ((distX < 20 && distX> -20) || (distY <20 && distY> -20)) {
            if ((distX < 20 && distX> -20) && !(distY <20 && distY> -20)) {
                if (distY > 0) {
                    for (int i = 0; i < Math.ceil(distY/threshold); i++)
                        sendData("w");
                }else{
                    for (int i = 0; i < Math.ceil(Math.abs(distY/threshold)); i++)
                        sendData("s");
                }
            } else {
                if ((distY <20 && distY> -20) && !(distX < 20 && distX> -20)) {
                    if (distX > 0){
                        for (int i = 0; i < Math.ceil(distX/threshold); i++)
                            sendData("a");
                    }else{
                        for (int i = 0; i < Math.ceil(Math.abs(distX/threshold)); i++)
                            sendData("d");
                    }

                }
            }

        }else {
            if ((distX < 0 && distY < 0)) {
                for (int i = 0; i < (Math.abs(Math.min(distX, distY)/threshold)); i++)
                    sendData("g");

            } else {
                if (distX > 0 && distY < 0) {
                    for (int i = 0; i < (Math.abs(Math.min(distX, distY)/threshold)); i++)
                        sendData("h");

                } else {
                    if (distX > 0 && distY > 0) {
                        for (int i = 0; i < (Math.abs(Math.min(distX, distY)/threshold)); i++)
                            sendData("t");
                    } else {
                        if (distX < 0 && distY > 0) {
                            for (int i = 0; i < (Math.abs(Math.min(distX, distY)/threshold)); i++)
                                sendData("y");
                        }
                    }
                }

            }
        }

        //Log.d(DEBUG_TAG, distX+ " distanceX");
        //Log.d(DEBUG_TAG, distY+" distanceY");
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
       // Log.d(DEBUG_TAG, "onLongPress: " + e.toString());
        sendData("e");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;

    }
}
