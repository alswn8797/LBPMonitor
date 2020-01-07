package com.ait.minju.lbpmonitor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RecordAdapter extends ArrayAdapter<Record> {
    private Context context;
    private ArrayList<Record> records;

    private ArrayList<String> keys;

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference("Records");

    private LineChart mChart1;
    private LineChart mChart2;

    LineData data1;
    LineData data2;

    public RecordAdapter(Context context, ArrayList<Record> records) {
        super(context, 0, records);
        this.context = context;
        this.records = records;
    }

    public RecordAdapter(Context context, ArrayList<Record> records, ArrayList<String> keys) {
        super(context, 0, records);
        this.context = context;
        this.records = records;
        this.keys = keys;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.row_record, parent, false);
        }

        //reference all of the textViews in our layout via code

        TextView tvTime = (TextView)convertView.findViewById(R.id.tvTime);
        TextView tvDuration = (TextView)convertView.findViewById(R.id.tvDuration);

        //get the current item we are trying to list
        final Record record = this.getItem(position);

        String duration = calcDuration(record.getTimeDiff());

        tvTime.setText(record.getStartTime() + " - " + record.getEndTime());
        tvDuration.setText(duration);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDetail(record, position);
            }
        });

        return convertView;
    }

    private String calcDuration(int timeDiff){
        int seconds = timeDiff * -1;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = ((seconds % 3600) % 60 );

        String duration = Integer.toString(hours) + ":"+ Integer.toString(minutes) +":"+Integer.toString(seconds);
        return duration;
    }

    private void openDetail(final Record record, final int position){

        final Dialog dialog = new Dialog(context);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog); // layout of your dialog

        // Set dialog title
        dialog.setTitle("Detail");

        String duration = calcDuration(record.getTimeDiff());

        TextView tvTime = (TextView) dialog.findViewById(R.id.tvTime);
        tvTime.setText(record.getStartTime() + " - " + record.getEndTime() + " ( "+duration+")");

        mChart1 = (LineChart) dialog.findViewById(R.id.lineChart1);
        mChart2 = (LineChart) dialog.findViewById(R.id.lineChart2);

        data1 = createData(createSet(readChartData(record.getLeft())));
        data2 = createData(createSet(readChartData(record.getRight())));

        setChart(mChart1, data1, "Muscle Sensor - Left");
        setChart(mChart2, data2, "Muscle Sensor - Right");

        //TODO
        //mChart1.setVisibleXRangeMaximum(100);
        //mChart1.moveViewToX(data1.getEntryCount());

        //mChart2.setVisibleXRangeMaximum(100);
        //mChart2.moveViewToX(data2.getEntryCount());

        float chart1Max = data1.getYMax();
        float chart1Min = data1.getYMin();
        float chart1avg = calcAverageData(record.getLeft());
        float chart2Max = data2.getYMax();
        float chart2Min = data2.getYMin();
        float chart2avg = calcAverageData(record.getRight());

        TextView max1Value = (TextView) dialog.findViewById(R.id.max1Value);
        TextView min1Value = (TextView) dialog.findViewById(R.id.min1Value);
        TextView avg1Value = (TextView) dialog.findViewById(R.id.avg1Value);
        TextView max2Value = (TextView) dialog.findViewById(R.id.max2Value);
        TextView min2Value = (TextView) dialog.findViewById(R.id.min2Value);
        TextView avg2Value = (TextView) dialog.findViewById(R.id.avg2Value);

        max1Value.setText(String.format("%.2f", chart1Max));
        min1Value.setText(String.format("%.2f", chart1Min));
        avg1Value.setText(String.format("%.2f", chart1avg));
        max2Value.setText(String.format("%.2f", chart2Max));
        min2Value.setText(String.format("%.2f", chart2Min));
        avg2Value.setText(String.format("%.2f", chart2avg));

        Button btnDelete = (Button) dialog.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(R.string.app_name);
                alertDialogBuilder
                        .setMessage(R.string.button_confirm_delete)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int id) {
                                        String key = keys.get(position);
                                        mDatabaseReference.child(key).removeValue();
                                        keys.remove(key);
                                        records.remove(record);
                                        dialogInterface.cancel();
                                        dialog.dismiss();
                                    }
                                })

                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                dialogInterface.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private void setChart(LineChart chart, LineData data, String title){
        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(title);
        chart.setNoDataText("No Data for the moment");

        chart.setDragEnabled(true);
        chart.setTouchEnabled(true);
        chart.setScaleEnabled(true);

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

    private LineData createData(LineDataSet lDataSet){
        LineData data = new LineData(lDataSet);
        return data;
    }

    private LineDataSet createSet(ArrayList<Entry> yValues){
        LineDataSet set = new LineDataSet(yValues, "Dynamic Data");
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

    private ArrayList<Entry> readChartData(ArrayList<MyEntry> chartData){
        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < chartData.size(); i++) {
            MyEntry currentMyEntry = chartData.get(i);
            Entry currentEntry = new Entry();
            currentEntry.setX(currentMyEntry.getX());
            currentEntry.setY(currentMyEntry.getY());
            entries.add(currentEntry);
        }

        return entries;
    }


    //TODO
    private Float calcAverageData(ArrayList<MyEntry> chartData){
        ArrayList<Entry> entries = new ArrayList<>();
        float avg = 0.0f;
        float total = 0.0f;

        for (int i = 0; i < chartData.size(); i++) {
            MyEntry myEntry = chartData.get(i);
            total += myEntry.getY();
        }

        avg = total / chartData.size();
        return avg;
    }
}