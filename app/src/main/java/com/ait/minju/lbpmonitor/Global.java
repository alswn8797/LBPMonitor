package com.ait.minju.lbpmonitor;

import android.app.Application;

import com.github.mikephil.charting.data.LineData;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Global extends Application {
    static Timestamp startTime;
    static String startTimeString;
    static Boolean chkConnection = false;
    static Boolean BTdisconnected = false;
    static String status;
    static BluetoothConnectionService mBluetoothConnection;
    static LineData data1 = new LineData();
    static LineData data2 = new LineData();
    static ArrayList<MyEntry> arrData1 = new ArrayList<>();
    static ArrayList<MyEntry> arrData2 = new ArrayList<>();
}
