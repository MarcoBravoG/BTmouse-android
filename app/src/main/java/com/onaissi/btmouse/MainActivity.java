package com.onaissi.btmouse;

import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.LabeledIntent;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by alionaissi on 15/10/2015.
 */

public class MainActivity extends Activity implements OnClickListener,AdapterView.OnItemClickListener{

    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    //private Button gesture;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;


    private BluetoothDevice selectedDevice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onBtn = (Button)findViewById(R.id.turnOn);
        offBtn = (Button)findViewById(R.id.turnOff);
        listBtn = (Button)findViewById(R.id.paired);
        findBtn = (Button)findViewById(R.id.search);
        myListView = (ListView)findViewById(R.id.listView1);
        text = (TextView) findViewById(R.id.text);
        //gesture = (Button) findViewById(R.id.gesture);
        //gesture.setOnClickListener(this);
        onBtn.setOnClickListener(this);
        offBtn.setOnClickListener(this);
        listBtn.setOnClickListener(this);
        findBtn.setOnClickListener(this);

        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (!myBluetoothAdapter.isEnabled()){
                text.setText("Status: Disconnected");
                offBtn.setEnabled(false);
                listBtn.setEnabled(false);
                findBtn.setEnabled(false);
                myListView.setEnabled(false);
            }
            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);
            myListView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.turnOn:
                on(v);
                break;
            case R.id.turnOff:
                off(v);
                break;
            case R.id.paired:
                list();
                break;
            case R.id.search:
                find(v);
                break;
           // case R.id.gesture: Intent i = new Intent(this,Gestures1.class); startActivity(i);
        }
    }

    public void on(View view){
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
                onBtn.setEnabled(true);
                offBtn.setEnabled(true);
                listBtn.setEnabled(true);
                findBtn.setEnabled(true);
                myListView.setEnabled(true);
            } else {
                text.setText("Status: Disabled");
            }
        }
    }

    public void list(){
        // get paired devices
        pairedDevices = myBluetoothAdapter.getBondedDevices();
        BTArrayAdapter.clear();
        // put it's one to the adapter
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

       // Toast.makeText(getApplicationContext(),"Show Paired Devices",
       //         Toast.LENGTH_SHORT).show();

    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        pairedDevices = myBluetoothAdapter.getBondedDevices();
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        } else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();
            getApplicationContext().registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void off(View view){
        myBluetoothAdapter.disable();
        text.setText("Status: Disconnected");
        offBtn.setEnabled(false);
        listBtn.setEnabled(false);
        findBtn.setEnabled(false);
        myListView.setEnabled(false);
        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_SHORT).show();
    }


    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(getApplicationContext(),"Paired",Toast.LENGTH_SHORT).show();
                    //pairedDevices.clear();
                    list();
                    //BTArrayAdapter.notifyDataSetChanged();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(getApplicationContext(),"Unpaired",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getApplicationContext().registerReceiver(mPairReceiver, intent);
        String address = (String) parent.getItemAtPosition(position);
        String [] lines = address.split("\\n");
        final BluetoothDevice dev =  myBluetoothAdapter.getRemoteDevice(lines[1]);


        CharSequence[] items = { "(un)pair", "select" };

        new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (!pairedDevices.contains(dev)) {
                                    pairDevice(dev);
                                }else{
                                    AlertDialog.Builder dg = new AlertDialog.Builder(MainActivity.this);
                                    dg.setTitle("Unpair device?");
                                    dg.setMessage("Are you sure you want to unpair " + dev.getName() + " ?");
                                    dg.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            unpairDevice(dev);
                                            list();
                                        }
                                    });
                                    dg.setNegativeButton(android.R.string.no, null);
                                    dg.show();
                                }
                                break;

                            case 1:
                                selectedDevice = dev;
                                Intent i = new Intent("com.onaissi.btmouse.SEND");
                                i.putExtra("ADDRESS", dev.getAddress());
                                startActivity(i);
                                break;// ------------------------------

                        }
                    }
                }).create().show();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            unregisterReceiver(mPairReceiver);
            unregisterReceiver(bReceiver);
        }catch (IllegalArgumentException e){
        }

    }

}