package com.ait.minju.lbpmonitor;

import android.app.ProgressDialog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/*

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment{

    private ListView lvRecords;
    private TextView tvEmpty;
    RecordAdapter adapter;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    ArrayList<Record> records = new ArrayList<>();
    ArrayList<String> keys = new ArrayList<>();
    ProgressDialog dia;

    public HistoryFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        lvRecords = view.findViewById(R.id.lvRecords);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        lvRecords.setEmptyView(tvEmpty);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("Records");

        adapter = new RecordAdapter(getContext(), records);
        //set its adapter
        lvRecords.setAdapter(adapter);

        dia = new ProgressDialog(getContext());
        dia.setMessage("please wait...");
        dia.show();

        records = new ArrayList<>();
        records.clear();
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                records.clear();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    String key = data.getKey();
                    keys.add(key);
                    Record record = data.getValue(Record.class);
                    records.add(record);
                }
                if(adapter != null) {
                    RecordAdapter adapter = new RecordAdapter(getContext(), records, keys);
                    lvRecords.setAdapter(adapter);
                    dia.dismiss();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                dia.dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        //addValueEventListener keep works when I save data in HomeFragment
        adapter = null;
        super.onDestroyView();
    }
}

