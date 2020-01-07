package com.ait.minju.lbpmonitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/*

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private LineChart mChart1, mChart2;
    private TextView max1Value, min1Value, max2Value, min2Value;
    private TextView tvTime;
    LinearLayout linearLayout;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    private boolean btnMatch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        findViewByIds(view);

        setChart(view, mChart1, Global.data1, "Muscle Sensor - Left");
        setChart(view, mChart2, Global.data2, "Muscle Sensor - Right");

        final Button btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Global.chkConnection){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle(R.string.app_name);
                    alertDialogBuilder
                            .setMessage(R.string.button_confirm_save)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Date date = new Date();
                                            Timestamp endTime = new Timestamp(date.getTime());
                                            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            String endTimeString = sdf.format(date);

                                            // get time difference in seconds
                                            long milliseconds = Global.startTime.getTime() - endTime.getTime();
                                            int timediff = (int) milliseconds / 1000;

                                            Record record = new Record(Global.arrData1, Global.arrData2, timediff, Global.startTimeString, endTimeString);

                                            mFirebaseDatabase = FirebaseDatabase.getInstance();
                                            mDatabaseReference = mFirebaseDatabase.getReference("Records");
                                            mDatabaseReference.push().setValue(record);

                                            //reset start time and graph
                                            date = new Date();
                                            Global.startTime = new Timestamp(date.getTime());
                                            Global.startTimeString = sdf.format(date);
                                            Global.data1.clearValues();
                                            Global.data2.clearValues();
                                            Global.arrData1.clear();
                                            Global.arrData2.clear();

                                            setChart(view, mChart1, Global.data1, "Muscle Sensor - Left");
                                            setChart(view, mChart2, Global.data2, "Muscle Sensor - Right");

                                            mHandler = new Handler(Looper.getMainLooper());
                                            onResume();
                                        }
                                    })

                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle(R.string.app_name);
                    alertDialogBuilder
                            .setMessage(R.string.no_connection)
                            .setCancelable(false)

                            .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });

        final Button btnBtConnect = (Button) view.findViewById(R.id.btnBtConnect);
        btnBtConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Global.chkConnection){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle(R.string.app_name);
                    alertDialogBuilder
                            .setMessage(R.string.already_has_connection)
                            .setCancelable(false)

                            .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    public void notifyDisconnection(){
        Global.chkConnection = false;
        Global.startTime = null;
        Global.startTimeString = null;
        Global.arrData1.clear();
        Global.arrData2.clear();
        Global.data1.clearValues();
        Global.data2.clearValues();

        Global.BTdisconnected = false;
    }

    public void findViewByIds(View view){
        linearLayout = (LinearLayout) view.findViewById(R.id.topButton);

        mChart1 = (LineChart) view.findViewById(R.id.lineChart1);
        mChart2 = (LineChart) view.findViewById(R.id.lineChart2);

        max1Value = (TextView) view.findViewById(R.id.max1Value);
        min1Value = (TextView) view.findViewById(R.id.min1Value);
        max2Value = (TextView) view.findViewById(R.id.max2Value);
        min2Value = (TextView) view.findViewById(R.id.min2Value);

        tvTime = (TextView) view.findViewById(R.id.tvTime);
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //add entries
                while (true) {

                    if(Global.mBluetoothConnection!=null) {
                        if (Global.chkConnection) {

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    addEntry(); //chart is notified of update in addEntry method
                                    updateTexts();
                                }
                            });

                            //pause between adds
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if(Global.BTdisconnected) {
                                notifyDisconnection();
                                break;
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void setChart(View view, LineChart chart, LineData data, String title){
        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(title);
        chart.setNoDataText("No Data for the moment");

        chart.setDragEnabled(true);
        chart.setTouchEnabled(true);
        chart.setScaleEnabled(false);

        chart.setPinchZoom(true);
        chart.setBackgroundColor(Color.LTGRAY);

        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(1023f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void updateTexts(){
        Date date = new Date();
        Timestamp tempTime = new Timestamp(date.getTime());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String tempTimeString = sdf.format(date);

        long milliseconds = Global.startTime.getTime() - tempTime.getTime();
        int timediff = (int) milliseconds / 1000;

        int seconds = timediff * -1;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = ((seconds % 3600) % 60 );

        if(Global.data1.getDataSetCount()>0) {
            max1Value.setText(String.valueOf(Global.data1.getYMax()));
            min1Value.setText(String.valueOf(Global.data1.getYMin()));
        }
        if(Global.data2.getDataSetCount()>0) {
            max2Value.setText(String.valueOf(Global.data2.getYMax()));
            min2Value.setText(String.valueOf(Global.data2.getYMin()));
        }

        tvTime.setText(Global.startTimeString + " - " + tempTimeString + " ("+Integer.toString(hours) + ":"+ Integer.toString(minutes) +":"+Integer.toString(seconds)+")");
    }

    private void addEntry(){
        LineData data1 = mChart1.getData();
        LineData data2 = mChart2.getData();

        if(data1 != null){
            ILineDataSet set1 = data1.getDataSetByIndex(0);
            ILineDataSet set2 = data2.getDataSetByIndex(0);

            if(set1 == null){
                set1 = createSet();
                data1.addDataSet(set1);
            }

            if(set2 == null){
                set2 = createSet();
                data2.addDataSet(set2);
            }

            float[] sensorValues = getSensorValues();
            data1.addEntry(new Entry(set1.getEntryCount(), sensorValues[0]), 0);
            data2.addEntry(new Entry(set2.getEntryCount(), sensorValues[1]), 0);

            //Save as Arraylist to save in file
            Global.arrData1.add(new MyEntry(set1.getEntryCount(), sensorValues[0]));
            Global.arrData2.add(new MyEntry(set2.getEntryCount(), sensorValues[1]));

            updateChart(mChart1, data1);
            updateChart(mChart2, data2);
        }
    }

    private void updateChart(LineChart chart, LineData data){
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(100);
        chart.moveViewToX(data.getEntryCount());
    }

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.BLUE);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private float[] getSensorValues(){
        float[] sensorValues = new float[2];

        if(Global.mBluetoothConnection!=null) {
            if (Global.chkConnection) {
                while(true){
                    String inputStream = Global.mBluetoothConnection.inputStream;
                    char splitChar = '|';
                    //Log.d("Home------", inputStream);

                    if(inputStream.indexOf(splitChar)!=-1) {
                        String[] values = inputStream.split("\\|");
                        values[0] = values[0].replaceAll("\\D", "");
                        values[1] = values[1].replaceAll("\\D", "");
                        //Log.d("Home------", "0: " + values[0] + " 1:" + values[1]);
                        sensorValues[0] = Float.parseFloat(values[0]);
                        sensorValues[1] = Float.parseFloat(values[1]);
                        break;
                    }
                }
            }
        }
        return sensorValues;
    }


}

