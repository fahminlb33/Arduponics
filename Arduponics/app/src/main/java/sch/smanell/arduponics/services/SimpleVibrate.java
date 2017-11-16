package sch.smanell.arduponics.services;

import android.content.Context;
import android.os.Vibrator;

import java.net.ConnectException;

/**
 * Created by Fahmi Noor Fiqri on 05/11/2017.
 * This file is subject to GNU GPL v3 License.
 */

public class SimpleVibrate {
    public static void vibrate(Context c){
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }
}
