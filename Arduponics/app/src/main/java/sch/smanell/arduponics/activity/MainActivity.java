package sch.smanell.arduponics.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import sch.smanell.arduponics.R;
import sch.smanell.arduponics.services.ArudponicsBluetooth;
import sch.smanell.arduponics.services.SimpleVibrate;

public class MainActivity extends AppCompatActivity {
    private TextView mTextWaterHeight;
    private TextView mTextHumidity;
    private TextView mTextTemperature;
    private TextView mStatusText;
    private ImageView mStatusImage;
    private ArudponicsBluetooth btService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btService = new ArudponicsBluetooth(this, mHandler);
        btService.registerBroadcastListener();

        mTextWaterHeight = (TextView) findViewById(R.id.textWaterHeight);
        mTextHumidity = (TextView) findViewById(R.id.textHumidity);
        mTextTemperature = (TextView) findViewById(R.id.textTemperature);
        mStatusText = (TextView) findViewById(R.id.statusText);
        mStatusImage = (ImageView) findViewById(R.id.statusImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect:
                if (btService.isConnected()){
                    btService.disconnect();
                } else {
                    btService.connect();
                }
                return true;
            case R.id.action_about:
                Intent frm = new Intent(this, AboutActivity.class);
                startActivity(frm);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        btService.disconnect();
        btService.unregisterBroadcastListener();
        super.onDestroy();
    }

    private final Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ArudponicsBluetooth.EVENT_CONNECTION_CHANGED) {
                switch (Integer.parseInt(msg.obj.toString())) {
                    case ArudponicsBluetooth.DEVICE_CONNECTING:
                        mStatusText.setText(R.string.status_connecting);
                        break;

                    case ArudponicsBluetooth.DEVICE_CONNECTED:
                        mStatusText.setText(R.string.status_connected);
                        SimpleVibrate.vibrate(MainActivity.this);
                        break;

                    case ArudponicsBluetooth.DEVICE_DISCONNECTED:
                        mStatusText.setText(R.string.initial_status_text);
                        mTextWaterHeight.setText(R.string.initial_height_value);
                        mTextHumidity.setText(R.string.initial_humidity_value);
                        mTextTemperature.setText(R.string.initial_temp_value);
                        mStatusImage.setImageResource(R.drawable.ic_status_connect);
                        break;

                    case ArudponicsBluetooth.DEVICE_CONNECT_FAILED:
                        mStatusText.setText(R.string.initial_status_text);
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.cannot_connect_message),
                                Toast.LENGTH_LONG).show();
                        break;

                    case ArudponicsBluetooth.DEVICE_BT_NOT_ENABLED:
                        mStatusText.setText(R.string.initial_status_text);
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.bt_not_enabled),
                                Toast.LENGTH_LONG).show();
                        break;

                    case ArudponicsBluetooth.DEVICE_NOT_PAIRED:
                        mStatusText.setText(R.string.initial_status_text);
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.device_not_paired),
                                Toast.LENGTH_LONG).show();
                        break;
                }
            } else if (msg.what == ArudponicsBluetooth.EVENT_STATUS_CHANGED) {
                switch (Integer.parseInt(msg.obj.toString())) {
                    case ArudponicsBluetooth.STATUS_OK:
                        mStatusText.setText(R.string.status_ok);
                        mStatusImage.setImageResource(R.drawable.ic_level_ok);
                        break;

                    case ArudponicsBluetooth.STATUS_COLD:
                        mStatusText.setText(R.string.status_cold);
                        mStatusImage.setImageResource(R.drawable.ic_level_cool);
                        break;

                    case ArudponicsBluetooth.STATUS_HOT:
                        mStatusText.setText(R.string.status_hot);
                        mStatusImage.setImageResource(R.drawable.ic_level_hot);
                        break;
                }
            } else if (msg.what == ArudponicsBluetooth.EVENT_DATA_RECEIVED) {
                switch (msg.arg1) {
                    case ArudponicsBluetooth.DATA_HEIGHT:
                        mTextWaterHeight.setText(String.valueOf(msg.arg2) + "%");
                        break;

                    case ArudponicsBluetooth.DATA_HUMIDITY:
                        mTextHumidity.setText(String.valueOf(msg.arg2) + "%");
                        break;

                    case ArudponicsBluetooth.DATA_TEMPERATURE:
                        mTextTemperature.setText(String.valueOf(msg.arg2) + "°C");
                        break;
                }
            }
        }
    };
}