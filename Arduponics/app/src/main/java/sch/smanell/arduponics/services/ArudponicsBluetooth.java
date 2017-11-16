package sch.smanell.arduponics.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fahmi Noor Fiqri on 14/09/2017.
 * This file is subject to GNU GPL v3 License.
 */

public class ArudponicsBluetooth {
    // Name for the SDP record when creating server socket
    private static final String DeviceName = "HC-05";
    private static final UUID DeviceUUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");
    private static final byte CrCharacter = 10;
    private static final byte LfCharacter = 13;
    private static final int TemperatureUpper = 25;
    private static final int TemperatureLower = 22;
    private final List<String> EmptyChars = Arrays.asList("", null);

    // message
    public static final int EVENT_CONNECTION_CHANGED = 10;
    public static final int DEVICE_CONNECTED = 11;
    public static final int DEVICE_CONNECTING = 12;
    public static final int DEVICE_DISCONNECTED = 13;
    public static final int DEVICE_CONNECT_FAILED = 14;
    public static final int DEVICE_BT_NOT_ENABLED = 15;
    public static final int DEVICE_NOT_PAIRED = 16;

    public static final int EVENT_STATUS_CHANGED = 20;
    public static final int STATUS_OK = 21;
    public static final int STATUS_HOT = 22;
    public static final int STATUS_COLD = 23;

    public static final int EVENT_DATA_RECEIVED = 30;
    public static final int DATA_HEIGHT = 31;
    public static final int DATA_HUMIDITY = 32;
    public static final int DATA_TEMPERATURE = 33;

    // Member fields
    private final Context mContext;
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ListenDataThread mListenDataThread;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;

    public ArudponicsBluetooth(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mAdapter.isEnabled()) {
            mAdapter.enable();
        }
    }

    public boolean isConnected(){
        return mSocket != null && mSocket.isConnected();
    }

    public void connect() {
        if (!mAdapter.isEnabled()){
            mHandler.obtainMessage(EVENT_CONNECTION_CHANGED, DEVICE_BT_NOT_ENABLED).sendToTarget();
            return;
        }
        if (mSocket != null && mSocket.isConnected()){
            disconnect();
        }
        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    public void disconnect() {
        try {
            if (mInputStream != null) mInputStream.close();
            if (mSocket != null) mSocket.close();
            if (mConnectThread != null)
                mConnectThread = null;
            if (mListenDataThread != null)
                mListenDataThread = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerBroadcastListener()
    {
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(bluetoothStateReceiver, filter1);
        mContext.registerReceiver(bluetoothStateReceiver, filter2);
    }

    public void unregisterBroadcastListener(){
        mContext.unregisterReceiver(bluetoothStateReceiver);
    }

    private BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.obtainMessage(EVENT_CONNECTION_CHANGED, DEVICE_DISCONNECTED).sendToTarget();
        }
    };

    private class ConnectThread extends Thread {
        @Override
        public void run() {
            mHandler.obtainMessage(EVENT_CONNECTION_CHANGED, DEVICE_CONNECTING).sendToTarget();

            mAdapter.cancelDiscovery();

            try {
                String address = "";
                for (BluetoothDevice d : mAdapter.getBondedDevices()) {
                    if (d.getName().equals(DeviceName)) address = d.getAddress();
                }

                if (address.equals("")){
                    mHandler.obtainMessage(EVENT_CONNECTION_CHANGED, DEVICE_NOT_PAIRED).sendToTarget();
                    return;
                }

                BluetoothDevice device = mAdapter.getRemoteDevice(address);
                mSocket = device.createInsecureRfcommSocketToServiceRecord(DeviceUUID);
                mSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mSocket != null && mSocket.isConnected()) {
                mHandler.obtainMessage(EVENT_CONNECTION_CHANGED, DEVICE_CONNECTED).sendToTarget();

                mListenDataThread = new ListenDataThread();
                mListenDataThread.start();
            }
            else
            {
                mHandler.obtainMessage(EVENT_CONNECTION_CHANGED, DEVICE_CONNECT_FAILED).sendToTarget();
            }
        }
    }

    private class ListenDataThread extends Thread {
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void run() {
            try {
                mInputStream = mSocket.getInputStream();
                int readBufferPosition = 0;
                byte[] readBuffer = new byte[1024];
                while (mSocket.isConnected()) {
                    int bytesAvailable = mInputStream.available();
                    if (bytesAvailable <= 0) continue;

                    byte[] packet = new byte[bytesAvailable];
                    mInputStream.read(packet);
                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = packet[i];
                        if (b == CrCharacter || b == LfCharacter) {
                            byte[] encoded = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encoded, 0, encoded.length);
                            readBufferPosition = 0;

                            processData(new String(encoded, "US-ASCII"));
                        } else {
                            readBuffer[readBufferPosition++] = b;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            mHandler.obtainMessage(EVENT_CONNECTION_CHANGED, DEVICE_DISCONNECTED).sendToTarget();
        }

        private void processData(String data){
            try
            {
                ArrayList<String> entries = new ArrayList<>(Arrays.asList(data.split("X")));
                entries.removeAll(EmptyChars);

                for (String item: entries) {
                    checkData(item);
                }
            }
            catch (Exception ex){
                Log.d("Arduponics", ex.toString());
            }
        }

        private void checkData(String data)
        {
            String[] splitted = data.split("-");
            int value = (int)Float.parseFloat(splitted[1]);

            switch (Integer.valueOf(splitted[0]))
            {
                case 0:
                    int mapped = Math.round((float)value / 1024 * 100);
                    mHandler.obtainMessage(EVENT_DATA_RECEIVED, DATA_HEIGHT, mapped).sendToTarget();
                    break;

                case 1:
                    mHandler.obtainMessage(EVENT_DATA_RECEIVED, DATA_HUMIDITY, value).sendToTarget();
                    break;

                case 2:
                    mHandler.obtainMessage(EVENT_DATA_RECEIVED, DATA_TEMPERATURE, value).sendToTarget();
                    if (value > TemperatureLower && value < TemperatureUpper)
                    {
                        mHandler.obtainMessage(EVENT_STATUS_CHANGED, STATUS_OK).sendToTarget();
                    }
                    else if (value > TemperatureUpper)
                    {
                        mHandler.obtainMessage(EVENT_STATUS_CHANGED, STATUS_HOT).sendToTarget();
                    }
                    else if (value < TemperatureLower)
                    {
                        mHandler.obtainMessage(EVENT_STATUS_CHANGED, STATUS_COLD).sendToTarget();
                    }
                    break;
            }
        }
    }
}