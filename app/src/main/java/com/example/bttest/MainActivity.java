package com.example.bttest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    public TextView mBTStatus;
    public TextView mTXbox;
    public TextView mTransmitBuffer;
    public TextView mReceiveBuffer;
    public TextView mReceiveBuffer2;
    public TextView mReceiveBuffer3;
    public Button mScanBtn;
    public Button mOffBtn;
    public Button mListPairedDevicesBtn;
    public Button mDiscoverBtn;
    public BluetoothAdapter mBTAdapter;
    public Set<BluetoothDevice> mPairedDevices;
    public ArrayAdapter<String> mBTArrayAdapter;
    public ListView mDevicesListView;
    public Switch mfactorySwitch;
    public Switch mNightModeSwitch;
    public TextView mDetails;

    public Handler mHandler;
    public ConnectedThread mConnectedThread;
    public BluetoothSocket mBTSocket = null;

    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public final static int REQUEST_ENABLE_BT = 1;
    public final static int MESSAGE_READ = 2;
    public final static int CONNECTING_STATUS = 3;
    public boolean mBTconnected = false; //zjebana java

    public boolean flagLangPL = false;
    public boolean flagLangENG = true;

    int PM2_5 = 0;
    int PM10 = 0;
    int PM1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mBTStatus = findViewById(R.id.bluetoothStatus);
        mTransmitBuffer = findViewById(R.id.readBuffer);
        mTXbox = findViewById(R.id.TX);
        mReceiveBuffer = findViewById(R.id.RX);
        mReceiveBuffer2 = findViewById(R.id.RX2);
        mReceiveBuffer3 = findViewById(R.id.RX4);
        mScanBtn = findViewById(R.id.scan);
        mOffBtn = findViewById(R.id.off);
        mfactorySwitch = findViewById(R.id.factoryEnv);
        mNightModeSwitch = findViewById(R.id.nightMode);
        mDetails = findViewById(R.id.textAboutAir);
        mDetails.setText("Are you ready to check air today? :)");

        mListPairedDevicesBtn = findViewById(R.id.PairedBtn);

        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        mDevicesListView = findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter);
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        mTransmitBuffer.setVisibility(View.INVISIBLE); //Hide transmit buffer
        mTXbox.setVisibility(View.INVISIBLE);

        mNightModeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                int textColorBlack = 0xFF000000;
                int textColorGrey = -1979711488;
                int textColorWhite = 0xFFFFFFFF;
                if(mNightModeSwitch.isChecked())
                {
                    mNightModeSwitch.setTextColor(textColorWhite);
                    mfactorySwitch.setTextColor(textColorWhite);
                    findViewById(R.id.relativeLayout).setBackgroundColor(textColorBlack);
                    ((TextView)findViewById(R.id.textView3)).setTextColor(textColorWhite);
                    ((TextView)findViewById(R.id.textView)).setTextColor(textColorWhite);
                    ((TextView)findViewById(R.id.textView10)).setTextColor(textColorWhite);
                    ((TextView)findViewById(R.id.statusTextView)).setTextColor(textColorWhite);
                    mBTStatus.setTextColor(textColorWhite);
                    mDetails.setTextColor(textColorWhite);


                }
                else
                {
                    mNightModeSwitch.setTextColor(textColorBlack);
                    mfactorySwitch.setTextColor(textColorBlack);
                    findViewById(R.id.relativeLayout).setBackgroundColor(textColorWhite);
                    ((TextView)findViewById(R.id.textView3)).setTextColor(textColorGrey);
                    ((TextView)findViewById(R.id.textView)).setTextColor(textColorGrey);
                    ((TextView)findViewById(R.id.textView10)).setTextColor(textColorGrey);
                    ((TextView)findViewById(R.id.statusTextView)).setTextColor(textColorGrey);
                    mBTStatus.setTextColor(textColorGrey);
                    mDetails.setTextColor(textColorGrey);
                }
            }
        });


        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //mTransmitBuffer.setText(readMessage);
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        mBTStatus.setText("Connected"); //to: " + (String) (msg.obj));
                        mListPairedDevicesBtn.setText("DISCONNECT");
                    } else
                    {
                        mBTStatus.setText("Connection Failed");
                        mListPairedDevicesBtn.setText("CONNECT");
                    }
                }
            }
        };

        if (mBTArrayAdapter == null) {
            mBTStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        } else {


            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOff(v);
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listPairedDevices(v);
                }
            });


            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {

                public void updateDetails()
                {
                    if(PM2_5 > 0 && PM2_5 <= 12)
                    {
                        if(flagLangENG)
                            mDetails.setText("Perfect air today! You should not avoid spending some" +
                                    " time outside! Take a deep breath!");
                        if(flagLangPL)
                            mDetails.setText("Dziś jest wspaniałe powietrze! Postaraj się spędzić trochę" +
                                    " czasu na zewnątrz. Weź głęboki oddech!");
                    }
                       // t.setTextColor(0xFF00B000);
                    if(PM2_5 > 12 && PM2_5 <= 30)
                    {
                        if(flagLangENG)
                            mDetails.setText("Today air is ok. Try to go and take a walk :)");
                        if(flagLangPL)
                            mDetails.setText("Dziś jest dobre powietrze. Powinieneś iść na spacer :)");
                    }
                    if(PM2_5 > 30 && PM2_5 <= 50)
                    {
                        if(flagLangENG)
                            mDetails.setText("Moderate air quality. Try to avoid being outside for too long.");
                        if(flagLangPL)
                            mDetails.setText("Jakość powietrza jest dzisiaj średnia. Postaraj się unikać " +
                                    "przebywania zbyt długiego czasu na zewnątrz.");

                    }
                    if(PM2_5 > 50 && PM2_5 <= 90)
                    {
                        if(flagLangENG)
                            mDetails.setText("Air today is quite polluted. Don't spend time outside. Children and" +
                                    " older pepole should stay at home.");
                        if(flagLangPL)
                            mDetails.setText("Dziś powietrze jest mocno zanieczyszczone. Dzieci i osoby starsze " +
                                    "powinny unikać przebywania na zewnątrz.");
                    }
                    if(PM2_5 > 90 && PM2_5 <= 120)
                    {
                        if(flagLangENG)
                            mDetails.setText("Pollution is very high. Don't go outside. Wear a mask if you have to go and" +
                                    " turn on air purifier if you have one.");
                        if(flagLangPL)
                            mDetails.setText("Powietrze jest bardzo zanieczyszczone. Załóż maskę jeśli musisz gdzieś iść " +
                                    "i włącz oczyszczacz powietrza jeśli to możliwe.");
                    }
                    if(PM2_5 > 120)
                    {
                        if(flagLangENG)
                            mDetails.setText("Pollution allert!! Don't go outside. Stay at home and get yourself air purifier!");
                        if(flagLangPL)
                            mDetails.setText("Alarm zanieczyszczenia powietrza! Zostań w domu i spraw sobie oczyszczcz powietrza!");
                    }
                }

                @Override
                public void run() {
//                    if (mConnectedThread != null)
//                        mConnectedThread.write("H");
                    updateDetails();

                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnable);

        }
    }

    public void bluetoothOn(View view) {
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBTStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
        mListPairedDevicesBtn.setText("CONNECT");
        mBTconnected = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent Data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {

                mBTStatus.setText("Enabled");
            } else
                mBTStatus.setText("Disabled");
        }
    }

    public void bluetoothOff(View view) {
        mBTAdapter.disable();
        mBTStatus.setText("Bluetooth disabled");
        //mKeypad.setEnabled(false);
        Toast.makeText(getApplicationContext(), "Bluetooth turned Off", Toast.LENGTH_SHORT).show();
        mBTconnected = false;
    }

    public void discover(View view) {
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void listPairedDevices(View view) {
        mPairedDevices = mBTAdapter.getBondedDevices();
        mBTArrayAdapter.clear();
        if (mBTAdapter.isEnabled()) {
            for (BluetoothDevice device : mPairedDevices)
            {
                mBTStatus.setText("Connecting...");
                //mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                final String info = device.getName() + "\n" + device.getAddress();
                final String address = info.substring(info.length() - 17);
                final String name = info.substring(0, info.length() - 18);
                if(name.equals("HC-05")) {
                    new Thread() {
                        public void run() {
                            boolean fail = false;
                            BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                            try {
                                if (mBTconnected == false)
                                    mBTSocket = createBluetoothSocket(device);
                            } catch (IOException e) {
                                fail = true;
                                Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            }
                            try {
                                if (mBTconnected == true) {
                                    mBTSocket.close();
                                    mBTconnected = false;
                                }
                                if (mBTconnected == false) {
                                    mBTSocket.connect();
                                    mBTconnected = true;
                                }
                            } catch (IOException e) {
                                try {
                                    fail = true;
                                    mBTSocket.close();
                                    mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                            .sendToTarget();
                                } catch (IOException e2) {
                                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if (fail == false) {
                                mConnectedThread = new ConnectedThread(mBTSocket);
                                mConnectedThread.start();
                                mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
                            }
                        }
                    }.start();
                }
            }

//            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } //else
//            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }



    public AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if (!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }
//            mBTStatus.setText("Connecting...");
//            String info = ((TextView) v).getText().toString();
//            final String address = info.substring(info.length() - 17);
//            final String name = info.substring(0, info.length() - 17);
//            new Thread() {
//                public void run() {
//                    boolean fail = false;
//                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
//                    try {
//                        if (connected == false)
//                            mBTSocket = createBluetoothSocket(device);
//                    } catch (IOException e) {
//                        fail = true;
//                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
//                    }
//                    try {
//                        if (connected == true) {
//                            mBTSocket.close();
//                            connected = false;
//                        }
//                        if (connected == false) {
//                            mBTSocket.connect();
//                            connected = true;
//                        }
//                    } catch (IOException e) {
//                        try {
//                            fail = true;
//                            mBTSocket.close();
//                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
//                                    .sendToTarget();
//                        } catch (IOException e2) {
//                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    if (fail == false) {
//                        mConnectedThread = new ConnectedThread(mBTSocket);
//                        mConnectedThread.start();
//                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
//                    }
//                }
//            }.start();
        }
    };

    public BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }



    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void setQAIcolorAndDetails(int PMval, TextView t, String PMtype)
        {
            if(PMtype == "PM2.5")
            {
                if(PMval <= 12)
                    t.setTextColor(0xFF00B000);
                if(PMval > 12 && PMval <= 30)
                    t.setTextColor(0xFF8AB000);
                if(PMval > 30 && PMval <= 50)
                    t.setTextColor(0xFFF0D400);
                if(PMval > 50 && PMval <= 90)
                    t.setTextColor(0xFFD62D1B);
                if(PMval > 90 && PMval <= 120)
                    t.setTextColor(0xFF9A0005);
                if(PMval > 120)
                    t.setTextColor(0xFF6E0F2F);
            }
            if(PMtype == "PM10")
            {
                if(PMval <= 30)
                    t.setTextColor(0xFF00B000);
                if(PMval > 30 && PMval <= 70)
                    t.setTextColor(0xFF8AB000);
                if(PMval > 70 && PMval <= 120)
                    t.setTextColor(0xFFF0D400);
                if(PMval > 120 && PMval <= 170)
                    t.setTextColor(0xFFD62D1B);
                if(PMval > 170 && PMval <= 220)
                    t.setTextColor(0xFF9A0005);
                if(PMval > 220)
                    t.setTextColor(0xFF6E0F2F);
            }

        }

        public void convertBytesToUnsignedInts(byte[] from, int[] to, int size)
        {
            for(int i = 0; i < size; i++)
            {
                to[i] = from[i] & 0xFF;
            }
        }

        public boolean checksum(int[] buff, int offset)
        {
            int l_checksum = 0;
            for(int idx = 0; idx < 30; ++idx)
            {
                l_checksum += buff[idx + offset];
            }
            if(l_checksum == ((buff[30 + offset] << 8) | buff[31 + offset]))
                return true;

            return false;
        }

        public void run() {
            byte[] buffer = new byte[64];
            int[] ints = new int[64];
            int bytes;
            int idxOffset = 0;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, 64);
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                        convertBytesToUnsignedInts(buffer, ints, 64); // need to convert to unsigned bytes

                        while(idxOffset < 55)
                        {
                            if(ints[idxOffset] != 66)
                            {
                                idxOffset++;
                            }
                            else
                            {
                                if(checksum(ints, idxOffset))
                                {
                                    if(mfactorySwitch.isChecked())
                                    {
                                        PM1 = (ints[idxOffset + 4] << 8) | ints[idxOffset + 5];
                                        PM2_5 = (ints[idxOffset + 6] << 8) | ints[idxOffset + 7];
                                        PM10 = (ints[idxOffset + 8] << 8) | ints[idxOffset + 9];
                                    }
                                    else
                                    {
                                        PM1 = (ints[idxOffset + 10] << 8) | ints[idxOffset + 11];
                                        PM2_5 = (ints[idxOffset + 12] << 8) | ints[idxOffset + 13];
                                        PM10 = (ints[idxOffset + 14] << 8) | ints[idxOffset + 15];
                                    }

                                    mReceiveBuffer.setText(Integer.toString(PM2_5) + " ug/m3");
                                    mReceiveBuffer2.setText(Integer.toString(PM10) + " ug/m3");
                                    mReceiveBuffer3.setText(Integer.toString(PM1) + " ug/m3");
                                    setQAIcolorAndDetails(PM2_5, mReceiveBuffer, "PM2.5");
                                    setQAIcolorAndDetails(PM10, mReceiveBuffer2, "PM10");
                                    setQAIcolorAndDetails(PM1, mReceiveBuffer3, "PM2.5");
                                }
                                break;
                            }
                        }
                        idxOffset = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        public void write(String input) {
            byte[] bytes = input.getBytes();
//            try {
//                mmOutStream.write(bytes);
                mTransmitBuffer.setText(input);
//            } catch (IOException e) { }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
